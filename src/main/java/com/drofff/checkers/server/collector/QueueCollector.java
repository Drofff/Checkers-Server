package com.drofff.checkers.server.collector;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;

public class QueueCollector<T> implements Collector<T, Deque<T>, Queue<T>> {

    @Override
    public Supplier<Deque<T>> supplier() {
        return ArrayDeque::new;
    }

    @Override
    public BiConsumer<Deque<T>, T> accumulator() {
        return Deque::add;
    }

    @Override
    public BinaryOperator<Deque<T>> combiner() {
        return (acc0, acc1) -> {
            acc0.addAll(acc1);
            return acc0;
        };
    }

    @Override
    public Function<Deque<T>, Queue<T>> finisher() {
        return deque -> deque;
    }

    @Override
    public Set<Characteristics> characteristics() {
        return new HashSet<>();
    }

}
