
# ubiquitous language
## Account

* Account는 서비스에 로그인할 수 있는 계정이다
* Account는 고유한 AccountId로 식별된다
* Account는 하나의 Email을 가진다
* Account는 하나의 Password를 가진다
* Account는 AccountStatus 상태를 가진다
* Account는 UserRole 권한을 가진다

### Identity

* AccountId는 Account의 기술적 식별자이다
* Email은 Account의 비즈니스 식별자이다
* AccountId와 Email은 생성 이후 변경되지 않는다

### Email

* Email은 로그인 시 사용되는 식별자이다
* Email은 유효한 이메일 형식을 만족해야 한다
* Email은 중복될 수 없다
* Email은 생성 시점에 검증된다

### Password

* Password는 평문으로 저장되지 않는다
* Password는 암호화된 값만 보관한다
* Password는 외부로 노출되지 않는다
* Password는 입력된 평문 값과의 일치 여부를 검증할 수 있다
* Password는 Account 내부에서만 변경될 수 있다

### AccountStatus

* AccountStatus는 Account의 로그인 가능 상태를 나타낸다
* AccountStatus는 다음 값을 가진다
    * ACTIVE
    * LOCKED
    * SUSPENDED
* ACTIVE 상태의 Account만 로그인할 수 있다

### Authentication (Login)

* 로그인은 Email과 Password를 사용하여 Account를 인증하는 과정이다
* AccountStatus가 ACTIVE가 아니면 로그인은 실패한다
* Password가 일치하지 않으면 로그인은 실패한다
* 로그인 실패 시 실패 횟수가 증가한다
* 로그인 실패 횟수가 기준을 초과하면 Account는 LOCKED 상태가 된다
* 로그인에 성공하면 로그인 실패 횟수는 초기화된다

### Password Change

* Password 변경은 인증된 Account만 수행할 수 있다
* Password 변경 시 기존 Password 검증이 필요하다
* Password 변경 후 기존 Password는 더 이상 유효하지 않다

### Technical Boundary

* Account는 JWT를 알지 못한다
* Account는 Session을 알지 못한다
* Account는 Spring Security를 알지 못한다

----------------------------------
