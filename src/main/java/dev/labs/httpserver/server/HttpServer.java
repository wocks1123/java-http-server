package dev.labs.httpserver.server;

import dev.labs.httpserver.servlet.ServletContainer;
import dev.labs.httpserver.servlet.ServletHttpHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class HttpServer {

    private static final Logger log = LoggerFactory.getLogger(HttpServer.class);

    private final int port;
    private final ServletContainer servletContainer;

    private static final int THREAD_POOL_SIZE = 10;
    private static final int DEFAULT_KEEP_ALIVE_TIMEOUT_MS = 10_000;

    private final int keepAliveTimeoutMs;
    private volatile boolean running = false;
    private ServerSocket serverSocket;
    private ExecutorService threadPool;

    public HttpServer(int port, ServletContainer servletContainer) {
        this(port, servletContainer, DEFAULT_KEEP_ALIVE_TIMEOUT_MS);
    }

    public HttpServer(int port, ServletContainer servletContainer, int keepAliveTimeoutMs) {
        this.port = port;
        this.servletContainer = servletContainer;
        this.keepAliveTimeoutMs = keepAliveTimeoutMs;
    }

    public void start() {
        threadPool = Executors.newFixedThreadPool(THREAD_POOL_SIZE);
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            this.serverSocket = serverSocket;
            running = true;
            log.info("HttpServer started on port {}", serverSocket.getLocalPort());

            while (running) {
                Socket clientSocket = serverSocket.accept();
                threadPool.submit(() -> {
                    RequestContext.init();
                    log.info("accept");
                    try {
                        clientSocket.setSoTimeout(keepAliveTimeoutMs);
                        handleRequest(clientSocket, servletContainer);
                    } catch (IOException e) {
                        log.error("Error configuring client socket:", e);
                    } finally {
                        RequestContext.clear();
                    }
                });
            }
        } catch (IOException e) {
            if (running) {
                log.error("Server error:", e);
            }
        } finally {
            running = false;
            threadPool.shutdown();
        }
    }

    public void stop() {
        running = false;
        if (serverSocket != null && !serverSocket.isClosed()) {
            try {
                serverSocket.close();
            } catch (IOException e) {
                log.warn("Error closing server socket", e);
            }
        }
    }

    public int getPort() {
        return serverSocket != null ? serverSocket.getLocalPort() : port;
    }

    private static void handleRequest(Socket socket, ServletContainer servletContainer) {
        try (
                InputStream inputStream = socket.getInputStream();
                OutputStream outputStream = socket.getOutputStream()
        ) {
            HttpProcessor requestHandler = new HttpProcessor(new ServletHttpHandler(servletContainer));
            requestHandler.handle(inputStream, outputStream);
        } catch (IOException e) {
            log.error("Error handling client request:", e);
        }
    }
}
