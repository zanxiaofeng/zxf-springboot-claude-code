---
name: "API Test Guide"
description: "API test conventions with WebTestClient, JSON fixtures, @Sql, DatabaseVerifier, and WireMock"
applyTo: "**/apitest/**/*.java,**/test/resources/**"
---

# API Test 编码规范

## 1. 总览与设计原则

API Test 是端到端集成测试，启动完整的 Spring Boot 应用（RANDOM_PORT），通过 `WebTestClient` 发起真实 HTTP 请求，验证整个请求链路（Controller -> Service -> Repository -> Database + Downstream）。

| 原则 | 说明 |
|------|------|
| **真实 HTTP** | WebTestClient 发起真实 HTTP 请求，非 MockMvc |
| **H2 数据库** | 使用 H2（MySQL 兼容模式），Flyway 建表 |
| **@Sql 管理数据** | 预置种子数据，不通过 API 运行时创建 |
| **JSON Fixture** | 请求/响应使用 JSON 文件 + 模板变量，不硬编码 |
| **JSONAssert** | 使用 JSONAssert + 自定义比较器，忽略动态字段 |
| **DB 直验** | 通过 DatabaseVerifier 直接查询 DB 验证状态 |
| **MockFactory/Verifier** | WireMock 下游 mock 通过 Factory 创建、Verifier 验证 |
| **Given/When/Then** | 每个测试方法严格遵循三段式结构 |

### 目录结构概览

```
src/test/
├── java/com/example/demo/
│   ├── apitest/                              # API 测试根包
│   │   ├── {Entity}ApiTests.java             # 测试类
│   │   └── support/                          # 测试基础设施
│   │       ├── BaseApiTest.java              # 抽象基类（WebTestClient + WireMock + @Sql）
│   │       ├── json/                         # JsonLoader + JsonComparatorFactory
│   │       ├── sql/                          # DatabaseVerifier
│   │       └── mocks/                        # {Service}MockFactory + {Service}MockVerifier
├── resources/
│   ├── sql/{cleanup|init|cases}/             # @Sql 脚本
│   ├── test-data/{entity}/{operation}/       # JSON Fixture
│   ├── mock-data/{mappings|__files}/         # WireMock 静态资源
│   └── application-test.yml                  # 测试配置
```

***

## 3. 命名规范

### 类命名

| 类型 | 格式 | 示例 |
|------|------|------|
| 测试类 | `{Entity}ApiTests` | `UserApiTests`, `OrderApiTests` |
| Mock Factory | `{Service}MockFactory` | `PaymentMockFactory` |
| Mock Verifier | `{Service}MockVerifier` | `PaymentMockVerifier` |

### 方法命名

格式：`test{Action}{Entity}[{Condition}]`，如 `testCreateUser`、`testCreateUserWithValidationError`、`testGetUserByIdNotFound`。

### 文件命名

| 类型 | 路径格式 |
|------|---------|
| 请求 fixture | `test-data/{entity}/{operation}/request.json` |
| 成功响应 | `test-data/{entity}/{operation}/ok.json` 或 `created.json` |
| 错误响应 | `test-data/{entity}/{operation}/not-found.json` 或 `validation-error.json` |
| 种子 SQL | `sql/init/data.sql` |
| 用例 SQL | `sql/cases/{case-name}.sql` |
| CLOB 文件 | `sql/cases/{case-name}-details.txt` |

***

## 4. Support 类参考（读源码，不内联）

Support 类是测试基础设施，**不要在 fixture 中复制其代码**，直接引用即可：

| 类 | 路径 | 职责 |
|----|------|------|
| `BaseApiTest` | `apitest/support/BaseApiTest.java` | 抽象基类：@SpringBootTest + WebTestClient + WireMock + @Sql 种子数据 + HTTP 辅助方法（`httpGetAndAssert`/`httpPostAndAssert`/`httpPutAndAssert`/`httpDeleteAndAssert`） |
| `JsonLoader` | `apitest/support/json/JsonLoader.java` | 加载 classpath JSON 文件 + `${variable}` 模板变量替换 |
| `JsonComparatorFactory` | `apitest/support/json/JsonComparatorFactory.java` | JSONAssert 比较器，忽略动态字段（id, timestamp, traceId, createdAt, updatedAt） |
| `DatabaseVerifier` | `apitest/support/sql/DatabaseVerifier.java` | JDBC 直接查询验证 DB 状态（count{Entities}, {entity}Exists, find{Entity}IdBy{Field} 等） |
| `{Service}MockFactory` | `apitest/support/mocks/` | WireMock stub 创建（`mock{Service}{Scenario}`） |
| `{Service}MockVerifier` | `apitest/support/mocks/` | WireMock 调用验证（`verify{Service}{Action}`） |

***

## 5. SQL 测试数据规则

| 规则 | 说明 |
|------|------|
| 三层结构 | Cleanup (`sql/cleanup/`) -> Init (`sql/init/`) -> Cases (`sql/cases/`) |
| Cleanup | `DELETE FROM` 按外键反序，不用 TRUNCATE |
| Init 种子 | 硬编码 ID（1-99），BCrypt 密码，显式时区时间戳 |
| Case 级别 | 特定场景额外数据，ID >= 100，用 `@Sql` 注解加载 |
| CLOB 技巧 | H2: `UTF8TOSTRING(FILE_READ('classpath:sql/cases/xxx.txt'))` |
| 执行顺序 | BaseApiTest @Sql -> method @Sql -> test body |

***

## 6. JSON Fixture 规则

| 规则 | 说明 |
|------|------|
| 目录 | `test-data/{entity}/{operation}/` |
| 请求模板 | 使用 `${variable}` 变量，运行时 `JsonLoader.load(path, Map.of(...))` 替换 |
| 成功响应 | 可用模板变量，动态字段（id, timestamp 等）不写由比较器忽略 |
| 错误响应 | 完全静态，不使用模板变量 |
| 比较模式 | JSONAssert `LENIENT` 模式，fixture 可省略非关键字段 |

***

## 7. WireMock 命名模式

- **MockFactory 方法**: `mock{Service}{Scenario}` — 如 `mockPaymentAccepted()`、`mockPaymentRejected()`
- **MockVerifier 方法**: `verify{Service}{Action}` — 如 `verifyPaymentCalled(count)`、`verifyPaymentCalledWith(params...)`
- **静态文件**: `mock-data/mappings/{service}-{scenario}.json`，`__files/` 用 `.txt` 扩展名

***

## 8. 三层断言体系

| 层级 | 工具 | 验证目标 | 使用场景 |
|------|------|---------|---------|
| HTTP Response | JSONAssert + JsonComparator | 响应 JSON 结构和字段值 | **每个测试必须** |
| Database | DatabaseVerifier + AssertJ | 数据库状态 | 创建/更新/删除操作 |
| Downstream | MockVerifier | 下游服务调用 | 有下游集成的操作 |

***

## 9. 测试方法模板

### 标准模板（Given/When/Then）

```java
@Test
void testCreate{Entity}() throws Exception {
    // Given
    String requestBody = JsonLoader.load("test-data/{entity}/post/request.json",
            Map.of("{field1}", "value1", "{field2}", "value2"));
    {Service}MockFactory.mock{Service}{Scenario}();
    int initialCount = databaseVerifier.count{Entities}();

    // When
    ResponseEntity<String> response = httpPostAndAssert("/api/v1/{resources}",
            commonHeadersAndJson(), requestBody,
            String.class, HttpStatus.CREATED, MediaType.APPLICATION_JSON);

    // Then — 响应验证
    String expected = JsonLoader.load("test-data/{entity}/post/created.json",
            Map.of("{field1}", "value1", "{field2}", "value2"));
    JSONAssert.assertEquals(expected, response.getBody(), jsonComparator);

    // And — 数据库状态验证
    assertThat(databaseVerifier.count{Entities}()).isEqualTo(initialCount + 1);

    // And — 下游调用验证
    {Service}MockVerifier.verify{Service}CalledWith("value1", "value2");
}
```

### 错误场景模板

```java
@Test
void testCreate{Entity}WithValidationError() throws Exception {
    // Given — 不需要 MockFactory（验证失败不触发下游）
    String requestBody = "{\"{field1}\":\"\",\"{field2}\":\"invalid\"}";

    // When
    ResponseEntity<String> response = httpPostAndAssert("/api/v1/{resources}",
            commonHeadersAndJson(), requestBody,
            String.class, HttpStatus.BAD_REQUEST, MediaType.APPLICATION_JSON);

    // Then — 静态 fixture，无模板变量
    String expected = JsonLoader.load("test-data/{entity}/post/validation-error.json", Map.of());
    JSONAssert.assertEquals(expected, response.getBody(), jsonComparator);

    // And — 下游未被调用
    {Service}MockVerifier.verify{Service}Called(0);
}
```

***

## 10. 配置规范（application-test.yml）

```yaml
spring:
  datasource:
    url: jdbc:h2:mem:testdb;MODE=MySQL;DB_CLOSE_DELAY=-1;DATABASE_TO_LOWER=TRUE;CASE_INSENSITIVE_IDENTIFIERS=TRUE
    driver-class-name: org.h2.Driver
  jpa:
    hibernate:
      ddl-auto: none
    show-sql: true
  flyway:
    enabled: true
    locations: classpath:db/migration

wiremock:
  server:
    port: 0

app:
  downstream:
    {service}:
      base-url: http://localhost:${wiremock.server.port}
```

***

## 12. Checklist

### 新增实体 API Test

- [ ] 测试类命名 `{Entity}ApiTests`，继承 `BaseApiTest`
- [ ] `@BeforeEach` 创建 `jsonComparator = JsonComparatorFactory.buildApiResponseComparator()`
- [ ] 方法命名 `test{Action}{Entity}[{Condition}]`
- [ ] 每个方法包含 `// Given` / `// When` / `// Then` / `// And` 注释
- [ ] 请求体通过 `JsonLoader.load()` + `Map.of()` 模板变量
- [ ] 响应断言使用 `JSONAssert.assertEquals(expected, actual, jsonComparator)`
- [ ] 写操作使用 `DatabaseVerifier` 验证 DB 状态
- [ ] 有下游调用时使用 `MockFactory` + `MockVerifier`
- [ ] 所有 HTTP 调用通过 `httpXxxAndAssert()`，响应类型参数 `String.class`
- [ ] 种子数据已加入 `sql/init/data.sql`，清理 SQL 已更新 `sql/cleanup/clean-up.sql`
- [ ] JSON fixture 已创建在 `test-data/{entity}/` 下
- [ ] DatabaseVerifier 中添加了新实体的查询方法

### Support 类扩展

- [ ] 工具类用 `@UtilityClass`，Spring Bean 用 `@Component` + `@RequiredArgsConstructor`
- [ ] MockFactory: `mock{Service}{Scenario}` | MockVerifier: `verify{Service}{Action}`
- [ ] DatabaseVerifier: `{verb}{Entity}{Field}` 或 `{verb}{Entity}By{Condition}`
