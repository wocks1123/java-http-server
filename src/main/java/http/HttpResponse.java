package http;

import java.util.HashMap;
import java.util.Map;

public class HttpResponse {

    private String version = "HTTP/1.1";
    private HttpStatus status;
    private final Map<String, String> headers = new HashMap<>();
    private String body;


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

    public String getBody() {
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

    public void setBody(String body) {
        if (body == null) {
            body = "";
        }
        this.headers.put("Content-Length", Integer.toString(body.length()));
        this.body = body;
    }

    public void addHeader(String name, String value) {
        this.headers.put(name, value);
    }

    public byte[] getBytes() {
        String response = """
                %s %d %s\r
                %s%s\r
                """
                .formatted(
                        version, status.getCode(), status.getMessage(),
                        headers.entrySet().stream()
                                .map(entry -> entry.getKey() + ": " + entry.getValue())
                                .reduce("", (acc, header) -> acc + header + "\r\n"),
                        body != null ? body : ""
                );
        return response.getBytes();
    }

}
