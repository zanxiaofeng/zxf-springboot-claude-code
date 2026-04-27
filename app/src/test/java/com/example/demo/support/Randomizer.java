package com.example.demo.support;

import org.apache.commons.lang3.RandomStringUtils;

import java.time.OffsetDateTime;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Utility class for generating random test data values.
 * All methods return deterministic random values suitable for test scenarios.
 *
 * @author Demo Team
 * @since 1.0.0
 */
public final class Randomizer {

    private static final java.util.Random RANDOM = ThreadLocalRandom.current();

    private Randomizer() {
    }

    /** @return a random positive Long ID between 1 and 1,000,000. */
    public static Long nextId() {
        return RANDOM.nextLong(1, 1_000_000);
    }

    /** @return a random UUID as a string. */
    public static String uuid() {
        return UUID.randomUUID().toString();
    }

    /** @return a random username in the form "user_{alphanumeric}". */
    public static String username() {
        return "user_" + RandomStringUtils.randomAlphanumeric(8).toLowerCase();
    }

    /** @return a random email address. */
    public static String email() {
        return RandomStringUtils.randomAlphanumeric(8).toLowerCase() + "@test.com";
    }

    /** @return a random password meeting complexity requirements. */
    public static String password() {
        return "Pass" + RandomStringUtils.randomAlphanumeric(10) + "1!";
    }

    /** @return the current {@link OffsetDateTime}. */
    public static OffsetDateTime now() {
        return OffsetDateTime.now();
    }

    /** @return a random past {@link OffsetDateTime} within the last 365 days. */
    public static OffsetDateTime past() {
        return OffsetDateTime.now().minusDays(RANDOM.nextInt(1, 365));
    }
}
