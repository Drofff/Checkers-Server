package com.drofff.checkers.server.utils;

import com.fasterxml.jackson.databind.ObjectMapper;

public class JsonUtils {

    private JsonUtils() {}

    public static <T> String serializeIntoJson(T obj) {
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.valueToTree(obj).toString();
    }

}