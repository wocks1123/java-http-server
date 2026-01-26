package dev.labs.httpserver.app.todo;

import java.sql.Connection;
import java.util.Optional;

public class FailingUpdateTodoStatsRepositoryV2 extends JdbcTodoStatsRepositoryV2 {

    private final JdbcTodoStatsRepositoryV2 todoStatsRepository;

    public FailingUpdateTodoStatsRepositoryV2(JdbcTodoStatsRepositoryV2 todoStatsRepository1) {
        this.todoStatsRepository = todoStatsRepository1;
    }

    @Override
    public void increaseTotalCount(String userId, Connection connection) {
        throw new RuntimeException("forced failure");
    }

    @Override
    public Optional<TodoStats> findByUserId(String userId, Connection connection) {
        return todoStatsRepository.findByUserId(userId, connection);
    }


}
