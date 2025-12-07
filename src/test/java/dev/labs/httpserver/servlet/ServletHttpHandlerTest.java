package dev.labs.httpserver.servlet;

import dev.labs.httpserver.fixture.HttpRequestFixture;
import dev.labs.httpserver.http.HttpMethod;
import dev.labs.httpserver.http.HttpRequest;
import dev.labs.httpserver.http.HttpResponse;
import dev.labs.httpserver.http.HttpStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

class ServletHttpHandlerTest {

    private ServletHttpHandler sut;
    private final String mappedPath = "/test";
    private final SpyServlet servlet = new SpyServlet();

    @BeforeEach
    void setup() {
        ServletContainer servletContainer = new ServletContainer();
        servletContainer.registerServlet(mappedPath, servlet);
        sut = new ServletHttpHandler(servletContainer);
    }

    @Test
    @DisplayName("매핑한 path에 해당하는 Servlet이 호출된다.")
    void testServletHttpHandlerInvokesMappedServlet() {
        // given
        final HttpRequest request = HttpRequestFixture.builder()
                .method(HttpMethod.GET)
                .path(mappedPath)
                .build();
        final HttpResponse response = new HttpResponse();

        // when
        sut.handle(request, response);

        // then
        assertAll(
                () -> assertEquals(1, servlet.getInvokeCount()),
                () -> assertEquals(HttpStatus.OK, response.getStatus())
        );
    }

    @Test
    @DisplayName("매핑되지 않은 path로 요청이 오면 404 응답을 반환한다.")
    void testServletHttpHandlerReturns404ForUnmappedPath() {
        // given
        final HttpRequest request = HttpRequestFixture.builder()
                .method(HttpMethod.GET)
                .path("/unmapped")
                .build();
        final HttpResponse response = new HttpResponse();

        // when
        sut.handle(request, response);

        // then
        assertAll(
                () -> assertEquals(0, servlet.getInvokeCount()),
                () -> assertEquals(HttpStatus.NOT_FOUND, response.getStatus())
        );
    }

    private static class SpyServlet implements Servlet {
        private int invokeCount = 0;

        @Override
        public void service(HttpRequest request, HttpResponse response) {
            this.invokeCount++;
            response.setStatus(HttpStatus.OK);
        }

        public int getInvokeCount() {
            return invokeCount;
        }
    }

}