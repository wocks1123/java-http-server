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

        String longestMatchPrefix = null;
        Servlet longestMatchServlet = null; // 일치하는 서블릿 중 가장 긴 프리픽스를 가진 서블릿
        for (Map.Entry<String, Servlet> entry : servletMap.entrySet()) {
            String pattern = entry.getKey();

            if (pattern.endsWith("/*")) {
                String prefix = pattern.substring(0, pattern.length() - 1);

                if (path.startsWith(prefix)) {
                    // 하위 경로와 매칭되는 가장 긴 프리픽스를 찾는다
                    if (longestMatchPrefix == null || prefix.length() > longestMatchPrefix.length()) {
                        longestMatchPrefix = prefix;
                        longestMatchServlet = entry.getValue();
                    }
                }
            }
        }

        return longestMatchServlet;
    }

}
