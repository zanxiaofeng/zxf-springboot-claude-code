# Refactor Guide

## When to Refactor
- After making a test pass (Green phase in TDD)
- When code duplication is found
- When a method exceeds 20 lines
- When a class has too many responsibilities

## Safe Refactoring Steps
1. Ensure tests pass before refactoring
2. Make small, incremental changes
3. Run tests after each change
4. Commit after each successful refactoring

## Common Refactorings
- Extract Method: long methods -> smaller methods
- Extract Class: God class -> multiple focused classes
- Rename: unclear naming -> expressive naming
- Move Method: method in wrong class -> correct class
