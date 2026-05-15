Safely refactor a module with incremental changes and continuous verification.

## Pre-conditions
- All tests pass (`mvn test`)
- Code coverage > 80%

## Steps
1. Identify code smells (duplication, long methods, unclear naming, tight coupling)
2. Write characterization tests if coverage is low
3. Apply refactoring incrementally — one change at a time
4. Run `mvn test` after each change
5. Update documentation if public API changed

## Safety Rules
- Never refactor without passing tests
- One refactoring at a time
- Prefer small commits
- If tests break, revert and reassess

## Validation
- `mvn test` passes after each change
- `mvn compile` succeeds
