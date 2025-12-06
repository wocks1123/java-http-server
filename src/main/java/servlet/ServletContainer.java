package servlet;

import java.util.HashMap;
import java.util.Map;

public class ServletContainer {

    private final Map<String, Servlet> servletMap = new HashMap<>();


    public void registerServlet(String path, Servlet servlet) {
        servletMap.put(path, servlet);
    }

    public Servlet resolveServlet(String path) {
        Servlet servlet = servletMap.get(path);
        if (servlet != null) {
            return servlet;
        }

        for (Map.Entry<String, Servlet> entry : servletMap.entrySet()) {
            String key = entry.getKey();
            if (key.endsWith("/*")) {
                String basePath = key.substring(0, key.length() - 1);
                if (path.startsWith(basePath)) {
                    return entry.getValue();
                }
            }
        }

        return null;
    }

}
