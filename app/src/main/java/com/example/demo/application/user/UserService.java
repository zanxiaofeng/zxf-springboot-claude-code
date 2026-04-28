package com.example.demo.application.user;

import com.example.demo.application.user.dto.CreateUserRequest;
import com.example.demo.application.user.dto.UpdateUserRequest;
import com.example.demo.application.user.dto.UserResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

/**
 * Service interface for user management operations.
 * Defines the contract for user CRUD operations.
 *
 * @author Demo Team
 * @since 1.0.0
 */
public interface UserService {

    /**
     * Creates a new user.
     *
     * @param request the user creation request
     * @return the created user response
     * @throws com.example.demo.domain.common.BusinessException if email already exists
     */
    UserResponse createUser(CreateUserRequest request);

    /**
     * Retrieves a user by their unique identifier.
     *
     * @param id the user ID
     * @return optional containing the user if found
     */
    Optional<UserResponse> findUserById(Long id);

    /**
     * Lists all users with pagination.
     *
     * @param pageable pagination information
     * @return page of user responses
     */
    Page<UserResponse> listUsers(Pageable pageable);

    /**
     * Updates an existing user.
     *
     * @param id      the user ID
     * @param request the update request
     * @return the updated user response
     */
    UserResponse updateUser(Long id, UpdateUserRequest request);

    /**
     * Deletes a user by their ID.
     *
     * @param id the user ID
     */
    void deleteUser(Long id);
}
