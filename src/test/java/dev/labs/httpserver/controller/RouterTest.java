package dev.labs.httpserver.controller;

import dev.labs.httpserver.http.HttpMethod;
import dev.labs.httpserver.http.HttpRequest;
import dev.labs.httpserver.http.HttpResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
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
        final Router.MatchResult result = sut.findController(method, path);

        // then
        assertNotNull(result);
        assertEquals(controller, result.controller());
    }

    @Test
    @DisplayName("등록되지 않은 라우트를 찾으면 null을 반환한다")
    void testFindUnregisteredRoute() {
        // when
        final Router.MatchResult result = sut.findController(HttpMethod.GET, "/not-exist");

        // then
        assertNull(result);
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
        final Router.MatchResult getResult = sut.findController(HttpMethod.GET, path);
        final Router.MatchResult postResult = sut.findController(HttpMethod.POST, path);

        // then
        assertNotNull(getResult);
        assertNotNull(postResult);
        assertEquals(getController, getResult.controller());
        assertEquals(postController, postResult.controller());
        assertNotEquals(getResult.controller(), postResult.controller());
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
        assertEquals(controller1, sut.findController(HttpMethod.GET, "/path1").controller());
        assertEquals(controller2, sut.findController(HttpMethod.POST, "/path2").controller());
        assertEquals(controller3, sut.findController(HttpMethod.PUT, "/path3").controller());
    }

    @Test
    @DisplayName("Path Variable과 매칭한다")
    void testPathVariableMatch() {
        // given
        final Controller controller = new DummyController();
        sut.addRoute(HttpMethod.GET, "/todos/{id}", controller);

        // when
        final Router.MatchResult result = sut.findController(HttpMethod.GET, "/todos/123");

        // then
        assertNotNull(result);
        assertEquals(controller, result.controller());
        assertEquals("123", result.pathVariables().get("id"));
    }

    @Test
    @DisplayName("여러 Path Variable과 매칭한다")
    void testMultiplePathVariables() {
        // given
        final Controller controller = new DummyController();
        sut.addRoute(HttpMethod.GET, "/users/{userId}/posts/{postId}", controller);

        // when
        final Router.MatchResult result = sut.findController(HttpMethod.GET, "/users/42/posts/999");

        // then
        assertNotNull(result);
        final Map<String, String> pathVariables = result.pathVariables();
        assertEquals("42", pathVariables.get("userId"));
        assertEquals("999", pathVariables.get("postId"));
    }

    @Test
    @DisplayName("정확한 매칭이 Path Variable 매칭보다 우선된다")
    void testExactMatchHasPriority() {
        // given
        final Controller exactController = new DummyController();
        final Controller variableController = new DummyController();
        sut.addRoute(HttpMethod.GET, "/todos/new", exactController);
        sut.addRoute(HttpMethod.GET, "/todos/{id}", variableController);

        // when
        final Router.MatchResult result = sut.findController(HttpMethod.GET, "/todos/new");

        // then
        assertNotNull(result);
        assertEquals(exactController, result.controller());
    }

    @Test
    @DisplayName("세그먼트 개수가 다르면 매칭에 실패한다")
    void testDifferentSegmentCountNotMatch() {
        // given
        final Controller controller = new DummyController();
        sut.addRoute(HttpMethod.GET, "/todos/{id}", controller);

        // when
        final Router.MatchResult result1 = sut.findController(HttpMethod.GET, "/todos");
        final Router.MatchResult result2 = sut.findController(HttpMethod.GET, "/todos/1/comments");

        // then
        assertNull(result1);
        assertNull(result2);
    }

    private static class DummyController implements Controller {
        @Override
        public void handle(HttpRequest request, HttpResponse response) {
            // do nothing
        }
    }

}
