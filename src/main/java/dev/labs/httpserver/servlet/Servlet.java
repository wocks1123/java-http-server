package dev.labs.httpserver.servlet;

import dev.labs.httpserver.http.HttpRequest;
import dev.labs.httpserver.http.HttpResponse;

public interface Servlet {

    void service(HttpRequest request, HttpResponse response);

}
