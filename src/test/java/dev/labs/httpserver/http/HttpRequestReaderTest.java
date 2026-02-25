package dev.labs.httpserver.http;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
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

    @Test
    @DisplayName("같은 스트림에서 GET 요청 2개를 순서대로 읽는다")
    void readTwoConsecutiveGetRequests() throws IOException {
        // given
        byte[] req1 = "GET /a HTTP/1.1\r\nHost: localhost\r\n\r\n".getBytes(StandardCharsets.UTF_8);
        byte[] req2 = "GET /b HTTP/1.1\r\nHost: localhost\r\n\r\n".getBytes(StandardCharsets.UTF_8);
        InputStream inputStream = new ByteArrayInputStream(concat(req1, req2));

        // when
        byte[] result1 = sut.read(inputStream);
        byte[] result2 = sut.read(inputStream);

        // then
        assertArrayEquals(req1, result1);
        assertArrayEquals(req2, result2);
    }

    @Test
    @DisplayName("같은 스트림에서 GET 요청 3개를 순서대로 읽는다")
    void readThreeConsecutiveGetRequests() throws IOException {
        // given
        byte[] req1 = "GET /a HTTP/1.1\r\nHost: localhost\r\n\r\n".getBytes(StandardCharsets.UTF_8);
        byte[] req2 = "GET /b HTTP/1.1\r\nHost: localhost\r\n\r\n".getBytes(StandardCharsets.UTF_8);
        byte[] req3 = "GET /c HTTP/1.1\r\nHost: localhost\r\n\r\n".getBytes(StandardCharsets.UTF_8);
        InputStream inputStream = new ByteArrayInputStream(concat(req1, req2, req3));

        // when
        byte[] result1 = sut.read(inputStream);
        byte[] result2 = sut.read(inputStream);
        byte[] result3 = sut.read(inputStream);

        // then
        assertArrayEquals(req1, result1);
        assertArrayEquals(req2, result2);
        assertArrayEquals(req3, result3);
    }

    @Test
    @DisplayName("같은 스트림에서 POST 요청 후 GET 요청을 읽는다")
    void readPostThenGet() throws IOException {
        // given
        String body = "title=buy+milk";
        byte[] req1 = ("POST /todos HTTP/1.1\r\nContent-Length: " + body.length() + "\r\n\r\n" + body).getBytes(StandardCharsets.UTF_8);
        byte[] req2 = "GET /todos HTTP/1.1\r\nHost: localhost\r\n\r\n".getBytes(StandardCharsets.UTF_8);
        InputStream inputStream = new ByteArrayInputStream(concat(req1, req2));

        // when
        byte[] result1 = sut.read(inputStream);
        byte[] result2 = sut.read(inputStream);

        // then
        assertArrayEquals(req1, result1);
        assertArrayEquals(req2, result2);
    }

    private static byte[] concat(byte[]... arrays) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        for (byte[] array : arrays) {
            out.write(array);
        }
        return out.toByteArray();
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
