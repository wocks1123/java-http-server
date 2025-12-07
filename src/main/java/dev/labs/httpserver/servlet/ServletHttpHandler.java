package dev.labs.httpserver.servlet;

import dev.labs.httpserver.http.HttpRequest;
import dev.labs.httpserver.http.HttpResponse;
import dev.labs.httpserver.http.HttpStatus;
import dev.labs.httpserver.server.HttpHandler;

public class ServletHttpHandler implements HttpHandler {

    private final ServletContainer servletContainer;

    public ServletHttpHandler(ServletContainer servletContainer) {
        this.servletContainer = servletContainer;
    }


    @Override
    public void handle(HttpRequest request, HttpResponse response) {
        Servlet servlet = servletContainer.resolveServlet(request.getPath());
        if (servlet == null) {
            response.setStatus(HttpStatus.NOT_FOUND);
            return;
        }

        servlet.service(request, response);
    }

}
