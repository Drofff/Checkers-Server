package com.drofff.checkers.server.message;

import com.drofff.checkers.server.enums.MessageType;

public interface Message {

    MessageType getMessageType();

    Object getPayload();

}