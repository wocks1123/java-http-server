package dev.labs.httpserver.http;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

class HttpRequestReaderTest {

    private final HttpRequestReader sut = new HttpRequestReader();

    @Test
    @DisplayName("한 번에 모두 도착하는 GET 요청을 읽는다")
    void readCompleteGetRequest() throws IOException {
        // given
        byte[] request = "GET /index.html HTTP/1.1\r\nHost: localhost\r\n\r\n".getBytes(StandardCharsets.UTF_8);
        InputStream inputStream = new ByteArrayInputStream(request);

        // when
        byte[] result = sut.read(inputStream);

        // then
        assertArrayEquals(request, result);
    }

    @Test
    @DisplayName("헤더가 여러 번에 나뉘어 도착하는 경우")
    void readSplitHeader() throws IOException {
        // given
        byte[] request = "GET /index.html HTTP/1.1\r\nHost: localhost\r\n\r\n".getBytes(StandardCharsets.UTF_8);
        InputStream inputStream = new ChunkedInputStream(request, 10);

        // when
        byte[] result = sut.read(inputStream);

        // then
        assertArrayEquals(request, result);
    }

    @Test
    @DisplayName("헤더는 한 번에, body가 여러 번에 나뉘어 도착하는 경우")
    void readSplitBody() throws IOException {
        // given
        String body = "{\"title\":\"test\"}";
        byte[] request = ("POST /todos HTTP/1.1\r\nContent-Length: " + body.length() + "\r\n\r\n" + body)
                .getBytes(StandardCharsets.UTF_8);
        InputStream inputStream = new ChunkedInputStream(request, 15);

        // when
        byte[] result = sut.read(inputStream);

        // then
        assertArrayEquals(request, result);
    }

    @Test
    @DisplayName("Content-Length가 없는 GET 요청은 헤더까지만 읽는다")
    void readGetRequestWithoutContentLength() throws IOException {
        // given
        byte[] request = "GET /test HTTP/1.1\r\nAccept: text/html\r\n\r\n".getBytes(StandardCharsets.UTF_8);
        InputStream inputStream = new ChunkedInputStream(request, 5);

        // when
        byte[] result = sut.read(inputStream);

        // then
        assertArrayEquals(request, result);
    }

    @Test
    @DisplayName("빈 스트림이면 빈 배열을 반환한다")
    void readEmptyStream() throws IOException {
        // given
        InputStream inputStream = new ByteArrayInputStream(new byte[0]);

        // when
        byte[] result = sut.read(inputStream);

        // then
        assertEquals(0, result.length);
    }

    /**
     * 지정된 크기(chunkSize)만큼만 잘라서 반환하는 InputStream.
     * TCP 분할 수신을 시뮬레이션한다.
     */
    private static class ChunkedInputStream extends InputStream {
        private final byte[] data;
        private final int chunkSize;
        private int position = 0;

        ChunkedInputStream(byte[] data, int chunkSize) {
            this.data = data;
            this.chunkSize = chunkSize;
        }

        @Override
        public int read() {
            if (position >= data.length) {
                return -1;
            }
            return data[position++] & 0xFF;
        }

        @Override
        public int read(byte[] b, int off, int len) {
            if (position >= data.length) {
                return -1;
            }
            int bytesToRead = Math.min(Math.min(len, chunkSize), data.length - position);
            System.arraycopy(data, position, b, off, bytesToRead);
            position += bytesToRead;
            return bytesToRead;
        }
    }
}
