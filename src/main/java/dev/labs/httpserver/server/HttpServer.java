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

public class HttpServer {

    private static final Logger log = LoggerFactory.getLogger(HttpServer.class);

    private final int port;
    private final ServletContainer servletContainer;

    private volatile boolean running = false;
    private ServerSocket serverSocket;

    public HttpServer(int port, ServletContainer servletContainer) {
        this.port = port;
        this.servletContainer = servletContainer;
    }

    public void start() {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            this.serverSocket = serverSocket;
            running = true;
            log.info("HttpServer started on port {}", serverSocket.getLocalPort());

            while (running) {
                Socket clientSocket = serverSocket.accept();
                log.info("Client connected: {}", clientSocket.getInetAddress());
                handleRequest(clientSocket, servletContainer);
            }
        } catch (IOException e) {
            if (running) {
                log.error("Server error:", e);
            }
        } finally {
            running = false;
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
            RequestHandler requestHandler = new RequestHandler(new ServletHttpHandler(servletContainer));
            requestHandler.handle(inputStream, outputStream);
        } catch (IOException e) {
            log.error("Error handling client request:", e);
        }
    }
}
