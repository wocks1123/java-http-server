package dev.labs.httpserver.server;

import dev.labs.httpserver.http.HttpMethod;
import dev.labs.httpserver.http.HttpRequest;
import dev.labs.httpserver.http.HttpResponse;
import dev.labs.httpserver.http.HttpStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class RequestHandlerTest {

    private RequestHandler sut;
    private SpyHttpHandler httpHandler;

    @BeforeEach
    void setup() {
        httpHandler = new SpyHttpHandler();
        sut = new RequestHandler(httpHandler);
    }

    @Test
    @DisplayName("InputStream의 HTTP 텍스트가 HttpRequest로 파싱된다")
    void handleParsesHttpRequest() {
        // given
        final byte[] rawRequest = ("""
                GET /index.html HTTP/1.1\r
                Host: localhost:8080\r
                Accept: text/html\r
                \r
                """)
                .getBytes();
        final ByteArrayInputStream inputStream = new ByteArrayInputStream(rawRequest);
        final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        // when
        sut.handle(inputStream, outputStream);

        // then
        final HttpRequest request = httpHandler.getCapturedRequest();
        assertAll("HTTP Request Line",
                () -> assertEquals(HttpMethod.GET, request.getMethod()),
                () -> assertEquals("/index.html", request.getPath()),
                () -> assertEquals("HTTP/1.1", request.getVersion()),
                () -> {
                    Map<String, String> headers = request.getHeaders();
                    assertAll("Headers",
                            () -> assertEquals(2, headers.size()),
                            () -> assertEquals("localhost:8080", headers.get("Host")),
                            () -> assertEquals("text/html", headers.get("Accept"))
                    );
                }
        );
    }

    @Test
    @DisplayName("HttpHandler가 처리한 HttpResponse가 OutputStream으로 작성된다")
    void handleWritesHttpResponseToOutputStream() {
        // given
        final byte[] rawRequest = "GET /test HTTP/1.1\r\n".getBytes();
        final ByteArrayInputStream inputStream = new ByteArrayInputStream(rawRequest);
        final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        // when
        sut.handle(inputStream, outputStream);

        // then
        final String rawResponse = outputStream.toString(StandardCharsets.UTF_8);
        assertEquals("HTTP/1.1 200 OK\r\n", rawResponse);
    }

    @Test
    @DisplayName("HttpHandler가 HttpRequest와 HttpResponse를 처리한다")
    void handleInvokesHttpHandler() {
        // given
        final byte[] rawRequest = "GET /test HTTP/1.1\r\n".getBytes();
        final ByteArrayInputStream inputStream = new ByteArrayInputStream(rawRequest);
        final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        // when
        sut.handle(inputStream, outputStream);

        // then
        assertEquals(1, httpHandler.getInvokeCount());
        assertNotNull(httpHandler.getCapturedRequest());
        assertNotNull(httpHandler.getCapturedResponse());
    }

    private static class SpyHttpHandler implements HttpHandler {
        private int invokeCount = 0;
        private HttpRequest capturedRequest;
        private HttpResponse capturedResponse;

        @Override
        public void handle(HttpRequest request, HttpResponse response) {
            this.capturedRequest = request;
            response.setStatus(HttpStatus.OK);
            this.capturedResponse = response;
            this.invokeCount++;
        }

        public HttpRequest getCapturedRequest() {
            return capturedRequest;
        }

        public HttpResponse getCapturedResponse() {
            return capturedResponse;
        }

        public int getInvokeCount() {
            return invokeCount;
        }
    }

}