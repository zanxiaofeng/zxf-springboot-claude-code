---
paths:
  - "**/*.java"
---
# Java 编码规范：工具库与简洁性实践

**版本：** 1.0
**生效日期：** 2026-03-23
**适用范围：** 所有基于 Java 21+ 的后端项目（含 Spring Boot 3.5+）

---

## 1. 核心原则

### 1.1 可读性是第一原则

> Code is read more than written. 优先选择意图清晰、易于维护的写法，避免过度抽象或炫技。

### 1.2 能用 1 行完成的代码绝不用 2 行

> Concise but not cryptic. 利用现代语法和工具库减少样板代码，但确保不牺牲可读性。

**注意**：这里的"简洁"指**代码意图的简洁**，而非字符数。工具方法（如 `StringUtils.defaultString()`）在表达"提供默认值"这一意图时，比三元表达式更清晰，应优先使用。

### 1.3 优先使用框架/库能力

> Don't reinvent the wheel. 按照以下优先级选择实现方式：

| 优先级 | 类型 | 说明 |
|--------|------|------|
| 1 | JDK 特性 | 充分利用现代 JDK（21+）的语法和 API |
| 2 | Lombok | 减少 getter/setter、构造器、日志等样板代码 |
| 3 | Spring / Spring Boot | 若项目已使用 Spring，优先复用其内置工具类 |
| 4 | Apache Commons | 经典补充库（commons-lang3、commons-io 等） |
| 5 | 其他第三方库 | Guava、Hutool 等，仅在以上均无法简洁实现时按需引入 |

---

## 2. 各层次详细规范

### 2.1 JDK 特性优先

**原则：** 凡 JDK 原生 API 可简洁实现的，绝不引入第三方依赖。

#### 常用替代对照表

| 需求 | 避免 | 推荐（JDK 原生） |
|------|------|---------------------|
| 字符串判空 | `StringUtils.isEmpty(str)` | `str == null \|\| str.isBlank()` (JDK 11+) |
| 集合判空 | `CollectionUtils.isEmpty(list)` | `list == null \|\| list.isEmpty()` |
| 集合创建 | `Lists.newArrayList(...)` | `List.of(...)` (JDK 9+) |
| 文件读取 | `FileUtils.readFileToString(...)` | `Files.readString(Path.of(...))` (JDK 11+) |
| HTTP 调用 | `HttpUtil.get(url)` | `HttpClient.newHttpClient().send(...)` (JDK 11+) |
| 日期时间 | `DateUtils / Date` | `java.time.*` (JDK 8+) |
| Base64 编码 | `Base64.encodeBase64String(...)` | `java.util.Base64.getEncoder().encodeToString(...)` (JDK 8+) |
| 数值范围限制 | `Math.min(Math.max(val, min), max)` | `Math.clamp(val, min, max)` (JDK 21) |
| 字符串默认值 | `str != null ? str : ""` | `StringUtils.defaultString(str)` — 工具方法意图更清晰 |
| 对象默认值 | `obj != null ? obj : default` | `ObjectUtils.defaultIfNull(obj, default)` — 工具方法意图更清晰 |
| 类型判断分支 | `if (obj instanceof X) { X x = (X) obj; ... }` | `if (obj instanceof X x) { ... }` (JDK 16+ 模式匹配) |
| 获取集合首/末元素 | 手动 `get(0)` / `get(size()-1)` | `sequencedCollection.getFirst()` / `getLast()` (JDK 21) |

> **JDK 21 新特性：** 模式匹配 switch、Record Pattern、SequencedCollection、`Math.clamp()`、Virtual Threads 等应在项目中积极使用。

---

### 2.2 Lombok 简化代码

**原则：** 在允许使用 Lombok 的项目中，使用注解替代手写样板代码。

| 注解 | 用途 | 示例 |
|------|------|------|
| `@Data` | 生成 getter/setter、toString、equals、hashCode | `@Data public class User { ... }` |
| `@Builder` | 生成建造者模式 | `@Builder @Data public class Order { ... }` |
| `@Slf4j` | 自动创建 log 对象 | `@Slf4j public class Service { ... }` |
| `@AllArgsConstructor` / `@NoArgsConstructor` | 生成构造器 | - |
| `@Value` | 创建不可变类 | `@Value public class Config { ... }` |

- 若团队禁用 Lombok，则手动编写等价代码，但必须遵循可读性原则。
- 避免滥用 `@Data` 在 JPA 实体上，可能导致循环依赖问题。

---

### 2.3 Spring / Spring Boot 内置能力

**原则：** Spring 项目中，优先使用 spring-core、spring-web 等模块提供的工具类。

| 类 | 常用方法 | 说明 |
|----|----------|------|
| `org.springframework.util.StringUtils` | `hasText()`, `trimAllWhitespace()`, `commaDelimitedListToSet()` | 字符串工具 |
| `org.springframework.util.CollectionUtils` | `isEmpty()`, `mergeArrayIntoCollection()` | 集合工具 |
| `org.springframework.util.Assert` | `notNull()`, `hasLength()`, `state()`, `isTrue()` | 参数校验（Spring Boot 项目首选） |
| `org.springframework.util.FileCopyUtils` | `copy()`, `copyToByteArray()` | 文件/流复制 |
| `org.springframework.beans.BeanUtils` | `copyProperties()`, `instantiateClass()` | Bean 操作 |

**Spring Boot 3.5 特性：** Virtual Threads（`spring.threads.virtual.enabled=true`）、RestClient（替代 RestTemplate 的 Fluent API）、CDS 加速启动。

---

### 2.4 Apache Commons 库

**原则：** 当 JDK 和 Spring 均无法简洁实现时，引入具体的 Commons 模块，并注释说明原因。

| 模块 | 常见用途 | 典型场景 |
|------|----------|----------|
| commons-lang3 | 字符串增强、对象工具、枚举工具 | `join()` 复杂分隔符、`abbreviate()` 等高级操作 |
| commons-io | 文件/流操作（FileUtils、IOUtils） | 递归删除目录、复制大文件、读取资源流 |
| commons-collections4 | 高级集合（Bag、BidiMap）、集合工具 | 集合交集、差集或双向 Map |
| commons-codec | 编码解码（Base64、Hex、DigestUtils） | MD5 或 Hex 编码（若 JDK 未覆盖） |
| commons-pool2 | 对象池 | 连接池、资源池 |

- 只引入具体模块，避免 commons 父依赖。
- 在 pom.xml 或 build.gradle 中添加注释说明引入原因。

---

### 2.5 其他第三方库（Guava、Hutool 等）

**原则：** 仅在以上所有层次都无法满足时使用，并严格按需引入模块。

| 场景 | 推荐库 | 原因 |
|------|--------|------|
| 本地缓存 | Guava `CacheBuilder` 或 Caffeine | JDK 无原生支持，且实现复杂 |
| 限流 | Guava `RateLimiter` | 简单可靠的令牌桶实现 |
| 不可变集合增强 | Guava `ImmutableXXX` | JDK 不可变集合功能有限 |
| Excel 简单读写 | Hutool `ExcelUtil` | 封装简洁，避免直接操作 POI |
| 验证码生成 | Hutool `CaptchaUtil` | 开箱即用 |

---

## 3. 依赖管理规范

1. **按需引入，避免全量依赖** — Commons 只引入需要的模块；Hutool 优先引入模块（如 hutool-http），而非 hutool-all。
2. **版本统一** — Spring Boot 项目利用 BOM 管理版本；非 Spring 项目在 `<dependencyManagement>` 中统一管理。
3. **显式注释** — 引入非 JDK/Spring 依赖时，必须在构建文件或代码中添加注释说明原因。
4. **冲突检查** — 引入新依赖前，运行 `mvn dependency:tree` 或 `gradle dependencies` 检查版本冲突。

---

## 4. 冲突与规避

- **HTTP 客户端：** Spring Boot 3.5+ 项目优先使用 `RestClient`，`RestTemplate` 已进入维护模式。
- **文件上传：** 若使用 MultipartFile，禁止额外引入 commons-fileupload。
- **日志门面：** 若项目使用 SLF4J，避免引入 commons-logging。
- **Bean 属性复制：** 优先使用 Spring `BeanUtils.copyProperties()`，避免 Apache Commons BeanUtils。
- **集合工具：** Spring 项目中使用 Spring 的 `CollectionUtils`；避免混用多个库的集合工具。
- **异步编程：** 优先使用 JDK `CompletableFuture`。

---

## 5-10. 编码规范速查

| # | 主题 | 规则 |
|---|------|------|
| 5 | 命名 | Classes: PascalCase, Constants: UPPER_SNAKE, Other: camelCase |
| 6 | 依赖注入 | Constructor injection only, `@RequiredArgsConstructor` |
| 7 | Null 处理 | Return `Optional<T>`, never return null from service methods |
| 8 | 日期时间 | Use `OffsetDateTime`, never `Date` or `Calendar` |
| 9 | 集合 | Return immutable: `List.of()`, `Collections.unmodifiableList()` |
| 10 | 异常 | Extend `BusinessException`, never throw raw `RuntimeException` |

---
---

# Java 契约编程规范

**版本：** 1.0
**生效日期：** 2026-03-23
**适用范围：** 所有基于 Java 21+ 的服务端、客户端及基础库代码（含 Spring Boot 3.5+）

---

## 1. 目的

1. 明确方法的前置条件（Preconditions）、后置条件（Postconditions）与类不变式（Class Invariants）。
2. 通过"快失败"（Fail Fast）原则，尽早暴露错误，降低调试成本。
3. 提高代码可读性，使方法的依赖与约束显式化，契约即文档。

---

## 2. 核心原则

### 2.1 契约即代码

方法的契约应当通过代码显式表达，而非仅依赖注释。

### 2.2 区分输入校验与内部断言

| 场景 | 机制 | 用途 |
|------|------|------|
| 外部输入 | `Preconditions` / `Objects.requireNonNull` | 参数、配置、外部系统返回的校验 |
| 内部假设 | `assert` | 验证代码逻辑假设、不变式、后置条件 |

### 2.3 明确异常类型

违反契约应抛出**非受检异常**：

| 异常类型 | 使用场景 |
|----------|----------|
| `IllegalArgumentException` | 参数值不合法 |
| `IllegalStateException` | 对象状态不正确 |
| `NullPointerException` | 参数或状态为 null（优先使用 `Objects.requireNonNull`） |
| `IndexOutOfBoundsException` | 索引越界 |

### 2.4 信息充分原则

异常信息必须包含：参数/状态名称、预期的约束条件、实际值（若安全且有助于排查）。

---

## 3. 参数校验（前置条件）

所有 `public` / `protected` 方法的所有参数，必须在方法入口处进行校验。

### 工具选择优先级

| 优先级 | 工具 | 适用场景 |
|--------|------|----------|
| 1 | `Objects.requireNonNull()` | JDK 原生，仅非空校验 |
| 2 | `org.springframework.util.Assert` | Spring Boot 项目首选，零额外依赖 |
| 3 | `Preconditions` (Guava) | 非 Spring 项目或复杂校验场景 |
| 4 | 自建工具类 | 以上均不可用时 |

### Spring Boot 示例（`org.springframework.util.Assert`）

```java
public void updateUser(@NonNull String userId, int age, List<String> tags) {
    Assert.hasText(userId, "userId must not be blank");
    Assert.notNull(tags, "tags must not be null");
    Assert.isTrue(age >= 0 && age <= 150,
        () -> "age must be in range [0, 150], was: " + age);
    Assert.isTrue(!tags.isEmpty(), "tags must not be empty");
}
```

### 非 Spring 示例（Guava `Preconditions`）

```java
public void updateUser(@Nonnull String userId, int age, List<String> tags) {
    Objects.requireNonNull(userId, "userId must not be null");
    Preconditions.checkArgument(!userId.isBlank(), "userId must not be blank");
    Preconditions.checkArgument(age >= 0 && age <= 150,
        "age must be in range [0, 150], was: %s", age);
}
```

---

## 4. 内部断言（不变式与后置条件）

`assert` 仅用于验证**代码内部逻辑假设**，不可用于外部输入校验。开发/测试环境使用 `-ea` 开启，生产环境使用 `-da` 关闭（默认）。断言失败应视为编程错误，不应被 try-catch 捕获处理。

---

## 5. 使用注解声明契约

| 注解 | 来源 | 用途 |
|------|------|------|
| `@NonNull` / `@Nullable` | `jakarta.annotation` (Jakarta EE 10) | Spring Boot 3.x 标准注解 |
| `@Nonnull` / `@CheckForNull` | `jakarta.annotation` | Jakarta 注解 |
| `@NonNull` | `lombok` | Lombok 项目使用 |

> **注意：** Spring Boot 3.x 使用 `jakarta.annotation`，禁止使用旧版 `javax.annotation`。校验逻辑必须与注解声明的契约保持一致。

---

## 6. 异常信息规范

信息格式：`[参数/状态名] must [约束条件], but was: [实际值]`

```java
Preconditions.checkArgument(age >= 0 && age <= 150,
    "age must be in range [0, 150], but was: %s", age);
Preconditions.checkArgument(!name.isBlank(),
    "name must not be blank, but was: '%s'", name);
if (!isInitialized()) {
    throw new IllegalStateException(
        "service must be initialized before use, current state: UNINITIALIZED");
}
```

---

## 7. 契约与继承

子类重写方法时：不能放宽前置条件、不能加强后置条件、不能削弱类不变式。

---

## 8. 类不变式（Class Invariants）

- 在构造器末尾和每个修改状态的方法末尾，调用 `assert invariant()` 验证不变式。
- `invariant()` 方法应为 `private`，返回 `boolean`，检查所有字段的合法性。
- 不变式断言失败说明存在编程错误。

---

## 9. 测试契约

为每个公开方法编写负面测试，验证非法输入抛出预期异常：

```java
@Test
void updateUser_NullUserId_ShouldThrowException() {
    IllegalArgumentException exception = assertThrows(
        IllegalArgumentException.class,
        () -> service.updateUser(null, 25)
    );
    assertEquals("userId must not be null", exception.getMessage());
}
```

---

## 10. 代码审查清单

- [ ] 所有 `public` / `protected` 方法是否都有参数校验？
- [ ] Spring Boot 项目是否使用了 `jakarta.annotation`（而非 `javax.annotation`）？
- [ ] 异常信息是否清晰，包含参数名、期望值和实际值？
- [ ] 是否将 `assert` 误用于外部输入校验？
- [ ] 注解声明（`@NonNull` / `@Nullable`）是否与校验逻辑一致？
- [ ] 子类重写方法是否遵循 LSP 原则？
- [ ] 类不变式是否在关键方法后得到维护？
- [ ] 测试是否覆盖了契约的边界情况？

---

## 快速参考：校验与断言模板

| 场景 | 代码 |
|------|------|
| 非空 | `Objects.requireNonNull(param, "paramName must not be null");` |
| 字符串非空 | `Assert.hasText(str, "str must not be blank");` |
| 数值范围 | `Assert.isTrue(value >= MIN && value <= MAX, () -> "value must be in range [...]");` |
| 集合非空 | `Assert.isTrue(!collection.isEmpty(), "collection must not be empty");` |
| 状态检查 | `Assert.state(isReady(), "service must be ready");` |
| 后置条件 | `assert result != null : "method must not return null";` |
| 不变式 | `assert invariant() : "class invariant violated";` |
| 内部假设 | `assert index >= 0 && index < size : "index out of bounds";` |