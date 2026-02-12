package dev.labs.httpserver.server;

import java.util.concurrent.atomic.AtomicLong;

public class RequestIdGenerator {
    private static final AtomicLong counter = new AtomicLong(0);

    public static long next() {
        return counter.incrementAndGet();
    }
}
