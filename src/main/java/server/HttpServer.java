package server;

import http.HttpRequest;
import http.HttpResponse;
import http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class HttpServer {

    private static final Logger log = LoggerFactory.getLogger(HttpServer.class);

    private static final int PORT = 8080;


    public static void main(String[] args) {
        log.info("WebServer started on port " + PORT);

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            while (true) {
                Socket clientSocket = serverSocket.accept();
                log.info("Client connected: {}", clientSocket.getInetAddress());

                handleRequest(clientSocket);
            }
        } catch (Exception e) {
            log.error("Server error:", e);
        }
    }

    private static void handleRequest(Socket socket) {
        try (
                InputStream inputStream = socket.getInputStream();
                OutputStream outputStream = socket.getOutputStream()
        ) {
            RequestHandler requestHandler = new RequestHandler(new SimpleHttpHandler());
            requestHandler.handle(inputStream, outputStream);
        } catch (IOException e) {
            log.error("Error handling client request:", e);
        }
    }

    static class SimpleHttpHandler implements HttpHandler {
        @Override
        public void handle(HttpRequest request, HttpResponse response) {
            response.setStatus(HttpStatus.OK);
            response.setBody("Hello World\r\n");
        }
    }

}
