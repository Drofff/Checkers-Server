package com.drofff.checkers.server.utils;

import com.drofff.checkers.server.document.Piece;

import static java.lang.Math.max;
import static java.lang.Math.min;

public class PositionUtils {

    private PositionUtils() {}

    public static boolean isPositionInBounds(Piece.Position position, Piece.Position leftBound,
                                             Piece.Position rightBound) {
        return isPositionInBoundsByColumn(position, leftBound, rightBound) &&
                isPositionInBoundsByRow(position, leftBound, rightBound);
    }

    private static boolean isPositionInBoundsByColumn(Piece.Position position, Piece.Position leftBound,
                                                      Piece.Position rightBound) {
        int leftBoundColumn = leftBound.getColumn();
        int rightBoundColumn = rightBound.getColumn();
        int column = position.getColumn();
        return isBetween(column, leftBoundColumn, rightBoundColumn);
    }

    private static boolean isPositionInBoundsByRow(Piece.Position position, Piece.Position leftBound,
                                                   Piece.Position rightBound) {
        int leftBoundRow = leftBound.getRow();
        int rightBoundRow = rightBound.getRow();
        int row = position.getRow();
        return isBetween(row, leftBoundRow, rightBoundRow);
    }

    private static boolean isBetween(int num, int leftBound, int rightBound) {
        int maxValue = max(leftBound, rightBound);
        int minValue = min(leftBound, rightBound);
        return num < maxValue && num > minValue;
    }

}