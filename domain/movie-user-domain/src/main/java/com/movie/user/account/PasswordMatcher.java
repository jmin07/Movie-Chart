package com.movie.user.account;

// Domain 이 “비밀번호 비교가 필요하다”는 사실만 알고 “어떻게 비교하는지”는 모르도록 하기 위해서
@FunctionalInterface
public interface PasswordMatcher {
    boolean matches(String raw, String encoded);
}
