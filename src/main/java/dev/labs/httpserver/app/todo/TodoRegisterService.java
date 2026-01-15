package dev.labs.httpserver.app.todo;

public class TodoRegisterService {

    private final TodoRepository todoRepository;
    private final TodoStatsRepository todoStatsRepository;

    public TodoRegisterService(TodoRepository todoRepository,
                               TodoStatsRepository todoStatsRepository) {
        this.todoRepository = todoRepository;
        this.todoStatsRepository = todoStatsRepository;
    }

    public TodoId registerTodo(RegisterTodoCommand cmd) {
        Todo savedTodo = todoRepository.save(cmd.toTodo());
        todoStatsRepository.increaseTotalCount(cmd.userId());
        return savedTodo.getId();
    }

}
