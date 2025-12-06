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

    @Test
    @DisplayName("쿼리 스트링이 포함된 GET 요청을 HttpRequest 객체로 변환")
    void parseGetRequestWithQueryString() {
        // given
        final byte[] rawRequest = ("""
                GET /search?q=chicken&lang=ko HTTP/1.1\r
                Host: localhost:8080\r
                \r
                """)
                .getBytes();

        // when
        final HttpRequest httpRequest = HttpRequest.parse(rawRequest);

        // then
        assertAll("HTTP Request Line",
                () -> assertEquals(HttpMethod.GET, httpRequest.getMethod()),
                () -> assertEquals("HTTP/1.1", httpRequest.getVersion()),
                () -> assertEquals("/search", httpRequest.getPath()),
                () -> {
                    Map<String, String> queryParams = httpRequest.getQueryParams();
                    assertAll("Query Parameters",
                            () -> assertEquals(2, queryParams.size()),
                            () -> assertEquals("chicken", queryParams.get("q")),
                            () -> assertEquals("ko", queryParams.get("lang"))
                    );
                },
                () -> {
                    Map<String, String> headers = httpRequest.getHeaders();
                    assertAll("Headers",
                            () -> assertEquals(1, headers.size()),
                            () -> assertEquals("localhost:8080", headers.get("Host"))
                    );
                }
        );
    }

    @Test
    @DisplayName("잘못된 형식의 쿼리 파라미터는 무시")
    void parseRequestWithMalformedQueryParams() {
        // given
        final byte[] rawRequest = ("""
                GET /search?q=chicken&invalid&lang=ko HTTP/1.1\r
                Host: localhost:8080\r
                \r
                """)
                .getBytes();

        // when
        final HttpRequest httpRequest = HttpRequest.parse(rawRequest);

        // then
        assertAll("Malformed query params handling",
                () -> assertEquals("/search", httpRequest.getPath()),
                () -> {
                    Map<String, String> queryParams = httpRequest.getQueryParams();
                    assertAll("Query Parameters",
                            () -> assertEquals(2, queryParams.size()),
                            () -> assertEquals("chicken", queryParams.get("q")),
                            () -> assertEquals("ko", queryParams.get("lang")),
                            () -> assertNull(httpRequest.getQueryParams().get("invalid"))
                    );
                },
                () -> {
                    Map<String, String> headers = httpRequest.getHeaders();
                    assertAll("Headers",
                            () -> assertEquals(1, headers.size()),
                            () -> assertEquals("localhost:8080", headers.get("Host"))
                    );
                }
        );
    }

    @Test
    @DisplayName("POST 요청을 HttpRequest 객체로 변환")
    void parsePostRequest() {
        // given
        final String requestBody = "name=JohnDoe&age=29&city=NY";
        final byte[] rawRequest = ("""
                POST /submit HTTP/1.1\r
                Host: localhost:8080\r
                Content-Type: application/x-www-form-urlencoded\r
                Content-Length: %d\r
                \r
                %s
                """.formatted(requestBody.length(), requestBody)
        ).getBytes();

        // when
        final HttpRequest httpRequest = HttpRequest.parse(rawRequest);

        // then
        assertAll("HTTP Request Line",
                () -> assertEquals(HttpMethod.POST, httpRequest.getMethod()),
                () -> assertEquals("/submit", httpRequest.getPath()),
                () -> assertEquals("HTTP/1.1", httpRequest.getVersion()),
                () -> {
                    Map<String, String> headers = httpRequest.getHeaders();
                    assertAll("Headers",
                            () -> assertEquals(3, headers.size()),
                            () -> assertEquals("localhost:8080", headers.get("Host")),
                            () -> assertEquals("application/x-www-form-urlencoded", headers.get("Content-Type")),
                            () -> assertEquals("27", headers.get("Content-Length"))
                    );
                },
                () -> assertEquals(requestBody, httpRequest.getBody())
        );
    }

    @Test
    @DisplayName("body가 없는 POST 요청 파싱")
    void parsePostRequestWithoutBody() {
        // given
        final byte[] rawRequest = ("""
                POST /submit HTTP/1.1\r
                Host: localhost:8080\r
                Content-Type: application/x-www-form-urlencoded\r
                \r
                """)
                .getBytes();

        // when
        final HttpRequest httpRequest = HttpRequest.parse(rawRequest);

        // then
        assertAll("POST request without body",
                () -> assertEquals(HttpMethod.POST, httpRequest.getMethod()),
                () -> assertEquals("/submit", httpRequest.getPath()),
                () -> assertEquals("HTTP/1.1", httpRequest.getVersion()),
                () -> {
                    Map<String, String> headers = httpRequest.getHeaders();
                    assertAll("Headers",
                            () -> assertEquals(2, headers.size()),
                            () -> assertEquals("localhost:8080", headers.get("Host")),
                            () -> assertEquals("application/x-www-form-urlencoded", headers.get("Content-Type"))
                    );
                },
                () -> assertEquals("", httpRequest.getBody())
        );
    }


}
