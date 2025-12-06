package http;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class HttpResponseTest {

    @Test
    @DisplayName("상태코드만 설정된 응답 생성")
    void createResponseWithStatusCode() {
        // given
        final HttpResponse response = new HttpResponse();

        // when
        response.setStatus(HttpStatus.NOT_FOUND);

        // then
        assertEquals("HTTP/1.1", response.getVersion());
        assertEquals(HttpStatus.NOT_FOUND, response.getStatus());
    }

    @Test
    @DisplayName("숫자로 상태코드 설정된 응답 생성")
    void createResponseWithNumericStatusCode() {
        // given
        final HttpResponse response = new HttpResponse();

        // when
        response.setStatus(500);

        // then
        assertEquals("HTTP/1.1", response.getVersion());
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatus());
    }

    @Test
    @DisplayName("지원하지 않은 상태코드 설정 시 예외 발생")
    void createResponseWithUnsupportedStatusCode() {
        // given
        final HttpResponse response = new HttpResponse();

        // when & then
        assertThrows(IllegalArgumentException.class, () -> response.setStatus(999));
    }

    @Test
    @DisplayName("헤더를 추가한 응답 생성")
    void createResponseWithHeaders() {
        // given
        final HttpResponse response = new HttpResponse();

        // when
        response.addHeader("Content-Type", "application/json");
        response.addHeader("Cache-Control", "no-cache");

        // then
        final Map<String, String> headers = response.getHeaders();
        assertAll(
                () -> assertEquals(2, headers.size()),
                () -> assertEquals("application/json", headers.get("Content-Type")),
                () -> assertEquals("no-cache", headers.get("Cache-Control"))
        );
    }

    @Test
    @DisplayName("본문을 포함한 응답 생성")
    void createResponseWithBody() {
        // given
        final HttpResponse response = new HttpResponse();
        final String bodyContent = "Hello, World!";

        // when
        response.setBody(bodyContent);

        // then
        assertEquals(bodyContent, response.getBody());
        assertEquals(bodyContent.length(), Integer.parseInt(response.getHeaders().get("Content-Length")));
    }

    @Test
    @DisplayName("본문을 포함하지 않으면 Content-Length 헤더가 없다")
    void createResponseWithoutBody() {
        // given
        final HttpResponse response = new HttpResponse();

        // when
        response.setStatus(HttpStatus.OK);

        // then
        assertEquals(HttpStatus.OK, response.getStatus());
        assertNull(response.getHeaders().get("Content-Length"));
    }

    @Test
    @DisplayName("Status Line이 올바르게 생성된다")
    void statusLineIsGeneratedCorrectly() {
        // given
        final HttpResponse response = new HttpResponse();
        response.setStatus(HttpStatus.BAD_REQUEST);

        // when
        String statusLine = new String(response.getBytes(), StandardCharsets.UTF_8);

        // then
        assertEquals("HTTP/1.1 400 Bad Request\r\n", statusLine);
    }

    @Test
    @DisplayName("헤더가 올바르게 생성된다")
    void headersAreGeneratedCorrectly() {
        // given
        final HttpResponse response = new HttpResponse();
        response.setStatus(HttpStatus.OK);
        response.addHeader("Content-Type", "text/plain");

        // when
        String headersString = new String(response.getBytes(), StandardCharsets.UTF_8);

        // then
        assertEquals("HTTP/1.1 200 OK\r\nContent-Type: text/plain\r\n\r\n", headersString);
    }

    @Test
    @DisplayName("헤더와 본문 사이에 빈 줄이 포함된다")
    void blankLineBetweenHeadersAndBody() {
        // given
        final HttpResponse response = new HttpResponse();
        response.setStatus(HttpStatus.OK);
        response.setBody("Response Body");
        response.addHeader("Content-Type", "text/plain");

        // when
        String responseString = new String(response.getBytes(), StandardCharsets.UTF_8);

        // then
        String[] parts = responseString.split("\r\n\r\n", 2);
        assertEquals(2, parts.length);
        assertEquals("Response Body", parts[1]);
    }

}