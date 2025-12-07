package dev.labs.httpserver.app.todo;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

public class InMemoryTodoRepository implements TodoRepository {

    private final Map<TodoId, Todo> store = new ConcurrentHashMap<>();
    private final AtomicLong sequence = new AtomicLong(0);

    @Override
    public TodoId nextId() {
        return null;
    }

    public Todo save(Todo todo) {
        if (todo.getId() == null) {
            long id = sequence.incrementAndGet();
            todo.setId(new TodoId(id));
        }

        store.put(todo.getId(), todo);
        return todo;
    }

    @Override
    public Optional<Todo> findById(TodoId id) {
        return Optional.ofNullable(store.get(id));
    }

    @Override
    public List<Todo> findAll() {
        return List.copyOf(store.values());
    }

}
