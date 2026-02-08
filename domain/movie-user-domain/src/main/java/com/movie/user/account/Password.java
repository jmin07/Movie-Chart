package com.movie.user.account;

public record Password(String encoded) {

    // 정적 팩토리 메서드(static factory method)
    // 이 값은 이미 암호화된 비밀번호다 라는 도메인 규칙을 코드로 강제하는 장치.
    public static Password ofEncoded(String encoded) {
        return new Password(encoded);
    }

    // default 접근 제어자
    // 같은 패키지에서만 호출 가능
    boolean matches(String raw, PasswordMatcher matcher) {
        return matcher.matches(raw, encoded);
    }
}
