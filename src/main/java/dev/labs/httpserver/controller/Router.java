package dev.labs.httpserver.controller;

import dev.labs.httpserver.http.HttpMethod;
import org.jspecify.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public class Router {

    private final Map<RouteKey, Controller> routes = new HashMap<>();

    public void addRoute(HttpMethod method, String path, Controller controller) {
        RouteKey key = new RouteKey(method, path);
        routes.put(key, controller);
    }

    public @Nullable Controller findController(HttpMethod method, String path) {
        RouteKey key = new RouteKey(method, path);
        return routes.get(key);
    }

}
