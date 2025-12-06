package http;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;


class HttpRequestTest {

    @Test
    @DisplayName("GET 요청을 HttpRequest 객체로 변환")
    void parseGetRequest() {
        // given
        final byte[] rawRequest = ("""
                GET /index.html HTTP/1.1\r
                Host: localhost:8080\r
                Accept: text/html\r
                \r
                """)
                .getBytes();

        // when
        final HttpRequest httpRequest = HttpRequest.parse(rawRequest);

        // then
        assertAll("HTTP Request Line",
                () -> assertEquals(HttpMethod.GET, httpRequest.getMethod()),
                () -> assertEquals("/index.html", httpRequest.getPath()),
                () -> assertEquals("HTTP/1.1", httpRequest.getVersion()),
                () -> {
                    Map<String, String> headers = httpRequest.getHeaders();
                    assertAll("Headers",
                            () -> assertEquals(2, headers.size()),
                            () -> assertEquals("localhost:8080", headers.get("Host")),
                            () -> assertEquals("text/html", headers.get("Accept"))
                    );
                }
        );
    }

    @Test
    @DisplayName("단순 GET 요청을 HttpRequest 객체로 변환")
    void parseSimpleGetRequest() {
        // given
        final byte[] rawRequest = ("""
                GET / HTTP/1.1\r
                """)
                .getBytes();

        // when
        final HttpRequest httpRequest = HttpRequest.parse(rawRequest);

        // then
        assertAll("HTTP Request Line",
                () -> assertEquals(HttpMethod.GET, httpRequest.getMethod()),
                () -> assertEquals("/", httpRequest.getPath()),
                () -> assertEquals("HTTP/1.1", httpRequest.getVersion()),
                () -> assertEquals(0, httpRequest.getHeaders().size())
        );
    }

}
