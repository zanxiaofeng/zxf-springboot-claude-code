package com.example.demo.support.fixture;

import com.example.demo.domain.user.User;
import com.example.demo.domain.user.UserStatus;
import com.example.demo.support.builder.UserBuilder;

/**
 * Predefined user fixtures for tests requiring fixed data.
 * Reduces repetitive builder calls for common test scenarios.
 *
 * @author Demo Team
 * @since 1.0.0
 */
public final class UserFixtures {

    private UserFixtures() {
    }

    /** @return an active user fixture with ID 1. */
    public static User activeUser() {
        return UserBuilder.aUser()
                .withId(1L)
                .withUsername("active.user")
                .withEmail("active@test.com")
                .withStatus(UserStatus.ACTIVE)
                .build();
    }

    /** @return an inactive user fixture with ID 2. */
    public static User inactiveUser() {
        return UserBuilder.aUser()
                .withId(2L)
                .withUsername("inactive.user")
                .withEmail("inactive@test.com")
                .withStatus(UserStatus.INACTIVE)
                .build();
    }
}
