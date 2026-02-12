package dev.labs.httpserver.controller;

import dev.labs.httpserver.http.HttpRequest;
import dev.labs.httpserver.http.HttpResponse;
import dev.labs.httpserver.http.HttpStatus;
import dev.labs.httpserver.servlet.Servlet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FrontControllerServlet implements Servlet {

    private static final Logger log = LoggerFactory.getLogger(FrontControllerServlet.class);
    private final Router router;

    public FrontControllerServlet(Router router) {
        this.router = router;
    }

    @Override
    public void service(HttpRequest request, HttpResponse response) {
        var matchResult = router.findController(request.getMethod(), request.getPath());

        if (matchResult == null) {
            log.info("routed: NOT_FOUND");
            response.setStatus(HttpStatus.NOT_FOUND);
            return;
        }

        Controller controller = matchResult.controller();
        log.info("routed: {}", controller.getClass().getSimpleName());
        controller.handle(request, response);
    }

}
