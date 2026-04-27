package com.example.demo.integration.interfaces;

import com.example.demo.application.user.UserService;
import com.example.demo.application.user.dto.CreateUserRequest;
import com.example.demo.application.user.dto.UserResponse;
import com.example.demo.domain.user.UserStatus;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.OffsetDateTime;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for {@link com.example.demo.interfaces.user.UserController}.
 * Uses {@link MockMvc} + H2 database to verify end-to-end API behaviour.
 *
 * @author Demo Team
 * @since 1.0.0
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
class UserControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    private static final String BASE_PATH = "/api/v1/users";

    // ---------------------------------------------------------------
    // POST /api/v1/users
    // ---------------------------------------------------------------

    @Test
    @DisplayName("POST should create user and return 201 with Location header")
    void createUser_shouldReturn201_whenValidRequest() throws Exception {
        // Arrange
        CreateUserRequest request = new CreateUserRequest("john.doe", "john@test.com", "Pass1234!");
        UserResponse response = new UserResponse(1L, "john.doe", "john@test.com", UserStatus.ACTIVE, OffsetDateTime.now());
        when(userService.createUser(any(CreateUserRequest.class))).thenReturn(response);

        // Act & Assert
        mockMvc.perform(post(BASE_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/v1/users/1"))
                .andExpect(jsonPath("$.code").value("SUCCESS"))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.username").value("john.doe"))
                .andExpect(jsonPath("$.data.email").value("john@test.com"));
    }

    @Test
    @DisplayName("POST should return 400 when request body is invalid")
    void createUser_shouldReturn400_whenInvalidRequest() throws Exception {
        // Arrange
        CreateUserRequest request = new CreateUserRequest("", "invalid-email", "short");

        // Act & Assert
        mockMvc.perform(post(BASE_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("002001"))
                .andExpect(jsonPath("$.errors").isArray());
    }

    // ---------------------------------------------------------------
    // GET /api/v1/users/{id}
    // ---------------------------------------------------------------

    @Test
    @DisplayName("GET should return user when found")
    void getUser_shouldReturn200_whenFound() throws Exception {
        // Arrange
        UserResponse response = new UserResponse(1L, "john.doe", "john@test.com", UserStatus.ACTIVE, OffsetDateTime.now());
        when(userService.findUserById(1L)).thenReturn(Optional.of(response));

        // Act & Assert
        mockMvc.perform(get(BASE_PATH + "/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("SUCCESS"))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.username").value("john.doe"));
    }

    @Test
    @DisplayName("GET should return 404 when user not found")
    void getUser_shouldReturn404_whenNotFound() throws Exception {
        // Arrange
        when(userService.findUserById(99L)).thenReturn(Optional.empty());

        // Act & Assert
        mockMvc.perform(get(BASE_PATH + "/99"))
                .andExpect(status().isNotFound());
    }

    // ---------------------------------------------------------------
    // PUT /api/v1/users/{id}
    // ---------------------------------------------------------------

    @Test
    @DisplayName("PUT should update user and return 200")
    void updateUser_shouldReturn200_whenValidRequest() throws Exception {
        // Arrange
        CreateUserRequest request = new CreateUserRequest("updated.name", "updated@test.com", "NewPass123!");
        UserResponse response = new UserResponse(1L, "updated.name", "updated@test.com", UserStatus.ACTIVE, OffsetDateTime.now());
        when(userService.updateUser(any(Long.class), any(CreateUserRequest.class))).thenReturn(response);

        // Act & Assert
        mockMvc.perform(put(BASE_PATH + "/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("SUCCESS"))
                .andExpect(jsonPath("$.data.username").value("updated.name"));
    }

    // ---------------------------------------------------------------
    // DELETE /api/v1/users/{id}
    // ---------------------------------------------------------------

    @Test
    @DisplayName("DELETE should remove user and return 204")
    void deleteUser_shouldReturn204_whenFound() throws Exception {
        // Act & Assert
        mockMvc.perform(delete(BASE_PATH + "/1"))
                .andExpect(status().isNoContent());
    }
}
