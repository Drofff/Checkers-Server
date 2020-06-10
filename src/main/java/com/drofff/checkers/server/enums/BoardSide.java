package com.drofff.checkers.server.enums;

public enum BoardSide {

    RED, BLACK;

    public BoardSide oppositeSide() {
        return this == RED ? BLACK : RED;
    }

}