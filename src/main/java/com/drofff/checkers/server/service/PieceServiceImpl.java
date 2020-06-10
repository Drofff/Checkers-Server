package com.drofff.checkers.server.service;

import com.drofff.checkers.server.document.Piece;
import com.drofff.checkers.server.document.Step;
import com.drofff.checkers.server.document.User;
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
        Mono<User> currentUser = getCurrentUser();
        return getCurrentUser().filter(user -> isOwnerOfPiece(user, piece))
                .flatMap(user -> getBoardSideOfUser(currentUser))
                .switchIfEmpty(getBoardSideOfOpponentOfUser(currentUser));
    }

    private boolean isOwnerOfPiece(User user, Piece piece) {
        return user.getId().equals(piece.getOwnerId());
    }

    private Mono<BoardSide> getBoardSideOfOpponentOfUser(Mono<User> userMono) {
        return getBoardSideOfUser(userMono).map(BoardSide::oppositeSide);
    }

    private Mono<BoardSide> getBoardSideOfUser(Mono<User> userMono) {
        return userMono.flatMap(user -> sessionService.getSessionOfUser(user).map(session ->
                session.isOwnedBy(user) ? BoardSide.RED : BoardSide.BLACK));
    }

    @Override
    public Mono<Void> doStep(Step step) {
        LOG.info("Piece step from [{}] to [{}]", step.getFromPosition(), step.getToPosition());
        Mono<Piece> voidMono = validateSquareAtPositionIsEmpty(step.getToPosition());
        Piece.Position fromPosition = step.getFromPosition();
        return voidMono.flatMap(piece -> getPieceAtPosition(fromPosition))
                .switchIfEmpty(error(new ValidationException("Missing piece at position " + fromPosition.toString())))
                .flatMap(piece -> {
                    piece.setPosition(step.getToPosition());
                    return updatePiece(piece);
                });
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

    private Mono<Void> updatePiece(Piece piece) {
        return sessionService.getCurrentSession().flatMap(session -> {
                    List<Piece> pieces = session.getGameBoard().getPieces();
                    pieces.remove(piece);
                    pieces.add(piece);
                    return sessionService.updateSession(session);
                });
    }

}