---
description: "API Test 编码规范 — 完整指南，涵盖包结构、命名、Support 类、SQL 数据、JSON Fixture、WireMock、JSONAssert 断言"
---

# API Test 编码规范

## 1. 总览与设计原则

API Test 是端到端集成测试，启动完整的 Spring Boot 应用（RANDOM_PORT），通过 `WebTestClient` 发起真实 HTTP 请求，验证整个请求链路（Controller → Service → Repository → Database + Downstream）。

### 核心原则

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

### Maven 依赖

```xml
<!-- WebTestClient 支持 -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-webflux</artifactId>
    <scope>test</scope>
</dependency>
<!-- 已包含在 spring-boot-starter-test 中: JUnit 5, AssertJ, Mockito -->
<!-- 已包含: spring-cloud-contract-wiremock 提供 WireMock -->
<dependency>
    <groupId>com.github.tomakehurst.wiremock</groupId>  <!-- 间接引入 -->
    <artifactId>wiremock</artifactId>
    <scope>test</scope>
</dependency>
<!-- JSONAssert -->
<dependency>
    <groupId>org.skyscreamer</groupId>
    <artifactId>jsonassert</artifactId>
    <scope>test</scope>
</dependency>
```

---

## 2. 目录与包结构

```
src/test/
├── java/com/example/demo/
│   ├── apitest/                              # API 测试根包
│   │   ├── {Entity}ApiTests.java             # 测试类（每个实体一个）
│   │   └── support/                          # 测试基础设施
│   │       ├── BaseApiTest.java              # 抽象基类
│   │       ├── json/                         # JSON 工具
│   │       │   ├── JsonLoader.java           # Fixture 加载 + 模板变量替换
│   │       │   └── JsonComparatorFactory.java # JSONAssert 自定义比较器
│   │       ├── sql/                          # 数据库工具
│   │       │   └── DatabaseVerifier.java     # JDBC 直接查询验证
│   │       └── mocks/                        # WireMock 工具
│   │           ├── {Service}MockFactory.java  # 下游 stub 创建
│   │           └── {Service}MockVerifier.java # 下游调用验证
│   └── contract/                             # Contract 测试（独立体系）
│       └── ContractBaseTest.java
├── resources/
│   ├── sql/                                  # @Sql 脚本
│   │   ├── cleanup/
│   │   │   └── clean-up.sql                  # 数据清理
│   │   ├── init/
│   │   │   └── data.sql                      # 种子数据
│   │   └── cases/                            # 测试用例级别 SQL
│   │       ├── {case-name}.sql
│   │       └── {case-name}-details.txt       # CLOB 外部文件
│   ├── test-data/                            # JSON Fixture
│   │   └── {entity}/
│   │       ├── post/
│   │       │   ├── request.json
│   │       │   ├── created.json
│   │       │   └── validation-error.json
│   │       ├── get-by-id/
│   │       │   ├── ok.json
│   │       │   └── not-found.json
│   │       ├── get-all/
│   │       │   └── ok.json
│   │       ├── put/
│   │       │   ├── request.json
│   │       │   ├── ok.json
│   │       │   ├── not-found.json
│   │       │   └── validation-error.json
│   │       └── delete/
│   │           ├── ok.json
│   │           └── not-found.json
│   ├── mock-data/                            # WireMock 静态资源
│   │   ├── mappings/                         # Stub 映射文件
│   │   └── __files/                          # 响应体文件
│   └── application-test.yml                  # 测试配置
```

---

## 3. 命名规范

### 3.1 类命名

| 类型 | 命名格式 | 示例 |
|------|---------|------|
| 测试类 | `{Entity}ApiTests` | `UserApiTests`, `OrderApiTests` |
| 基类 | `BaseApiTest` | （固定名称） |
| JSON 工具 | `JsonLoader`, `JsonComparatorFactory` | （固定名称） |
| DB 工具 | `DatabaseVerifier` | （固定名称） |
| Mock Factory | `{Service}MockFactory` | `NotificationMockFactory` |
| Mock Verifier | `{Service}MockVerifier` | `NotificationMockVerifier` |

### 3.2 方法命名

格式：`test{Action}{Entity}[{Condition}]`

| Action | Condition | 示例 |
|--------|-----------|------|
| Create | （空 = 正常） | `testCreateUser` |
| Create | WithDownstreamFailure | `testCreateUserWithDownstreamFailure` |
| Create | WithValidationError | `testCreateUserWithValidationError` |
| Get | ById | `testGetUserById` |
| Get | ByIdNotFound | `testGetUserByIdNotFound` |
| Get | All | `testGetAllUsers` |
| Update | （空 = 正常） | `testUpdateUser` |
| Update | NotFound | `testUpdateUserNotFound` |
| Delete | （空 = 正常） | `testDeleteUser` |
| 特殊 | WithCaseLevelSql | `testGetUserById_WithCaseLevelSql` |

### 3.3 文件命名

| 类型 | 路径格式 | 示例 |
|------|---------|------|
| 请求 fixture | `test-data/{entity}/{operation}/request.json` | `test-data/user/post/request.json` |
| 成功响应 | `test-data/{entity}/{operation}/ok.json` | `test-data/user/get-by-id/ok.json` |
| 创建成功 | `test-data/{entity}/{operation}/created.json` | `test-data/user/post/created.json` |
| 未找到 | `test-data/{entity}/{operation}/not-found.json` | `test-data/user/get-by-id/not-found.json` |
| 验证错误 | `test-data/{entity}/{operation}/validation-error.json` | `test-data/user/post/validation-error.json` |
| 清理 SQL | `sql/cleanup/clean-up.sql` | （固定名称） |
| 种子 SQL | `sql/init/data.sql` | （固定名称） |
| 用例 SQL | `sql/cases/{case-name}.sql` | `sql/cases/user-bio-test.sql` |
| CLOB 文件 | `sql/cases/{case-name}-details.txt` | `sql/cases/user-bio-test-details.txt` |
| WireMock 映射 | `mock-data/mappings/{service}-{scenario}.json` | `mock-data/mappings/notification-user-created-success.json` |
| WireMock 响应 | `mock-data/__files/{name}.txt` | `mock-data/__files/notification-accepted-response.txt` |

---

## 4. Support 类详解

### 4.1 BaseApiTest（抽象基类）

**职责**: 启动 Spring 上下文、配置 WebTestClient 和 WireMock、管理 @Sql 种子数据、提供 HTTP 辅助方法。

```java
package com.example.demo.apitest.support;

import com.github.tomakehurst.wiremock.WireMockServer;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.reactive.server.EntityExchangeResult;
import org.springframework.test.web.reactive.server.WebTestClient;

/**
 * API 测试抽象基类。
 * 子类自动获得 WebTestClient、DatabaseVerifier、WireMockServer 注入。
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@AutoConfigureWireMock(port = 0)
@ActiveProfiles("test")
@Sql(scripts = {"classpath:sql/cleanup/clean-up.sql", "classpath:sql/init/data.sql"})
public abstract class BaseApiTest {

    @Autowired
    protected WebTestClient webTestClient;

    @Autowired
    protected DatabaseVerifier databaseVerifier;

    @Autowired
    protected WireMockServer wireMockServer;

    @BeforeEach
    void resetWireMock() {
        wireMockServer.resetAll();
    }

    // ==================== GET ====================
    protected <T> ResponseEntity<T> httpGetAndAssert(String url, HttpHeaders requestHeaders,
            Class<T> tClass, HttpStatus expectedStatus, MediaType expectedContentType) {
        var responseSpec = webTestClient.get()
                .uri(url)
                .headers(h -> h.putAll(requestHeaders))
                .exchange();

        responseSpec.expectStatus().isEqualTo(expectedStatus);
        assertContentType(responseSpec, expectedContentType);

        return toResponseEntity(responseSpec.expectBody(tClass).returnResult());
    }

    // ==================== POST ====================
    protected <T> ResponseEntity<T> httpPostAndAssert(String url, HttpHeaders requestHeaders,
            String requestBody, Class<T> tClass, HttpStatus expectedStatus, MediaType expectedContentType) {
        var requestSpec = webTestClient.post()
                .uri(url)
                .headers(h -> h.putAll(requestHeaders));

        var responseSpec = (requestBody != null ? requestSpec.bodyValue(requestBody) : requestSpec)
                .exchange();

        responseSpec.expectStatus().isEqualTo(expectedStatus);
        assertContentType(responseSpec, expectedContentType);

        return toResponseEntity(responseSpec.expectBody(tClass).returnResult());
    }

    // ==================== PUT ====================
    protected <T> ResponseEntity<T> httpPutAndAssert(String url, HttpHeaders requestHeaders,
            String requestBody, Class<T> tClass, HttpStatus expectedStatus, MediaType expectedContentType) {
        var responseSpec = webTestClient.put()
                .uri(url)
                .headers(h -> h.putAll(requestHeaders))
                .bodyValue(requestBody)
                .exchange();

        responseSpec.expectStatus().isEqualTo(expectedStatus);
        assertContentType(responseSpec, expectedContentType);

        return toResponseEntity(responseSpec.expectBody(tClass).returnResult());
    }

    // ==================== DELETE ====================
    protected <T> ResponseEntity<T> httpDeleteAndAssert(String url, HttpHeaders requestHeaders,
            Class<T> tClass, HttpStatus expectedStatus, MediaType expectedContentType) {
        var responseSpec = webTestClient.delete()
                .uri(url)
                .headers(h -> h.putAll(requestHeaders))
                .exchange();

        responseSpec.expectStatus().isEqualTo(expectedStatus);
        assertContentType(responseSpec, expectedContentType);

        return toResponseEntity(responseSpec.expectBody(tClass).returnResult());
    }

    // ==================== Common ====================
    private void assertContentType(WebTestClient.ResponseSpec responseSpec, MediaType expectedContentType) {
        if (expectedContentType != null) {
            responseSpec.expectHeader().contentType(expectedContentType);
        }
    }

    private <T> ResponseEntity<T> toResponseEntity(EntityExchangeResult<T> result) {
        return ResponseEntity.status(result.getStatus())
                .headers(result.getResponseHeaders())
                .body(result.getResponseBody());
    }

    protected HttpHeaders commonHeaders() {
        return new HttpHeaders();
    }

    protected HttpHeaders commonHeadersAndJson() {
        HttpHeaders headers = commonHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }
}
```

**关键设计决策**:
- **响应类型始终为 `String.class`**: HTTP 辅助方法返回 `ResponseEntity<String>`，由 JSONAssert 做字符串级别的 JSON 比较，避免 Jackson 反序列化丢失字段顺序或格式差异。
- **Status + ContentType 在 When 阶段断言**: `httpXxxAndAssert` 方法内部自动验证 HTTP 状态码和 Content-Type，测试方法无需重复断言。
- **WireMock 每次测试前重置**: `@BeforeEach resetWireMock()` 确保测试隔离。

### 4.2 JsonLoader（JSON Fixture 加载器）

**职责**: 从 classpath 加载 JSON 文件，支持 `${variable}` 模板变量替换。

```java
package com.example.demo.apitest.support.json;

import lombok.experimental.UtilityClass;
import org.springframework.core.io.ClassPathResource;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * JSON fixture 加载器，支持 ${variable} 模板变量替换。
 * 文件路径相对于 classpath:test-data/ 目录。
 */
@UtilityClass
public class JsonLoader {

    /**
     * 加载 JSON fixture 并替换模板变量。
     *
     * @param path 相对于 classpath 的完整路径，如 "test-data/user/post/request.json"
     * @param variables 模板变量映射，如 Map.of("username", "john.doe")
     * @return 替换后的 JSON 字符串
     */
    public String load(String path, Map<String, String> variables) {
        try {
            var resource = new ClassPathResource(path);
            String content = resource.getContentAsString(StandardCharsets.UTF_8);
            for (var entry : variables.entrySet()) {
                content = content.replace("${" + entry.getKey() + "}", entry.getValue());
            }
            return content;
        } catch (Exception e) {
            throw new RuntimeException("Failed to load JSON fixture: " + path, e);
        }
    }
}
```

**模板变量语法**: fixture JSON 中使用 `${variableName}`，运行时通过 `Map.of()` 传入实际值：

```json
{
  "username": "${username}",
  "email": "${email}",
  "password": "${password}"
}
```

调用方式：
```java
String request = JsonLoader.load("test-data/user/post/request.json",
        Map.of("username", "new.user", "email", "new@example.com", "password", "TestPass123!"));
```

### 4.3 JsonComparatorFactory（JSON 比较器工厂）

**职责**: 创建 JSONAssert 自定义比较器，忽略动态字段（id, timestamp, traceId 等）。

```java
package com.example.demo.apitest.support.json;

import lombok.experimental.UtilityClass;
import org.skyscreamer.jsonassert.Customization;
import org.skyscreamer.jsonassert.JSONComparator;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.skyscreamer.jsonassert.RegularExpressionValueMatcher;
import org.skyscreamer.jsonassert.comparator.CustomComparator;

/**
 * JSONAssert 比较器工厂。
 * 创建忽略动态字段的比较器，使 fixture 文件无需包含正确的动态值。
 */
@UtilityClass
public class JsonComparatorFactory {

    /**
     * 构建 API 响应专用比较器。
     * 忽略的字段（使用正则 ".*" 匹配任意值）：
     * - timestamp, traceId: ApiResponse 信封的动态字段
     * - id: 自增主键
     * - createdAt, updatedAt, created_at, updated_at: 时间戳字段
     *
     * @return JSONAssert 比较器
     */
    public JSONComparator buildApiResponseComparator() {
        var regexMatcher = new RegularExpressionValueMatcher<>(".*");
        return new CustomComparator(JSONCompareMode.LENIENT,
                new Customization("timestamp", regexMatcher),
                new Customization("traceId", regexMatcher),
                new Customization("id", regexMatcher),
                new Customization("createdAt", regexMatcher),
                new Customization("updatedAt", regexMatcher),
                new Customization("created_at", regexMatcher),
                new Customization("updated_at", regexMatcher)
        );
    }
}
```

**使用方式**: 在测试类的 `@BeforeEach` 中创建一次：
```java
private JSONComparator jsonComparator;

@BeforeEach
void setUp() {
    jsonComparator = JsonComparatorFactory.buildApiResponseComparator();
}
```

### 4.4 DatabaseVerifier（数据库验证器）

**职责**: 通过 JDBC 直接查询数据库，验证业务操作后的数据库状态。

```java
package com.example.demo.apitest.support.sql;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 数据库状态验证器。
 * 提供常用查询方法，测试中直接验证 DB 状态而非仅依赖 HTTP 响应。
 */
@Component
@RequiredArgsConstructor
public class DatabaseVerifier {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    /** 统计用户总数 */
    public int countUsers() {
        return jdbcTemplate.queryForObject("SELECT COUNT(*) FROM users", Map.of(), Integer.class);
    }

    /** 判断用户是否存在 */
    public boolean userExists(Long id) {
        return jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM users WHERE id = :id", Map.of("id", id), Integer.class) > 0;
    }

    /** 获取用户名 */
    public String getUsername(Long id) {
        return jdbcTemplate.queryForObject(
                "SELECT username FROM users WHERE id = :id", Map.of("id", id), String.class);
    }

    /** 获取邮箱 */
    public String getEmail(Long id) {
        return jdbcTemplate.queryForObject(
                "SELECT email FROM users WHERE id = :id", Map.of("id", id), String.class);
    }

    /** 按用户名查找 ID */
    public Long findUserIdByUsername(String username) {
        return jdbcTemplate.queryForObject(
                "SELECT id FROM users WHERE username = :username",
                Map.of("username", username), Long.class);
    }
}
```

**新增实体时的扩展模式**: 为每个实体添加对应的查询方法。方法命名：`{verb}{Entity}{Field}` 或 `{verb}{Entity}By{Condition}`。

```java
// 扩展示例：订单实体
public int countOrders() { ... }
public boolean orderExists(Long id) { ... }
public String getOrderStatus(Long id) { ... }
public Long findOrderIdByOrderNo(String orderNo) { ... }
```

### 4.5 MockFactory / MockVerifier（WireMock 下游 Mock）

**职责**: 将 WireMock 的 stub 创建和调用验证分别封装到独立类中，分离关注点。

**MockFactory** — 创建 stub：

```java
package com.example.demo.apitest.support.mocks;

import com.github.tomakehurst.wiremock.client.WireMock;
import lombok.experimental.UtilityClass;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

/**
 * 下游通知服务的 WireMock stub 工厂。
 * 在测试的 Given 阶段调用，设置下游服务预期行为。
 */
@UtilityClass
public class NotificationMockFactory {

    /** Mock: 下游通知服务返回成功 (202 ACCEPTED) */
    public void mockNotificationAccepted() {
        WireMock.stubFor(post(urlEqualTo("/api/v1/notifications/user-created"))
                .withHeader("Content-Type", equalTo("application/json"))
                .willReturn(aResponse()
                        .withStatus(202)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"status\":\"ACCEPTED\"}")));
    }

    /** Mock: 下游通知服务返回失败 (500) */
    public void mockNotificationFailure() {
        WireMock.stubFor(post(urlEqualTo("/api/v1/notifications/user-created"))
                .willReturn(aResponse()
                        .withStatus(500)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"error\":\"Internal Server Error\"}")));
    }
}
```

**MockVerifier** — 验证调用：

```java
package com.example.demo.apitest.support.mocks;

import com.github.tomakehurst.wiremock.client.WireMock;
import lombok.experimental.UtilityClass;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

/**
 * 下游通知服务的 WireMock 调用验证器。
 * 在测试的 Then/And 阶段调用，验证下游是否被正确调用。
 */
@UtilityClass
public class NotificationMockVerifier {

    /** 验证下游被调用了指定次数 */
    public void verifyNotificationCalled(int count) {
        WireMock.verify(count, postRequestedFor(urlEqualTo("/api/v1/notifications/user-created"))
                .withHeader("Content-Type", equalTo("application/json")));
    }

    /** 验证下游被调用且请求体包含指定字段 */
    public void verifyNotificationCalledWith(String username, String email) {
        WireMock.verify(postRequestedFor(urlEqualTo("/api/v1/notifications/user-created"))
                .withHeader("Content-Type", equalTo("application/json"))
                .withRequestBody(containing("\"username\":\"" + username + "\""))
                .withRequestBody(containing("\"email\":\"" + email + "\"")));
    }
}
```

**新增下游服务时的扩展模式**: 每个下游服务创建一对 `{Service}MockFactory` + `{Service}MockVerifier`。

---

## 5. SQL 测试数据管理

### 5.1 三层 SQL 结构

| 层级 | 路径 | 作用 | 执行时机 |
|------|------|------|---------|
| Cleanup | `sql/cleanup/clean-up.sql` | 清空所有表数据 | 每次测试前（最先执行） |
| Init | `sql/init/data.sql` | 插入种子数据 | Cleanup 之后 |
| Cases | `sql/cases/{name}.sql` | 特定测试用例的额外数据 | 标注了 `@Sql` 的测试方法前 |

### 5.2 Cleanup 脚本

```sql
-- sql/cleanup/clean-up.sql
-- 注意：按外键依赖反序 DELETE
DELETE FROM orders;
DELETE FROM users;
```

**规范**:
- 使用 `DELETE FROM`，不使用 `TRUNCATE`（H2 兼容性更好）
- 按外键依赖反序排列
- 不使用 WHERE 条件（全量清理）

### 5.3 Init 种子数据

```sql
-- sql/init/data.sql
-- 种子用户数据 — 所有 GET/PUT/DELETE 测试依赖此数据
INSERT INTO users (id, username, email, password, status, bio, created_at) VALUES
    (1, 'john.doe', 'john@example.com',
     '$2a$10$dXJ3SW6G7P50lGmMQgel6uVktDQd7hF1C3lM5H/dBs6G2aMGiG2G.',
     'ACTIVE', NULL, '2026-01-15T10:00:00+08:00'),
    (2, 'jane.smith', 'jane@example.com',
     '$2a$10$dXJ3SW6G7P50lGmMQgel6uVktDQd7hF1C3lM5H/dBs6G2aMGiG2G.',
     'ACTIVE', NULL, '2026-02-20T14:30:00+08:00');
```

**规范**:
- **硬编码 ID**: 使用确定性 ID（1, 2, 3...），方便测试引用
- **BCrypt 密码**: 所有测试用户使用同一个 BCrypt hash（对应明文 "TestPass123!"）
- **显式时间戳**: 带时区偏移量（`+08:00`），不依赖数据库函数
- **ID 范围约定**: 种子数据用 1-99，用例级别 SQL 用 100+

### 5.4 Case 级别 SQL

用于特定测试场景需要的额外数据（如 CLOB 大文本、边界数据等）：

```sql
-- sql/cases/user-bio-test.sql
-- 测试 CLOB bio 字段：从外部文件加载大文本
INSERT INTO users (id, username, email, password, status, bio, created_at)
VALUES (10, 'bio.user', 'bio@example.com',
        '$2a$10$dXJ3SW6G7P50lGmMQgel6uVktDQd7hF1C3lM5H/dBs6G2aMGiG2G.',
        'ACTIVE',
        UTF8TOSTRING(FILE_READ('classpath:sql/cases/user-bio-test-details.txt')),
        CURRENT_TIMESTAMP);
```

**H2 CLOB 技巧**: `UTF8TOSTRING(FILE_READ('classpath:...'))` 将外部文本文件内容加载为 CLOB 字段值。

**使用方式**: 在测试方法上标注 `@Sql`：

```java
@Test
@Sql(scripts = "classpath:sql/cases/user-bio-test.sql")
void testGetUserById_WithCaseLevelSql() throws Exception {
    // 此测试除了种子数据，还额外加载了 bio.user
}
```

### 5.5 @Sql 执行顺序

```
BaseApiTest @Sql (class-level)          ← cleanup + init，每个测试方法前都执行
    ↓
UserApiTests method @Sql (method-level) ← 仅标注了 @Sql 的方法额外执行
    ↓
Test method body                        ← 测试逻辑
```

---

## 6. JSON Fixture 规范

### 6.1 目录组织

```
test-data/{entity}/
├── post/                    # POST 相关 fixture
│   ├── request.json         # 请求体模板
│   ├── created.json         # 201 成功响应
│   └── validation-error.json # 400 验证错误响应
├── get-by-id/               # GET /{id} 相关 fixture
│   ├── ok.json              # 200 成功响应
│   └── not-found.json       # 404 响应
├── get-all/                 # GET 列表相关 fixture
│   └── ok.json              # 200 分页列表响应
├── put/                     # PUT 相关 fixture
│   ├── request.json         # 请求体模板
│   ├── ok.json              # 200 成功响应
│   ├── not-found.json       # 404 响应
│   └── validation-error.json # 400 验证错误响应
└── delete/                  # DELETE 相关 fixture
    ├── ok.json              # 200 成功响应
    └── not-found.json       # 404 响应
```

### 6.2 Fixture 内容规范

**请求模板**（使用 `${variable}` 变量）:
```json
{
  "username": "${username}",
  "email": "${email}",
  "password": "${password}"
}
```

**成功响应**（静态值，动态字段被 JsonComparatorFactory 忽略）:
```json
{
  "code": "SUCCESS",
  "data": {
    "username": "${username}",
    "email": "${email}",
    "status": "ACTIVE"
  }
}
```

**错误响应**（完全静态，不使用模板变量）:
```json
{
  "code": "001001",
  "message": "User not found"
}
```

**分页列表响应**:
```json
{
  "code": "SUCCESS",
  "data": {
    "content": [
      {"id": 1, "username": "john.doe", "email": "john@example.com", "status": "ACTIVE"},
      {"id": 2, "username": "jane.smith", "email": "jane@example.com", "status": "ACTIVE"}
    ],
    "totalElements": 2,
    "totalPages": 1,
    "size": 10,
    "number": 0
  }
}
```

### 6.3 模板变量规范

| 规则 | 说明 |
|------|------|
| 语法 | `${variableName}` — 花括号包裹 |
| 替换时机 | `JsonLoader.load()` 时做简单字符串替换 |
| 请求 fixture | 必须使用模板变量（每次测试参数不同） |
| 成功响应 fixture | 建议使用模板变量（与请求参数关联时） |
| 错误响应 fixture | 不使用模板变量（错误消息固定） |
| 动态字段 | **不放入 fixture** — `id`, `createdAt`, `timestamp`, `traceId` 由 JsonComparatorFactory 忽略 |

### 6.4 新增实体的 Fixture 清单

为每个实体创建以下 fixture 文件（按需增减）：

| 操作 | 必需文件 | 可选文件 |
|------|---------|---------|
| POST | `request.json`, `created.json` | `validation-error.json` |
| GET by ID | `ok.json` | `not-found.json` |
| GET all | `ok.json` | — |
| PUT | `request.json`, `ok.json` | `not-found.json`, `validation-error.json` |
| DELETE | `ok.json` | `not-found.json` |

---

## 7. WireMock & Mock Data 规范

### 7.1 MockFactory / MockVerifier 模式

**每个下游服务** 创建一对 Factory + Verifier，放置在 `support/mocks/` 包下。

**MockFactory 方法命名**: `mock{Service}{Scenario}`
- `mockNotificationAccepted()` — 成功场景
- `mockNotificationFailure()` — 失败场景

**MockVerifier 方法命名**: `verify{Service}{Action}`
- `verifyNotificationCalled(int count)` — 验证调用次数
- `verifyNotificationCalledWith(params...)` — 验证调用参数

### 7.2 测试中的使用模式

```java
// Given: 设置下游 stub
NotificationMockFactory.mockNotificationAccepted();

// When: 调用被测 API（触发下游调用）
ResponseEntity<String> response = httpPostAndAssert(...);

// Then: 验证下游被正确调用
NotificationMockVerifier.verifyNotificationCalledWith("new.user", "new@example.com");

// 或者验证下游未被调用（如验证失败场景）
NotificationMockVerifier.verifyNotificationCalled(0);
```

### 7.3 静态 Stub 文件（mock-data/）

静态 WireMock 映射文件位于 `mock-data/mappings/`，响应体文件位于 `mock-data/__files/`。

**映射文件格式**（WireMock 2.x 兼容，不使用 `jsonBody`、`id`、`name` 字段）：
```json
{
  "request": {
    "method": "POST",
    "url": "/api/v1/notifications/user-created",
    "headers": {
      "Content-Type": { "equalTo": "application/json" }
    }
  },
  "response": {
    "status": 202,
    "headers": { "Content-Type": "application/json" },
    "body": "{\"status\":\"ACCEPTED\"}"
  }
}
```

**注意事项**:
- 当前测试使用 **程序化 stub**（MockFactory），不依赖静态映射文件
- 静态文件仅作为参考/文档用途
- `__files/` 中的文件使用 `.txt` 扩展名（避免被 WireMock 误解析为映射）

---

## 8. 断言模式

### 8.1 三层断言体系

| 层级 | 工具 | 验证目标 | 使用场景 |
|------|------|---------|---------|
| HTTP Response | JSONAssert + JsonComparator | 响应 JSON 结构和字段值 | **每个测试必须** |
| Database | DatabaseVerifier + AssertJ | 数据库状态 | 创建/更新/删除操作 |
| Downstream | MockVerifier | 下游服务调用 | 有下游集成的操作 |

### 8.2 JSONAssert 断言

```java
// 基本用法
JSONAssert.assertEquals(expectedJson, actualJson, jsonComparator);

// jsonComparator 来自 @BeforeEach
private JSONComparator jsonComparator;
@BeforeEach
void setUp() {
    jsonComparator = JsonComparatorFactory.buildApiResponseComparator();
}
```

**比较模式**: `LENIENT` — 允许 fixture 中省略非关键字段，只验证列出的字段。

**被忽略的动态字段**: `timestamp`, `traceId`, `id`, `createdAt`, `updatedAt`（由 JsonComparatorFactory 统一处理）。

**fixture 中不写动态字段**: 因为会被忽略，写任意值或不写都可以。建议不写以保持 fixture 简洁。

### 8.3 DatabaseVerifier 断言

```java
// 创建后验证
assertThat(databaseVerifier.countUsers()).isEqualTo(initialCount + 1);
assertThat(databaseVerifier.findUserIdByUsername("new.user")).isNotNull();

// 更新后验证
assertThat(databaseVerifier.getUsername(userId)).isEqualTo("updated.name");
assertThat(databaseVerifier.getEmail(userId)).isEqualTo("updated@example.com");

// 删除后验证
assertThat(databaseVerifier.userExists(userId)).isFalse();
```

**模式**: 先捕获初始状态（`int initialCount = databaseVerifier.countUsers()`），操作后对比。

### 8.4 MockVerifier 断言

```java
// 验证下游被调用 1 次
NotificationMockVerifier.verifyNotificationCalled(1);

// 验证下游被调用且参数正确
NotificationMockVerifier.verifyNotificationCalledWith("john.doe", "john@example.com");

// 验证下游未被调用（如验证失败场景）
NotificationMockVerifier.verifyNotificationCalled(0);
```

---

## 9. 测试方法模板

### 9.1 标准模板（Given/When/Then）

```java
@Test
void testCreateUser() throws Exception {
    // Given
    String username = "new.user";
    String email = "new@example.com";
    String requestBody = JsonLoader.load("test-data/user/post/request.json",
            Map.of("username", username, "email", email, "password", "TestPass123!"));
    NotificationMockFactory.mockNotificationAccepted();
    int initialCount = databaseVerifier.countUsers();

    // When
    ResponseEntity<String> response = httpPostAndAssert("/api/v1/users",
            commonHeadersAndJson(), requestBody,
            String.class, HttpStatus.CREATED, MediaType.APPLICATION_JSON);

    // Then — 响应验证
    String expected = JsonLoader.load("test-data/user/post/created.json",
            Map.of("username", username, "email", email));
    JSONAssert.assertEquals(expected, response.getBody(), jsonComparator);

    // And — 数据库状态验证
    assertThat(databaseVerifier.countUsers()).isEqualTo(initialCount + 1);
    assertThat(databaseVerifier.findUserIdByUsername(username)).isNotNull();

    // And — 下游调用验证
    NotificationMockVerifier.verifyNotificationCalledWith(username, email);
}
```

### 9.2 错误场景模板

```java
@Test
void testCreateUserWithValidationError() throws Exception {
    // Given — 不需要 MockFactory（验证失败不触发下游）
    String requestBody = "{\"username\":\"\",\"email\":\"invalid\"}";

    // When
    ResponseEntity<String> response = httpPostAndAssert("/api/v1/users",
            commonHeadersAndJson(), requestBody,
            String.class, HttpStatus.BAD_REQUEST, MediaType.APPLICATION_JSON);

    // Then — 响应验证（使用静态 fixture，无模板变量）
    String expected = JsonLoader.load("test-data/user/post/validation-error.json", Map.of());
    JSONAssert.assertEquals(expected, response.getBody(), jsonComparator);

    // And — 下游未被调用
    NotificationMockVerifier.verifyNotificationCalled(0);
}
```

### 9.3 查询场景模板（使用种子数据）

```java
@Test
void testGetUserById() throws Exception {
    // Given — 使用种子数据（id=1, john.doe），无需额外准备
    // When
    ResponseEntity<String> response = httpGetAndAssert("/api/v1/users/1",
            commonHeaders(), String.class,
            HttpStatus.OK, MediaType.APPLICATION_JSON);

    // Then
    String expected = JsonLoader.load("test-data/user/get-by-id/ok.json", Map.of());
    JSONAssert.assertEquals(expected, response.getBody(), jsonComparator);
}
```

### 9.4 Case 级别 SQL 模板

```java
@Test
@Sql(scripts = "classpath:sql/cases/user-bio-test.sql")
void testGetUserById_WithCaseLevelSql() throws Exception {
    // Given — @Sql 已加载 id=10 的 bio.user
    // When
    ResponseEntity<String> response = httpGetAndAssert("/api/v1/users/10",
            commonHeaders(), String.class,
            HttpStatus.OK, MediaType.APPLICATION_JSON);

    // Then
    assertThat(response.getBody()).contains("bio.user");
    assertThat(response.getBody()).contains("senior software engineer");
}
```

---

## 10. 配置规范（application-test.yml）

```yaml
spring:
  datasource:
    url: jdbc:h2:mem:testdb;MODE=MySQL;DB_CLOSE_DELAY=-1;DATABASE_TO_LOWER=TRUE;CASE_INSENSITIVE_IDENTIFIERS=TRUE
    driver-class-name: org.h2.Driver
  jpa:
    hibernate:
      ddl-auto: none                    # 依赖 Flyway 建表
    show-sql: true
    properties:
      hibernate:
        format_sql: true
  flyway:
    enabled: true
    locations: classpath:db/migration    # 使用生产迁移脚本

wiremock:
  server:
    port: 0                              # 随机端口

app:
  downstream:
    notification:
      base-url: http://localhost:${wiremock.server.port}  # 指向 WireMock

logging:
  level:
    com.example.demo: DEBUG
    org.springframework.test: WARN
```

---

## 11. 完整测试类示例

以下是一个新实体 `Order` 的完整 API 测试类示例：

```java
package com.example.demo.apitest;

import com.example.demo.apitest.support.BaseApiTest;
import com.example.demo.apitest.support.json.JsonComparatorFactory;
import com.example.demo.apitest.support.json.JsonLoader;
import com.example.demo.apitest.support.mocks.NotificationMockFactory;
import com.example.demo.apitest.support.mocks.NotificationMockVerifier;
import org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONComparator;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.jdbc.Sql;

import java.util.Map;

/**
 * Order API tests.
 * Covers CRUD + validation + downstream integration scenarios.
 */
class OrderApiTests extends BaseApiTest {

    private JSONComparator jsonComparator;

    @BeforeEach
    void setUp() {
        jsonComparator = JsonComparatorFactory.buildApiResponseComparator();
    }

    @Test
    void testCreateOrder() throws Exception {
        // Given
        String orderNo = "ORD-2026-001";
        String requestBody = JsonLoader.load("test-data/order/post/request.json",
                Map.of("orderNo", orderNo, "productId", "100", "quantity", "2"));
        NotificationMockFactory.mockNotificationAccepted();
        int initialCount = databaseVerifier.countOrders();

        // When
        ResponseEntity<String> response = httpPostAndAssert("/api/v1/orders",
                commonHeadersAndJson(), requestBody,
                String.class, HttpStatus.CREATED, MediaType.APPLICATION_JSON);

        // Then
        String expected = JsonLoader.load("test-data/order/post/created.json",
                Map.of("orderNo", orderNo, "status", "PENDING"));
        JSONAssert.assertEquals(expected, response.getBody(), jsonComparator);

        // And
        assertThat(databaseVerifier.countOrders()).isEqualTo(initialCount + 1);
        NotificationMockVerifier.verifyNotificationCalled(1);
    }

    @Test
    void testGetOrderById() throws Exception {
        // Given — seed data has order id=1
        // When
        ResponseEntity<String> response = httpGetAndAssert("/api/v1/orders/1",
                commonHeaders(), String.class,
                HttpStatus.OK, MediaType.APPLICATION_JSON);

        // Then
        String expected = JsonLoader.load("test-data/order/get-by-id/ok.json", Map.of());
        JSONAssert.assertEquals(expected, response.getBody(), jsonComparator);
    }

    @Test
    void testGetOrderByIdNotFound() throws Exception {
        // Given — no order with id=99999
        // When
        ResponseEntity<String> response = httpGetAndAssert("/api/v1/orders/99999",
                commonHeaders(), String.class,
                HttpStatus.NOT_FOUND, MediaType.APPLICATION_JSON);

        // Then
        String expected = JsonLoader.load("test-data/order/get-by-id/not-found.json", Map.of());
        JSONAssert.assertEquals(expected, response.getBody(), jsonComparator);
    }

    @Test
    void testDeleteOrder() throws Exception {
        // Given — seed data has order id=2
        assertThat(databaseVerifier.orderExists(2L)).isTrue();

        // When
        ResponseEntity<String> response = httpDeleteAndAssert("/api/v1/orders/2",
                commonHeaders(), String.class,
                HttpStatus.OK, MediaType.APPLICATION_JSON);

        // Then
        String expected = JsonLoader.load("test-data/order/delete/ok.json", Map.of());
        JSONAssert.assertEquals(expected, response.getBody(), jsonComparator);

        // And
        assertThat(databaseVerifier.orderExists(2L)).isFalse();
    }

    @Test
    @Sql(scripts = "classpath:sql/cases/order-large-amount-test.sql")
    void testCreateOrder_WithLargeAmount() throws Exception {
        // Given — @Sql loads special test data
        // When & Then...
    }
}
```

---

## 12. Checklist（AI 生成代码时必须逐项检查）

### 新增实体 API Test 时

- [ ] 测试类命名为 `{Entity}ApiTests`，放置在 `com.example.demo.apitest` 包下
- [ ] 继承 `BaseApiTest`
- [ ] `@BeforeEach` 创建 `jsonComparator = JsonComparatorFactory.buildApiResponseComparator()`
- [ ] 测试方法命名为 `test{Action}{Entity}[{Condition}]`
- [ ] 每个测试方法包含 `// Given` / `// When` / `// Then` / `// And` 注释
- [ ] 请求体通过 `JsonLoader.load()` 加载，使用 `Map.of()` 传入模板变量
- [ ] 响应断言使用 `JSONAssert.assertEquals(expected, actual, jsonComparator)`
- [ ] 写操作（POST/PUT/DELETE）使用 `DatabaseVerifier` 验证 DB 状态
- [ ] 有下游调用的操作使用 `MockFactory` 设置 stub + `MockVerifier` 验证调用
- [ ] 所有 HTTP 调用通过 `httpXxxAndAssert()` 辅助方法
- [ ] 响应类型参数始终为 `String.class`
- [ ] 种子数据已添加到 `sql/init/data.sql`
- [ ] JSON fixture 已创建在 `test-data/{entity}/` 下
- [ ] Cleanup SQL 已更新（`sql/cleanup/clean-up.sql` 中添加新表的 DELETE）
- [ ] DatabaseVerifier 中添加了新实体的查询方法

### Support 类扩展时

- [ ] 工具类使用 `@UtilityClass`（Lombok）
- [ ] Spring Bean 使用 `@Component` + `@RequiredArgsConstructor`
- [ ] 所有 public 方法有 JavaDoc
- [ ] MockFactory 方法命名：`mock{Service}{Scenario}`
- [ ] MockVerifier 方法命名：`verify{Service}{Action}`
- [ ] DatabaseVerifier 方法命名：`{verb}{Entity}{Field}` 或 `{verb}{Entity}By{Condition}`
