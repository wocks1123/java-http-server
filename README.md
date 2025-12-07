# Java HTTP Server

Java 소켓 프로그래밍을 통해 HTTP 웹 서버를 직접 구현하는 학습 프로젝트

## 학습 목표

- Spring MVC, 톰캣, 서블릿 컨테이너가 내부적으로 어떻게 동작하는지 직접 구현하며 이해하는 것이 목표
- HTTP 요청/응답 구조 및 프로토콜 원리 이해
- 서블릿 컨테이너가 하는 일을 직접 구현하며 체득
- FrontController(DispatcherServlet) 패턴 이해
- 세션/쿠키 기반 인증 흐름 이해

## 기술 스택

- Java 21
- Gradle (Kotlin DSL)
- JUnit 5

## 실행 방법

```bash
# 빌드
./gradlew build

# 실행
./gradlew run

# 테스트
./gradlew test
```

서버 실행 후 브라우저에서 `http://localhost:8080` 접속

## 구현 계획

구현 계획은 초안으로 진행 상황에 따라 유동적으로 변경될 수 있습니다.

* [x] 환경 세팅 및 최소 HTTP 서버 구현
    * [x] ServerSocket으로 연결 수락
    * [x] 하드코딩된 HTTP 응답 전송

* [x] HTTP 요청/응답 파싱
    * [x] Request Line 파싱 (Method, Path, Version)
    * [x] Header 파싱
    * [x] Body 파싱 (POST)
    * [x] HttpRequest / HttpResponse 클래스 설계
    * [x] 단위 테스트 작성

* [x] RequestHandler 구현
    * [x] InputStream → HttpRequest
    * [x] HttpResponse → OutputStream
    * [ ] 기본 요청 흐름 통합 테스트

* [x] Servlet / ServletContainer
    * [x] Servlet 인터페이스 정의
    * [x] URL → Servlet 매핑
    * [x] StaticResourceServlet 구현
    * [x] 컨테이너와 서블릿 책임 분리

* [ ] FrontController & 라우팅
    * [ ] Controller 인터페이스 정의
    * [ ] RouteKey(HttpMethod + Path) 기반 라우팅 구조
    * [ ] FrontControllerServlet 구현
    * [ ] Spring MVC DispatcherServlet 구조 이해

* [ ] 로그인 & 세션
    * [ ] User / UserRepository 구현
    * [ ] SessionManager 구현 (Cookie 기반)
    * [ ] 회원가입, 로그인, PRG 패턴 적용
    * [ ] 로그인 여부 확인 로직

* [ ] 멀티스레드 서버
    * [ ] ExecutorService로 요청 병렬 처리
    * [ ] 공유 저장소 동시성 고려
    * [ ] thread-safe 자료구조 도입

* [x] TODO LIST 기능 (간단 버전)
    * [x] Todo / TodoRepository
    * [x] TODO 목록 조회 (GET)
    * [x] TODO 추가 (POST)
    * [ ] 사용자별 TODO 관리
