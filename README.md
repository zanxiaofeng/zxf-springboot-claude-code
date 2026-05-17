# AI Agent Harness for Spring Boot 3 REST API

本项目为 **基于 Spring Boot 3 的 Java REST API 项目提供 AI 编程脚手架（AI Agent Harness）**。通过为 GitHub Copilot 和 Claude Code 构建完整、精确、工程化的规范系统（架构规范、编码标准、工作流、子代理等），使 AI 助手能在任何同类项目中持续输出高质量、符合行业标准的代码。

项目内含一个可运行的 Spring Boot 3 REST API 示例应用，作为脚手架规范的**参考实现**，验证所有规范端到端可用。

---

## 项目定位

### 脚手架 > 演示

本项目的核心交付物是 **AI 配置层**（rules、workflows、agents），而非 Spring Boot 应用本身。示例代码的作用是证明脚手架有效——它是规范的落地验证，不是最终产品。

### 通用标准，而非项目定制

`claude/` 和 `copilot/` 中的规范、工作流和代理配置面向 **所有 Spring Boot 3 REST API 项目**，不局限于本示例。它们基于：

- **行业标准**：六边形架构、DDD 模式、RESTful 约定、Spring 官方推荐实践
- **行业最佳实践**：TDD 工作流、契约优先、四层分层、领域事件
- **个人总结的最佳实践**：API 测试策略、下游集成模式、异常体系设计

规范中使用 `{Entity}`、`{Project}` 等通用占位符，示例代码提供具体实例。

### 合规的生成代码

所有示例代码必须严格遵守脚手架中定义的规范。代码是规范正确性的证据，因此必须是模范实现。

---

## 解决什么问题

在 Java 后端项目中使用 AI 编程助手时，开发者通常遇到：

- **上下文碎片化**：AI 不了解项目架构、分层规则和技术约束，生成的代码常违反团队规范
- **重复纠正**：每次让 AI 生成代码后，都需要人工修正命名、包位置、事务注解、注入方式等基础问题
- **测试策略混乱**：AI 不知道项目使用 WebTestClient + JSON fixtures 编写 API 测试，不了解 WireMock 的集成方式
- **工作流不一致**：不同开发者使用 AI 的方式不同，有的跳过契约测试，有的忘记更新 API 文档

本 Harness 将工程约束、行业最佳实践、工作流规范全部编码为 AI 可直接消费的上下文：

| 目标 | 效果 |
|------|------|
| **架构一致性** | AI 生成的代码自动遵循四层架构（Domain → Application → Infrastructure → Interfaces） |
| **测试规范** | AI 自动使用 WebTestClient + JSON fixtures + DatabaseVerifier 编写 API 测试 |
| **契约优先** | AI 在实现 API 前先编写 Spring Cloud Contract 契约测试 |
| **TDD 工作流** | AI 遵循 Red → Green → Refactor → Contract → Docs 的严格顺序 |
| **代码审查** | Claude Code 子代理可执行架构合规、安全检查的自动化审查 |

---

## AI 配置目录结构

AI 工具会自动发现并加载以下标准路径的配置：

```
# GitHub Copilot
.github/copilot-instructions.md          # 主指令
.github/instructions/*.instructions.md   # 子指令（路径限定）
.github/prompts/*.prompt.md              # 可复用工作流
.github/workflows/copilot-setup-steps.yml # Coding Agent 环境配置

# Claude Code
CLAUDE.md                                # 主指令
.claude/settings.json                    # 权限与项目设置
.claude/rules/*.md                       # 路径限定规则
.claude/skills/<name>/SKILL.md           # 可复用工作流
.claude/agents/*.md                      # 子代理
```

项目包含两套独立的 Harness 配置，共享同一份示例代码：

```
├── copilot/                  # GitHub Copilot Harness
│   ├── .github/
│   │   ├── copilot-instructions.md
│   │   ├── instructions/           # 15 个带 applyTo 的子指令
│   │   ├── prompts/                # 3 个可复用工作流
│   │   └── workflows/              # Coding Agent 环境配置
│   ├── docs/、scripts/、src/、pom.xml...
└── claude/                     # Claude Code Harness
    ├── CLAUDE.md
    ├── .claude/
    │   ├── rules/                  # 15 个带 paths 的规则
    │   ├── skills/                 # 3 个可复用工作流
    │   ├── agents/                 # 4 个子代理
    │   └── settings.json           # 权限配置
    ├── docs/、scripts/、src/、pom.xml...
```

---

## Copilot vs Claude：两套配置的设计差异

| 维度 | GitHub Copilot | Claude Code |
|------|---------------|-------------|
| **机制** | 扁平文件 + `applyTo` frontmatter 条件加载 | 三层结构：rules + skills + agents |
| **主文件** | `.github/copilot-instructions.md` | `CLAUDE.md`（项目根目录） |
| **子指令** | `.github/instructions/*.instructions.md` | `.claude/rules/*.md` |
| **路径限定语法** | `applyTo: "**/*.java"`（逗号分隔字符串） | `paths: ["**/*.java"]`（YAML 列表） |
| **可复用工作流** | `.github/prompts/*.prompt.md` 独立文件 | 独立目录 `skills/<name>/SKILL.md` |
| **工作流参数** | 不支持参数化 | 支持 `arguments` 声明命名参数 |
| **子代理** | 不支持 | `agents/code-reviewer.md`、`agents/security-auditor.md` 等 |
| **权限控制** | 无 | `.claude/settings.json`（allow/deny 列表） |
| **环境设置** | `copilot-setup-steps.yml`（CI 环境预设） | 无需（直接运行在本地环境） |

---

## 核心 AI 配置内容

### 15 个知识模块（两平台内容一致，组织方式不同）

| 模块 | 作用 |
|------|------|
| **architecture** | 四层架构定义（Domain → Application → Infrastructure → Interfaces） |
| **tech-stack** | 技术栈版本约束（Java 21, Spring Boot 3.5.x, Spring Cloud Contract 4.3.0） |
| **api-conventions** | URL 命名、HTTP 方法语义、统一响应格式 `ApiResponse<T>` |
| **service-conventions** | `@Transactional(readOnly = true)` 类级默认、接口+实现模式 |
| **db-conventions** | Flyway 迁移命名、实体规则、MySQL/H2 兼容对照表 |
| **test-conventions** | 测试分层（API/Integration/Contract）、测试数据准备 |
| **apitest-guide** | API 测试完整指南（WebTestClient、JSON fixtures、DatabaseVerifier、WireMock stub） |
| **downstream-conventions** | 下游接口在 domain、实现在 infrastructure、WireMock 测试规范 |
| **tdd-workflow** | 七步 TDD 流程（Red → Green → Refactor → Contract → Docs） |
| **contract-test** | Spring Cloud Contract Groovy DSL 模板和检查清单 |
| **db-migration** | Flyway 目录结构、破坏性变更处理流程 |
| **code-review** | 审查检查清单（JavaDoc、构造器注入、DTO record、Contract 覆盖） |
| **java-coding-standard** | Java 编码规范（命名、不可变性、Optional、Stream、异常、泛型） |
| **logging** | 日志规范（`@Slf4j`、结构化日志、敏感数据脱敏） |
| **validation** | Bean Validation 模式（DTO 校验、自定义校验器、分组校验） |

### Claude 独有的高级功能

| 功能 | 文件 | 用途 |
|------|------|------|
| **Feature 工作流** | `skills/implement-feature/SKILL.md` | 从需求到实现的完整 TDD 流程（含下游集成步骤） |
| **Add Endpoint 工作流** | `skills/add-endpoint/SKILL.md` | 添加新 API 端点的契约优先流程 |
| **Refactor 工作流** | `skills/refactor-module/SKILL.md` | 安全重构的增量变更流程 |
| **代码审查代理** | `agents/code-reviewer.md` | 独立子代理执行架构合规和代码质量审查 |
| **安全审计代理** | `agents/security-auditor.md` | 独立子代理执行 OWASP 安全检查 |
| **TDD 引导代理** | `agents/tdd-guide.md` | 独立子代理强制执行 TDD 工作流，先写测试再实现 |
| **构建错误修复代理** | `agents/build-error-resolver.md` | 独立子代理诊断并修复 Maven 构建和测试失败 |

---

## 参考实现（示例应用）

这是一个四层架构的 Spring Boot 3 REST API 用户管理模块，用于验证 Harness 规范的端到端可用性。

### 技术栈

- Java 21 + Spring Boot 3.5.x + Maven
- Spring Data JPA + Spring Validation + Spring Security（CSRF 禁用）
- MySQL 8.0（生产）/ H2（测试）
- Flyway 10.x 数据库迁移
- JUnit 5 + AssertJ + JSONAssert
- Spring Cloud Contract 4.3.0 + WireMock

### API 端点

| Method | Endpoint | 说明 |
|--------|----------|------|
| POST | /api/v1/users | 创建用户（触发下游通知） |
| GET | /api/v1/users/{id} | 查询用户 |
| GET | /api/v1/users | 分页列表 |
| PUT | /api/v1/users/{id} | 更新用户 |
| DELETE | /api/v1/users/{id} | 删除用户 |

### 关键设计

- **四层架构**：Domain（纯 Java）→ Application（Service + DTO）→ Infrastructure（RepositoryImpl + Config + Downstream）→ Interfaces（Controller + ExceptionHandler）
- **API 测试**：WebTestClient + JSON fixtures + DatabaseVerifier + `@Sql` seed data + WireMock stub
- **契约测试**：Spring Cloud Contract Groovy DSL，生成 Stub 验证 API 契约
- **下游通知**：`POST /api/v1/users` 成功后发送 `POST /api/v1/notifications/user-created`

---

## 快速启动

### 环境要求
- Java 21+
- Maven 3.9+
- Docker（可选，用于 MySQL）

### Docker 一键启动
```bash
cd claude  # 或 cd copilot
docker-compose up -d
```

### 本地开发
```bash
cd claude  # 或 cd copilot

# 1. 启动 MySQL
docker-compose up -d mysql

# 2. 初始化数据库
./scripts/setup-dev-env.sh

# 3. 运行应用
mvn spring-boot:run
```

### 测试
```bash
cd claude  # 或 cd copilot

# 快速测试（跳过契约测试）
./scripts/fast-test.sh

# 完整测试流水线（单元 + 契约 + 集成）
./scripts/full-ci.sh

# 仅契约测试
./scripts/run-contract-tests.sh
```

---

## 项目文件索引

| 文件/目录 | 说明 |
|-----------|------|
| `README.md` | 本文档（Harness 总览 + 参考实现说明） |
| `CLAUDE.md` | Claude Code 根级项目指令（核心原则 + 工作规范） |
| `copilot/` | GitHub Copilot Harness + 参考实现 |
| `claude/` | Claude Code Harness + 参考实现 |
| `docs/` | 参考资料（AI Coding 文章、官方文档副本） |

每个子项目内部结构相同：
- `docs/` — 需求文档、设计 ADR、API 规范、领域模型、文档模板
- `scripts/` — 便捷脚本（fast-test、full-ci、contract-tests、setup-dev、generate-openapi）
- `src/` — 源代码（Domain + Application + Infrastructure + Interfaces + Tests）
