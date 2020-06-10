package com.drofff.checkers.server.controller;

import com.drofff.checkers.server.document.Piece;
import com.drofff.checkers.server.document.Step;
import com.drofff.checkers.server.enums.BoardSide;
import com.drofff.checkers.server.service.PieceService;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
public class PieceController {

    private final PieceService pieceService;

    public PieceController(PieceService pieceService) {
        this.pieceService = pieceService;
    }

    @MessageMapping("piece.sideByPosition")
    public Mono<BoardSide> getSideOfPieceAtPosition(@Payload Piece.Position piecePosition) {
        return pieceService.getSideOfPieceAtPosition(piecePosition);
    }

    @MessageMapping("piece.step")
    public Mono<Void> movePieceByStep(@Payload Step step) {
        return pieceService.doStep(step);
    }

}