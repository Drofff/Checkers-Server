package com.drofff.checkers.server.message;

import com.drofff.checkers.server.exception.ValidationException;

import java.util.HashMap;
import java.util.Map;

import static com.drofff.checkers.server.constants.ParameterConstants.MESSAGE_TEXT;

public class FieldErrorsMessage extends ErrorMessage {

    private final Map<String, String> fieldErrorsMap;

    public static Message from(ValidationException e) {
        return new FieldErrorsMessage(e.getMessage(), e.getFieldErrorsMap());
    }

    private FieldErrorsMessage(String messageText, Map<String, String> fieldErrorsMap) {
        super(messageText);
        this.fieldErrorsMap = fieldErrorsMap;
    }

    @Override
    public Object getPayload() {
        Map<String, Object> payload = new HashMap<>();
        payload.put(MESSAGE_TEXT, super.getPayload());
        payload.put("fieldErrors", fieldErrorsMap);
        return payload;
    }

}