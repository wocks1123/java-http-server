package server;

import http.HttpRequest;
import http.HttpResponse;

public interface HttpHandler {

    void handle(HttpRequest request, HttpResponse response);

}
