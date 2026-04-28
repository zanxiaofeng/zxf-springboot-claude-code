package com.example.demo.support;

import org.springframework.test.annotation.DirtiesContext;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.test.context.ActiveProfiles;

/**
 * Base class for all integration tests.
 * Starts a real embedded server on a random port with H2 database
 * and an auto-configured WireMock server for stubbing downstream services.
 *
 * <p>Context is reset after each test class to ensure H2 database isolation.</p>
 *
 * <p>Subclasses should use {@link org.springframework.boot.test.web.client.TestRestTemplate}
 * to send actual HTTP requests, and {@link com.github.tomakehurst.wiremock.client.WireMock}
 * to stub downstream calls.</p>
 *
 * @author Demo Team
 * @since 1.0.0
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWireMock(port = 0)
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public abstract class IntegrationTestBase {

    @Autowired
    protected com.github.tomakehurst.wiremock.WireMockServer wireMockServer;

    /** Resets WireMock stubs and request journal before each test. */
    @BeforeEach
    void resetWireMock() {
        wireMockServer.resetAll();
    }
}
