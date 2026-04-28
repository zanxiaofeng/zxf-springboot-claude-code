---
name: refactor-module
description: Safely refactor a module with characterization tests, incremental changes, and continuous verification.
---

# Refactor Module

## Pre-conditions
- [ ] All tests pass
- [ ] Code coverage > 80%

## Steps

1. **Identify code smells** — duplication, long methods, unclear naming, tight coupling
2. **Write characterization tests** if coverage is low
3. **Apply refactoring incrementally** — one change at a time
4. **Run tests after each change** — `mvn test` or `./scripts/run-integration-tests.sh`
5. **Update documentation** if public API changed

## Safety Rules
- Never refactor without passing tests
- One refactoring at a time
- Prefer small commits
- If tests break, revert and reassess

## Common Refactorings
- Extract Method — long methods
- Extract Class — large classes with multiple responsibilities
- Move Method — methods in wrong layer
- Rename — unclear naming
- Introduce Parameter Object — long parameter lists
- Replace Conditional with Polymorphism — complex switch/if chains
