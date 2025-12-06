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
        this.queryParams = queryParams != null ? Map.copyOf(queryParams) : Map.of();
        this.headers = headers != null ? Map.copyOf(headers) : Map.of();
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
        String version = requestLineParts[2];
        String path = requestLineParts[1];

        Map<String, String> queryParams = new HashMap<>();
        String[] pathParts = path.split("\\?", 2);
        if (pathParts.length == 2) {
            path = pathParts[0];
            String queryString = pathParts[1];
            String[] pairs = queryString.split("&");
            for (String pair : pairs) {
                String[] keyValue = pair.split("=", 2);
                if (keyValue.length == 2) {
                    queryParams.put(keyValue[0], keyValue[1]);
                }
            }
        }

        Map<String, String> headers = new HashMap<>();
        for (int i = 1; i < lines.length; i++) {
            String[] headerParts = lines[i].split(": ", 2);
            if (headerParts.length == 2) {
                headers.put(headerParts[0], headerParts[1]);
            }
        }

        int contentLength = headers.containsKey("Content-Length") ? Integer.parseInt(headers.get("Content-Length")) : 0;
        if (contentLength > 0 && bodyPart.length() > contentLength) {
            bodyPart = bodyPart.substring(0, contentLength);
        }

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
