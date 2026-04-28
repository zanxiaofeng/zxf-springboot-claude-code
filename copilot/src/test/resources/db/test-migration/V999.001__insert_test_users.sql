-- Insert test users for integration tests
-- Only executed in test environment

INSERT INTO users (id, username, email, password, status, created_at)
VALUES (999, 'test.user', 'test@example.com', 'TestPass123!', 'ACTIVE', CURRENT_TIMESTAMP);
