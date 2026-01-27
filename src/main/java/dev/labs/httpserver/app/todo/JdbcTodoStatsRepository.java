package dev.labs.httpserver.app.todo;

import dev.labs.httpserver.db.ConnectionProvider;
import dev.labs.httpserver.db.DatabaseConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

public class JdbcTodoStatsRepository implements TodoStatsRepository {

    private static final Logger log = LoggerFactory.getLogger(JdbcTodoStatsRepository.class);

    private final DatabaseConfig dbConfig;

    public JdbcTodoStatsRepository(DatabaseConfig dbConfig) {
        this.dbConfig = dbConfig;
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

        Connection conn = null;
        try {
            conn = ConnectionProvider.get(dbConfig);
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, userId);
                pstmt.setString(2, userId);
                pstmt.setString(3, userId);

                pstmt.executeUpdate();
            }
        } catch (SQLException e) {
            log.error("Failed to increase total count with transaction", e);
            throw new RuntimeException(e);
        } finally {
            if (conn != null) {
                try {
                    ConnectionProvider.release(conn);
                } catch (SQLException e) {
                    log.warn("Failed to release connection", e);
                }
            }
        }
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

        Connection conn = null;
        try {
            conn = ConnectionProvider.get(dbConfig);
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, userId);
                pstmt.setString(2, userId);
                pstmt.setString(3, userId);

                pstmt.executeUpdate();
            }
        } catch (SQLException e) {
            log.error("Failed to increase completed count with transaction", e);
            throw new RuntimeException(e);
        } finally {
            if (conn != null) {
                try {
                    ConnectionProvider.release(conn);
                } catch (SQLException e) {
                    log.warn("Failed to release connection", e);
                }
            }
        }
    }

    @Override
    public Optional<TodoStats> findByUserId(String userId) {
        String sql = "SELECT user_id, total_count, completed_count FROM todo_stats WHERE user_id = ?";

        Connection conn = null;
        try {
            conn = ConnectionProvider.get(dbConfig);
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, userId);

                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        return Optional.of(mapToEntity(rs));
                    }
                }

                return Optional.empty();
            }
        } catch (SQLException e) {
            log.error("Failed to find stats by userId with transaction", e);
            throw new RuntimeException(e);
        } finally {
            if (conn != null) {
                try {
                    ConnectionProvider.release(conn);
                } catch (SQLException e) {
                    log.warn("Failed to release connection", e);
                }
            }
        }
    }

    private TodoStats mapToEntity(ResultSet rs) throws SQLException {
        return new TodoStats(
                rs.getString("user_id"),
                rs.getInt("total_count"),
                rs.getInt("completed_count")
        );
    }

}
