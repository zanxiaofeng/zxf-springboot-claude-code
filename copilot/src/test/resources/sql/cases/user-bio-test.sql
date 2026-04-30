-- Case-level SQL: Insert a user with CLOB bio loaded from external file
-- Demonstrates H2's FILE_READ function for loading CLOB content
-- Usage: @Sql(scripts = "classpath:sql/cases/user-bio-test.sql") on specific test method
INSERT INTO users (id, username, email, password, status, bio, created_at)
VALUES (10, 'bio.user', 'bio@example.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'ACTIVE',
        UTF8TOSTRING(FILE_READ('classpath:sql/cases/user-bio-test-details.txt')),
        CURRENT_TIMESTAMP);
