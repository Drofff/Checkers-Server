package com.drofff.checkers.server.document;

public class Step {

    private Piece.Position fromPosition;

    private Piece.Position toPosition;

    public Piece.Position getFromPosition() {
        return fromPosition;
    }

    public void setFromPosition(Piece.Position fromPosition) {
        this.fromPosition = fromPosition;
    }

    public Piece.Position getToPosition() {
        return toPosition;
    }

    public void setToPosition(Piece.Position toPosition) {
        this.toPosition = toPosition;
    }

}