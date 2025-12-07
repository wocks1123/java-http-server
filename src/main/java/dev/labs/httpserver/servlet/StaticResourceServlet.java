package dev.labs.httpserver.servlet;

import dev.labs.httpserver.http.HttpRequest;
import dev.labs.httpserver.http.HttpResponse;
import dev.labs.httpserver.http.HttpStatus;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;

public class StaticResourceServlet implements Servlet {

    private static final String STATIC_RESOURCE_ROOT_PATH = "static";
    private static final String URL_PREFIX = "/static";

    @Override
    public void service(HttpRequest request, HttpResponse response) {
        String requestPath = request.getPath();

        if (!requestPath.startsWith(URL_PREFIX)) {
            response.setStatus(HttpStatus.NOT_FOUND);
            return;
        }

        String resourcePath = extractResourcePath(requestPath);

        if (resourcePath.contains("..") || resourcePath.contains("//")) {
            response.setStatus(HttpStatus.NOT_FOUND);
            return;
        }

        try (InputStream inputStream = getClass()
                .getClassLoader()
                .getResourceAsStream(Paths.get(STATIC_RESOURCE_ROOT_PATH, resourcePath).toString())) {

            if (inputStream == null) {
                response.setStatus(HttpStatus.NOT_FOUND);
                return;
            }

            byte[] content = inputStream.readAllBytes();
            String contentType = determineContentType(resourcePath);
            response.addHeader("Content-Type", contentType);
            response.setStatus(HttpStatus.OK);
            response.setBody(content);
        } catch (IOException e) {
            response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private String extractResourcePath(String requestPath) {
        // "/static/index.html" -> "index.html"
        String path = requestPath.substring(URL_PREFIX.length());
        if (path.startsWith("/")) {
            path = path.substring(1);
        }

        if (path.isEmpty()) {
            path = "index.html";
        }

        return path;
    }

    private String determineContentType(String resourcePath) {
        String lowerPath = resourcePath.toLowerCase();

        if (lowerPath.endsWith(".html")) {
            return "text/html";
        } else if (lowerPath.endsWith(".css")) {
            return "text/css";
        } else if (lowerPath.endsWith(".js")) {
            return "application/javascript";
        } else if (lowerPath.endsWith(".png")) {
            return "image/png";
        } else if (lowerPath.endsWith(".jpg") || lowerPath.endsWith(".jpeg")) {
            return "image/jpeg";
        } else if (lowerPath.endsWith(".gif")) {
            return "image/gif";
        } else if (lowerPath.endsWith(".svg")) {
            return "image/svg+xml";
        } else if (lowerPath.endsWith(".ico")) {
            return "image/x-icon";
        }

        return "application/octet-stream";
    }
}