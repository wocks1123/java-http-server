package http;

public class HttpResponse {
    
    public byte[] getBytes() {
        String response = """
                HTTP/1.1 200 OK
                Content-Type: text/plain
                Content-Length: 11
                
                Hello World
                """;
        return response.getBytes();
    }

}
