package com.example.demo.interfaces.user;

import com.example.demo.application.user.UserService;
import com.example.demo.application.user.dto.CreateUserRequest;
import com.example.demo.application.user.dto.UserResponse;
import com.example.demo.interfaces.common.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

/**
 * REST controller for user management endpoints.
 * All endpoints return {@link ApiResponse} wrapped responses.
 *
 * @author Demo Team
 * @since 1.0.0
 */
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /**
     * Creates a new user.
     *
     * @param request the user creation request
     * @return 201 Created with the created user data
     */
    @PostMapping
    public ResponseEntity<ApiResponse<UserResponse>> createUser(
            @Valid @RequestBody CreateUserRequest request) {
        UserResponse created = userService.createUser(request);
        return ResponseEntity
                .created(URI.create("/api/v1/users/" + created.id()))
                .body(ApiResponse.success(created));
    }

    /**
     * Retrieves a user by ID.
     *
     * @param id the user ID
     * @return 200 OK with user data
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<UserResponse>> getUser(@PathVariable Long id) {
        return userService.findUserById(id)
                .map(user -> ResponseEntity.ok(ApiResponse.success(user)))
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Lists all users with pagination.
     *
     * @param pageable pagination parameters
     * @return 200 OK with paginated user list
     */
    @GetMapping
    public ResponseEntity<ApiResponse<Page<UserResponse>>> listUsers(Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(userService.listUsers(pageable)));
    }

    /**
     * Updates an existing user.
     *
     * @param id      the user ID
     * @param request the update request
     * @return 200 OK with updated user data
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<UserResponse>> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody CreateUserRequest request) {
        return ResponseEntity.ok(ApiResponse.success(userService.updateUser(id, request)));
    }

    /**
     * Deletes a user by ID.
     *
     * @param id the user ID
     * @return 204 No Content
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
}
