package servlet;

import java.util.HashMap;
import java.util.Map;

public class ServletContainer {

    private final Map<String, Servlet> servletMap = new HashMap<>();


    public void registerServlet(String path, Servlet servlet) {
        servletMap.put(path, servlet);
    }

    public Servlet findServlet(String path) {
        return servletMap.get(path);
    }

    public Servlet resolveServlet(String path) {
        throw new UnsupportedOperationException("구현 예정");
    }

}
