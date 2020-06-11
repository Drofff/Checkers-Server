package com.drofff.checkers.server.message;

import com.drofff.checkers.server.enums.MessageType;

import static com.drofff.checkers.server.utils.MapUtils.strMapOf;

public class FinishMessage implements Message {

    private static final String WIN_MESSAGE = "Congratulations! You have won the game!";
    private static final String LOSE_MESSAGE = " won the game. Try again!";

    private final String messageText;

    public static FinishMessage win() {
        return new FinishMessage(WIN_MESSAGE);
    }

    public static FinishMessage loseTo(String winnerNickname) {
        String text = winnerNickname + LOSE_MESSAGE;
        return new FinishMessage(text);
    }

    private FinishMessage(String messageText) {
        this.messageText = messageText;
    }

    @Override
    public MessageType getMessageType() {
        return MessageType.FINISH;
    }

    @Override
    public Object getPayload() {
        return strMapOf("messageText", messageText);
    }

}