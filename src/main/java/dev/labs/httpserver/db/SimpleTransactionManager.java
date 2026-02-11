package dev.labs.httpserver.db;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.function.Supplier;

public class SimpleTransactionManager {

    private final DatabaseConfig dbConfig;

    public SimpleTransactionManager(DatabaseConfig dbConfig) {
        this.dbConfig = dbConfig;
    }

    public void begin() throws SQLException {
        Connection conn = dbConfig.getConnection();
        conn.setAutoCommit(false);
        ConnectionContext.bind(conn);
    }

    public void commit() throws SQLException {
        Connection connection = ConnectionContext.get();
        if (connection == null) {
            throw new IllegalStateException("No connection found in context to commit");
        }
        connection.commit();
    }

    public void rollback() throws SQLException {
        Connection conn = ConnectionContext.get();
        if (conn != null) conn.rollback();
    }

    public void end() throws SQLException {
        Connection conn = ConnectionContext.get();
        ConnectionContext.clear();
        if (conn != null) {
            conn.setAutoCommit(true);
            conn.close();
        }
    }

    public <T> T execute(Supplier<T> operation) {
        boolean isNewTransaction = (ConnectionContext.get() == null);
        try {
            if (isNewTransaction) {
                begin();
            }

            T result = operation.get();

            if (isNewTransaction) {
                commit();
            }

            return result;
        } catch (RuntimeException e) {
            if (isNewTransaction) {
                try {
                    rollback();
                } catch (SQLException se) {
                    e.addSuppressed(se);
                }
            }
            throw e;
        } catch (Exception e) {
            RuntimeException re = new RuntimeException(e);
            if (isNewTransaction) {
                try {
                    rollback();
                } catch (SQLException ex) {
                    re.addSuppressed(ex);
                }
            }
            throw re;
        } finally {
            if (isNewTransaction) {
                try {
                    end();
                } catch (SQLException ignored) {
                }
            }
        }
    }

    public void execute(Runnable action) {
        execute(() -> {
            action.run();
            return null;
        });
    }

}
