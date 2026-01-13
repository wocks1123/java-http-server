package dev.labs.httpserver.controller;

import dev.labs.httpserver.http.HttpMethod;
import org.jspecify.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public class Router {

    private final Map<RouteKey, Controller> routes = new HashMap<>();

    public void addRoute(HttpMethod method, String pathPattern, Controller controller) {
        RouteKey key = new RouteKey(method, pathPattern);
        routes.put(key, controller);
    }

    public @Nullable MatchResult findController(HttpMethod method, String path) {
        RouteKey exactKey = new RouteKey(method, path);

        if (!hasPathVariable(path)) {
            Controller exactController = routes.get(exactKey);
            if (exactController != null) {
                return new MatchResult(exactController, Map.of());
            } else {
                return null;
            }
        }

        for (Map.Entry<RouteKey, Controller> entry : routes.entrySet()) {
            RouteKey key = entry.getKey();
            if (key.method() == method) {
                Map<String, String> pathVariables = matchPathPattern(key.path(), path);
                if (pathVariables != null) {
                    return new MatchResult(entry.getValue(), pathVariables);
                }
            }
        }

        return null;
    }

    private boolean hasPathVariable(String pathPattern) {
        return pathPattern.contains("{") && pathPattern.contains("}");
    }

    private @Nullable Map<String, String> matchPathPattern(String pattern, String path) {
        String[] patternSegments = pattern.split("/");
        String[] pathSegments = path.split("/");

        if (patternSegments.length != pathSegments.length) {
            return null;
        }

        Map<String, String> pathVariables = new HashMap<>();

        for (int i = 0; i < patternSegments.length; i++) {
            String patternSegment = patternSegments[i];
            String pathSegment = pathSegments[i];

            if (patternSegment.startsWith("{") && patternSegment.endsWith("}")) {
                String variableName = patternSegment.substring(1, patternSegment.length() - 1);
                pathVariables.put(variableName, pathSegment);
            } else if (!patternSegment.equals(pathSegment)) {
                return null;
            }
        }

        return pathVariables;
    }

    public record MatchResult(Controller controller, Map<String, String> pathVariables) {
    }

}
