package dev.labs.httpserver.app.todo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Deprecated(forRemoval = true)
public class JdbcTodoRepositoryV2 {

    private static final Logger log = LoggerFactory.getLogger(JdbcTodoRepositoryV2.class);


    public Todo save(Todo todo, Connection connection) {
        String sql = "INSERT INTO todos (user_id, title, completed) VALUES (?, ?, ?)";

        try (PreparedStatement pstmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, todo.getUserId());
            pstmt.setString(2, todo.getTitle());
            pstmt.setBoolean(3, todo.isCompleted());

            int affectedRows = pstmt.executeUpdate();

            if (affectedRows == 0) {
                throw new SQLException("Creating todo failed, no rows affected.");
            }

            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    long id = generatedKeys.getLong(1);
                    todo.setId(new TodoId(id));
                } else {
                    throw new SQLException("Creating todo failed, no ID obtained.");
                }
            }

            return todo;
        } catch (SQLException e) {
            log.error("Failed to insert todo", e);
            throw new RuntimeException(e);
        }
    }

    public List<Todo> findByUserId(String userId, Connection connection) {
        String sql = "SELECT id, user_id, title, completed FROM todos WHERE user_id = ?";
        List<Todo> todos = new ArrayList<>();

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {

            pstmt.setString(1, userId);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    todos.add(mapToEntity(rs));
                }
            }

            return todos;
        } catch (SQLException e) {
            log.error("Failed to find todos by userId", e);
            throw new RuntimeException(e);
        }
    }

    private Todo mapToEntity(ResultSet rs) throws SQLException {
        Todo todo = new Todo(rs.getString("user_id"), rs.getString("title"));
        todo.setId(new TodoId(rs.getLong("id")));
        if (rs.getBoolean("completed")) {
            todo.complete();
        }
        return todo;
    }

}
