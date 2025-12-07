package dev.labs.httpserver.app.todo;

import dev.labs.httpserver.http.HttpRequest;
import dev.labs.httpserver.http.HttpResponse;
import dev.labs.httpserver.http.HttpStatus;
import dev.labs.httpserver.servlet.Servlet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.net.URLDecoder.decode;

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
        if (isPathRoot(path)) {
            List<Todo> todos = todoRepository.findAll();
            StringBuilder responseBody = new StringBuilder();
            responseBody.append("[");
            for (Todo todo : todos) {
                if (responseBody.length() > 1) {
                    responseBody.append(",");
                }
                responseBody.append(todo.toString());
            }
            responseBody.append("]");
            response.setStatus(HttpStatus.OK);
            response.addHeader("Content-Type", "application/json");
            response.setBody(responseBody.toString());
        } else {
            TodoId todoId = extractId(path).orElse(null);
            if (todoId == null) {
                response.setStatus(HttpStatus.NOT_FOUND);
            }
            Optional<Todo> todoOpt = todoRepository.findById(todoId);
            if (todoOpt.isEmpty()) {
                response.setStatus(HttpStatus.NOT_FOUND);
                return;
            }
            response.setStatus(HttpStatus.OK);
            response.addHeader("Content-Type", "application/json");
            response.setBody(todoOpt.get().toString());
        }
    }

    private void handlePost(String path, HttpRequest request, HttpResponse response) {
        if (!isPathRoot(path)) {
            handleMethodNotAllowed(response);
            return;
        }

        Map<String, String> params = parseFormBody(request.getBody());
        todoRepository.save(new Todo(params.get("title")));
        response.setStatus(HttpStatus.OK);
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

    private boolean isPathRoot(String path) {
        // /todos 또는 /todos/
        return "/todos".equals(path) || "/todos/".equals(path);
    }

    private Optional<TodoId> extractId(String path) {
        // /todos/{id}
        String[] parts = path.split("/");
        if (parts.length != 3) {
            return Optional.empty();
        }
        if (!"todos".equals(parts[1])) {
            return Optional.empty();
        }
        try {
            Long value = Long.parseLong(parts[2]);
            return Optional.of(new TodoId(value));
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
    }

    private Map<String, String> parseFormBody(String body) {
        Map<String, String> params = new HashMap<>();
        if (body == null || body.isBlank()) {
            return params;
        }

        String[] pairs = body.split("&");
        for (String pair : pairs) {
            int idx = pair.indexOf('=');
            if (idx == -1) continue;

            String name = decode(pair.substring(0, idx), StandardCharsets.UTF_8);
            String value = decode(pair.substring(idx + 1), StandardCharsets.UTF_8);

            params.put(name, value);
        }
        return params;
    }

}
