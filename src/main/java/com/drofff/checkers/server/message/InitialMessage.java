package com.drofff.checkers.server.message;

import com.drofff.checkers.server.enums.MessageType;

public abstract class InitialMessage implements Message {

    @Override
    public MessageType getMessageType() {
        return MessageType.INITIAL;
    }

}