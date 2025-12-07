package dev.labs.httpserver.http;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

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
        String requestString = new String(rawRequest, StandardCharsets.UTF_8);

        String[] requestParts = requestString.split("\r\n\r\n", 2);
        String headerPart = requestParts[0];
        String bodyPart = requestParts.length > 1 ? requestParts[1] : "";

        String[] headerLines = headerPart.split("\r\n");
        String[] requestLineParts = headerLines[0].split(" ");
        HttpMethod method = HttpMethod.valueOf(requestLineParts[0]);
        String version = requestLineParts[2];

        String fullPath = requestParts[0].split(" ")[1];
        String path = extractPath(fullPath);
        Map<String, String> queryParams = extractQueryParams(fullPath);

        Map<String, String> headers = parseHeaders(headerLines);

        int contentLength = headers.containsKey("Content-Length") ? Integer.parseInt(headers.get("Content-Length")) : 0;
        if (contentLength > 0 && bodyPart.length() > contentLength) {
            bodyPart = bodyPart.substring(0, contentLength);
        }

        return new HttpRequest(method, path, version, queryParams, headers, bodyPart);
    }

    private static String extractPath(String fullPath) {
        int queryIndex = fullPath.indexOf("?");
        return queryIndex != -1 ? fullPath.substring(0, queryIndex) : fullPath;
    }

    private static Map<String, String> extractQueryParams(String fullPath) {
        String[] pathParts = fullPath.split("\\?", 2);
        if (pathParts.length < 2) {
            return Map.of();
        }

        Map<String, String> queryParamMap = new HashMap<>();
        String[] pairs = pathParts[1].split("&");
        for (String pair : pairs) {
            String[] keyValue = pair.split("=", 2);
            if (keyValue.length == 2) {
                queryParamMap.put(keyValue[0], keyValue[1]);
            }
        }
        return queryParamMap;
    }

    private static Map<String, String> parseHeaders(String[] headerLines) {
        Map<String, String> headerMap = new HashMap<>();
        for (int i = 1; i < headerLines.length; i++) {
            String line = headerLines[i];
            String[] keyValue = line.split(":", 2);
            if (keyValue.length == 2) {
                headerMap.put(keyValue[0].trim(), keyValue[1].trim());
            }
        }
        return headerMap;
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

    public String toString() {
        String base = String.format("[HttpRequest] %s %s %s", method, path, version);

        String queryParamsSection = queryParams.isEmpty() ? "" :
                "\n\n[Query Parameters]\n" +
                queryParams.entrySet().stream()
                        .map(e -> String.format("  %s = %s", e.getKey(), e.getValue()))
                        .collect(Collectors.joining("\n"));

        String headersSection = headers.isEmpty() ? "" :
                "\n\n[Headers]\n" +
                headers.entrySet().stream()
                        .map(e -> String.format("  %s: %s", e.getKey(), e.getValue()))
                        .collect(Collectors.joining("\n"));

        String bodySection = (body == null || body.isEmpty()) ? "" :
                "\n\n[Body]\n" + body;

        return base + queryParamsSection + headersSection + bodySection;
    }

}
