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
        Controller controller = router.findController(request.getMethod(), request.getPath());

        if (controller == null) {
            response.setStatus(HttpStatus.NOT_FOUND);
            return;
        }

        controller.handle(request, response);
    }

}
