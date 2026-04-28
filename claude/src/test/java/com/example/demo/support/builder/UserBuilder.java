package com.example.demo.support.builder;

import com.example.demo.domain.user.User;
import com.example.demo.domain.user.UserStatus;
import com.example.demo.support.Randomizer;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

/**
 * Pure builder for constructing {@link User} instances in unit tests.
 * Provides random defaults that can be overridden via fluent setters.
 *
 * @author Demo Team
 * @since 1.0.0
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class UserBuilder implements TestDataBuilder<User> {

    private Long id = Randomizer.nextId();
    private String username = Randomizer.username();
    private String email = Randomizer.email();
    private String password = Randomizer.password();
    private UserStatus status = UserStatus.ACTIVE;
    private OffsetDateTime createdAt = Randomizer.past();

    /** Creates a new UserBuilder with random defaults. */
    public static UserBuilder aUser() {
        return new UserBuilder();
    }

    /** Sets the user ID. */
    public UserBuilder withId(Long id) {
        this.id = id;
        return this;
    }

    /** Sets the username. */
    public UserBuilder withUsername(String username) {
        this.username = username;
        return this;
    }

    /** Sets the email. */
    public UserBuilder withEmail(String email) {
        this.email = email;
        return this;
    }

    /** Sets the password. */
    public UserBuilder withPassword(String password) {
        this.password = password;
        return this;
    }

    /** Sets the status. */
    public UserBuilder withStatus(UserStatus status) {
        this.status = status;
        return this;
    }

    /** Creates a copy of this builder with current values. */
    public UserBuilder but() {
        UserBuilder copy = new UserBuilder();
        copy.id = this.id;
        copy.username = this.username;
        copy.email = this.email;
        copy.password = this.password;
        copy.status = this.status;
        copy.createdAt = this.createdAt;
        return copy;
    }

    /**
     * Builds a User entity from the current builder state.
     *
     * @return the constructed User
     */
    @Override
    public User build() {
        return User.builder()
                .id(id)
                .username(username)
                .email(email)
                .password(password)
                .status(status)
                .createdAt(createdAt)
                .build();
    }
}
