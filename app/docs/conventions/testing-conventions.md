# Testing Conventions

## Test Layers
| Layer | Annotation | DB | Naming |
|-------|-----------|-----|--------|
| Unit | @ExtendWith(MockitoExtension.class) | None | *Test |
| Integration | @SpringBootTest + @AutoConfigureMockMvc | H2 | *IT |
| Contract | @AutoConfigureStubRunner | H2 | *ContractTest |

## Rules
1. Tests must be independent, no @DependsOn
2. Use @Sql or TestDataBuilder to prepare data
3. One assertion subject per test (Arrange-Act-Assert)
4. @DirtiesContext used sparingly

## Builder Usage
```java
// Unit test
User user = UserBuilder.aUser().withUsername("test").build();

// Integration test
User saved = persistableUserBuilder.withEmail("unique@test.com").buildAndPersist();

// Fixture reuse
User admin = UserFixtures.activeUser();
```
