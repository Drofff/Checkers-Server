package com.drofff.checkers.server.message;

import com.drofff.checkers.server.document.Step;
import com.drofff.checkers.server.enums.BoardSide;
import com.drofff.checkers.server.utils.JsonUtils;

import java.util.Map;

import static com.drofff.checkers.server.utils.MapUtils.strMapOf;

public class StepMessage extends UpdateMessage {

    private final BoardSide userSide;
    private final Step step;

    private Boolean isKing;

    public StepMessage(BoardSide userSide, Step step) {
        this.userSide = userSide;
        this.step = step;
    }

    @Override
    public Map<String, String> getPayload() {
        String stepJson = JsonUtils.serializeIntoJson(step);
        return strMapOf("userSide", userSide.name(),
                "step", stepJson, "isKing", isKing + "");
    }

    public void setKing(Boolean king) {
        isKing = king;
    }

}