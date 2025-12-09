package dev.labs.httpserver;

import dev.labs.httpserver.app.todo.InMemoryTodoRepository;
import dev.labs.httpserver.app.todo.TodoServlet;
import dev.labs.httpserver.http.HttpStatus;
import dev.labs.httpserver.server.HttpServer;
import dev.labs.httpserver.servlet.Servlet;
import dev.labs.httpserver.servlet.ServletContainer;
import dev.labs.httpserver.servlet.StaticResourceServlet;

public class HttpServerApplication {

    private static final int PORT = 8080;

    public static void main(String[] args) {
        ServletContainer servletContainer = new ServletContainer();
        servletContainer.registerServlet("/*", (request, response) -> {
            response.setStatus(HttpStatus.OK);
            response.setBody("Hello, World!\r\n");
        });
        servletContainer.registerServlet("/static/*", new StaticResourceServlet());

        Servlet todoServlet = new TodoServlet(new InMemoryTodoRepository());
        servletContainer.registerServlet("/todos/*", todoServlet);
        servletContainer.registerServlet("/todos", todoServlet);

        HttpServer server = new HttpServer(PORT, servletContainer);
        server.start();
    }

}
