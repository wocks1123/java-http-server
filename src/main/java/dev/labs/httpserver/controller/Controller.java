package dev.labs.httpserver.controller;

import dev.labs.httpserver.http.HttpRequest;
import dev.labs.httpserver.http.HttpResponse;

public interface Controller {

    void handle(HttpRequest request, HttpResponse response);

}
