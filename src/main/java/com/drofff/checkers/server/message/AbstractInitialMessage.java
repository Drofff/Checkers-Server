package com.drofff.checkers.server.message;

import com.drofff.checkers.server.enums.MessageType;

public abstract class AbstractInitialMessage implements Message {

    @Override
    public MessageType getMessageType() {
        return MessageType.INITIAL;
    }

}