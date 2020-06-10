package com.drofff.checkers.server.message;

import com.drofff.checkers.server.document.Step;

public class StepMessage extends UpdateMessage {

    private final Step step;

    public StepMessage(Step step) {
        this.step = step;
    }

    @Override
    public Object getPayload() {
        return step;
    }

}
