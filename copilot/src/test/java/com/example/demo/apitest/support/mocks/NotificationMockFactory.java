package com.example.demo.apitest.support.mocks;

import com.github.tomakehurst.wiremock.client.WireMock;
import lombok.experimental.UtilityClass;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

/**
 * Factory for creating WireMock stubs for the downstream notification service.
 */
@UtilityClass
public class NotificationMockFactory {

    private static final String NOTIFICATION_PATH = "/api/v1/notifications/user-created";

    /**
     * Mocks a successful (202 ACCEPTED) notification response.
     */
    public void mockNotificationAccepted() {
        WireMock.stubFor(WireMock.post(urlEqualTo(NOTIFICATION_PATH))
                .withHeader(HttpHeaders.CONTENT_TYPE, equalTo(MediaType.APPLICATION_JSON_VALUE))
                .willReturn(WireMock.aResponse()
                        .withStatus(202)
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withBody("{\"status\":\"ACCEPTED\"}")));
    }

    /**
     * Mocks a failed (500) notification response.
     */
    public void mockNotificationFailure() {
        WireMock.stubFor(WireMock.post(urlEqualTo(NOTIFICATION_PATH))
                .willReturn(WireMock.aResponse()
                        .withStatus(500)
                        .withBody("{\"error\":\"Internal Server Error\"}")));
    }
}
