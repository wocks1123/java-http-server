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

### HTTP 서버 기초

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
    * [x] 기본 요청 흐름 통합 테스트

### 서블릿 & MVC

* [x] Servlet / ServletContainer
    * [x] Servlet 인터페이스 정의
    * [x] URL → Servlet 매핑
    * [x] StaticResourceServlet 구현
    * [x] 컨테이너와 서블릿 책임 분리

* [x] FrontController & 라우팅
    * [x] Controller 인터페이스 정의
    * [x] RouteKey(HttpMethod + Path) 기반 라우팅 구조
    * [x] FrontControllerServlet 구현
    * [x] Spring MVC DispatcherServlet 구조 이해

* [ ] 로그인 & 세션
    * [ ] User / UserRepository 구현
    * [ ] SessionManager 구현 (Cookie 기반)
    * [ ] 회원가입, 로그인, PRG 패턴 적용
    * [ ] 로그인 여부 확인 로직

* [x] 멀티스레드 서버
    * [x] ExecutorService로 요청 병렬 처리
    * [x] 공유 저장소 동시성 고려
    * [x] thread-safe 자료구조 도입 (`ConcurrentHashMap`, `AtomicLong`)

* [x] TODO LIST 기능 (간단 버전)
    * [x] Todo / TodoRepository
    * [x] TODO 목록 조회 (GET)
    * [x] TODO 추가 (POST)
    * [ ] 사용자별 TODO 관리

### JDBC & 트랜잭션

* [x] JDBC Foundations
    * [x] `DataSource → Connection → PreparedStatement → ResultSet` 흐름을 코드로 확인 (`DataSource` 인터페이스 대신 `DriverManager` 직접 사용, `DatabaseConfig`)
    * [x] `autoCommit=true/false` 차이 재현 (부분 커밋 vs 원자성)
    * [x] `commit / rollback` 직접 호출해 트랜잭션 경계 체감
    * [x] `close()` 타이밍(commit 전/후, 예외 시) 차이 관찰
    * [x] “트랜잭션 = Connection 상태”를 설명할 수 있다

* [x] JDBC Repository (직사용 버전)
    * [x] `JdbcTodoRepository` 구현 (CRUD 최소 세트) (`JdbcTodoRepositoryV2`가 직사용 버전, `JdbcTodoRepository`는 이후 `JdbcExecutor`기반 버전)
    * [x] try/finally로 `Statement/ResultSet/Connection` 자원 해제 보장
    * [x] `SQLException`을 Runtime 예외로 변환해 전파
    * [x] 부분 커밋이 실제로 발생하는 유즈케이스를 서비스 레벨에서 재현

* [ ] Spring JDBC 책임 1: 실행 흐름 템플릿화 (JdbcTemplate)
    * [ ] `JdbcTemplate#update(sql, PreparedStatementSetter)` 구현
    * [ ] `JdbcTemplate#query(sql, PreparedStatementSetter, RowMapper<T>)` 구현
    * [ ] `JdbcTemplate#queryForObject(...)` 구현 (0건/2건 이상 정책 포함)
    * [ ] `PreparedStatementSetter` 도입 (파라미터 바인딩 책임 분리)
    * [ ] `RowMapper<T>` 도입 (ResultSet 매핑 책임 분리)
    * [ ] Repository에서 JDBC 반복 코드(획득/해제/예외 처리)를 제거

* [x] Spring JDBC 책임 2: 트랜잭션-커넥션 동기화 (Connection Binding)
    * [x] `TransactionSynchronizationManager` 구현 (ThreadLocal 리소스 바인딩) (`ConnectionContext`로 구현)
    * [ ] `ConnectionHolder` 구현 (Connection + 최소 상태 보관) (Connection을 ThreadLocal에 직접 바인딩)
    * [x] `DataSourceUtils#getConnection(DataSource)` 구현 (`ConnectionProvider`로 구현)
        - 트랜잭션 중이면 바인딩된 Connection 반환
        - 아니면 새 Connection 반환
    * [x] `JdbcTemplate`이 `dataSource.getConnection()` 대신 `DataSourceUtils`를 통해 커넥션을 얻도록 변경 (`JdbcExecutor`가`ConnectionProvider` 사용)
    * [x] “같은 트랜잭션 = 같은 Connection” 재현 (여러 DAO 호출에서 동일 커넥션 사용)

* [x] Spring JDBC 책임 3: 트랜잭션 경계 관리 (TransactionManager)
    * [ ] `PlatformTransactionManager`(최소 인터페이스) 정의 (인터페이스 없이 `SimpleTransactionManager` 단일 클래스로 구현)
    * [x] `DataSourceTransactionManager` 구현 (`SimpleTransactionManager`로 구현)
        - begin: `autoCommit=false` + Connection 바인딩
        - commit/rollback
        - cleanup: 바인딩 해제 + close
    * [x] 트랜잭션 유무에 따른 동작 차이 테스트
        - 트랜잭션 없음: 호출마다 새 Connection + 즉시 커밋
        - 트랜잭션 있음: 하나의 Connection 공유 + 마지막에 commit/rollback

* [ ] (Optional) 선언적 트랜잭션 맛보기
    * [ ] `TransactionTemplate#execute(...)` 형태로 경계 분리
    * [ ] (선택) 애노테이션 기반 프록시로 `@Transactional` 흐름 최소 재현
    * [ ] self-invocation / 스레드 경계(ThreadLocal)의 한계 설명 가능

### Keep-Alive (HTTP/1.1)

* [ ] 연결 재사용 지원
    * [ ] 요청 1회 처리 후 소켓 유지
    * [ ] 동일 연결에서 다중 요청 처리

* [ ] 요청 경계 처리 강화
    * [ ] `\r\n\r\n` 기준 헤더 파싱 안정화
    * [ ] `Content-Length` 기반 바디 정확히 읽기
    * [ ] 남은 버퍼 재사용

* [ ] 연결 종료 정책
    * [ ] `Connection: close` 지원
    * [ ] `keepAliveTimeout` 적용
    * [ ] `maxRequestsPerConnection` 제한

* [ ] 응답 정합성 보장
    * [ ] `Content-Length` 정확히 설정
    * [ ] keep-alive / close 헤더 일관성 유지

* [ ] 통합 테스트
    * [ ] 동일 연결 다중 요청 성공
    * [ ] close / timeout 정상 동작 확인
