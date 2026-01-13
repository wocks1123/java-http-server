package dev.labs.httpserver.controller;

import dev.labs.httpserver.http.HttpRequest;
import dev.labs.httpserver.http.HttpResponse;
import dev.labs.httpserver.http.HttpStatus;
import dev.labs.httpserver.servlet.Servlet;

public class FrontControllerServlet implements Servlet {

    private final Router router;

    public FrontControllerServlet(Router router) {
        this.router = router;
    }

    @Override
    public void service(HttpRequest request, HttpResponse response) {
        var matchResult = router.findController(request.getMethod(), request.getPath());

        if (matchResult == null) {
            response.setStatus(HttpStatus.NOT_FOUND);
            return;
        }

        matchResult.controller().handle(request, response);
    }

}
