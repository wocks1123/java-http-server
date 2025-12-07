package dev.labs.httpserver.servlet;

import dev.labs.httpserver.fixture.HttpRequestFixture;
import dev.labs.httpserver.http.HttpMethod;
import dev.labs.httpserver.http.HttpRequest;
import dev.labs.httpserver.http.HttpResponse;
import dev.labs.httpserver.http.HttpStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class StaticResourceServletTest {

    private static final String TEST_HTML_CONTENT = """
            <!DOCTYPE html>\r
            <html lang="en">\r
            <head>\r
                <meta charset="UTF-8">\r
                <title>Test</title>\r
            </head>\r
            <body>\r
            Hello Test\r
            </body>\r
            </html>\r
            """;

    @Test
    @DisplayName("html 파일을 정상적으로 반환한다")
    void testServeExistingHtmlFile() {
        // given
        final String filePath = "/static/test.html";
        final HttpRequest request = HttpRequestFixture.builder()
                .method(HttpMethod.GET)
                .path(filePath)
                .build();
        final HttpResponse response = new HttpResponse();
        final StaticResourceServlet servlet = new StaticResourceServlet();

        // when
        servlet.service(request, response);

        // then
        assertAll(
                () -> assertEquals(HttpStatus.OK, response.getStatus()),
                () -> assertEquals("text/html", response.getHeaders().get("Content-Type")),
                () -> assertNotNull(response.getBody()),
                () -> {
                    byte[] body = response.getBody();
                    assertEquals(TEST_HTML_CONTENT, new String(body, StandardCharsets.UTF_8));
                }
        );
    }

    @Test
    @DisplayName("존재하지 않는 파일 요청 시 404를 반환한다")
    void testServeNonExistentFileReturns404() {
        // given
        final String filePath = "/static/nonexistent.html";
        final HttpRequest request = HttpRequestFixture.builder()
                .method(HttpMethod.GET)
                .path(filePath)
                .build();
        final HttpResponse response = new HttpResponse();
        final StaticResourceServlet servlet = new StaticResourceServlet();

        // when
        servlet.service(request, response);

        // then
        assertEquals(HttpStatus.NOT_FOUND, response.getStatus());
    }

    @Test
    @DisplayName("부적절한 경로에 대한 요청을 차단한다 - NOT_FOUND로 처리한다")
    void testBlockInvalidPath() {
        // given
        final String filePath = "/static/../secret.txt";
        final HttpRequest request = HttpRequestFixture.builder()
                .method(HttpMethod.GET)
                .path(filePath)
                .build();
        final HttpResponse response = new HttpResponse();
        final StaticResourceServlet servlet = new StaticResourceServlet();

        // when
        servlet.service(request, response);

        // then
        assertEquals(HttpStatus.NOT_FOUND, response.getStatus());
    }

}