---
paths:
  - "**/*.java"
---

# Logging Conventions

Use `@Slf4j` (Lombok). Never declare a manual `Logger` field.

## SLF4J Rules

1. **Use placeholder format** — never `String.format` or string concatenation
2. **Exception logs must include the exception object** as the last argument (outputs stack trace)
3. **Placeholder count must match parameter count** — the exception object does not count as a placeholder

```java
// Correct
log.error("Failed to process, key: {}", key, ex);
log.info("User login, userId: {}, username: {}", userId, username);
log.warn("Retry attempt {} for {}", attempt, operation, ex);

// Wrong — missing exception object, no stack trace
log.error("Failed to process, key: {}, exception: {}", key, ex.getMessage());
// Wrong — string concatenation
log.debug("User login, userId: " + userId);
```

## Log Levels

| Level | When to use | Production |
|-------|-------------|------------|
| **ERROR** | Unrecoverable failure (DB connection lost, config error) | Enabled |
| **WARN** | Recoverable issue (degraded service, retry, deprecated API) | Enabled |
| **INFO** | Business events & lifecycle (login, order, startup) | Enabled |
| **DEBUG** | Diagnostics (params, flow, HTTP req/res) | Disabled |

## What to Log

- Application lifecycle: startup, shutdown, config loaded
- Business operations: `log.info("Order created, orderId: {}, amount: {}", orderId, amount)`
- All exceptions with context: `log.error("Payment failed, orderId: {}", orderId, ex)`
- External calls: `log.debug("Calling API, url: {}, method: {}", url, method)`

## What NOT to Log

- Passwords, tokens, PII — always mask sensitive data
- Debug-level object creation without `if (log.isDebugEnabled())` guard

## Standard Templates

```java
// Normal operation
log.info("Operation completed, result: {}", result);
log.debug("Processing request, param: {}", param);

// Exception — include context + exception object
log.error("Failed to process, key: {}", key, ex);

// Performance
log.info("Query completed, duration: {}ms, rows: {}", duration, rows);
```
