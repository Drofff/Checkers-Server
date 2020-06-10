package com.drofff.checkers.server.message;

import com.drofff.checkers.server.enums.MessageType;

public class ErrorMessage implements Message {

    private final String messageText;

    public static Message of(String text) {
        return new ErrorMessage(text);
    }

    protected ErrorMessage(String messageText) {
        this.messageText = messageText;
    }

    @Override
    public MessageType getMessageType() {
        return MessageType.ERROR;
    }

    @Override
    public Object getPayload() {
        return messageText;
    }

}