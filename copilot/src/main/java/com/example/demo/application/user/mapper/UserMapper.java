package com.example.demo.application.user.mapper;

import com.example.demo.application.user.dto.CreateUserRequest;
import com.example.demo.application.user.dto.UserResponse;
import com.example.demo.domain.user.User;
import org.springframework.stereotype.Component;

/**
 * Mapper for converting between User domain entity and DTOs.
 *
 * @author Demo Team
 * @since 1.0.0
 */
@Component
public class UserMapper {

    /**
     * Converts a CreateUserRequest to a User entity.
     *
     * @param request the creation request
     * @return the user entity
     */
    public User toEntity(CreateUserRequest request) {
        return User.builder()
                .username(request.username())
                .email(request.email())
                .password(request.password())
                .build();
    }

    /**
     * Converts a User entity to a UserResponse DTO.
     *
     * @param user the user entity
     * @return the user response DTO
     */
    public UserResponse toResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getStatus(),
                user.getCreatedAt()
        );
    }
}
