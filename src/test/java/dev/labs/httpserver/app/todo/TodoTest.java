package dev.labs.httpserver.app.todo;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TodoTest {

    @Test
    @DisplayName("Todo를 생성한다")
    void testCreateTodo() {
        // given
        final String title = "Test Todo";

        // when
        final Todo todo = new Todo(title);

        // then
        assertNull(todo.getId());
        assertEquals(title, todo.getTitle());
        assertFalse(todo.isCompleted());
    }

    @Test
    @DisplayName("Todo를 완료 상태로 변경한다")
    void testToggleTodoCompletion() {
        // given
        final Todo todo = new Todo("Test Todo");

        // when
        todo.complete();

        // then
        assertTrue(todo.isCompleted());
    }

    @Test
    @DisplayName("Todo를 미완료 상태로 변경한다")
    void testUncompleteTodo() {
        // given
        final Todo todo = new Todo("Test Todo");
        todo.complete();

        // when
        todo.reopen();

        // then
        assertFalse(todo.isCompleted());
    }

    @Test
    @DisplayName("Todo의 제목을 변경한다")
    void testUpdateTodoTitle() {
        // given
        final Todo todo = new Todo("Old Title");
        final String newTitle = "New Title";

        // when
        todo.modifyTitle(newTitle);

        // then
        assertEquals(newTitle, todo.getTitle());
    }

}
