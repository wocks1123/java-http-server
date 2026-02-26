package dev.labs.httpserver.server;

import dev.labs.httpserver.servlet.ServletContainer;
import dev.labs.httpserver.servlet.StaticResourceServlet;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class KeepAliveIntegrationTest {

    private HttpServer server;

    @BeforeEach
    void setUp() throws InterruptedException {
        ServletContainer servletContainer = new ServletContainer();
        servletContainer.registerServlet("/static/*", new StaticResourceServlet());

        server = new HttpServer(0, servletContainer, 1000);
        Thread serverThread = new Thread(server::start);
        serverThread.setDaemon(true);
        serverThread.start();

        Thread.sleep(100);
    }

    @AfterEach
    void tearDown() {
        server.stop();
    }

    @Test
    @DisplayName("동일 연결에서 index.html, style.css를 순서대로 요청한다")
    void requestsMultipleStaticResourcesOnSameConnection() throws Exception {
        try (Socket socket = new Socket("localhost", server.getPort())) {
            socket.setSoTimeout(2000);
            OutputStream out = socket.getOutputStream();
            InputStream in = socket.getInputStream();

            // 1st request: index.html
            sendRequest(out, """
                    GET /static/index.html HTTP/1.1\r
                    Host: localhost\r
                    Connection: keep-alive\r
                    \r
                    """);
            RawHttpResponse response1 = readResponse(in);

            // 2nd request: style.css (same connection)
            sendRequest(out, """
                    GET /static/style.css HTTP/1.1\r
                    Host: localhost\r
                    Connection: keep-alive\r
                    \r
                    """);
            RawHttpResponse response2 = readResponse(in);

            assertAll(
                    () -> assertEquals(200, response1.statusCode()),
                    () -> assertTrue(response1.headers().contains("Content-Type: text/html"), "1st response must be text/html"),
                    () -> assertTrue(response1.headers().contains("Connection: keep-alive"), "1st response must keep connection"),
                    () -> assertFalse(response1.body().isBlank(), "index.html body must not be empty"),

                    () -> assertEquals(200, response2.statusCode()),
                    () -> assertTrue(response2.headers().contains("Content-Type: text/css"), "2nd response must be text/css"),
                    () -> assertTrue(response2.headers().contains("Connection: keep-alive"), "2nd response must keep connection"),
                    () -> assertFalse(response2.body().isBlank(), "style.css body must not be empty")
            );
        }
    }

    @Test
    @DisplayName("파이프라이닝: 응답을 기다리지 않고 요청 두 개를 연속 전송한다")
    void pipelining() throws Exception {
        try (Socket socket = new Socket("localhost", server.getPort())) {
            socket.setSoTimeout(2000);
            OutputStream out = socket.getOutputStream();
            InputStream in = socket.getInputStream();

            // 응답을 기다리지 않고 요청 두 개를 한 번에 전송
            sendRequest(out, """
                    GET /static/index.html HTTP/1.1\r
                    Host: localhost\r
                    Connection: keep-alive\r
                    \r
                    GET /static/style.css HTTP/1.1\r
                    Host: localhost\r
                    Connection: keep-alive\r
                    \r
                    """);

            // 그 다음에 응답을 순서대로 읽음
            RawHttpResponse response1 = readResponse(in);
            RawHttpResponse response2 = readResponse(in);

            assertAll(
                    () -> assertEquals(200, response1.statusCode()),
                    () -> assertTrue(response1.headers().contains("Content-Type: text/html"), "1st response must be text/html"),
                    () -> assertEquals(200, response2.statusCode()),
                    () -> assertTrue(response2.headers().contains("Content-Type: text/css"), "2nd response must be text/css")
            );
        }
    }

    @Test
    @DisplayName("Connection: close 요청 후 서버가 연결을 종료한다")
    void serverClosesConnectionAfterCloseRequest() throws Exception {
        try (Socket socket = new Socket("localhost", server.getPort())) {
            socket.setSoTimeout(2000);
            OutputStream out = socket.getOutputStream();
            InputStream in = socket.getInputStream();

            sendRequest(out, """
                    GET /static/index.html HTTP/1.1\r
                    Host: localhost\r
                    Connection: close\r
                    \r
                    """);
            RawHttpResponse response = readResponse(in);

            assertEquals(200, response.statusCode());
            assertTrue(response.headers().contains("Connection: close"));
            assertEquals(-1, in.read(), "서버가 Connection: close 후 연결을 종료해야 한다");
        }
    }

    private void sendRequest(OutputStream out, String rawRequest) throws IOException {
        out.write(rawRequest.getBytes(StandardCharsets.UTF_8));
        out.flush();
    }

    /**
     * InputStream에서 HTTP 응답 하나를 읽어 파싱한다.
     * 헤더 섹션(\r\n\r\n까지) 읽기 → Content-Length 추출 → 바디 읽기
     */
    private RawHttpResponse readResponse(InputStream in) throws IOException {
        ByteArrayOutputStream headerBuffer = new ByteArrayOutputStream();
        int[] window = new int[]{-1, -1, -1, -1};
        int b;

        while ((b = in.read()) != -1) {
            headerBuffer.write(b);
            window[0] = window[1];
            window[1] = window[2];
            window[2] = window[3];
            window[3] = b;
            if (window[0] == '\r' && window[1] == '\n' && window[2] == '\r' && window[3] == '\n') {
                break;
            }
        }

        String rawHeaders = headerBuffer.toString(StandardCharsets.UTF_8);
        int statusCode = parseStatusCode(rawHeaders);
        int contentLength = parseContentLength(rawHeaders);

        byte[] bodyBytes = contentLength > 0 ? in.readNBytes(contentLength) : new byte[0];
        String body = new String(bodyBytes, StandardCharsets.UTF_8);

        return new RawHttpResponse(statusCode, rawHeaders, body);
    }

    private int parseStatusCode(String rawHeaders) {
        String statusLine = rawHeaders.split("\r\n")[0];
        return Integer.parseInt(statusLine.split(" ")[1]);
    }

    private int parseContentLength(String rawHeaders) {
        for (String line : rawHeaders.split("\r\n")) {
            if (line.toLowerCase().startsWith("content-length:")) {
                return Integer.parseInt(line.split(":", 2)[1].trim());
            }
        }
        return 0;
    }

    private record RawHttpResponse(int statusCode, String headers, String body) {
    }

}
