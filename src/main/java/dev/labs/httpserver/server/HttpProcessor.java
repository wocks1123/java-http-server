package dev.labs.httpserver.server;

import dev.labs.httpserver.http.HttpRequest;
import dev.labs.httpserver.http.HttpRequestReader;
import dev.labs.httpserver.http.HttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.SocketTimeoutException;

public class HttpProcessor {

    private static final Logger log = LoggerFactory.getLogger(HttpProcessor.class);
    private static final int DEFAULT_MAX_REQUESTS = 100;

    private final HttpHandler httpHandler;
    private final int maxRequestsPerConnection;
    private final HttpRequestReader httpRequestReader = new HttpRequestReader();

    public HttpProcessor(HttpHandler httpHandler) {
        this(httpHandler, DEFAULT_MAX_REQUESTS);
    }

    public HttpProcessor(HttpHandler httpHandler, int maxRequestsPerConnection) {
        this.httpHandler = httpHandler;
        this.maxRequestsPerConnection = maxRequestsPerConnection;
    }

    public void handle(InputStream inputStream, OutputStream outputStream) {
        int requestCount = 0;

        while (true) {
            try {
                byte[] rawRequest = httpRequestReader.read(inputStream);
                if (rawRequest.length == 0) {
                    log.debug("Connection closed by client.");
                    break;
                }

                HttpRequest request = HttpRequest.parse(rawRequest);
                log.info("parsed: {} {}", request.getMethod(), request.getPath());

                HttpResponse response = new HttpResponse();
                httpHandler.handle(request, response);

                requestCount++;
                boolean shouldClose = "close".equalsIgnoreCase(request.getHeaders().get("Connection"))
                        || requestCount >= maxRequestsPerConnection;

                response.addHeader("Connection", shouldClose ? "close" : "keep-alive");

                byte[] responseBytes = response.toBytes();
                outputStream.write(responseBytes);
                outputStream.flush();

                int bodyLength = response.getBody() != null ? response.getBody().length : 0;
                log.info("wrote: {} {}bytes", response.getStatusCode(), bodyLength);

                if (shouldClose) {
                    break;
                }
            } catch (SocketTimeoutException e) {
                log.debug("Keep-alive connection timed out.");
                break;
            } catch (Exception e) {
                log.error("Exception while handling request:", e);
                break;
            }
        }
    }

}
