package com.drofff.checkers.server.document;

import com.drofff.checkers.server.type.Removed;

import static java.lang.Math.max;
import static java.lang.Math.min;

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
        return capturesPieceByColumn(piece) && capturesPieceByRow(piece);
    }

    private boolean capturesPieceByColumn(Piece piece) {
        int fromColumn = fromPosition.getColumn();
        int toColumn = toPosition.getColumn();
        int pieceColumn = piece.getPosition().getColumn();
        return isBetween(pieceColumn, fromColumn, toColumn);
    }

    private boolean capturesPieceByRow(Piece piece) {
        int fromRow = fromPosition.getRow();
        int toRow = toPosition.getRow();
        int pieceRow = piece.getPosition().getRow();
        return isBetween(pieceRow, fromRow, toRow);
    }

    private boolean isBetween(int num, int leftBound, int rightBound) {
        int maxValue = max(leftBound, rightBound);
        int minValue = min(leftBound, rightBound);
        return num < maxValue && num > minValue;
    }

}