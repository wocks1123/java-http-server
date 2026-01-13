package dev.labs.httpserver.controller;

import dev.labs.httpserver.http.HttpMethod;

public record RouteKey(HttpMethod method, String path) {
}
