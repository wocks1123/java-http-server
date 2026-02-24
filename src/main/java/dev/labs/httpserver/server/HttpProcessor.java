package dev.labs.httpserver.server;

import dev.labs.httpserver.http.HttpRequest;
import dev.labs.httpserver.http.HttpRequestReader;
import dev.labs.httpserver.http.HttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.io.OutputStream;

public class HttpProcessor {

    private static final Logger log = LoggerFactory.getLogger(HttpProcessor.class);

    private final HttpHandler httpHandler;
    private final HttpRequestReader httpRequestReader = new HttpRequestReader();

    public HttpProcessor(HttpHandler httpHandler) {
        this.httpHandler = httpHandler;
    }

    public void handle(InputStream inputStream, OutputStream outputStream) {
        try {
            byte[] rawRequest = httpRequestReader.read(inputStream);
            if (rawRequest.length == 0) {
                log.warn("No data received from client.");
                return;
            }
            HttpRequest request = HttpRequest.parse(rawRequest);
            log.info("parsed: {} {}", request.getMethod(), request.getPath());

            HttpResponse response = new HttpResponse();
            httpHandler.handle(request, response);

            byte[] responseBytes = response.toBytes();
            outputStream.write(responseBytes);
            outputStream.flush();

            int bodyLength = response.getBody() != null ? response.getBody().length : 0;
            log.info("wrote: {} {}bytes", response.getStatusCode(), bodyLength);
        } catch (Exception e) {
            log.error("Exception while handling request:", e);
        }
    }

}
