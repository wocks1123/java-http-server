package dev.labs.httpserver.app.todo;

import dev.labs.httpserver.http.HttpRequest;
import dev.labs.httpserver.http.HttpResponse;
import dev.labs.httpserver.http.HttpStatus;
import dev.labs.httpserver.servlet.Servlet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TodoServlet implements Servlet {

    private static final Logger log = LoggerFactory.getLogger(TodoServlet.class);

    private final TodoRepository todoRepository;

    public TodoServlet(TodoRepository todoRepository) {
        this.todoRepository = todoRepository;
    }

    @Override
    public void service(HttpRequest request, HttpResponse response) {
        try {
            switch (request.getMethod()) {
                case GET -> handleGet(request.getPath(), request, response);
                case POST -> handlePost(request.getPath(), request, response);
                case PUT -> handlePut(request.getPath(), request, response);
                case DELETE -> handleDelete(request.getPath(), request, response);
                default -> handleMethodNotAllowed(response);
            }
        } catch (Exception e) {
            log.error("Exception in TodoServlet:", e);
            response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private void handleGet(String path, HttpRequest request, HttpResponse response) {
        // TODO: 구현 필요
        handleMethodNotAllowed(response);
    }

    private void handlePost(String path, HttpRequest request, HttpResponse response) {
        // TODO: 구현 필요
        handleMethodNotAllowed(response);
    }

    private void handlePut(String path, HttpRequest request, HttpResponse response) {
        // TODO: 구현 필요
        handleMethodNotAllowed(response);
    }

    private void handleDelete(String path, HttpRequest request, HttpResponse response) {
        // TODO: 구현 필요
        handleMethodNotAllowed(response);
    }

    private void handleMethodNotAllowed(HttpResponse response) {
        response.setStatus(HttpStatus.METHOD_NOT_ALLOWED);
    }

}
