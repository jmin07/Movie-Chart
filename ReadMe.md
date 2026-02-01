
# Movie Chart
Movie Chart는 외부 영화 데이터 API를 활용하여 영화 순위(차트), 상세 정보, 출연진 정보 등을 제공하는 영화 정보 서비스입니다.  
단순한 데이터 조회를 넘어, 도메인 중심 설계(Domain-Driven Design, DDD) 를 적용하여 확장성과 유지보수성을 고려한 백엔드 아키텍처를 목표로 합니다.

### 프로젝트 목적
* Domain-Driven Design 기반의 백엔드 구조 설계 연습
* 도메인 규칙과 책임 분리 경험
* 멀티 모듈 프로젝트 구조 연습
* 분산 처리 및 확장성 설계 연습

### 기술 스택
* Java 17
* Spring Boot 3.x
* Spring Security (JWT / Session)
* Redis (캐시, 세션, 동시성 제어)
* Gradle 멀티 모듈
* (추후) Kafka / RabbitMQ

### 향후 확장 아이디어
* CQRS 적용 (조회/쓰기 분리 및 조회 성능 최적화)
* 이벤트 기반 구조 (Kafka / RabbitMQ를 활용한 비동기 처리)

### Reference Documentation
These additional references should also help you:

* https://tech.kakaopay.com/post/backend-domain-driven-design/

### 프로젝트 구조
👉 controller → Application(usecase) → domain <- Infrastructure (Persistence)

본 프로젝트는 도메인 경계를 컴파일 타임에 강제하기 위해 Gradle 멀티 모듈 구조를 채택했습니다.
```
| 패키지         | 역할                 
| ----------- | -------------------
| controller  | HTTP / Web 계층      
| usecase     | Application Service
| persistence | infra 구현체(DB, ORM, SQL, Redis 등등)
| security    | 인증/인가 
| domain      | 순수 도메인 모델 및 규칙 (멀티모듈로 분리)
```

```
movie
    ├─ core-app
    │   └─ movie-core-app
    │       ├─ security
    │       │   ├─ config
    │       │   ├─ jwt
    │       │   └─ session
    │       ├─ web
    │       ├─ application (usecase)
    │       └─ MovieApplication.java
    │
    └─ domain
       ├─ movie-chart-domain
       │  └─ movie
       │     ├─ Movie.java
       │     ├─ MovieId.java
       │     └─ MovieRepository.java
       │
       └─ movie-user-domain
          └─ account
            ├─ Account.java                // Aggregate Root
            ├─ AccountId.java              // Value Object
            ├─ Email.java                  // Value Object
            ├─ Password.java               // Value Object
            ├─ AccountStatus.java          // Enum
            ├─ UserRole.java               // Enum
            ├─ AccountRepository.java      // Repository (interface)
            └─ social
               ├─ SocialAccount            // Entity (Account에 종속)
               ├─ SocialProvider           // Enum (GOOGLE, KAKAO, NAVER)
               └─ SocialAccountId          // VO (provider + providerUserId)
```