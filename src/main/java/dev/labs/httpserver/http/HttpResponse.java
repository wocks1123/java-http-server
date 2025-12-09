package dev.labs.httpserver.http;

import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

public class HttpResponse {

    private String version = "HTTP/1.1";
    private HttpStatus status;
    private final Map<String, String> headers = new LinkedHashMap<>();
    private byte[] body;


    public String getVersion() {
        return version;
    }

    public int getStatusCode() {
        if (status == null) {
            throw new IllegalStateException("Status is not set");
        }
        return status.getCode();
    }

    public HttpStatus getStatus() {
        return status;
    }

    public String getStatusMessage() {
        if (status == null) {
            throw new IllegalStateException("Status is not set");
        }
        return status.getMessage();
    }

    public Map<String, String> getHeaders() {
        return Map.copyOf(headers);
    }

    public byte[] getBody() {
        return body == null ? null : body.clone();
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public void setStatus(HttpStatus status) {
        this.status = status;
    }

    public void setStatus(int code) {
        this.status = HttpStatus.fromCode(code);
    }

    public void setBody(byte[] bodyBytes) {
        if (bodyBytes == null) {
            this.body = null;
            this.headers.remove("Content-Length");
            return;
        }
        this.body = bodyBytes.clone();
        this.headers.put("Content-Length", Integer.toString(this.body.length));
    }

    public void setBody(String body) {
        setBody(body == null ? null : body.getBytes(StandardCharsets.UTF_8));
    }

    public void addHeader(String name, String value) {
        this.headers.put(name, value);
    }

    public byte[] toBytes() {
        StringBuilder response = new StringBuilder();

        if (status == null) {
            throw new IllegalStateException("Status is not set");
        }

        response.append(String.format("%s %d %s\r\n", version, status.getCode(), status.getMessage()));

        Map<String, String> tempHeaders = new LinkedHashMap<>(headers);
        tempHeaders.put("Content-Length", body == null ? "0" : Integer.toString(body.length));
        tempHeaders.put("Connection", "close");

        tempHeaders.forEach((key, value) ->
                response.append(key).append(": ").append(value).append("\r\n")
        );

        if (!tempHeaders.isEmpty()) {
            response.append("\r\n");
        }

        if (body == null) {
            return response.toString().getBytes(StandardCharsets.UTF_8);
        }

        byte[] headerBytes = response.toString().getBytes(StandardCharsets.UTF_8);
        byte[] result = new byte[headerBytes.length + body.length];
        System.arraycopy(headerBytes, 0, result, 0, headerBytes.length);
        System.arraycopy(body, 0, result, headerBytes.length, body.length);
        return result;
    }

    public String toString() {
        return new String(toBytes(), StandardCharsets.UTF_8);
    }

}
