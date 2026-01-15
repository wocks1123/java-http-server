package dev.labs.httpserver.app.todo;

import java.util.Optional;

public class FailingUpdateTodoStatsRepository implements TodoStatsRepository {

    private final TodoStatsRepository delegate;

    public FailingUpdateTodoStatsRepository(TodoStatsRepository delegate) {
        this.delegate = delegate;
    }

    @Override
    public void increaseTotalCount(String userId) {
        throw new RuntimeException("forced failure");
    }

    @Override
    public void increaseCompletedCount(String userId) {
        throw new RuntimeException("forced failure");
    }

    @Override
    public Optional<TodoStats> findByUserId(String userId) {
        return delegate.findByUserId(userId);
    }

}