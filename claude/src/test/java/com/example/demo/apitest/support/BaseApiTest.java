package com.example.demo.apitest.support;

import com.github.tomakehurst.wiremock.WireMockServer;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.reactive.server.EntityExchangeResult;
import org.springframework.test.web.reactive.server.WebTestClient;
import com.example.demo.apitest.support.sql.DatabaseVerifier;

/**
 * Base test class for API tests using WebTestClient.
 * Provides common GET/POST/PUT/DELETE methods with status assertion.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@AutoConfigureWireMock(port = 0, stubs = "classpath:mock-data")
@ActiveProfiles("test")
@Sql(scripts = {"classpath:sql/cleanup/clean-up.sql", "classpath:sql/init/data.sql"})
public abstract class BaseApiTest {

    @Autowired
    protected WebTestClient webTestClient;

    @Autowired
    protected DatabaseVerifier databaseVerifier;

    @Autowired
    protected WireMockServer wireMockServer;

    @BeforeEach
    void resetWireMock() {
        wireMockServer.resetAll();
    }

    // ==================== GET ====================

    protected <T> ResponseEntity<T> httpGetAndAssert(String url, HttpHeaders requestHeaders,
            Class<T> tClass, HttpStatus expectedStatus, MediaType expectedContentType) {
        var responseSpec = webTestClient.get()
                .uri(url)
                .headers(h -> h.putAll(requestHeaders))
                .exchange();

        responseSpec.expectStatus().isEqualTo(expectedStatus);
        assertContentType(responseSpec, expectedContentType);

        return toResponseEntity(responseSpec.expectBody(tClass).returnResult());
    }

    // ==================== POST ====================

    protected <T> ResponseEntity<T> httpPostAndAssert(String url, HttpHeaders requestHeaders,
            String requestBody, Class<T> tClass, HttpStatus expectedStatus, MediaType expectedContentType) {
        var requestSpec = webTestClient.post()
                .uri(url)
                .headers(h -> h.putAll(requestHeaders));

        var responseSpec = (requestBody != null ? requestSpec.bodyValue(requestBody) : requestSpec)
                .exchange();

        responseSpec.expectStatus().isEqualTo(expectedStatus);
        assertContentType(responseSpec, expectedContentType);

        return toResponseEntity(responseSpec.expectBody(tClass).returnResult());
    }

    // ==================== PUT ====================

    protected <T> ResponseEntity<T> httpPutAndAssert(String url, HttpHeaders requestHeaders,
            String requestBody, Class<T> tClass, HttpStatus expectedStatus, MediaType expectedContentType) {
        var responseSpec = webTestClient.put()
                .uri(url)
                .headers(h -> h.putAll(requestHeaders))
                .bodyValue(requestBody)
                .exchange();

        responseSpec.expectStatus().isEqualTo(expectedStatus);
        assertContentType(responseSpec, expectedContentType);

        return toResponseEntity(responseSpec.expectBody(tClass).returnResult());
    }

    // ==================== DELETE ====================

    protected <T> ResponseEntity<T> httpDeleteAndAssert(String url, HttpHeaders requestHeaders,
            Class<T> tClass, HttpStatus expectedStatus, MediaType expectedContentType) {
        var responseSpec = webTestClient.delete()
                .uri(url)
                .headers(h -> h.putAll(requestHeaders))
                .exchange();

        responseSpec.expectStatus().isEqualTo(expectedStatus);
        assertContentType(responseSpec, expectedContentType);

        return toResponseEntity(responseSpec.expectBody(tClass).returnResult());
    }

    // ==================== Common ====================

    private void assertContentType(WebTestClient.ResponseSpec responseSpec, MediaType expectedContentType) {
        if (expectedContentType != null) {
            responseSpec.expectHeader().contentType(expectedContentType);
        }
    }

    private <T> ResponseEntity<T> toResponseEntity(EntityExchangeResult<T> result) {
        return ResponseEntity.status(result.getStatus())
                .headers(result.getResponseHeaders())
                .body(result.getResponseBody());
    }

    protected HttpHeaders commonHeaders() {
        return new HttpHeaders();
    }

    protected HttpHeaders commonHeadersAndJson() {
        HttpHeaders headers = commonHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }
}
