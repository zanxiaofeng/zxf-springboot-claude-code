package com.example.demo.domain.user;

import jakarta.persistence.*;

import java.time.OffsetDateTime;
import java.util.Optional;

/**
 * User entity representing a user account in the system.
 * Contains core user attributes and status management.
 *
 * @author Demo Team
 * @since 1.0.0
 */
@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 20)
    private String username;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private UserStatus status;

    @Column(nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    private OffsetDateTime updatedAt;

    /** Protected constructor for JPA. */
    protected User() {
    }

    /**
     * Creates a new User instance with builder pattern.
     */
    User(Builder builder) {
        this.id = builder.id;
        this.username = builder.username;
        this.email = builder.email;
        this.password = builder.password;
        this.status = builder.status;
        this.createdAt = builder.createdAt;
        this.updatedAt = builder.updatedAt;
    }

    /** @return the unique identifier of the user. */
    public Long getId() {
        return id;
    }

    /** @return the username of the user. */
    public String getUsername() {
        return username;
    }

    /** @return the email address of the user. */
    public String getEmail() {
        return email;
    }

    /** @return the password of the user. */
    public String getPassword() {
        return password;
    }

    /** @return the current status of the user account. */
    public UserStatus getStatus() {
        return status;
    }

    /** @return the timestamp when the user was created. */
    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    /** @return the timestamp when the user was last updated. */
    public Optional<OffsetDateTime> getUpdatedAt() {
        return Optional.ofNullable(updatedAt);
    }

    /**
     * Updates user information with the provided builder values.
     *
     * @param builder the builder containing updated values
     */
    public void update(Builder builder) {
        if (builder.username != null) {
            this.username = builder.username;
        }
        if (builder.email != null) {
            this.email = builder.email;
        }
        if (builder.password != null) {
            this.password = builder.password;
        }
        this.updatedAt = OffsetDateTime.now();
    }

    /**
     * Builder for constructing User instances.
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Builder class for User entity.
     */
    public static class Builder {
        private Long id;
        private String username;
        private String email;
        private String password;
        private UserStatus status = UserStatus.ACTIVE;
        private OffsetDateTime createdAt = OffsetDateTime.now();
        private OffsetDateTime updatedAt;

        public Builder id(Long id) {
            this.id = id;
            return this;
        }

        public Builder username(String username) {
            this.username = username;
            return this;
        }

        public Builder email(String email) {
            this.email = email;
            return this;
        }

        public Builder password(String password) {
            this.password = password;
            return this;
        }

        public Builder status(UserStatus status) {
            this.status = status;
            return this;
        }

        public Builder createdAt(OffsetDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public Builder updatedAt(OffsetDateTime updatedAt) {
            this.updatedAt = updatedAt;
            return this;
        }

        /**
         * Builds a new User instance.
         *
         * @return the constructed User
         */
        public User build() {
            return new User(this);
        }
    }

    @Override
    public String toString() {
        return "User{id=" + id + ", username='" + username + "', email='" + email + "', status=" + status + "}";
    }
}
