package com.example.demo.apitest.support.sql;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * JDBC-based database state verification for API tests.
 */
@Component
public class DatabaseVerifier {

    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;

    public boolean userExists(Long id) {
        Long count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM users WHERE id = :id",
                Map.of("id", id),
                Long.class);
        return count != null && count > 0;
    }

    public String getUsername(Long id) {
        return jdbcTemplate.queryForObject(
                "SELECT username FROM users WHERE id = :id",
                Map.of("id", id),
                String.class);
    }

    public String getEmail(Long id) {
        return jdbcTemplate.queryForObject(
                "SELECT email FROM users WHERE id = :id",
                Map.of("id", id),
                String.class);
    }

    public int countUsers() {
        return jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM users",
                Map.of(),
                Integer.class);
    }

    public Long findUserIdByUsername(String username) {
        return jdbcTemplate.queryForObject(
                "SELECT id FROM users WHERE username = :username",
                Map.of("username", username),
                Long.class);
    }
}
