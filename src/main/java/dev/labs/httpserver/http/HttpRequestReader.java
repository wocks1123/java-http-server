package dev.labs.httpserver.http;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class HttpRequestReader {

    private static final byte[] HEADER_END_MARKER = "\r\n\r\n".getBytes(StandardCharsets.UTF_8);
    private static final int READ_BUFFER_SIZE = 4096;

    private byte[] remainingBytes = new byte[0];

    public byte[] read(InputStream inputStream) throws IOException {
        ByteArrayOutputStream accumulated = new ByteArrayOutputStream();

        // 이전 read()에서 초과 읽은 바이트가 있으면 먼저 accumulated에 넣기
        if (remainingBytes.length > 0) {
            accumulated.write(remainingBytes);
            remainingBytes = new byte[0];
        }

        byte[] buffer = new byte[READ_BUFFER_SIZE];
        int headerEndIndex = findHeaderEndIndex(accumulated.toByteArray());

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

        if (headerEndIndex == -1) {
            return accumulated.toByteArray();
        }

        // Phase 2: Content-Length가 있으면 body가 모두 도착할 때까지 읽기
        int contentLength = extractContentLength(accumulated.toByteArray(), headerEndIndex);
        int requestEnd;

        if (contentLength > 0) {
            int headerSize = headerEndIndex + HEADER_END_MARKER.length;
            int totalExpected = headerSize + contentLength;

            while (accumulated.size() < totalExpected) {
                int bytesRead = inputStream.read(buffer);
                if (bytesRead == -1) {
                    break;
                }
                accumulated.write(buffer, 0, bytesRead);
            }
            requestEnd = Math.min(totalExpected, accumulated.size());
        } else {
            requestEnd = headerEndIndex + HEADER_END_MARKER.length;
        }

        byte[] allBytes = accumulated.toByteArray();

        // 현재 요청 경계를 넘은 바이트는 remainingBytes에 보관
        if (allBytes.length > requestEnd) {
            remainingBytes = Arrays.copyOfRange(allBytes, requestEnd, allBytes.length);
        }

        return Arrays.copyOfRange(allBytes, 0, requestEnd);
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
