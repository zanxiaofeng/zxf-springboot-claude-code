---
paths:
  - "**/interfaces/**/*.java"
  - "**/application/**/*.java"
  - "**/domain/**/*.java"
  - "**/infrastructure/**/*Config*.java"
  - "**/infrastructure/**/*ExceptionHandler*.java"
---
# Bean Validation 与参数校验规范

**适用范围：** JDK 21 + Spring Boot 3.5 + Jakarta Validation 3.0

---

## 1. 分层验证职责

| 层 | 验证方式 | 验证内容 |
|----|---------|---------|
| Controller | `@Valid` / `@Validated` | 格式验证（非空、长度、格式、正则） |
| Service | `@Validated` + Bean Validation 或 `Assert` | 业务验证（存在性、状态、权限） |
| Entity | `@PrePersist` / `@PreUpdate` | 不变式（数据一致性约束） |

---

## 2. Controller 层参数验证

### 请求体 `@RequestBody`

```java
@PostMapping
public ApiResponse<Long> create(@RequestBody @Valid UserCreateDTO dto) {
    return ApiResponse.success(userService.create(dto));
}
```

### 路径变量 `@PathVariable`

```java
@GetMapping("/{id}")
public ApiResponse<UserVO> getById(
        @PathVariable @Positive(message = "ID必须为正数") Long id) { ... }
```

### 查询参数 `@RequestParam`

```java
@GetMapping
public ApiResponse<PageResult<UserVO>> list(
        @RequestParam(defaultValue = "1") @Min(1) Integer pageNum,
        @RequestParam(defaultValue = "10") @Min(1) @Max(100) Integer pageSize) { ... }
```

### 查询对象 `@ParameterObject`

```java
@Data @Builder
public class UserQueryDTO {
    @Min(1) private Integer pageNum = 1;
    @Min(1) @Max(100) private Integer pageSize = 10;
    @Pattern(regexp = "^[a-zA-Z0-9_]*$") private String username;

    // 跨字段验证
    @AssertTrue(message = "结束日期必须晚于开始日期")
    public boolean isDateRangeValid() {
        if (startDate == null || endDate == null) return true;
        return !endDate.isBefore(startDate);
    }
}
```

---

## 3. DTO 字段验证注解

### 注解速查表

| 注解 | 用途 | 示例 |
|------|------|------|
| `@NotNull` | 值不为 null | `@NotNull Integer age` |
| `@NotBlank` | 字符串非空白 | `@NotBlank String username` |
| `@NotEmpty` | 集合/数组/字符串非空 | `@NotEmpty List<String> tags` |
| `@Size` | 长度/大小范围 | `@Size(min = 3, max = 20)` |
| `@Min` / `@Max` | 数值范围（整数） | `@Min(0) @Max(150)` |
| `@DecimalMin` / `@DecimalMax` | 数值范围（小数） | `@DecimalMin("0.00")` |
| `@Digits` | 数字精度 | `@Digits(integer = 9, fraction = 2)` |
| `@Pattern` | 正则匹配 | `@Pattern(regexp = "^1[3-9]\\d{9}$")` |
| `@Email` | 邮箱格式 | `@Email String email` |
| `@Past` / `@Future` | 过去/未来时间 | `@Past LocalDate birthday` |
| `@Positive` / `@PositiveOrZero` | 正数 | `@Positive Long id` |
| `@AssertTrue` / `@AssertFalse` | 布尔断言 | `@AssertTrue Boolean agreed` |
| `@Valid` | 级联验证嵌套对象 | `@Valid AddressDTO address` |

### 字段验证示例

```java
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class UserCreateDTO {
    @NotBlank(message = "用户名不能为空")
    @Size(min = 3, max = 20, message = "用户名长度3-20")
    @Pattern(regexp = "^[a-zA-Z][a-zA-Z0-9_]*$", message = "用户名必须字母开头")
    private String username;

    @NotBlank @Email(message = "邮箱格式不正确") @Size(max = 100)
    private String email;

    @NotBlank @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;

    @NotNull @Min(0) @Max(150)
    private Integer age;

    @NotNull @DecimalMin("0.00") @Digits(integer = 9, fraction = 2)
    private BigDecimal balance;

    @Valid @NotNull  // 级联验证嵌套对象
    private AddressDTO address;
}
```

### 列表/集合元素验证

```java
@Data
public class BatchCreateRequest {
    @NotEmpty @Size(max = 100)
    @Valid  // 级联验证列表元素
    private List<@NotNull UserCreateDTO> users;
}
```

### Map 验证

```java
@Data
public class ConfigUpdateRequest {
    @NotEmpty
    private Map<@NotBlank String, @NotNull String> configs;
}
```

---

## 4. 验证组（分组验证）

```java
// 组定义
public interface ValidationGroups {
    interface Create {}
    interface Update {}
}

// DTO 使用分组
@Data
public class UserDTO {
    @Null(groups = Create.class, message = "创建时不能指定ID")
    @NotNull(groups = Update.class, message = "更新时ID不能为空")
    private Long id;

    @NotBlank(groups = {Create.class, Update.class})
    @Size(min = 3, max = 20, groups = {Create.class, Update.class})
    private String username;

    @NotBlank(groups = Create.class)  // 仅创建时必填
    private String password;
}

// Controller 使用分组
@PostMapping
public ApiResponse<Long> create(
        @RequestBody @Validated(ValidationGroups.Create.class) UserDTO dto) { ... }

@PutMapping("/{id}")
public ApiResponse<Void> update(
        @RequestBody @Validated(ValidationGroups.Update.class) UserDTO dto) { ... }
```

---

## 5. 自定义验证注解

### 手机号验证（模板）

```java
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = PhoneValidator.class)
@Documented
public @interface Phone {
    String message() default "手机号格式不正确";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}

public class PhoneValidator implements ConstraintValidator<Phone, String> {
    private static final Pattern PATTERN = Pattern.compile("^1[3-9]\\d{9}$");

    @Override
    public boolean isValid(String value, ConstraintValidatorContext ctx) {
        return value == null || PATTERN.matcher(value).matches();
    }
}
```

### 枚举值验证

```java
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = EnumValueValidator.class)
@Documented
public @interface EnumValue {
    String message() default "枚举值不合法";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
    Class<? extends Enum<?>> enumClass();
}
```

### 自定义验证规则

- 验证器中 `null` 值返回 `true`（让 `@NotNull`/`@NotBlank` 处理空值）
- 需要多条错误信息时，用 `context.disableDefaultConstraintViolation()` + `buildConstraintViolationWithTemplate()`

---

## 6. 返回值验证（可选）

启用返回值验证的配置：

```java
@Configuration
public class ValidationConfig {
    @Bean
    public MethodValidationPostProcessor methodValidationPostProcessor(
            LocalValidatorFactoryBean validator) {
        MethodValidationPostProcessor processor = new MethodValidationPostProcessor();
        processor.setValidator(validator);
        processor.setValidateReturnedValue(true);
        return processor;
    }
}
```

Service 接口使用：

```java
public interface OrderService {
    @NotNull OrderVO getOrder(@NotNull Long orderId);
    @NotEmpty List<@NotNull OrderVO> getUserOrders(@NotNull Long userId);
}
```

---

## 7. 全局异常处理

```java
@Slf4j
@RestControllerAdvice
public class ValidationExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ApiResponse<Void> handleMethodArgumentNotValid(MethodArgumentNotValidException e) {
        List<String> errors = e.getBindingResult().getFieldErrors().stream()
                .map(err -> err.getField() + ": " + err.getDefaultMessage())
                .toList();
        log.warn("参数验证失败: {}", errors);
        return ApiResponse.error("VALIDATION_ERROR", String.join("; ", errors));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ApiResponse<Void> handleConstraintViolation(ConstraintViolationException e) {
        List<String> errors = e.getConstraintViolations().stream()
                .map(v -> v.getPropertyPath() + ": " + v.getMessage())
                .toList();
        log.warn("参数验证失败: {}", errors);
        return ApiResponse.error("VALIDATION_ERROR", String.join("; ", errors));
    }

    @ExceptionHandler(BindException.class)
    public ApiResponse<Void> handleBindException(BindException e) {
        List<String> errors = e.getFieldErrors().stream()
                .map(err -> err.getField() + ": " + err.getDefaultMessage())
                .toList();
        return ApiResponse.error("VALIDATION_ERROR", String.join("; ", errors));
    }
}
```

---

## 8. 最佳实践

### 推荐做法

- **分层验证**：Controller 做格式验证，Service 做业务验证，Entity 做不变式验证
- **错误消息清晰**：`@NotBlank(message = "用户名不能为空")`
- **使用验证组**区分创建/更新场景
- **Record + 验证**：`public record UserDTO(@NotBlank String username, @Email String email) {}`
- **组合注解**减少重复：将多个验证注解组合为一个自定义注解

### 避免做法

- **过度验证**：`@NotNull @NotEmpty @NotBlank @Size(min=1)` 冗余，选一个即可
- **重复验证**：Controller 和 Service 重复相同格式验证
- **验证器不处理 null**：自定义 `isValid()` 必须处理 `value == null` 返回 `true`
- **忽略 `@Valid` 级联**：嵌套对象必须加 `@Valid` 才会触发验证
