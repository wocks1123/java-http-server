package dev.labs.httpserver.controller;

import dev.labs.httpserver.http.HttpMethod;
import dev.labs.httpserver.http.HttpRequest;
import dev.labs.httpserver.http.HttpResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class RouterTest {

    private Router sut;

    @BeforeEach
    void setup() {
        sut = new Router();
    }

    @Test
    @DisplayName("등록한 라우터를 확인한다")
    void testRegisterAndFindRoute() {
        // given
        final String path = "/test";
        final Controller controller = new DummyController();
        final HttpMethod method = HttpMethod.GET;
        sut.addRoute(method, path, controller);

        // when
        final Controller foundController = sut.findController(method, path);

        // then
        assertEquals(foundController, controller);
    }

    @Test
    @DisplayName("등록되지 않은 라우트를 찾으면 null을 반환한다")
    void testFindUnregisteredRoute() {
        // when
        Controller found = sut.findController(HttpMethod.GET, "/not-exist");

        // then
        assertNull(found);
    }

    @Test
    @DisplayName("같은 경로라도 다른 메서드를 가지면 다른 라우트다")
    void testDifferentMethodsSamePath() {
        // given
        final String path = "/test";
        final Controller getController = new DummyController();
        final Controller postController = new DummyController();
        sut.addRoute(HttpMethod.GET, path, getController);
        sut.addRoute(HttpMethod.POST, path, postController);

        // when
        final Controller foundGetController = sut.findController(HttpMethod.GET, path);
        final Controller foundPostController = sut.findController(HttpMethod.POST, path);

        // then
        assertEquals(getController, foundGetController);
        assertEquals(postController, foundPostController);
        assertNotEquals(getController, postController);
    }

    @Test
    @DisplayName("여러 라우트를 등록할 수 있다")
    void testRegisterMultipleRoutes() {
        // given
        final Controller controller1 = new DummyController();
        final Controller controller2 = new DummyController();
        final Controller controller3 = new DummyController();
        sut.addRoute(HttpMethod.GET, "/path1", controller1);
        sut.addRoute(HttpMethod.POST, "/path2", controller2);
        sut.addRoute(HttpMethod.PUT, "/path3", controller3);

        // when & then
        assertEquals(controller1, sut.findController(HttpMethod.GET, "/path1"));
        assertEquals(controller2, sut.findController(HttpMethod.POST, "/path2"));
        assertEquals(controller3, sut.findController(HttpMethod.PUT, "/path3"));
    }

    private static class DummyController implements Controller {
        @Override
        public void handle(HttpRequest request, HttpResponse response) {
            // do nothing
        }
    }

}
