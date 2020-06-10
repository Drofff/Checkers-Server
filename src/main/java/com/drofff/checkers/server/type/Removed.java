package com.drofff.checkers.server.type;

import com.drofff.checkers.server.document.Piece;

public class Removed extends Piece.Position {

    private static final Removed SINGLETON = new Removed();

    public static Removed get() {
        return SINGLETON;
    }

    @Override
    public int getColumn() {
        return -1;
    }

    @Override
    public int getRow() {
        return -1;
    }

}
