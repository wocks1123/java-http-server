package fixture;

import http.HttpMethod;
import http.HttpRequest;

import java.util.HashMap;
import java.util.Map;

public class HttpRequestFixture {

    private HttpMethod method = HttpMethod.GET;
    private String path = "/";
    private String version = "HTTP/1.1";
    private final Map<String, String> queryParams = new HashMap<>();
    private final Map<String, String> headers = new HashMap<>();
    private String body = null;

    private HttpRequestFixture() {
    }

    public static HttpRequestFixture builder() {
        return new HttpRequestFixture();
    }

    public static HttpRequest defaultRequest() {
        return builder().build();
    }

    public HttpRequestFixture method(HttpMethod method) {
        this.method = method;
        return this;
    }

    public HttpRequestFixture get() {
        return method(HttpMethod.GET);
    }

    public HttpRequestFixture post() {
        return method(HttpMethod.POST);
    }

    public HttpRequestFixture path(String path) {
        this.path = path;
        return this;
    }

    public HttpRequestFixture version(String version) {
        this.version = version;
        return this;
    }

    public HttpRequestFixture queryParam(String key, String value) {
        this.queryParams.put(key, value);
        return this;
    }

    public HttpRequestFixture queryParams(Map<String, String> queryParams) {
        this.queryParams.putAll(queryParams);
        return this;
    }

    public HttpRequestFixture header(String key, String value) {
        this.headers.put(key, value);
        return this;
    }

    public HttpRequestFixture headers(Map<String, String> headers) {
        this.headers.putAll(headers);
        return this;
    }

    public HttpRequestFixture body(String body) {
        this.body = body;
        return this;
    }

    public HttpRequest build() {
        return new HttpRequest(
                method,
                path,
                version,
                queryParams.isEmpty() ? null : queryParams,
                headers.isEmpty() ? null : headers,
                body
        );
    }

}
