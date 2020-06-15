package com.drofff.checkers.server.document;

import com.drofff.checkers.server.type.Removed;
import com.drofff.checkers.server.utils.PositionUtils;

public class Step {

    private Piece.Position fromPosition;

    private Piece.Position toPosition;

    public static Step removeFromPosition(Piece.Position position) {
        Step removalStep = new Step();
        removalStep.setFromPosition(position);
        removalStep.setToPosition(Removed.get());
        return removalStep;
    }

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

    public boolean captures(Piece piece) {
        Piece.Position piecePosition = piece.getPosition();
        return PositionUtils.isPositionInBounds(piecePosition, fromPosition, toPosition);
    }

}