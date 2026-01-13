package dev.labs.httpserver.controller;

import dev.labs.httpserver.fixture.HttpRequestFixture;
import dev.labs.httpserver.http.HttpMethod;
import dev.labs.httpserver.http.HttpRequest;
import dev.labs.httpserver.http.HttpResponse;
import dev.labs.httpserver.http.HttpStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FrontControllerServletTest {

    private FrontControllerServlet sut;


    @Test
    @DisplayName("등록된 컨트롤러를 호출한다")
    void testInvokeRegisteredController() {
        // given
        final SpyController controller = new SpyController();
        final Router router = new Router();
        router.addRoute(HttpMethod.GET, "/test", controller);
        sut = new FrontControllerServlet(router);

        final HttpRequest request = HttpRequestFixture.builder()
                .method(HttpMethod.GET)
                .path("/test")
                .build();
        final HttpResponse response = new HttpResponse();

        // when
        sut.service(request, response);

        // then
        assertEquals(HttpStatus.OK, response.getStatus());
        assertTrue(controller.wasInvoked());
    }


    @Test
    @DisplayName("등록되지 않은 경로는 404를 반환한다")
    void testReturns404ForUnregisteredRoute() {
        // given
        sut = new FrontControllerServlet(new Router());

        final HttpRequest request = HttpRequestFixture.builder()
                .method(HttpMethod.GET)
                .path("/not-exist")
                .build();
        final HttpResponse response = new HttpResponse();

        // when
        sut.service(request, response);

        // then
        assertEquals(HttpStatus.NOT_FOUND, response.getStatus());
    }

    @Test
    @DisplayName("같은 경로라도 다른 메서드는 다른 컨트롤러를 호출한다")
    void testDifferentMethodsInvokeDifferentControllers() {
        // given
        final SpyController controller = new SpyController();
        final Router router = new Router();
        final SpyController getController = new SpyController();
        final SpyController postController = new SpyController();
        router.addRoute(HttpMethod.GET, "/todos", getController);
        router.addRoute(HttpMethod.POST, "/todos", postController);
        sut = new FrontControllerServlet(router);

        final HttpRequest getRequest = HttpRequestFixture.builder()
                .method(HttpMethod.GET)
                .path("/todos")
                .build();
        final HttpRequest postRequest = HttpRequestFixture.builder()
                .method(HttpMethod.POST)
                .path("/todos")
                .build();

        // when
        sut.service(getRequest, new HttpResponse());
        sut.service(postRequest, new HttpResponse());

        // then
        assertTrue(getController.wasInvoked());
        assertTrue(postController.wasInvoked());
    }


    private static class SpyController implements Controller {
        private boolean invoked = false;

        @Override
        public void handle(HttpRequest request, HttpResponse response) {
            this.invoked = true;
            response.setStatus(HttpStatus.OK);
        }

        public boolean wasInvoked() {
            return invoked;
        }
    }

}
