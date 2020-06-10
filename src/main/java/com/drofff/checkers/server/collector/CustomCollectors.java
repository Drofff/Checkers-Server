package com.drofff.checkers.server.collector;

public class CustomCollectors {

    private CustomCollectors() {}

    public static <T> QueueCollector<T> toQueue() {
        return new QueueCollector<>();
    }

}
