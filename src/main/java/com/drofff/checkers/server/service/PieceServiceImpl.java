package com.drofff.checkers.server.service;

import com.drofff.checkers.server.document.*;
import com.drofff.checkers.server.enums.BoardSide;
import com.drofff.checkers.server.exception.ValidationException;
import com.drofff.checkers.server.type.EmptyPiece;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Optional;

import static com.drofff.checkers.server.utils.SecurityUtils.getCurrentUser;
import static reactor.core.publisher.Mono.error;

@Service
public class PieceServiceImpl implements PieceService {

    private static final Logger LOG = LoggerFactory.getLogger(PieceServiceImpl.class);

    private final SessionService sessionService;

    public PieceServiceImpl(SessionService sessionService) {
        this.sessionService = sessionService;
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
                .flatMap(piece -> applyStepToPiece(step, piece));
    }

    private Mono<Piece> validateStep(Step step) {
        return validateIsTurnOfCurrentUser()
                .flatMap(piece -> validateSquareAtPositionIsEmpty(step.getToPosition()));
    }

    private Mono<Piece> validateIsTurnOfCurrentUser() {
        return isTurnOfCurrentUser()
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

    private Mono<Void> applyStepToPiece(Step step, Piece piece) {
        piece.setPosition(step.getToPosition());
        return sessionService.getCurrentSession()
                .map(session -> updateSessionPiece(session, piece))
                .flatMap(this::switchTurnAtSessionToOpponent)
                .flatMap(session -> sessionService.sendStepToOpponent(step).thenReturn(session))
                .flatMap(sessionService::updateSession);
    }

    private Session updateSessionPiece(Session session, Piece piece) {
        Board gameBoard = session.getGameBoard();
        List<Piece> pieces = gameBoard.getPieces();
        pieces.remove(piece);
        pieces.add(piece);
        return session;
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

    @Override
    public Mono<Boolean> isTurnOfCurrentUser() {
        Mono<User> currentUserMono = getCurrentUser();
        return sessionService.getBoardSideOfUser(currentUserMono)
                .flatMap(userSide -> sessionService.getCurrentSession()
                        .map(session -> {
                            BoardSide turnSide = session.getGameBoard().getTurnSide();
                            return turnSide == userSide;
                        }));
    }

}