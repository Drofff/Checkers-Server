package com.drofff.checkers.server.utils;

import org.springframework.data.util.Pair;

import java.util.Map;
import java.util.Queue;

import static com.drofff.checkers.server.collector.CustomCollectors.toQueue;

public class FormattingUtils {

    private static final String PARAM_PREFIX = "${";
    private static final String PARAM_SUFFIX = "}";

    private FormattingUtils() {}

    public static String putParamsIntoText(String text, String ... params) {
        Map<String, String> paramsMap = MapUtils.strMapOf(params);
        return putParamsIntoText(paramsMap, text);
    }

    public static String putParamsIntoText(Map<String, String> params, String text) {
        Queue<Pair<String, String>> paramsQueue = params.entrySet().stream()
                .map(paramEntry -> Pair.of(paramEntry.getKey(), paramEntry.getValue()))
                .collect(toQueue());
        return putParamsQueueIntoTextRecursively(paramsQueue, text);
    }

    private static String putParamsQueueIntoTextRecursively(Queue<Pair<String, String>> paramsQueue, String text) {
        Pair<String, String> param = paramsQueue.poll();
        if(param == null) {
            return text;
        }
        String paramName = PARAM_PREFIX + param.getFirst() + PARAM_SUFFIX;
        String textWithParam = text.replace(paramName, param.getSecond());
        return putParamsQueueIntoTextRecursively(paramsQueue, textWithParam);
    }

}