package com.example.demo.infrastructure.persistence;

import com.example.demo.domain.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Spring Data JPA repository for {@link User} entities.
 * Provides CRUD operations and custom query methods.
 *
 * @author Demo Team
 * @since 1.0.0
 */
@Repository
public interface UserJpaRepository extends JpaRepository<User, Long> {

    /**
     * Finds a user by their email address.
     *
     * @param email the email address
     * @return optional containing the user if found
     */
    Optional<User> findByEmail(String email);

    /**
     * Checks if a user exists with the given email.
     *
     * @param email the email address
     * @return true if exists
     */
    boolean existsByEmail(String email);
}
