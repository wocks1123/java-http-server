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
        String statusLine = "%s %d %s\r\n".formatted(version, status.getCode(), status.getMessage());
        String headersString = headers.entrySet().stream()
                .map(entry -> entry.getKey() + ": " + entry.getValue() + "\r\n")
                .reduce("", String::concat);
        String bodyString = body != null ? body : "";

        String separator = headers.isEmpty() ? "" : "\r\n";
        return (statusLine + headersString + separator + bodyString).getBytes();
    }

}
