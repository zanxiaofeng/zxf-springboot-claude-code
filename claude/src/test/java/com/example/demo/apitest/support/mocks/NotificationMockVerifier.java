package com.example.demo.apitest.support.mocks;

import com.github.tomakehurst.wiremock.client.WireMock;
import lombok.experimental.UtilityClass;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

/**
 * Verifier for checking notification service calls via WireMock.
 */
@UtilityClass
public class NotificationMockVerifier {

    private static final String NOTIFICATION_PATH = "/api/v1/notifications/user-created";

    /**
     * Verifies the notification service was called the expected number of times.
     */
    public void verifyNotificationCalled(int count) {
        WireMock.verify(count, postRequestedFor(urlEqualTo(NOTIFICATION_PATH))
                .withHeader(HttpHeaders.CONTENT_TYPE, equalTo(MediaType.APPLICATION_JSON_VALUE)));
    }

    /**
     * Verifies the notification service was called with the expected username and email.
     */
    public void verifyNotificationCalledWith(String username, String email) {
        WireMock.verify(postRequestedFor(urlEqualTo(NOTIFICATION_PATH))
                .withHeader(HttpHeaders.CONTENT_TYPE, equalTo(MediaType.APPLICATION_JSON_VALUE))
                .withRequestBody(containing("\"username\":\"" + username + "\""))
                .withRequestBody(containing("\"email\":\"" + email + "\"")));
    }
}
