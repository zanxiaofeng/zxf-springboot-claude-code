package com.example.demo.unit.application;

import com.example.demo.application.user.UserServiceImpl;
import com.example.demo.application.user.dto.CreateUserRequest;
import com.example.demo.application.user.dto.UserResponse;
import com.example.demo.application.user.mapper.UserMapper;
import com.example.demo.domain.common.BusinessException;
import com.example.demo.domain.user.User;
import com.example.demo.domain.user.UserRepository;
import com.example.demo.interfaces.common.ErrorCode;
import com.example.demo.support.builder.UserBuilder;
import com.example.demo.support.fixture.UserFixtures;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link UserServiceImpl}.
 * Tests business logic in isolation with mocked repository.
 *
 * @author Demo Team
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UserServiceImpl userService;

    // ---------------------------------------------------------------
    // createUser
    // ---------------------------------------------------------------

    @Test
    @DisplayName("createUser: should create user when email does not exist")
    void createUser_shouldCreate_whenEmailNotExists() {
        // Arrange
        CreateUserRequest request = new CreateUserRequest("john.doe", "john@test.com", "Pass1234!");
        User user = UserFixtures.activeUser();
        UserResponse expectedResponse = new UserResponse(1L, "john.doe", "john@test.com", user.getStatus(), user.getCreatedAt());

        when(userRepository.existsByEmail(request.email())).thenReturn(false);
        when(userMapper.toEntity(request)).thenReturn(user);
        when(userRepository.save(user)).thenReturn(user);
        when(userMapper.toResponse(user)).thenReturn(expectedResponse);

        // Act
        UserResponse result = userService.createUser(request);

        // Assert
        assertThat(result).isEqualTo(expectedResponse);
        verify(userRepository).existsByEmail(request.email());
        verify(userRepository).save(user);
    }

    @Test
    @DisplayName("createUser: should throw exception when email already exists")
    void createUser_shouldThrow_whenEmailExists() {
        // Arrange
        CreateUserRequest request = new CreateUserRequest("john.doe", "john@test.com", "Pass1234!");
        when(userRepository.existsByEmail(request.email())).thenReturn(true);

        // Act & Assert
        assertThatThrownBy(() -> userService.createUser(request))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> {
                    BusinessException be = (BusinessException) ex;
                    assertThat(be.getErrorCode()).isEqualTo(ErrorCode.USER_ALREADY_EXISTS);
                });
        verify(userRepository, never()).save(any());
    }

    // ---------------------------------------------------------------
    // findUserById
    // ---------------------------------------------------------------

    @Test
    @DisplayName("findUserById: should return user when found")
    void findUserById_shouldReturnUser_whenFound() {
        // Arrange
        User user = UserFixtures.activeUser();
        UserResponse expected = new UserResponse(1L, "active.user", "active@test.com", user.getStatus(), user.getCreatedAt());
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userMapper.toResponse(user)).thenReturn(expected);

        // Act
        Optional<UserResponse> result = userService.findUserById(1L);

        // Assert
        assertThat(result).isPresent().hasValue(expected);
    }

    @Test
    @DisplayName("findUserById: should return empty when not found")
    void findUserById_shouldReturnEmpty_whenNotFound() {
        // Arrange
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        // Act
        Optional<UserResponse> result = userService.findUserById(99L);

        // Assert
        assertThat(result).isEmpty();
    }

    // ---------------------------------------------------------------
    // listUsers
    // ---------------------------------------------------------------

    @Test
    @DisplayName("listUsers: should return paginated list")
    void listUsers_shouldReturnPaginatedList() {
        // Arrange
        User user = UserFixtures.activeUser();
        UserResponse response = new UserResponse(1L, "active.user", "active@test.com", user.getStatus(), user.getCreatedAt());
        Page<User> page = new PageImpl<>(List.of(user));
        when(userRepository.findAll(PageRequest.of(0, 20))).thenReturn(page);
        when(userMapper.toResponse(user)).thenReturn(response);

        // Act
        Page<UserResponse> result = userService.listUsers(PageRequest.of(0, 20));

        // Assert
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0)).isEqualTo(response);
    }

    // ---------------------------------------------------------------
    // updateUser
    // ---------------------------------------------------------------

    @Test
    @DisplayName("updateUser: should update user when found")
    void updateUser_shouldUpdate_whenFound() {
        // Arrange
        User existing = UserFixtures.activeUser();
        CreateUserRequest request = new CreateUserRequest("updated.name", "updated@test.com", "NewPass123!");
        UserResponse expected = new UserResponse(1L, "updated.name", "updated@test.com", existing.getStatus(), existing.getCreatedAt());

        when(userRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(userRepository.save(existing)).thenReturn(existing);
        when(userMapper.toResponse(existing)).thenReturn(expected);

        // Act
        UserResponse result = userService.updateUser(1L, request);

        // Assert
        assertThat(result.username()).isEqualTo("updated.name");
        assertThat(result.email()).isEqualTo("updated@test.com");
    }

    @Test
    @DisplayName("updateUser: should throw exception when user not found")
    void updateUser_shouldThrow_whenNotFound() {
        // Arrange
        CreateUserRequest request = new CreateUserRequest("name", "email@test.com", "Pass1234!");
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> userService.updateUser(99L, request))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> {
                    BusinessException be = (BusinessException) ex;
                    assertThat(be.getErrorCode()).isEqualTo(ErrorCode.USER_NOT_FOUND);
                });
    }

    // ---------------------------------------------------------------
    // deleteUser
    // ---------------------------------------------------------------

    @Test
    @DisplayName("deleteUser: should delete user when found")
    void deleteUser_shouldDelete_whenFound() {
        // Arrange
        when(userRepository.existsById(1L)).thenReturn(true);

        // Act
        userService.deleteUser(1L);

        // Assert
        verify(userRepository).deleteById(1L);
    }

    @Test
    @DisplayName("deleteUser: should throw exception when user not found")
    void deleteUser_shouldThrow_whenNotFound() {
        // Arrange
        when(userRepository.existsById(99L)).thenReturn(false);

        // Act & Assert
        assertThatThrownBy(() -> userService.deleteUser(99L))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> {
                    BusinessException be = (BusinessException) ex;
                    assertThat(be.getErrorCode()).isEqualTo(ErrorCode.USER_NOT_FOUND);
                });
    }
}
