package dev.labs.httpserver.app.todo;

import java.util.List;
import java.util.Optional;

public interface TodoRepository {

    TodoId nextId();

    Todo save(Todo todo);

    Optional<Todo> findById(TodoId id);

    List<Todo> findAll();

}
