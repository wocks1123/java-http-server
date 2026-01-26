package dev.labs.httpserver.app.todo;

import dev.labs.httpserver.db.DatabaseConfig;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TodoRegisterServiceV2Test {

    private TodoRegisterServiceV2 sut;
    private JdbcTodoRepositoryV2 todoRepository;
    private JdbcTodoStatsRepositoryV2 todoStatsRepository;
    private DatabaseConfig databaseConfig;

    @BeforeEach
    void setup() {
        databaseConfig = DatabaseConfig.forTest();
        databaseConfig.initializeSchema();
    }

    @AfterEach
    void teardown() {
        databaseConfig.cleanUp();
    }


    @Test
    @DisplayName("사용자가 할일을 등록하면 할일 목록과 개수 통계가 함께 저장된다")
    void registerTodo_SavesBothTodoAndStats() throws SQLException {
        todoRepository = new JdbcTodoRepositoryV2();
        todoStatsRepository = new JdbcTodoStatsRepositoryV2();
        sut = new TodoRegisterServiceV2(databaseConfig, todoRepository, todoStatsRepository);

        // given
        final String userId = "user123";
        final String title = "New Todo Item";
        final RegisterTodoCommand cmd = new RegisterTodoCommand(userId, title);

        // when
        sut.registerTodo(cmd);

        // then
        final List<Todo> todos = todoRepository.findByUserId(userId, databaseConfig.getConnection());
        assertNotNull(todos);
        assertEquals(1, todos.size());

        final TodoStats stats = todoStatsRepository.findByUserId(userId, databaseConfig.getConnection()).orElse(null);
        assertNotNull(stats);
        assertEquals(1, stats.getTotalCount());
        assertEquals(0, stats.getCompletedCount());

        assertEquals(todos.size(), stats.getTotalCount());
    }

    @Test
    @DisplayName("할일 등록 중 통계 업데이트가 실패하면 할일과 통계 모두 저장되지 않아야 한다")
    void registerTodo_WhenStatsUpdateFails_CausesDataInconsistency() throws SQLException {
        todoRepository = new JdbcTodoRepositoryV2();
        todoStatsRepository = new JdbcTodoStatsRepositoryV2();
        sut = new TodoRegisterServiceV2(databaseConfig, todoRepository, new FailingUpdateTodoStatsRepositoryV2(todoStatsRepository));

        // given
        final String userId = "user123";
        final String title = "New Todo Item";
        final RegisterTodoCommand cmd = new RegisterTodoCommand(userId, title);

        // when
        assertThrows(RuntimeException.class, () -> sut.registerTodo(cmd));

        // then
        List<Todo> todos = todoRepository.findByUserId(userId, databaseConfig.getConnection());
        assertEquals(0, todos.size());

        final TodoStats stats = todoStatsRepository.findByUserId(userId, databaseConfig.getConnection()).orElse(null);
        assertNull(stats);
    }

}