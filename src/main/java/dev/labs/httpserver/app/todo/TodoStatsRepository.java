package dev.labs.httpserver.app.todo;

import java.util.Optional;

public interface TodoStatsRepository {

    void increaseTotalCount(String userId);

    void increaseCompletedCount(String userId);

    Optional<TodoStats> findByUserId(String userId);

}
