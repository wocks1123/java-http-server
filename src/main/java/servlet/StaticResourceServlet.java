package servlet;

import http.HttpRequest;
import http.HttpResponse;

public class StaticResourceServlet implements Servlet {

    private static final String STATIC_RESOURCE_ROOT_PATH = "static";
    private static final String URL_PREFIX = "/static";

    @Override
    public void service(HttpRequest request, HttpResponse response) {
        throw new UnsupportedOperationException("구현 필요");
    }

}