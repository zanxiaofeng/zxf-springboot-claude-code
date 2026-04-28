package com.example.demo.domain.user;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

/**
 * Repository interface for User entity in the domain layer.
 * Defines the contract for user data access operations.
 *
 * @author Demo Team
 * @since 1.0.0
 */
public interface UserRepository {

    /**
     * Saves a user entity.
     *
     * @param user the user to save
     * @return the saved user
     */
    User save(User user);

    /**
     * Finds a user by their unique identifier.
     *
     * @param id the user ID
     * @return optional containing the user if found
     */
    Optional<User> findById(Long id);

    /**
     * Finds a user by their email address.
     *
     * @param email the email address
     * @return optional containing the user if found
     */
    Optional<User> findByEmail(String email);

    /**
     * Retrieves all users with pagination support.
     *
     * @param pageable pagination information
     * @return page of users
     */
    Page<User> findAll(Pageable pageable);

    /**
     * Checks if a user exists with the given ID.
     *
     * @param id the user ID
     * @return true if exists
     */
    boolean existsById(Long id);

    /**
     * Checks if a user exists with the given email.
     *
     * @param email the email address
     * @return true if exists
     */
    boolean existsByEmail(String email);

    /**
     * Deletes a user by their ID.
     *
     * @param id the user ID
     */
    void deleteById(Long id);
}
