package dev.labs.httpserver.app.todo;

import org.jspecify.annotations.Nullable;

public class Todo {

    private TodoId id;
    private final String userId;
    private String title;
    private boolean completed;

    public Todo(String userId, String title) {
        this.userId = userId;
        this.title = title;
        this.completed = false;
    }

    public void modifyTitle(String newTitle) {
        this.title = newTitle;
    }

    public void complete() {
        this.completed = true;
    }

    public void reopen() {
        this.completed = false;
    }

    public @Nullable TodoId getId() {
        return id;
    }

    public @Nullable Long getIdValue() {
        return id != null ? id.value() : null;
    }

    public String getUserId() {
        return userId;
    }

    public String getTitle() {
        return title;
    }

    public boolean isCompleted() {
        return completed;
    }

    void setId(TodoId id) {
        if (this.id != null) {
            throw new IllegalStateException("ID is already set");
        }
        this.id = id;
    }

    public String toString() {
        return "{\"id\":\"" + getIdValue() + "\", \"title\":\"" + title + "\", \"completed\":" + completed + "}";
    }

}
