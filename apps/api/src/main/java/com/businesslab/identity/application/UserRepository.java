
package com.businesslab.identity.application;

import com.businesslab.identity.domain.EmailAddress;
import com.businesslab.identity.domain.User;
import com.businesslab.identity.domain.UserId;

import java.util.Optional;

public interface UserRepository {

    void save(User user);

    Optional<User> findById(UserId userId);

    Optional<User> findByEmail(EmailAddress email);
}
