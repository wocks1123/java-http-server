package dev.labs.httpserver.app.todo;

import dev.labs.httpserver.http.HttpRequest;
import dev.labs.httpserver.http.HttpResponse;
import dev.labs.httpserver.http.HttpStatus;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.net.URLDecoder.decode;

public class TodoController {

    private static final Logger log = LoggerFactory.getLogger(TodoController.class);

    private final TodoRepository todoRepository;

    public TodoController(TodoRepository todoRepository) {
        this.todoRepository = todoRepository;
    }


    public void create(HttpRequest request, HttpResponse response) {
        Map<String, String> params = parseFormBody(request.getBody());
        String userId = params.get("userId");
        String title = params.get("title");

        if (!hasText(title) || !hasText(userId)) {
            response.setStatus(HttpStatus.BAD_REQUEST);
            return;
        }

        todoRepository.save(new Todo(userId, title));
        response.setStatus(HttpStatus.OK);
    }

    public void list(HttpRequest request, HttpResponse response) {
        List<Todo> todos = todoRepository.findAll();

        response.setStatus(HttpStatus.OK);
        response.addHeader("Content-Type", "application/json");
        response.setBody(todos.toString()); // FIXME: 직렬화
    }

    public void detail(HttpRequest request, HttpResponse response) {
        TodoId id = extractIdFromPath(request.getPath());

        if (id == null) {
            response.setStatus(HttpStatus.BAD_REQUEST);
            return;
        }

        Optional<Todo> todo = todoRepository.findById(id);

        if (todo.isEmpty()) {
            response.setStatus(HttpStatus.NOT_FOUND);
            return;
        }

        response.setStatus(HttpStatus.OK);
        response.addHeader("Content-Type", "application/json");
        response.setBody(todo.get().toString());
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

    private TodoId extractIdFromPath(String path) {
        String[] parts = path.split("/");
        if (parts.length != 3 || !"todos".equals(parts[1])) {
            return null;
        }

        try {
            Long value = Long.parseLong(parts[2]);
            return new TodoId(value);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private boolean hasText(@Nullable String str) {
        return str != null && !str.isBlank();
    }

}
