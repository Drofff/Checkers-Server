package com.drofff.checkers.server.service;

import com.drofff.checkers.server.document.Piece;
import com.drofff.checkers.server.document.Step;
import com.drofff.checkers.server.enums.BoardSide;
import reactor.core.publisher.Mono;

public interface PieceService {

    Mono<BoardSide> getSideOfPieceAtPosition(Piece.Position piecePosition);

    Mono<Void> doStep(Step step);

    Mono<Integer> countOpponentPiecesBetweenPositions(Piece.Position fromPosition, Piece.Position toPosition);

    Mono<Boolean> isPieceAtPositionKing(Piece.Position piecePosition);

}