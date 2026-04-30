-- Seed data for API tests
-- Password: "TestPass123!" (BCrypt hash)
INSERT INTO users (id, username, email, password, status, created_at) VALUES
    (1, 'john.doe', 'john@example.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'ACTIVE', '2026-01-15T10:00:00+08:00'),
    (2, 'jane.smith', 'jane@example.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'ACTIVE', '2026-02-20T14:30:00+08:00');
