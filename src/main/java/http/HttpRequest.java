package http;

import java.util.HashMap;
import java.util.Map;

public class HttpRequest {

    private final HttpMethod method;
    private final String path;
    private final String version;
    private final Map<String, String> queryParams;
    private final Map<String, String> headers;
    private final String body;

    public HttpRequest(HttpMethod method, String path, String version, Map<String, String> queryParams, Map<String, String> headers, String body) {
        this.method = method;
        this.path = path;
        this.version = version;
        this.queryParams = queryParams;
        this.headers = headers;
        this.body = body;
    }

    public static HttpRequest parse(byte[] rawRequest) {
        String requestString = new String(rawRequest);
        String[] requestParts = requestString.split("\r\n\r\n", 2);
        String headerPart = requestParts[0];
        String bodyPart = requestParts.length > 1 ? requestParts[1] : "";

        String[] lines = headerPart.split("\r\n");
        String requestLine = lines[0];
        String[] requestLineParts = requestLine.split(" ");
        HttpMethod method = HttpMethod.valueOf(requestLineParts[0]);
        String path = requestLineParts[1];
        String version = requestLineParts[2];

        Map<String, String> headers = new HashMap<>();
        for (int i = 1; i < lines.length; i++) {
            String[] headerParts = lines[i].split(": ", 2);
            if (headerParts.length == 2) {
                headers.put(headerParts[0], headerParts[1]);
            }
        }

        Map<String, String> queryParams = new HashMap<>();
        // TODO: 쿼리 파라미터 파싱 로직 추가

        return new HttpRequest(method, path, version, queryParams, headers, bodyPart);
    }

    public HttpMethod getMethod() {
        return method;
    }

    public String getPath() {
        return path;
    }

    public String getVersion() {
        return version;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public String getBody() {
        return body;
    }

    public Map<String, String> getQueryParams() {
        return queryParams;
    }
}
