package com.drofff.checkers.server.json;

import com.drofff.checkers.server.document.Board;
import com.drofff.checkers.server.document.Piece;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

import static com.drofff.checkers.server.enums.BoardSide.RED;
import static java.util.Collections.singletonList;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class ObjectMapperTest {

    private static final Logger LOG = LoggerFactory.getLogger(ObjectMapperTest.class);

    @Test
    public void objectToJsonStrTest() {
        ObjectMapper objectMapper = new ObjectMapper();
        Board testBoard = getTestBoard();
        String testBoardJson = objectMapper.valueToTree(testBoard).toString();
        LOG.info("result=[{}]", testBoardJson);
        assertNotNull(testBoardJson);
    }

    private Board getTestBoard() {
        Board board = new Board();
        board.setTurnSide(RED);
        board.setPieces(singletonList(getTestPiece()));
        return board;
    }

    private Piece getTestPiece() {
        Piece piece = new Piece();
        piece.setOwnerId(UUID.randomUUID().toString());
        return piece;
    }

}