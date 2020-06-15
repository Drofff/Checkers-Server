package com.drofff.checkers.server.service;

import com.drofff.checkers.server.document.*;
import com.drofff.checkers.server.enums.BoardSide;
import com.drofff.checkers.server.exception.ValidationException;
import com.drofff.checkers.server.message.FinishMessage;
import com.drofff.checkers.server.type.EmptyPiece;
import com.drofff.checkers.server.utils.PositionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import static com.drofff.checkers.server.constants.GameConstants.BOARD_ROW_SIZE;
import static com.drofff.checkers.server.utils.SecurityUtils.getCurrentUser;
import static java.util.stream.Collectors.toList;
import static reactor.core.publisher.Mono.error;

@Service
public class PieceServiceImpl implements PieceService {

    private static final Logger LOG = LoggerFactory.getLogger(PieceServiceImpl.class);

    private final SessionService sessionService;
    private final UserService userService;

    public PieceServiceImpl(SessionService sessionService, UserService userService) {
        this.sessionService = sessionService;
        this.userService = userService;
    }

    @Override
    public Mono<BoardSide> getSideOfPieceAtPosition(Piece.Position piecePosition) {
        return getPieceAtPosition(piecePosition).flatMap(this::getBoardSideOfPiece);
    }

    private Mono<BoardSide> getBoardSideOfPiece(Piece piece) {
        Mono<User> currentUserMono = getCurrentUser();
        return getCurrentUser().filter(user -> isOwnerOfPiece(user, piece))
                .flatMap(user -> sessionService.getBoardSideOfUser(currentUserMono))
                .switchIfEmpty(getBoardSideOfOpponentOfUser(currentUserMono));
    }

    private boolean isOwnerOfPiece(User user, Piece piece) {
        return user.getId().equals(piece.getOwnerId());
    }

    @Override
    public Mono<Void> doStep(Step step) {
        LOG.info("Piece step from [{}] to [{}]", step.getFromPosition(), step.getToPosition());
        Mono<Piece> pieceMono = validateStep(step);
        Piece.Position fromPosition = step.getFromPosition();
        return pieceMono.flatMap(piece -> getPieceAtPosition(fromPosition))
                .switchIfEmpty(error(new ValidationException("Missing piece at position " + fromPosition.toString())))
                .flatMap(piece -> applyStepToPiece(step, piece))
                .flatMap(s -> finishSessionIfGameOver());
    }

    private Mono<Piece> validateStep(Step step) {
        return validateIsTurnOfCurrentUser()
                .flatMap(piece -> validateSquareAtPositionIsEmpty(step.getToPosition()));
    }

    private Mono<Piece> validateIsTurnOfCurrentUser() {
        return sessionService.isTurnOfCurrentUser()
                .flatMap(isTurn -> isTurn ? Mono.just(new EmptyPiece()) :
                        error(new ValidationException("Wait for the opponent to make a step")));
    }

    private Mono<Piece> validateSquareAtPositionIsEmpty(Piece.Position position) {
        return getPieceAtPosition(position)
                .switchIfEmpty(Mono.just(new EmptyPiece()))
                .flatMap(piece -> isEmptyPiece(piece) ? Mono.just(piece) :
                        error(new ValidationException("Destination square should be empty")));
    }

    private Mono<Piece> getPieceAtPosition(Piece.Position position) {
        return sessionService.getCurrentSession().flatMap(session -> {
            List<Piece> pieces = session.getGameBoard().getPieces();
            Optional<Piece> pieceAtPosition = pieces.stream()
                    .filter(piece -> piece.hasPosition(position))
                    .findFirst();
            return pieceAtPosition.map(Mono::just).orElseGet(Mono::empty);
        });
    }

    private boolean isEmptyPiece(Piece piece) {
        return piece instanceof EmptyPiece;
    }

    private Mono<Step> applyStepToPiece(Step step, Piece piece) {
        return sessionService.getCurrentSession()
                .flatMap(session -> markPieceAsKingIfStepToOppositeEnd(piece, step, session))
                .flatMap(session -> updatePieceByStepInSession(piece, step, session))
                .flatMap(session -> removePiecesCapturedByStepFromSession(step, session))
                .flatMap(this::switchTurnAtSessionToOpponent)
                .flatMap(sessionService::updateSession)
                .thenReturn(step);
    }

    private Mono<Session> markPieceAsKingIfStepToOppositeEnd(Piece piece, Step step, Session session) {
        return sessionService.getBoardSideOfUser(getCurrentUser())
                .map(this::getOppositeEndRowForBoardSide)
                .map(oppositeEndRow -> markPieceAsKingInSessionIfStepToRow(piece, session, step, oppositeEndRow));
    }

    private int getOppositeEndRowForBoardSide(BoardSide boardSide) {
        return boardSide == BoardSide.RED ? 0 : getMaxRow();
    }

    private int getMaxRow() {
        return BOARD_ROW_SIZE - 1;
    }

    private Session markPieceAsKingInSessionIfStepToRow(Piece piece, Session session, Step step, int row) {
        int destinationRow = step.getToPosition().getRow();
        if(row == destinationRow) {
            piece.setKing(true);
            updatePieceInSession(piece, session);
        }
        return session;
    }

    private Mono<Session> updatePieceByStepInSession(Piece piece, Step step, Session session) {
        piece.setPosition(step.getToPosition());
        updatePieceInSession(piece, session);
        Mono<Void> stepMono = piece.isKing() ? sessionService.sendStepOfKingToOpponent(step) :
                sessionService.sendStepOfManToOpponent(step);
        return stepMono.thenReturn(session);
    }

    private Mono<Session> removePiecesCapturedByStepFromSession(Step step, Session session) {
        List<Piece> pieces = session.getGameBoard().getPieces();
        List<Piece> capturedPieces = pieces.stream().filter(step::captures).collect(toList());
        capturedPieces.forEach(pieces::remove);
        return notifyRemovedCapturedPieces(capturedPieces).then(Mono.just(session));
    }

    private Flux<Void> notifyRemovedCapturedPieces(List<Piece> pieces) {
        return Flux.fromStream(pieces.stream())
                .map(Piece::getPosition)
                .map(Step::removeFromPosition)
                .flatMap(sessionService::sendStepToSessionMembers);
    }

    private void updatePieceInSession(Piece piece, Session session) {
        List<Piece> pieces = session.getGameBoard().getPieces();
        pieces.remove(piece);
        pieces.add(piece);
    }

    private Mono<Session> switchTurnAtSessionToOpponent(Session session) {
        return getBoardSideOfOpponentOfUser(getCurrentUser())
                .map(opponentSide -> {
                    Board gameBoard = session.getGameBoard();
                    gameBoard.setTurnSide(opponentSide);
                    return session;
                });
    }

    private Mono<BoardSide> getBoardSideOfOpponentOfUser(Mono<User> userMono) {
        return sessionService.getBoardSideOfUser(userMono).map(BoardSide::oppositeSide);
    }

    private Mono<Void> finishSessionIfGameOver() {
        return isGameOver().filter(gameOver -> gameOver)
                .flatMap(go -> finishSession()).then();
    }

    private Mono<Boolean> isGameOver() {
        return sessionService.getCurrentSession().map(session -> {
            List<Piece> pieces = session.getGameBoard().getPieces();
            return allPiecesShareOwner(pieces);
        });
    }

    private boolean allPiecesShareOwner(List<Piece> pieces) {
        long distinctOwnersCount = pieces.stream()
                .map(Piece::getOwnerId)
                .distinct().count();
        return distinctOwnersCount == 1;
    }

    private Mono<Void> finishSession() {
        return sessionService.getCurrentSession()
                .flatMap(this::saveSessionResultForMembers)
                .flatMap(sessionService::removeSession);
    }

    private Mono<Session> saveSessionResultForMembers(Session session) {
        List<Piece> pieces = session.getGameBoard().getPieces();
        String winnerId = pieces.get(0).getOwnerId();
        String loserId = getOpponentOfUserWithIdInSession(winnerId, session);
        return saveWinForUserWithId(winnerId).thenReturn(loserId)
                .flatMap(id -> saveLoseForUserWithIdAgainstUser(id, winnerId))
                .thenReturn(session);
    }

    private String getOpponentOfUserWithIdInSession(String id, Session session) {
        String ownerId = session.getSessionOwnerId();
        return ownerId.equals(id) ? session.getSessionMemberId() : ownerId;
    }

    private Mono<Void> saveWinForUserWithId(String id) {
        return updateUserWithIdUsingConsumer(id, user -> {
            int winsCount = user.getWinsCount();
            user.setWinsCount(++winsCount);
            sessionService.sendMessageToUserWithId(FinishMessage.win(), user.getId());
        });
    }

    private Mono<Void> saveLoseForUserWithIdAgainstUser(String id, String winnerId) {
        return userService.getUserById(winnerId).flatMap(winner -> {
           String winnerNickname = winner.getNickname();
           return saveLoseForUserWithIdAgainstUserWithNickname(id, winnerNickname);
        });
    }

    private Mono<Void> saveLoseForUserWithIdAgainstUserWithNickname(String id, String winnerNickname) {
        return updateUserWithIdUsingConsumer(id, user -> {
            int losesCount = user.getLosesCount();
            user.setLosesCount(++losesCount);
            FinishMessage loseMessage = FinishMessage.loseTo(winnerNickname);
            sessionService.sendMessageToUserWithId(loseMessage, user.getId());
        });
    }

    private Mono<Void> updateUserWithIdUsingConsumer(String userId, Consumer<User> userUpdater) {
        return userService.getUserById(userId)
                .doOnNext(userUpdater)
                .flatMap(userService::updateUser);
    }

    @Override
    public Mono<Integer> countOpponentPiecesBetweenPositions(Piece.Position fromPosition, Piece.Position toPosition) {
        return sessionService.getCurrentSession().flatMap(session ->
                getCurrentUser().map(currentUser -> {
                    List<Piece> pieces = session.getGameBoard().getPieces();
                    return (int) pieces.stream().filter(piece -> isPieceNotOwnedByUser(piece, currentUser))
                            .filter(piece -> isPieceLocatedBetweenPositions(piece, fromPosition, toPosition))
                            .count();
                })
        );
    }

    private boolean isPieceNotOwnedByUser(Piece piece, User user) {
        return !user.getId().equals(piece.getOwnerId());
    }

    private boolean isPieceLocatedBetweenPositions(Piece piece, Piece.Position from, Piece.Position to) {
        Piece.Position piecePosition = piece.getPosition();
        return PositionUtils.isPositionInBounds(piecePosition, from, to);
    }

    @Override
    public Mono<Boolean> isPieceAtPositionKing(Piece.Position piecePosition) {
        return sessionService.getCurrentSession().map(session -> {
            List<Piece> pieces = session.getGameBoard().getPieces();
            return pieces.stream().filter(piece -> piece.hasPosition(piecePosition))
                    .anyMatch(Piece::isKing);
        });
    }

}