package servlet;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class StaticResourceServletTest {

    @Test
    @DisplayName("html 파일을 정상적으로 반환한다")
    void testServeExistingHtmlFile() {
        // given

        // when

        // then
    }

    @Test
    @DisplayName("존재하지 않는 파일 요청 시 404를 반환한다")
    void testServeNonExistentFileReturns404() {
        // given

        // when

        // then
    }

    @Test
    @DisplayName("부적절한 경로에 대한 요청을 차단한다")
    void testBlockInvalidPath() {
        // given

        // when

        // then
    }

}