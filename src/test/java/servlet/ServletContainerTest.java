package servlet;

import http.HttpRequest;
import http.HttpResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class ServletContainerTest {

    @Test
    @DisplayName("SevletContainer에 서블릿을 등록한 Servlet을 찾을 수 있다.")
    void testRegisterAndFindServlet() {
        // given
        final ServletContainer servletContainer = new ServletContainer();
        final String path = "/test";
        final Servlet servlet = new DummyServlet();

        // when
        servletContainer.registerServlet(path, servlet);
        final Servlet foundServlet = servletContainer.resolveServlet(path);

        // then
        assertNotNull(foundServlet);
        assertEquals(servlet, foundServlet);
    }

    @Test
    @DisplayName("등록되지 않은 경로로 Servlet을 찾으면 null을 반환한다.")
    void testFindServletNotRegistered() {
        // given
        final ServletContainer servletContainer = new ServletContainer();
        final String path = "/not-registered";

        // when
        final Servlet foundServlet = servletContainer.resolveServlet(path);

        // then
        assertNull(foundServlet);
    }

    @Test
    @DisplayName("와일드카드 패턴으로 서블릿을 등록할 수 있다")
    void testRegisterServletWithWildcardPattern() {
        // given
        final ServletContainer servletContainer = new ServletContainer();
        final String wildcardPath = "/api/*";
        final Servlet servlet = new DummyServlet();

        // when
        servletContainer.registerServlet(wildcardPath, servlet);
        final Servlet foundServlet = servletContainer.resolveServlet("/api/test");

        // then
        assertNotNull(foundServlet);
        assertEquals(servlet, foundServlet);
    }

    @Test
    @DisplayName("와일드카드 패턴과 일치하지 않는 경로는 매칭되지 않는다")
    void testWildcardPatternDoesNotMatchDifferentPath() {
        // given
        final ServletContainer servletContainer = new ServletContainer();
        final String wildcardPath = "/api/*";
        final Servlet servlet = new DummyServlet();

        // when
        servletContainer.registerServlet(wildcardPath, servlet);

        // then
        assertNull(servletContainer.resolveServlet("/static/test"));
        assertNull(servletContainer.resolveServlet("/static"));
        assertNull(servletContainer.resolveServlet("/api"));
    }

    @Test
    @DisplayName("정확한 경로 매칭이 와일드카드 패턴보다 우선한다")
    void testExactPathMatchHasPriorityOverWildcard() {
        // given
        final ServletContainer servletContainer = new ServletContainer();
        final String wildcardPath = "/api/*";
        final String specificPath = "/api/test";
        final Servlet wildcardServlet = new DummyServlet();
        final Servlet specificServlet = new DummyServlet();

        // when
        servletContainer.registerServlet(wildcardPath, wildcardServlet);
        servletContainer.registerServlet(specificPath, specificServlet);
        final Servlet foundServlet = servletContainer.resolveServlet(specificPath);

        // then
        assertEquals(specificServlet, foundServlet);
    }

    @Test
    @DisplayName("루트 와일드카드 패턴으로 모든 경로를 처리할 수 있다")
    void testRootWildcardPatternMatchesAllPaths() {
        // given
        final ServletContainer servletContainer = new ServletContainer();
        final String rootWildcardPath = "/*";
        final Servlet servlet = new DummyServlet();

        // when
        servletContainer.registerServlet(rootWildcardPath, servlet);

        // then
        assertEquals(servlet, servletContainer.resolveServlet("/any/path"));
        assertEquals(servlet, servletContainer.resolveServlet("/another/path"));
        assertEquals(servlet, servletContainer.resolveServlet("/"));
    }

    private static class DummyServlet implements Servlet {
        @Override
        public void service(HttpRequest request, HttpResponse response) {
            // do nothing
        }
    }

}
