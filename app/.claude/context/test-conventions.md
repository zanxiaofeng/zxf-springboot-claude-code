# Test Conventions

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
4. @DirtiesContext used sparingly to prevent context cache pollution
5. No real MySQL in tests (use H2)
