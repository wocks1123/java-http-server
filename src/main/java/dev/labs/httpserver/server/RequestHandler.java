package dev.labs.httpserver.server;

import dev.labs.httpserver.http.HttpRequest;
import dev.labs.httpserver.http.HttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.io.OutputStream;

public class RequestHandler {

    private static final Logger log = LoggerFactory.getLogger(RequestHandler.class);

    private final HttpHandler httpHandler;

    private static final int BUFFER_SIZE = 8192;


    public RequestHandler(HttpHandler httpHandler) {
        this.httpHandler = httpHandler;
    }

    public void handle(InputStream inputStream, OutputStream outputStream) {
        try {
            byte[] buffer = new byte[BUFFER_SIZE];
            int bytesRead = inputStream.read(buffer);
            if (bytesRead == -1) {
                log.warn("No data received from client.");
                return;
            }
            byte[] rawRequest = new byte[bytesRead];
            System.arraycopy(buffer, 0, rawRequest, 0, bytesRead);
            HttpRequest request = HttpRequest.parse(rawRequest);
            HttpResponse response = new HttpResponse();
            httpHandler.handle(request, response);
            outputStream.write(response.toBytes());
            outputStream.flush();
        } catch (Exception e) {
            log.error("Exception while handling request:", e);
        }
    }

}
