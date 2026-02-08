package com.movie.user.account;

import java.util.Optional;

public interface AccountRepository {

    Optional<Account> findByEmail(Email email);

    boolean existsByEmail(Email email);

    void save(Account account);
}
