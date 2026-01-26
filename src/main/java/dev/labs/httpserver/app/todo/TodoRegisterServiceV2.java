package dev.labs.httpserver.app.todo;

import dev.labs.httpserver.db.DatabaseConfig;

import java.sql.Connection;
import java.sql.SQLException;

public class TodoRegisterServiceV2 {

    private final DatabaseConfig dbConfig;

    private final JdbcTodoRepositoryV2 todoRepository;
    private final JdbcTodoStatsRepositoryV2 todoStatsRepository;

    public TodoRegisterServiceV2(DatabaseConfig dbConfig,
                                 JdbcTodoRepositoryV2 todoRepository,
                                 JdbcTodoStatsRepositoryV2 todoStatsRepository) {
        this.dbConfig = dbConfig;
        this.todoRepository = todoRepository;
        this.todoStatsRepository = todoStatsRepository;
    }

    public TodoId registerTodo(RegisterTodoCommand cmd) throws SQLException {
        Connection conn = null;
        try {
            conn = dbConfig.getConnection();
            conn.setAutoCommit(false);
            Todo savedTodo = todoRepository.save(cmd.toTodo(), conn);
            todoStatsRepository.increaseTotalCount(cmd.userId(), conn);
            conn.commit();
            return savedTodo.getId();
        } catch (Exception e) {
            if (conn != null) conn.rollback();
            throw new RuntimeException(e);
        } finally {
            if (conn != null) conn.close();
        }
    }

}
