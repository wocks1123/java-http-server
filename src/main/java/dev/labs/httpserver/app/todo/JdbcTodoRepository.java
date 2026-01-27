package dev.labs.httpserver.app.todo;

import dev.labs.httpserver.db.JdbcExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class JdbcTodoRepository implements TodoRepository {

    private static final Logger log = LoggerFactory.getLogger(JdbcTodoRepository.class);

    private final JdbcExecutor jdbcExecutor;

    public JdbcTodoRepository(JdbcExecutor jdbcExecutor) {
        this.jdbcExecutor = jdbcExecutor;
    }

    @Override
    public TodoId nextId() {
        // AUTO_INCREMENT를 사용하므로 null 반환
        return null;
    }

    @Override
    public Todo save(Todo todo) {
        if (todo.getId() == null) {
            return insert(todo);
        } else {
            return update(todo);
        }
    }

    private Todo insert(Todo todo) {
        String sql = "INSERT INTO todos (user_id, title, completed) VALUES (?, ?, ?)";

        return jdbcExecutor.execute(conn -> {
            try (PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                pstmt.setString(1, todo.getUserId());
                pstmt.setString(2, todo.getTitle());
                pstmt.setBoolean(3, todo.isCompleted());

                int affectedRows = pstmt.executeUpdate();
                if (affectedRows == 0) {
                    throw new SQLException("Creating todo failed, no rows affected.");
                }

                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (!generatedKeys.next()) {
                        throw new SQLException("Creating todo failed, no ID obtained.");
                    }
                    long id = generatedKeys.getLong(1);
                    todo.setId(new TodoId(id));
                    return todo;
                } catch (SQLException e) {
                    log.error("Failed to insert todo", e);
                    throw new RuntimeException(e);
                }
            }
        });
    }

    private Todo update(Todo todo) {
        String sql = "UPDATE todos SET title = ?, completed = ? WHERE id = ?";

        return jdbcExecutor.execute(conn -> {
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, todo.getTitle());
                pstmt.setBoolean(2, todo.isCompleted());
                pstmt.setLong(3, todo.getId().value());
                pstmt.executeUpdate();
                return todo;
            } catch (SQLException e) {
                log.error("Failed to update todo", e);
                throw new RuntimeException(e);
            }
        });
    }

    @Override
    public Optional<Todo> findById(TodoId id) {
        String sql = "SELECT id, user_id, title, completed FROM todos WHERE id = ?";
        return jdbcExecutor.execute(conn -> {
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setLong(1, id.value());

                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        return Optional.of(mapToEntity(rs));
                    }
                }
                return Optional.empty();
            } catch (SQLException e) {
                log.error("Failed to find todo by id", e);
                throw new RuntimeException(e);
            }
        });
    }

    @Override
    public List<Todo> findAll() {
        String sql = "SELECT id, user_id, title, completed FROM todos";

        return jdbcExecutor.execute(conn -> {
            List<Todo> todos = new ArrayList<>();
            try (PreparedStatement pstmt = conn.prepareStatement(sql);
                 ResultSet rs = pstmt.executeQuery()) {

                while (rs.next()) {
                    todos.add(mapToEntity(rs));
                }

                return todos;
            } catch (SQLException e) {
                log.error("Failed to find all todos", e);
                throw new RuntimeException(e);
            }
        });
    }

    @Override
    public List<Todo> findByUserId(String userId) {
        String sql = "SELECT id, user_id, title, completed FROM todos WHERE user_id = ?";
        List<Todo> todos = new ArrayList<>();

        return jdbcExecutor.execute(conn -> {
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {

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
        });
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
