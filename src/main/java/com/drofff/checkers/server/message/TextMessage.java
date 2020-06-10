package com.drofff.checkers.server.message;

import com.drofff.checkers.server.enums.MessageType;

public class TextMessage implements Message {

    private final String text;

    public static Message of(String text) {
        return new TextMessage(text);
    }

    private TextMessage(String text) {
        this.text = text;
    }

    @Override
    public MessageType getMessageType() {
        return MessageType.TEXT;
    }

    @Override
    public String getPayload() {
        return text;
    }

}