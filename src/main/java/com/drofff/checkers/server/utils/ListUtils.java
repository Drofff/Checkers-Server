package com.drofff.checkers.server.utils;

import java.util.ArrayList;
import java.util.List;

public class ListUtils {

    private ListUtils() {}

    public static <T> List<T> concatenateLists(List<T> list0, List<T> list1) {
        List<T> result = new ArrayList<>(list0);
        result.addAll(list1);
        return result;
    }

}
