# Code Review Skill

## Checklist
- [ ] JavaDoc on all public classes and methods
- [ ] Constructor injection (no @Autowired on fields)
- [ ] DTOs use record
- [ ] Service has interface + impl
- [ ] No business logic in Controller
- [ ] Test naming follows convention (*IT for integration, *ContractTest for contract)
- [ ] Contract Test covers new endpoints
- [ ] Flyway migration for DB changes
- [ ] Error handling uses BusinessException
- [ ] Downstream client interface exists in domain layer (if calling external services)
- [ ] WireMock stubs present for downstream calls in integration tests
