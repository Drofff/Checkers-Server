package com.drofff.checkers.server.message;

import com.drofff.checkers.server.enums.MessageType;

public abstract class UpdateMessage implements Message {

    @Override
    public MessageType getMessageType() {
        return MessageType.UPDATE;
    }

}