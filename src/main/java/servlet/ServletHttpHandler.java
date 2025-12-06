package servlet;

import http.HttpRequest;
import http.HttpResponse;
import http.HttpStatus;
import server.HttpHandler;

public class ServletHttpHandler implements HttpHandler {

    private final ServletContainer servletContainer;

    public ServletHttpHandler(ServletContainer servletContainer) {
        this.servletContainer = servletContainer;
    }


    @Override
    public void handle(HttpRequest request, HttpResponse response) {
        Servlet servlet = servletContainer.findServlet(request.getPath());
        if (servlet == null) {
            response.setStatus(HttpStatus.NOT_FOUND);
            return;
        }

        servlet.service(request, response);
    }

}
