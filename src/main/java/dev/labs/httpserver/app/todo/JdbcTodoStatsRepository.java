package dev.labs.httpserver.app.todo;

import dev.labs.httpserver.db.JdbcExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

public class JdbcTodoStatsRepository implements TodoStatsRepository {

    private static final Logger log = LoggerFactory.getLogger(JdbcTodoStatsRepository.class);

    private final JdbcExecutor jdbcExecutor;

    public JdbcTodoStatsRepository(JdbcExecutor jdbcExecutor) {
        this.jdbcExecutor = jdbcExecutor;
    }

    @Override
    public void increaseTotalCount(String userId) {
        String sql = """
                MERGE INTO todo_stats (user_id, total_count, completed_count)
                KEY(user_id)
                VALUES (?,
                        COALESCE((SELECT total_count FROM todo_stats WHERE user_id = ?), 0) + 1,
                        COALESCE((SELECT completed_count FROM todo_stats WHERE user_id = ?), 0))
                """;

        jdbcExecutor.execute(conn -> {
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, userId);
                pstmt.setString(2, userId);
                pstmt.setString(3, userId);

                pstmt.executeUpdate();
                return null;
            }
        });
    }

    @Override
    public void increaseCompletedCount(String userId) {
        String sql = """
                MERGE INTO todo_stats (user_id, total_count, completed_count)
                KEY(user_id)
                VALUES (?,
                        COALESCE((SELECT total_count FROM todo_stats WHERE user_id = ?), 0),
                        COALESCE((SELECT completed_count FROM todo_stats WHERE user_id = ?), 0) + 1)
                """;

        jdbcExecutor.execute(conn -> {
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, userId);
                pstmt.setString(2, userId);
                pstmt.setString(3, userId);

                pstmt.executeUpdate();
                return null;
            }
        });
    }

    @Override
    public Optional<TodoStats> findByUserId(String userId) {
        String sql = "SELECT user_id, total_count, completed_count FROM todo_stats WHERE user_id = ?";

        return jdbcExecutor.execute(conn -> {
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, userId);

                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        return Optional.of(mapToEntity(rs));
                    }
                }

                return Optional.empty();
            }
        });
    }

    private TodoStats mapToEntity(ResultSet rs) throws SQLException {
        return new TodoStats(
                rs.getString("user_id"),
                rs.getInt("total_count"),
                rs.getInt("completed_count")
        );
    }

}
