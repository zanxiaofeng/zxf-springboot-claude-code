package com.example.demo.integration.interfaces;

import com.example.demo.application.user.dto.CreateUserRequest;
import com.example.demo.application.user.dto.UpdateUserRequest;
import com.example.demo.application.user.dto.UserResponse;
import com.example.demo.domain.user.UserStatus;
import com.example.demo.interfaces.common.ApiResponse;
import com.example.demo.support.IntegrationTestBase;
import com.github.tomakehurst.wiremock.client.WireMock;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for {@link com.example.demo.interfaces.user.UserController}.
 * Spins up a real embedded server on a random port and sends actual HTTP requests
 * via {@link TestRestTemplate}.
 *
 * <p>All test data is prepared through the public API surface, not by directly
 * accessing the domain layer. Downstream notification service calls are stubbed
 * using WireMock.</p>
 *
 * @author Demo Team
 * @since 1.0.0
 */
class UserControllerIT extends IntegrationTestBase {

    @Autowired
    private TestRestTemplate restTemplate;

    @LocalServerPort
    private int port;

    private static final String DOWNSTREAM_NOTIFICATION_PATH = "/api/v1/notifications/user-created";

    private String baseUrl() {
        return "http://localhost:" + port + "/api/v1/users";
    }

    @BeforeEach
    void cleanDatabase() {
        // H2 database is automatically cleaned up by @DirtiesContext on IntegrationTestBase
        // after each test class. No manual cleanup needed within the class.
    }

    /**
     * Helper to build standard JSON request headers.
     */
    private HttpHeaders jsonHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }

    /**
     * Helper to create a user through the public API.
     * Ensures test data is always prepared via the API surface, not domain layer.
     */
    private UserResponse createUserViaApi(String username, String email, String password) {
        stubDownstreamNotificationAccepted();
        CreateUserRequest request = new CreateUserRequest(username, email, password);
        ResponseEntity<ApiResponse<UserResponse>> response = restTemplate.exchange(
                baseUrl(),
                HttpMethod.POST,
                new HttpEntity<>(request, jsonHeaders()),
                new ParameterizedTypeReference<>() {}
        );
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        return response.getBody().getData();
    }

    /**
     * Stubs the downstream notification endpoint to return HTTP 202 Accepted.
     */
    private void stubDownstreamNotificationAccepted() {
        wireMockServer.stubFor(
                post(urlEqualTo(DOWNSTREAM_NOTIFICATION_PATH))
                        .withHeader("Content-Type", equalTo("application/json"))
                        .willReturn(aResponse()
                                .withStatus(202)
                                .withHeader("Content-Type", "application/json")
                                .withBody("{\"status\":\"ACCEPTED\"}"))
        );
    }

    /**
     * Stubs the downstream notification endpoint to return HTTP 500.
     */
    private void stubDownstreamNotificationFailure() {
        wireMockServer.stubFor(
                post(urlEqualTo(DOWNSTREAM_NOTIFICATION_PATH))
                        .willReturn(aResponse()
                                .withStatus(500)
                                .withBody("{\"error\":\"Internal Server Error\"}"))
        );
    }

    // ---------------------------------------------------------------
    // POST /api/v1/users
    // ---------------------------------------------------------------

    @Test
    @DisplayName("POST should create user, send downstream notification, and return 201")
    void createUser_shouldReturn201_andCallDownstream_whenNotificationAccepted() {
        // Arrange
        stubDownstreamNotificationAccepted();
        CreateUserRequest request = new CreateUserRequest("john.doe", "john@test.com", "Pass1234!");

        // Act
        ResponseEntity<ApiResponse<UserResponse>> response = restTemplate.exchange(
                baseUrl(),
                HttpMethod.POST,
                new HttpEntity<>(request, jsonHeaders()),
                new ParameterizedTypeReference<>() {}
        );

        // Assert - HTTP response
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getHeaders().getLocation()).isNotNull();

        ApiResponse<UserResponse> body = response.getBody();
        assertThat(body).isNotNull();
        assertThat(body.getCode()).isEqualTo("SUCCESS");
        assertThat(body.getData().username()).isEqualTo("john.doe");
        assertThat(body.getData().email()).isEqualTo("john@test.com");
        assertThat(body.getData().status()).isEqualTo(UserStatus.ACTIVE);

        // Assert - downstream was called with correct payload
        wireMockServer.verify(
                postRequestedFor(urlEqualTo(DOWNSTREAM_NOTIFICATION_PATH))
                        .withHeader("Content-Type", equalTo("application/json"))
                        .withRequestBody(containing("\"username\":\"john.doe\""))
                        .withRequestBody(containing("\"email\":\"john@test.com\""))
        );
    }

    @Test
    @DisplayName("POST should create user and return 201 even when downstream fails")
    void createUser_shouldReturn201_whenDownstreamNotificationFails() {
        // Arrange
        stubDownstreamNotificationFailure();
        CreateUserRequest request = new CreateUserRequest("jane.doe", "jane@test.com", "Pass1234!");

        // Act
        ResponseEntity<ApiResponse<UserResponse>> response = restTemplate.exchange(
                baseUrl(),
                HttpMethod.POST,
                new HttpEntity<>(request, jsonHeaders()),
                new ParameterizedTypeReference<>() {}
        );

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody().getCode()).isEqualTo("SUCCESS");
        assertThat(response.getBody().getData().username()).isEqualTo("jane.doe");

        // Assert - downstream was called (notification failure should not break user creation)
        wireMockServer.verify(postRequestedFor(urlEqualTo(DOWNSTREAM_NOTIFICATION_PATH)));
    }

    @Test
    @DisplayName("POST should return 400 when request body is invalid")
    void createUser_shouldReturn400_whenInvalidRequest() {
        // Arrange
        CreateUserRequest request = new CreateUserRequest("", "invalid-email", "short");

        // Act
        ResponseEntity<ApiResponse> response = restTemplate.exchange(
                baseUrl(),
                HttpMethod.POST,
                new HttpEntity<>(request, jsonHeaders()),
                ApiResponse.class
        );

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        ApiResponse body = response.getBody();
        assertThat(body).isNotNull();
        assertThat(body.getCode()).isEqualTo("002001");
        assertThat(body.getErrors()).isNotEmpty();

        // Assert - downstream was NOT called for invalid requests
        wireMockServer.verify(0, postRequestedFor(urlEqualTo(DOWNSTREAM_NOTIFICATION_PATH)));
    }

    // ---------------------------------------------------------------
    // GET /api/v1/users/{id}
    // ---------------------------------------------------------------

    @Test
    @DisplayName("GET should return user when found")
    void getUser_shouldReturn200_whenFound() {
        // Arrange - create user via API (not via domain layer)
        UserResponse created = createUserViaApi("john.doe", "john@test.com", "Pass1234!");

        // Act
        ResponseEntity<ApiResponse<UserResponse>> response = restTemplate.exchange(
                baseUrl() + "/" + created.id(),
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<>() {}
        );

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        ApiResponse<UserResponse> body = response.getBody();
        assertThat(body).isNotNull();
        assertThat(body.getCode()).isEqualTo("SUCCESS");
        assertThat(body.getData().id()).isEqualTo(created.id());
        assertThat(body.getData().username()).isEqualTo("john.doe");
    }

    @Test
    @DisplayName("GET should return 404 when user not found")
    void getUser_shouldReturn404_whenNotFound() {
        // Act
        ResponseEntity<ApiResponse> response = restTemplate.exchange(
                baseUrl() + "/99999",
                HttpMethod.GET,
                null,
                ApiResponse.class
        );

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    // ---------------------------------------------------------------
    // GET /api/v1/users (list)
    // ---------------------------------------------------------------

    @Test
    @DisplayName("GET should return paginated list of users")
    void listUsers_shouldReturn200_withPagination() {
        // Arrange - create users via API
        createUserViaApi("user1", "u1@test.com", "Pass1234!");
        createUserViaApi("user2", "u2@test.com", "Pass1234!");

        // Act
        ResponseEntity<ApiResponse> response = restTemplate.exchange(
                baseUrl() + "?page=0&size=10",
                HttpMethod.GET,
                null,
                ApiResponse.class
        );

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        ApiResponse body = response.getBody();
        assertThat(body).isNotNull();
        assertThat(body.getCode()).isEqualTo("SUCCESS");
        assertThat(body.getData()).isNotNull();
    }

    // ---------------------------------------------------------------
    // PUT /api/v1/users/{id}
    // ---------------------------------------------------------------

    @Test
    @DisplayName("PUT should update user and return 200")
    void updateUser_shouldReturn200_whenValidRequest() {
        // Arrange - create user via API
        UserResponse created = createUserViaApi("old.name", "old@test.com", "Pass1234!");
        UpdateUserRequest update = new UpdateUserRequest("updated.name", "updated@test.com", "NewPass123!");

        // Act
        ResponseEntity<ApiResponse<UserResponse>> response = restTemplate.exchange(
                baseUrl() + "/" + created.id(),
                HttpMethod.PUT,
                new HttpEntity<>(update, jsonHeaders()),
                new ParameterizedTypeReference<>() {}
        );

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        ApiResponse<UserResponse> body = response.getBody();
        assertThat(body).isNotNull();
        assertThat(body.getCode()).isEqualTo("SUCCESS");
        assertThat(body.getData().username()).isEqualTo("updated.name");
        assertThat(body.getData().email()).isEqualTo("updated@test.com");
    }

    @Test
    @DisplayName("PUT should update user without changing password when password is null")
    void updateUser_shouldUpdateWithoutPassword_whenPasswordNull() {
        // Arrange - create user via API
        UserResponse created = createUserViaApi("old.name", "old@test.com", "OriginalPass123!");
        UpdateUserRequest update = new UpdateUserRequest("updated.name", "updated@test.com", null);

        // Act
        ResponseEntity<ApiResponse<UserResponse>> response = restTemplate.exchange(
                baseUrl() + "/" + created.id(),
                HttpMethod.PUT,
                new HttpEntity<>(update, jsonHeaders()),
                new ParameterizedTypeReference<>() {}
        );

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getData().username()).isEqualTo("updated.name");
    }

    // ---------------------------------------------------------------
    // DELETE /api/v1/users/{id}
    // ---------------------------------------------------------------

    @Test
    @DisplayName("DELETE should remove user and return 204")
    void deleteUser_shouldReturn204_whenFound() {
        // Arrange - create user via API
        UserResponse created = createUserViaApi("to.delete", "delete@test.com", "Pass1234!");

        // Act
        ResponseEntity<Void> response = restTemplate.exchange(
                baseUrl() + "/" + created.id(),
                HttpMethod.DELETE,
                null,
                Void.class
        );

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        // Verify user is gone by trying to GET it
        ResponseEntity<ApiResponse> getResponse = restTemplate.exchange(
                baseUrl() + "/" + created.id(),
                HttpMethod.GET,
                null,
                ApiResponse.class
        );
        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }
}
