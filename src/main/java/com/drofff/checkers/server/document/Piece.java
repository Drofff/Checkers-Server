package com.drofff.checkers.server.document;

import java.util.UUID;

import static com.drofff.checkers.server.constants.GameConstants.BOARD_ROW_SIZE;

public class Piece {

    private String uid;

    private Position position;

    private String ownerId;

    private boolean isKing;

    public static Piece manOf(int column, int row, String ownerId) {
        Piece piece = new Piece();
        piece.uid = UUID.randomUUID().toString();
        piece.position = Position.of(column, row);
        piece.ownerId = ownerId;
        piece.isKing = false;
        return piece;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public Position getPosition() {
        return position;
    }

    public void setPosition(Position position) {
        this.position = position;
    }

    public String getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
    }

    public boolean isKing() {
        return isKing;
    }

    public void setKing(boolean king) {
        isKing = king;
    }

    public boolean hasPosition(Position position) {
        return this.position.equals(position);
    }

    public void invertPosition() {
        position = position.inverse();
    }

    @Override
    public int hashCode() {
        return uid.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof Piece) {
            Piece piece = (Piece) obj;
            return uid.equals(piece.uid);
        }
        return super.equals(obj);
    }

    public static class Position {

        private int column;

        private int row;

        public static Position of(int column, int row) {
            return new Position(column, row);
        }

        public Position() {}

        private Position(int column, int row) {
            this.column = column;
            this.row = row;
        }

        public int getColumn() {
            return column;
        }

        public void setColumn(int column) {
            this.column = column;
        }

        public int getRow() {
            return row;
        }

        public void setRow(int row) {
            this.row = row;
        }

        public Position inverse() {
            int invertedColumn = invertPosition(column);
            int invertedRow = invertPosition(row);
            return new Position(invertedColumn, invertedRow);
        }

        private int invertPosition(int pos) {
            int maxPos = BOARD_ROW_SIZE - 1;
            return maxPos - pos;
        }

        @Override
        public int hashCode() {
            String hashCodeStr = column + "" + row;
            return Integer.parseInt(hashCodeStr);
        }

        @Override
        public boolean equals(Object obj) {
            if(obj instanceof Position) {
                Position position = (Position) obj;
                return position.row == this.row && position.column == this.column;
            }
            return super.equals(obj);
        }

        @Override
        public String toString() {
            return column + ", " + row;
        }

    }

}