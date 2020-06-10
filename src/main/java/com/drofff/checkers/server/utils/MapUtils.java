package com.drofff.checkers.server.utils;

import java.util.HashMap;
import java.util.Map;

public class MapUtils {

    private MapUtils() {}

    public static boolean isNotEmpty(Map<?, ?> map) {
        return !map.isEmpty();
    }

    public static Map<String, String> strMapOf(String ... args) {
        Map<String, String> map = new HashMap<>();
        int pairsCount = args.length / 2;
        for(int i = 0; i < pairsCount; i++) {
            int mapIndex = i * 2;
            map.put(args[mapIndex], args[mapIndex + 1]);
        }
        return map;
    }

}