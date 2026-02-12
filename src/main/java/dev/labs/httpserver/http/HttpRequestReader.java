package dev.labs.httpserver.http;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class HttpRequestReader {

    private static final byte[] HEADER_END_MARKER = "\r\n\r\n".getBytes(StandardCharsets.UTF_8);
    private static final int READ_BUFFER_SIZE = 4096;

    public byte[] read(InputStream inputStream) throws IOException {
        ByteArrayOutputStream accumulated = new ByteArrayOutputStream();
        byte[] buffer = new byte[READ_BUFFER_SIZE];

        int headerEndIndex = -1;

        // Phase 1: 헤더 끝(\r\n\r\n)이 나올 때까지 읽기
        while (headerEndIndex == -1) {
            int bytesRead = inputStream.read(buffer);
            if (bytesRead == -1) {
                break;
            }
            accumulated.write(buffer, 0, bytesRead);
            headerEndIndex = findHeaderEndIndex(accumulated.toByteArray());
        }

        if (accumulated.size() == 0) {
            return new byte[0];
        }

        // Phase 2: Content-Length가 있으면 body가 모두 도착할 때까지 읽기
        int contentLength = extractContentLength(accumulated.toByteArray(), headerEndIndex);
        if (contentLength > 0 && headerEndIndex != -1) {
            int headerSize = headerEndIndex + HEADER_END_MARKER.length;
            int totalExpected = headerSize + contentLength;

            while (accumulated.size() < totalExpected) {
                int bytesRead = inputStream.read(buffer);
                if (bytesRead == -1) {
                    break;
                }
                accumulated.write(buffer, 0, bytesRead);
            }
        }

        return accumulated.toByteArray();
    }

    private int findHeaderEndIndex(byte[] data) {
        for (int i = 0; i <= data.length - HEADER_END_MARKER.length; i++) {
            if (data[i] == '\r' && data[i + 1] == '\n' && data[i + 2] == '\r' && data[i + 3] == '\n') {
                return i;
            }
        }
        return -1;
    }

    private int extractContentLength(byte[] data, int headerEndIndex) {
        if (headerEndIndex == -1) {
            return 0;
        }
        String headerPart = new String(data, 0, headerEndIndex, StandardCharsets.UTF_8);
        for (String line : headerPart.split("\r\n")) {
            if (line.toLowerCase().startsWith("content-length:")) {
                return Integer.parseInt(line.substring("content-length:".length()).trim());
            }
        }
        return 0;
    }
}
