package com.drofff.checkers.server.document;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document
public class Session {

    @Id
    private String id;

    private Board gameBoard;

    private String sessionOwnerId;

    private String sessionMemberId;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Board getGameBoard() {
        return gameBoard;
    }

    public void setGameBoard(Board gameBoard) {
        this.gameBoard = gameBoard;
    }

    public String getSessionOwnerId() {
        return sessionOwnerId;
    }

    public void setSessionOwnerId(String sessionOwnerId) {
        this.sessionOwnerId = sessionOwnerId;
    }

    public String getSessionMemberId() {
        return sessionMemberId;
    }

    public void setSessionMemberId(String sessionMemberId) {
        this.sessionMemberId = sessionMemberId;
    }

    public boolean hasUser(User user) {
        String userId = user.getId();
        return sessionMemberId.equals(userId) || sessionOwnerId.equals(userId);
    }

    public boolean isOwnedBy(User user) {
        return sessionOwnerId.equals(user.getId());
    }

}