package dev.labs.httpserver.app.todo;

public record RegisterTodoCommand(
        String userId,
        String title
) {
    public RegisterTodoCommand {
        if (userId == null || userId.isBlank()) {
            throw new IllegalArgumentException("userId cannot be blank");
        }
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("title cannot be blank");
        }
    }

    public Todo toTodo() {
        return new Todo(userId, title);
    }
}
