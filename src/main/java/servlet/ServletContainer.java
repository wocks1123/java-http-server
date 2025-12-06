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

}
