package dev.labs.httpserver.app.todo;

import dev.labs.httpserver.db.SimpleTransactionManager;

public class TodoRegisterService {

    private final SimpleTransactionManager tx;
    private final TodoRepository todoRepository;
    private final TodoStatsRepository todoStatsRepository;

    public TodoRegisterService(SimpleTransactionManager tx,
                               TodoRepository todoRepository,
                               TodoStatsRepository todoStatsRepository) {
        this.tx = tx;
        this.todoRepository = todoRepository;
        this.todoStatsRepository = todoStatsRepository;
    }

    public TodoId registerTodo(RegisterTodoCommand cmd) {
        try {
            tx.begin();
            Todo savedTodo = todoRepository.save(cmd.toTodo());
            todoStatsRepository.increaseTotalCount(cmd.userId());
            tx.commit();
            return savedTodo.getId();
        } catch (Exception ex) {
            try {
                tx.rollback();
            } catch (Exception ignored) {
            }
            throw new RuntimeException(ex);
        } finally {
            try {
                tx.end();
            } catch (Exception ignored) {
            }
        }
    }

}
