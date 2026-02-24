package dev.labs.httpserver.server;

import dev.labs.httpserver.http.HttpStatus;
import dev.labs.httpserver.servlet.ServletContainer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HttpServerTest {

    private static final int HANDLER_DELAY_MS = 300;    // 핸들러당 처리 시간

    private static final int CONCURRENT_REQUESTS = 20;
    private static final int TIMEOUT_MS = 3000;

    private HttpServer server;

    @BeforeEach
    void setUp() throws InterruptedException {
        ServletContainer servletContainer = new ServletContainer();
        servletContainer.registerServlet("/*", (request, response) -> {
            try {
                Thread.sleep(HANDLER_DELAY_MS); // 실제 처리 시간 시뮬레이션
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            response.setStatus(HttpStatus.OK);
            response.setBody("OK");
        });

        server = new HttpServer(0, servletContainer);
        Thread serverThread = new Thread(server::start);
        serverThread.setDaemon(true);
        serverThread.start();

        Thread.sleep(100); // 서버 기동 대기
    }

    @AfterEach
    void tearDown() {
        server.stop();
    }

    @Test
    @DisplayName("요청이 병렬로 처리된다")
    void handlesRequestsInParallel() throws InterruptedException {
        // given
        CountDownLatch latch = new CountDownLatch(CONCURRENT_REQUESTS);
        AtomicInteger successCount = new AtomicInteger(0);

        HttpClient httpClient = HttpClient.newHttpClient();
        String url = "http://localhost:" + server.getPort() + "/";

        // when
        long start = System.currentTimeMillis();

        ExecutorService clients = Executors.newFixedThreadPool(CONCURRENT_REQUESTS);
        for (int i = 0; i < CONCURRENT_REQUESTS; i++) {
            clients.submit(() -> {
                try {
                    HttpResponse<String> response = httpClient.send(
                            HttpRequest.newBuilder()
                                    .uri(URI.create(url))
                                    .GET()
                                    .build(),
                            HttpResponse.BodyHandlers.ofString()
                    );
                    if (response.statusCode() == 200) {
                        successCount.incrementAndGet();
                    } else {
                        System.err.println("예상치 못한 상태코드: " + response.statusCode());
                    }
                } catch (Exception e) {
                    System.err.println("요청 실패: " + e);
                } finally {
                    latch.countDown();
                }
            });
        }

        boolean completed = latch.await(TIMEOUT_MS, TimeUnit.MILLISECONDS);
        long elapsed = System.currentTimeMillis() - start;
        clients.shutdown();

        // then
        assertTrue(completed, TIMEOUT_MS + "ms 안에 완료 실패");
        assertEquals(CONCURRENT_REQUESTS, successCount.get());

        long sequentialTime = (long) CONCURRENT_REQUESTS * HANDLER_DELAY_MS;
        assertTrue(elapsed < sequentialTime);
    }

}
