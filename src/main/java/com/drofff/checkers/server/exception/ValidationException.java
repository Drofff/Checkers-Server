package com.drofff.checkers.server.exception;

import java.util.HashMap;
import java.util.Map;

public class ValidationException extends RuntimeException {

    private final Map<String, String> fieldErrorsMap;

    public ValidationException(String message) {
        super(message);
        this.fieldErrorsMap = new HashMap<>();
    }

    public ValidationException(Map<String, String> fieldErrorsMap) {
        this.fieldErrorsMap = fieldErrorsMap;
    }

    public ValidationException(String message, Map<String, String> fieldErrorsMap) {
        super(message);
        this.fieldErrorsMap = fieldErrorsMap;
    }

    public Map<String, String> getFieldErrorsMap() {
        return fieldErrorsMap;
    }

}