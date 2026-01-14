package dev.labs.httpserver;

import dev.labs.httpserver.app.todo.InMemoryTodoRepository;
import dev.labs.httpserver.app.todo.TodoController;
import dev.labs.httpserver.app.todo.TodoRepository;
import dev.labs.httpserver.controller.FrontControllerServlet;
import dev.labs.httpserver.controller.Router;
import dev.labs.httpserver.db.DatabaseConfig;
import dev.labs.httpserver.http.HttpMethod;
import dev.labs.httpserver.http.HttpStatus;
import dev.labs.httpserver.server.HttpServer;
import dev.labs.httpserver.servlet.ServletContainer;
import dev.labs.httpserver.servlet.StaticResourceServlet;

public class HttpServerApplication {

    private static final int PORT = 8080;

    public static void main(String[] args) {

        DatabaseConfig databaseConfig = DatabaseConfig.forProduction();
        databaseConfig.initializeSchema();

        Router router = new Router();
        TodoRepository todoRepository = new InMemoryTodoRepository();
        TodoController todoController = new TodoController(todoRepository);
        router.addRoute(HttpMethod.GET, "/todos", todoController::list);
        router.addRoute(HttpMethod.POST, "/todos", todoController::create);
        router.addRoute(HttpMethod.GET, "/todos/{id}", todoController::detail);

        ServletContainer servletContainer = new ServletContainer();
        servletContainer.registerServlet("/*", (request, response) -> {
            response.setStatus(HttpStatus.OK);
            response.setBody("Hello, World!\r\n");
        });
        servletContainer.registerServlet("/static/*", new StaticResourceServlet());
        servletContainer.registerServlet("/*", new FrontControllerServlet(router));

        HttpServer server = new HttpServer(PORT, servletContainer);
        server.start();
    }

}
