package http;

import java.util.HashMap;
import java.util.Map;

public class HttpResponse {

    private String version = "HTTP/1.1";
    private HttpStatus status;
    private final Map<String, String> headers = new HashMap<>();
    private byte[] body;


    public String getVersion() {
        return version;
    }

    public int getStatusCode() {
        return status.getCode();
    }

    public HttpStatus getStatus() {
        return status;
    }

    public String getStatusMessage() {
        return status.getMessage();
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public byte[] getBody() {
        return body;
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
            return;
        }
        this.headers.put("Content-Length", Integer.toString(bodyBytes.length));
        this.body = bodyBytes;
    }

    public void setBody(String body) {
        setBody(body.getBytes());
    }

    public void addHeader(String name, String value) {
        this.headers.put(name, value);
    }

    public byte[] toBytes() {
        StringBuilder response = new StringBuilder();

        response.append(String.format("%s %d %s\r\n", version, status.getCode(), status.getMessage()));

        headers.forEach((key, value) ->
                response.append(key).append(": ").append(value).append("\r\n")
        );

        if (!headers.isEmpty()) {
            response.append("\r\n");
        }

        if (body == null) {
            return response.toString().getBytes();
        }

        byte[] headerBytes = response.toString().getBytes();
        byte[] result = new byte[headerBytes.length + body.length];
        System.arraycopy(headerBytes, 0, result, 0, headerBytes.length);
        System.arraycopy(body, 0, result, headerBytes.length, body.length);
        return result;
    }

}
