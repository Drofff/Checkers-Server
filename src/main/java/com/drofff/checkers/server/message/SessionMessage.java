package com.drofff.checkers.server.message;

import com.drofff.checkers.server.document.Board;
import com.drofff.checkers.server.document.Session;
import com.drofff.checkers.server.enums.BoardSide;
import com.fasterxml.jackson.databind.ObjectMapper;
import reactor.core.publisher.Mono;

import static com.drofff.checkers.server.enums.BoardSide.BLACK;
import static com.drofff.checkers.server.enums.BoardSide.RED;
import static com.drofff.checkers.server.utils.MapUtils.strMapOf;
import static com.drofff.checkers.server.utils.SecurityUtils.getCurrentUser;

public class SessionMessage extends AbstractInitialMessage {

    private final Board board;

    private final String userId;

    private final BoardSide userSide;

    public static Mono<Message> of(Session session) {
        Board gameBoard = session.getGameBoard();
        return getCurrentUser().map(user -> {
            BoardSide userSide = session.isOwnedBy(user) ? RED : BLACK;
            return new SessionMessage(gameBoard, user.getId(), userSide);
        });
    }

    private SessionMessage(Board board, String userId, BoardSide userSide) {
        this.board = board;
        this.userId = userId;
        this.userSide = userSide;
    }

    @Override
    public Object getPayload() {
        String boardJson = getBoardAsJson();
        return strMapOf("board", boardJson,
                "userSide", userSide.name(),
                "userId", userId);
    }

    private String getBoardAsJson() {
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.valueToTree(board).toString();
    }

}