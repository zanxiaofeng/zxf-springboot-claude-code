# Java Coding Standard

## Naming
- Classes: PascalCase
- Constants: UPPER_SNAKE
- Other: camelCase

## Injection
- Constructor injection only
- Use `@RequiredArgsConstructor` from Lombok

## Null Handling
- Return `Optional<T>` instead of raw null
- Never return null from service methods

## Date and Time
- Use `OffsetDateTime`, never `Date` or `Calendar`

## Collections
- Return immutable collections: `List.of()`, `Collections.unmodifiableList()`

## Exceptions
- Business exceptions extend `BusinessException`
- Never throw raw `RuntimeException`
