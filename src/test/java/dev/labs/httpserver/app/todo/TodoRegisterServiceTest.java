package dev.labs.httpserver.app.todo;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class TodoRegisterServiceTest {

    @Test
    @DisplayName("사용자가 할일을 등록하면 할일 목록과 개수 통계가 함께 저장된다")
    void registerTodo_SavesBothTodoAndStats() {

    }

    @Test
    @DisplayName("할일 등록 중 통계 업데이트가 실패하면 할일과 통계 모두 저장되지 않아야 한다")
    void registerTodo_WhenStatsUpdateFails_CausesDataInconsistency() {

    }

}