package com.drofff.checkers.server.document;

import com.drofff.checkers.server.enums.BoardSide;

import java.util.ArrayList;
import java.util.List;

public class Board {

    private List<Piece> pieces = new ArrayList<>();

    private BoardSide turnSide;

    public List<Piece> getPieces() {
        return pieces;
    }

    public void setPieces(List<Piece> pieces) {
        this.pieces = pieces;
    }

    public BoardSide getTurnSide() {
        return turnSide;
    }

    public void setTurnSide(BoardSide turnSide) {
        this.turnSide = turnSide;
    }

}
