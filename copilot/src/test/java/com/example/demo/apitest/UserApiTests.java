package com.example.demo.apitest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.comparator.JSONComparator;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import com.example.demo.apitest.support.BaseApiTest;
import com.example.demo.apitest.support.json.JsonComparatorFactory;
import com.example.demo.apitest.support.json.JsonLoader;
import com.example.demo.apitest.support.mocks.NotificationMockFactory;
import com.example.demo.apitest.support.mocks.NotificationMockVerifier;
import com.example.demo.apitest.support.sql.DatabaseVerifier;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * API tests for /api/v1/users endpoints.
 *
 * Features:
 * - Uses WebTestClient for HTTP calls
 * - Uses JSONAssert + fixture files for response validation
 * - Uses DatabaseVerifier for DB state checks
 * - Uses WireMock for downstream notification service mocking
 * - Follows Given/When/Then pattern
 */
public class UserApiTests extends BaseApiTest {

    private JSONComparator jsonComparator;

    @BeforeEach
    void setupForEach() {
        jsonComparator = JsonComparatorFactory.buildApiResponseComparator();
    }

    // ==================== POST /api/v1/users ====================

    @Test
    void testCreateUser() throws Exception {
        // Given
        String username = "new.user";
        String email = "newuser@example.com";
        String url = "/api/v1/users";
        String requestBody = JsonLoader.load("user/post/request.json",
                Map.of("username", username, "email", email, "password", "NewPass123!"));

        NotificationMockFactory.mockNotificationAccepted();
        int initialCount = databaseVerifier.countUsers();

        // When
        ResponseEntity<String> response = httpPostAndAssert(url, commonHeadersAndJson(),
                requestBody, String.class, HttpStatus.CREATED, MediaType.APPLICATION_JSON);

        // Then
        String expectedJson = JsonLoader.load("user/post/created.json",
                Map.of("username", username, "email", email));
        JSONAssert.assertEquals(expectedJson, response.getBody(), jsonComparator);

        // And - verify database state
        assertThat(databaseVerifier.countUsers()).isEqualTo(initialCount + 1);
        Long createdId = databaseVerifier.findUserIdByUsername(username);
        assertThat(createdId).isNotNull();

        // And - verify downstream notification was called
        NotificationMockVerifier.verifyNotificationCalledWith(username, email);
    }

    @Test
    void testCreateUserWithDownstreamFailure() throws Exception {
        // Given
        String username = "resilient.user";
        String email = "resilient@example.com";
        String url = "/api/v1/users";
        String requestBody = JsonLoader.load("user/post/request.json",
                Map.of("username", username, "email", email, "password", "Resilient123!"));

        NotificationMockFactory.mockNotificationFailure();

        // When
        ResponseEntity<String> response = httpPostAndAssert(url, commonHeadersAndJson(),
                requestBody, String.class, HttpStatus.CREATED, MediaType.APPLICATION_JSON);

        // Then
        String expectedJson = JsonLoader.load("user/post/created.json",
                Map.of("username", username, "email", email));
        JSONAssert.assertEquals(expectedJson, response.getBody(), jsonComparator);

        // And - verify downstream was still called (fire-and-forget)
        NotificationMockVerifier.verifyNotificationCalled(1);
    }

    @Test
    void testCreateUserWithValidationError() throws Exception {
        // Given
        String url = "/api/v1/users";
        String requestBody = JsonLoader.load("user/post/request.json",
                Map.of("username", "", "email", "invalid-email", "password", "short"));

        int initialCount = databaseVerifier.countUsers();

        // When
        ResponseEntity<String> response = httpPostAndAssert(url, commonHeadersAndJson(),
                requestBody, String.class, HttpStatus.BAD_REQUEST, MediaType.APPLICATION_JSON);

        // Then
        String expectedJson = JsonLoader.load("user/post/validation-error.json");
        JSONAssert.assertEquals(expectedJson, response.getBody(), jsonComparator);

        // And - verify database unchanged
        assertThat(databaseVerifier.countUsers()).isEqualTo(initialCount);

        // And - verify downstream was NOT called
        NotificationMockVerifier.verifyNotificationCalled(0);
    }

    // ==================== GET /api/v1/users/{id} ====================

    @Test
    void testGetUserById() throws Exception {
        // Given - 使用预置数据 (id=1, john.doe)
        String url = "/api/v1/users/1";

        // When
        ResponseEntity<String> response = httpGetAndAssert(url, commonHeaders(),
                String.class, HttpStatus.OK, MediaType.APPLICATION_JSON);

        // Then
        String expectedJson = JsonLoader.load("user/get-by-id/ok.json");
        JSONAssert.assertEquals(expectedJson, response.getBody(), jsonComparator);
    }

    @Test
    void testGetUserByIdNotFound() throws Exception {
        // Given
        String url = "/api/v1/users/99999";

        // When
        ResponseEntity<String> response = httpGetAndAssert(url, commonHeaders(),
                String.class, HttpStatus.NOT_FOUND, MediaType.APPLICATION_JSON);

        // Then
        String expectedJson = JsonLoader.load("user/get-by-id/not-found.json");
        JSONAssert.assertEquals(expectedJson, response.getBody(), jsonComparator);
    }

    // ==================== GET /api/v1/users ====================

    @Test
    void testGetAllUsers() throws Exception {
        // Given - 使用预置数据 (2 users)
        String url = "/api/v1/users?page=0&size=10";

        // When
        ResponseEntity<String> response = httpGetAndAssert(url, commonHeaders(),
                String.class, HttpStatus.OK, MediaType.APPLICATION_JSON);

        // Then
        String expectedJson = JsonLoader.load("user/get-all/ok.json");
        JSONAssert.assertEquals(expectedJson, response.getBody(), jsonComparator);
    }

    // ==================== PUT /api/v1/users/{id} ====================

    @Test
    void testUpdateUser() throws Exception {
        // Given - 使用预置数据 (id=1)
        Long userId = 1L;
        String newUsername = "updated.name";
        String newEmail = "updated@example.com";
        String url = "/api/v1/users/" + userId;
        String requestBody = JsonLoader.load("user/put/request.json",
                Map.of("username", newUsername, "email", newEmail, "password", "UpdatedPass123!"));

        // When
        ResponseEntity<String> response = httpPutAndAssert(url, commonHeadersAndJson(),
                requestBody, String.class, HttpStatus.OK, MediaType.APPLICATION_JSON);

        // Then
        String expectedJson = JsonLoader.load("user/put/ok.json",
                Map.of("username", newUsername, "email", newEmail));
        JSONAssert.assertEquals(expectedJson, response.getBody(), jsonComparator);

        // And - verify database state
        assertThat(databaseVerifier.getUsername(userId)).isEqualTo(newUsername);
        assertThat(databaseVerifier.getEmail(userId)).isEqualTo(newEmail);
    }

    @Test
    void testUpdateUserNotFound() throws Exception {
        // Given
        String url = "/api/v1/users/99999";
        String requestBody = JsonLoader.load("user/put/request.json",
                Map.of("username", "nobody", "email", "nobody@example.com", "password", "NobodyPass123!"));

        // When
        ResponseEntity<String> response = httpPutAndAssert(url, commonHeadersAndJson(),
                requestBody, String.class, HttpStatus.NOT_FOUND, MediaType.APPLICATION_JSON);

        // Then
        String expectedJson = JsonLoader.load("user/put/not-found.json");
        JSONAssert.assertEquals(expectedJson, response.getBody(), jsonComparator);
    }

    @Test
    void testUpdateUserValidationError() throws Exception {
        // Given
        String url = "/api/v1/users/1";
        String requestBody = JsonLoader.load("user/put/request.json",
                Map.of("username", "", "email", "invalid", "password", "short"));

        // When
        ResponseEntity<String> response = httpPutAndAssert(url, commonHeadersAndJson(),
                requestBody, String.class, HttpStatus.BAD_REQUEST, MediaType.APPLICATION_JSON);

        // Then
        String expectedJson = JsonLoader.load("user/put/validation-error.json");
        JSONAssert.assertEquals(expectedJson, response.getBody(), jsonComparator);
    }

    // ==================== DELETE /api/v1/users/{id} ====================

    @Test
    void testDeleteUser() throws Exception {
        // Given - 使用预置数据 (id=2, jane.smith)
        Long userId = 2L;
        String url = "/api/v1/users/" + userId;
        int initialCount = databaseVerifier.countUsers();
        assertThat(databaseVerifier.userExists(userId)).isTrue();

        // When
        ResponseEntity<String> response = httpDeleteAndAssert(url, commonHeaders(),
                String.class, HttpStatus.OK, MediaType.APPLICATION_JSON);

        // Then
        String expectedJson = JsonLoader.load("user/delete/ok.json");
        JSONAssert.assertEquals(expectedJson, response.getBody(), jsonComparator);

        // And - verify database state
        assertThat(databaseVerifier.userExists(userId)).isFalse();
        assertThat(databaseVerifier.countUsers()).isEqualTo(initialCount - 1);
    }

    @Test
    void testDeleteUserNotFound() throws Exception {
        // Given
        String url = "/api/v1/users/99999";

        // When
        ResponseEntity<String> response = httpDeleteAndAssert(url, commonHeaders(),
                String.class, HttpStatus.NOT_FOUND, MediaType.APPLICATION_JSON);

        // Then
        String expectedJson = JsonLoader.load("user/delete/not-found.json");
        JSONAssert.assertEquals(expectedJson, response.getBody(), jsonComparator);
    }
}
