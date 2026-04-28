package com.example.demo.integration.interfaces;

import com.example.demo.application.user.dto.CreateUserRequest;
import com.example.demo.application.user.dto.UpdateUserRequest;
import com.example.demo.application.user.dto.UserResponse;
import com.example.demo.domain.user.UserStatus;
import com.example.demo.interfaces.common.ApiResponse;
import com.example.demo.support.IntegrationTestBase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;

import java.util.concurrent.atomic.AtomicLong;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for {@link com.example.demo.interfaces.user.UserController}.
 * Uses unique usernames and emails per test to avoid collisions in shared H2 database.
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
    private static final AtomicLong COUNTER = new AtomicLong(0);

    private String baseUrl() {
        return "http://localhost:" + port + "/api/v1/users";
    }

    private String unique(String prefix) {
        return prefix + COUNTER.incrementAndGet();
    }

    private HttpHeaders jsonHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }

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
        String username = unique("john");
        String email = unique("john") + "@test.com";
        stubDownstreamNotificationAccepted();
        CreateUserRequest request = new CreateUserRequest(username, email, "Pass1234!");

        ResponseEntity<ApiResponse<UserResponse>> response = restTemplate.exchange(
                baseUrl(),
                HttpMethod.POST,
                new HttpEntity<>(request, jsonHeaders()),
                new ParameterizedTypeReference<>() {}
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getHeaders().getLocation()).isNotNull();

        ApiResponse<UserResponse> body = response.getBody();
        assertThat(body).isNotNull();
        assertThat(body.getCode()).isEqualTo("SUCCESS");
        assertThat(body.getData().username()).isEqualTo(username);
        assertThat(body.getData().email()).isEqualTo(email);
        assertThat(body.getData().status()).isEqualTo(UserStatus.ACTIVE);

        wireMockServer.verify(
                postRequestedFor(urlEqualTo(DOWNSTREAM_NOTIFICATION_PATH))
                        .withHeader("Content-Type", equalTo("application/json"))
                        .withRequestBody(containing("\"username\":\"" + username + "\""))
                        .withRequestBody(containing("\"email\":\"" + email + "\""))
        );
    }

    @Test
    @DisplayName("POST should create user and return 201 even when downstream fails")
    void createUser_shouldReturn201_whenDownstreamNotificationFails() {
        String username = unique("jane");
        String email = unique("jane") + "@test.com";
        stubDownstreamNotificationFailure();
        CreateUserRequest request = new CreateUserRequest(username, email, "Pass1234!");

        ResponseEntity<ApiResponse<UserResponse>> response = restTemplate.exchange(
                baseUrl(),
                HttpMethod.POST,
                new HttpEntity<>(request, jsonHeaders()),
                new ParameterizedTypeReference<>() {}
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody().getCode()).isEqualTo("SUCCESS");
        assertThat(response.getBody().getData().username()).isEqualTo(username);
        wireMockServer.verify(postRequestedFor(urlEqualTo(DOWNSTREAM_NOTIFICATION_PATH)));
    }

    @Test
    @DisplayName("POST should return 400 when request body is invalid")
    void createUser_shouldReturn400_whenInvalidRequest() {
        CreateUserRequest request = new CreateUserRequest("", "invalid-email", "short");

        ResponseEntity<ApiResponse> response = restTemplate.exchange(
                baseUrl(),
                HttpMethod.POST,
                new HttpEntity<>(request, jsonHeaders()),
                ApiResponse.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        ApiResponse body = response.getBody();
        assertThat(body).isNotNull();
        assertThat(body.getCode()).isEqualTo("002001");
        assertThat(body.getErrors()).isNotEmpty();
        wireMockServer.verify(0, postRequestedFor(urlEqualTo(DOWNSTREAM_NOTIFICATION_PATH)));
    }

    // ---------------------------------------------------------------
    // GET /api/v1/users/{id}
    // ---------------------------------------------------------------

    @Test
    @DisplayName("GET should return user when found")
    void getUser_shouldReturn200_whenFound() {
        String username = unique("getuser");
        String email = unique("getuser") + "@test.com";
        UserResponse created = createUserViaApi(username, email, "Pass1234!");

        ResponseEntity<ApiResponse<UserResponse>> response = restTemplate.exchange(
                baseUrl() + "/" + created.id(),
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<>() {}
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        ApiResponse<UserResponse> body = response.getBody();
        assertThat(body).isNotNull();
        assertThat(body.getCode()).isEqualTo("SUCCESS");
        assertThat(body.getData().id()).isEqualTo(created.id());
        assertThat(body.getData().username()).isEqualTo(username);
    }

    @Test
    @DisplayName("GET should return 404 when user not found")
    void getUser_shouldReturn404_whenNotFound() {
        ResponseEntity<ApiResponse> response = restTemplate.exchange(
                baseUrl() + "/99999",
                HttpMethod.GET,
                null,
                ApiResponse.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getCode()).isEqualTo("001001");
        assertThat(response.getBody().getTraceId()).isNotNull();
    }

    // ---------------------------------------------------------------
    // GET /api/v1/users (list)
    // ---------------------------------------------------------------

    @Test
    @DisplayName("GET should return paginated list of users")
    void listUsers_shouldReturn200_withPagination() {
        createUserViaApi(unique("list1"), unique("list1") + "@test.com", "Pass1234!");
        createUserViaApi(unique("list2"), unique("list2") + "@test.com", "Pass1234!");

        ResponseEntity<ApiResponse> response = restTemplate.exchange(
                baseUrl() + "?page=0&size=10",
                HttpMethod.GET,
                null,
                ApiResponse.class
        );

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
        String oldUsername = unique("old");
        String oldEmail = unique("old") + "@test.com";
        String newUsername = unique("updated");
        String newEmail = unique("updated") + "@test.com";
        UserResponse created = createUserViaApi(oldUsername, oldEmail, "Pass1234!");
        UpdateUserRequest update = new UpdateUserRequest(newUsername, newEmail, "NewPass123!");

        ResponseEntity<ApiResponse<UserResponse>> response = restTemplate.exchange(
                baseUrl() + "/" + created.id(),
                HttpMethod.PUT,
                new HttpEntity<>(update, jsonHeaders()),
                new ParameterizedTypeReference<>() {}
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        ApiResponse<UserResponse> body = response.getBody();
        assertThat(body).isNotNull();
        assertThat(body.getCode()).isEqualTo("SUCCESS");
        assertThat(body.getData().username()).isEqualTo(newUsername);
        assertThat(body.getData().email()).isEqualTo(newEmail);
    }

    @Test
    @DisplayName("PUT should update user without changing password when password is null")
    void updateUser_shouldUpdateWithoutPassword_whenPasswordNull() {
        String oldUsername = unique("oldnp");
        String oldEmail = unique("oldnp") + "@test.com";
        String newUsername = unique("updnp");
        String newEmail = unique("updnp") + "@test.com";
        UserResponse created = createUserViaApi(oldUsername, oldEmail, "OriginalPass123!");
        UpdateUserRequest update = new UpdateUserRequest(newUsername, newEmail, null);

        ResponseEntity<ApiResponse<UserResponse>> response = restTemplate.exchange(
                baseUrl() + "/" + created.id(),
                HttpMethod.PUT,
                new HttpEntity<>(update, jsonHeaders()),
                new ParameterizedTypeReference<>() {}
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getData().username()).isEqualTo(newUsername);
    }

    // ---------------------------------------------------------------
    // DELETE /api/v1/users/{id}
    // ---------------------------------------------------------------

    @Test
    @DisplayName("DELETE should remove user and return 200")
    void deleteUser_shouldReturn200_whenFound() {
        String username = unique("del");
        String email = unique("del") + "@test.com";
        UserResponse created = createUserViaApi(username, email, "Pass1234!");

        ResponseEntity<ApiResponse> response = restTemplate.exchange(
                baseUrl() + "/" + created.id(),
                HttpMethod.DELETE,
                null,
                ApiResponse.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getCode()).isEqualTo("SUCCESS");

        ResponseEntity<ApiResponse> getResponse = restTemplate.exchange(
                baseUrl() + "/" + created.id(),
                HttpMethod.GET,
                null,
                ApiResponse.class
        );
        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(getResponse.getBody()).isNotNull();
        assertThat(getResponse.getBody().getCode()).isEqualTo("001001");
    }
}
