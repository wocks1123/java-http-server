package dev.labs.httpserver.server;

import dev.labs.httpserver.http.HttpRequest;
import dev.labs.httpserver.http.HttpResponse;

public interface HttpHandler {

    void handle(HttpRequest request, HttpResponse response);

}
