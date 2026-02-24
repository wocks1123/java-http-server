package dev.labs.httpserver.server;

import org.slf4j.MDC;

public class RequestContext {
    public static final String REQUEST_ID_KEY = "requestId";

    public static void init() {
        MDC.put(REQUEST_ID_KEY, String.format("%05d", RequestIdGenerator.next()));
    }

    public static void clear() {
        MDC.clear();
    }
}
