# Ubiquitous Language

이 문서는 Movie 서비스의 인증/회원 도메인에서 팀이 공통으로 사용하는 용어를 정의한다.

## 1) Core Terms

### Account
* Account는 서비스 사용자의 인증 주체다.
* Account는 `AccountId`로 기술적으로 식별된다.
* Account는 `Email`, `Password`, `UserRole`, `AccountStatus`를 가진다.
* Account는 일반(Local) 또는 소셜(Social) 방식으로 생성될 수 있다.

### AccountId
* AccountId는 변경 불가능한 기술 식별자다.
* 외부 연동 키로 직접 노출하지 않는다.

### Email
* Email은 로그인 ID다.
* Email은 유효한 형식을 만족해야 한다.
* Email은 중복될 수 없다.
* 소셜 제공자가 이메일을 주지 않으면 정책에 따라 대체 이메일을 생성할 수 있다.

### Password
* Password는 평문으로 저장하지 않는다.
* Password는 암호화된 값으로만 저장한다.
* Password 검증은 도메인 규칙에 따라 수행한다.

### UserRole
* UserRole은 인가 수준을 나타낸다.
* 현재 기본 역할은 `USER`다.

### AccountStatus
* AccountStatus는 로그인 가능 상태를 나타낸다.
* `ACTIVE`: 로그인 가능 상태
* `LOCKED`: 로그인 실패 누적으로 잠금 상태
* `SUSPENDED`: 운영 정책에 의해 제한된 상태

## 2) Authentication Terms

### Login
* Login은 사용자를 인증하고 서버 세션을 수립하는 행위다.
* 일반 로그인은 Email + Password로 인증한다.
* 소셜 로그인은 Provider 인가 코드 기반으로 인증한다.

### Logout
* Logout은 서버 세션을 무효화하고 세션 쿠키를 제거하는 행위다.
* 소셜 로그아웃은 내부 로그아웃 후 Provider 로그아웃 리다이렉트를 포함할 수 있다.

### Session
* Session은 서버가 보관하는 인증 상태다.
* 클라이언트는 `JSESSIONID`만 알고, 실제 인증 상태는 서버(Redis)에 보관한다.
* Session에는 인증 상태(`AUTH_SESSION_STATE`)와 토큰 메타데이터가 저장된다.

### Access Token
* Access Token은 짧은 수명의 JWT다.
* 현재 정책 수명은 15분이다.
* 세션 상태를 기반으로 서버에서 재발급될 수 있다.

### Refresh Token
* Refresh Token은 Access Token 재발급을 위한 JWT다.
* 현재 정책 만료는 8시간이다.
* 세션 만료 조건을 넘기면 더 이상 재발급되지 않는다.

### Timeout
* Idle Timeout: 마지막 활동 기준 30분
* Absolute Timeout: 로그인 시점 기준 8시간
* Absolute Timeout 이후에는 재인증이 필요하다.

## 3) Social Login Terms

### SocialProvider
* 소셜 인증 제공자 타입이다.
* `GOOGLE`, `KAKAO`, `NAVER`, `LOCAL`
* API 입력은 대소문자 무관하게 해석한다.

### ProviderUserId
* Provider 내부 사용자 식별자다.
* `(SocialProvider, ProviderUserId)` 조합은 유일해야 한다.
* 소셜 계정 매핑의 기준 키다.

### Authorization Code
* 소셜 제공자가 콜백에서 전달하는 1회성 인가 코드다.
* 서버는 이 코드로 Access Token을 교환한다.

### State
* CSRF 방지를 위한 난수 값이다.
* 로그인 시작 시 세션에 저장하고 콜백에서 반드시 검증한다.

## 4) Business Rules

### 회원가입
* 일반 회원가입 시 Email 중복이면 실패한다.
* 소셜 로그인 최초 진입 시 기존 매핑이 없으면 계정을 생성한다.

### 로그인 성공/실패
* 비밀번호 불일치 시 실패 횟수를 증가시킨다.
* 실패 횟수 임계치 초과 시 `LOCKED`로 전이될 수 있다.
* 로그인 성공 시 실패 횟수를 초기화하고 마지막 로그인 시간을 갱신한다.

### 소셜 계정 매핑
* 소셜 로그인 재시도 시 `(provider, providerUserId)`로 기존 계정을 찾는다.
* 동일 키가 있으면 기존 계정으로 로그인 처리한다.

## 5) Bounded Context / Layer Language

### Controller Layer
* HTTP 요청/응답 변환, 상태코드, 리다이렉트 처리
* 비즈니스 규칙 자체는 구현하지 않는다.

### UseCase(Application) Layer
* 사용자 시나리오를 오케스트레이션한다.
* 도메인 객체와 Repository/Port를 사용한다.
* 외부 시스템 상세 구현을 직접 알지 않는다.

### Domain Layer
* 핵심 비즈니스 규칙과 상태 전이를 정의한다.
* 프레임워크(Spring/JPA/Security)에 의존하지 않는다.

### Infrastructure Layer
* DB, Redis, OAuth API, Security 구성의 구현체다.
* Application Port를 구현하는 Adapter다.

## 6) Forbidden / Ambiguous Terms

* "회원"과 "계정"을 혼용하지 않는다. 인증 문맥에서는 `Account`를 사용한다.
* "토큰 로그인"이라는 표현을 쓰지 않는다. 현재 기본 인증 단위는 `Session`이다.
* "소셜 회원가입 API"라는 단일 개념 대신 `소셜 로그인(최초 시 계정 생성)`으로 표현한다.
* "만료"와 "로그아웃"을 혼동하지 않는다. 만료는 시간 정책, 로그아웃은 명시적 종료다.

## 7) Update Rule

* 새로운 Provider 추가 시 `SocialProvider`, 콜백 경로, 매핑 규칙을 본 문서에 즉시 반영한다.
* 인증 정책(TTL/Timeout) 변경 시 본 문서와 설정 파일을 동시에 갱신한다.
