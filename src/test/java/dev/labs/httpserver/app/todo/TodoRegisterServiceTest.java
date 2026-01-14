package dev.labs.httpserver.app.todo;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class TodoRegisterServiceTest {

    @Test
    @DisplayName("사용자가 할일을 등록하면 할일 목록과 개수 통계가 함께 저장된다")
    void registerTodo_SavesBothTodoAndStats() {

    }

    @Test
    @DisplayName("할일은 저장되었지만 통계 업데이트가 실패하면 실제 할일 개수와 통계 수치가 일치하지 않는다")
    void registerTodo_WhenStatsUpdateFails_CausesDataInconsistency() {

    }

}