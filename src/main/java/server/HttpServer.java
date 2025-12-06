package server;

import http.HttpRequest;
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
    private static final int BUFFER_SIZE = 8192;


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
        try (
                InputStream in = socket.getInputStream();
                OutputStream out = socket.getOutputStream()
        ) {
            byte[] buffer = new byte[BUFFER_SIZE];
            int bytesRead = in.read(buffer);
            if (bytesRead == -1) {
                log.warn("No data received from client.");
                return;
            }

            byte[] rawRequest = new byte[bytesRead];
            System.arraycopy(buffer, 0, rawRequest, 0, bytesRead);
            HttpRequest request = HttpRequest.parse(rawRequest);
            log.info("Request received from client:\n{}", request);

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
