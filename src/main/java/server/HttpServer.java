package server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
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
        } catch (IOException e) {
            log.error("Server error:", e);
        }
    }

    private static void handleRequest(Socket socket) {
        try (OutputStream out = socket.getOutputStream()) {
            String response = """
                    HTTP/1.1 200 OK
                    Content-Type: text/plain
                    Content-Length: 11
                    
                    Hello World
                    """;
            out.write(response.getBytes());
            out.flush();
        } catch (IOException e) {
            log.error("Response error: ", e);
        }
    }
}
