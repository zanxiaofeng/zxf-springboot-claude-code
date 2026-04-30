# Spring Boot 3 REST API Demo + AI Coding Configuration Showcase

这是一个可运行的 Spring Boot 3 REST API 演示项目，同时也是一个 **AI Coding 工具上下文配置的最佳实践示范**。项目展示了如何为 GitHub Copilot 和 Claude Code 构建完整、精确、工程化的指令系统，使 AI 助手能够在大规模代码库中保持高质量的代码输出和一致的工作流。

---

## 为什么创建 Copilot 和 Claude 配置目录？

### 问题背景

在大规模 Java 项目中使用 AI Coding 助手时，开发者通常遇到以下问题：

- **上下文碎片化**：AI 不了解项目架构、分层规则和技术约束，生成的代码常违反团队规范
- **重复纠正**：每次让 AI 生成代码后，都需要人工修正命名、包位置、事务注解、注入方式等基础问题
- **测试策略混乱**：AI 不知道项目使用 WebTestClient + JSON fixtures 编写 API 测试，不了解 WireMock 的集成方式
- **下游服务盲区**：AI 不理解"接口在 domain、实现在 infrastructure"的下游集成模式
- **工作流不一致**：不同开发者使用 AI 的方式不同，有的跳过契约测试，有的忘记更新 API 文档

### 解决方案：项目级 AI 配置

本项目创建了 **两套完整且独立的 AI 配置**，分别针对 GitHub Copilot 和 Claude Code，将项目的工程约束、工作流规范、技术决策全部编码为 AI 可直接消费的上下文：

| 目标 | 效果 |
|------|------|
| **架构一致性** | AI 生成的代码自动遵循四层架构（Domain → Application → Infrastructure → Interfaces） |
| **测试规范** | AI 自动使用 WebTestClient + JSON fixtures + DatabaseVerifier 编写 API 测试 |
| **契约优先** | AI 在实现 API 前先编写 Spring Cloud Contract 契约测试 |
| **下游集成** | AI 自动在 domain 层定义接口、infrastructure 层实现、WireMock 中 stub |
| **TDD 工作流** | AI 遵循 Red → Green → Refactor → Contract → Docs 的严格顺序 |
| **代码审查** | Claude Code 子代理可执行架构合规、安全检查的自动化审查 |

---

## AI 配置目录结构

AI 工具会自动发现并加载以下标准路径的配置：

```
# GitHub Copilot
.github/copilot-instructions.md          # 主指令
.github/instructions/*.instructions.md   # 子指令（路径限定）

# Claude Code
CLAUDE.md                                # 主指令
.claude/settings.json                    # 权限与项目设置
.claude/rules/*.md                      # 路径限定规则
.claude/skills/<name>/SKILL.md          # 可复用工作流
.claude/agents/*.md                     # 子代理
```

项目包含两份完全独立的项目副本（代码 + AI 配置）：

```
├── copilot/                  # GitHub Copilot 完整项目副本
│   ├── .github/
│   │   ├── copilot-instructions.md
│   │   └── instructions/           # 11 个带 applyTo 的子指令
│   ├── docs/、scripts/、src/、pom.xml...
└── claude/                     # Claude Code 完整项目副本
    ├── CLAUDE.md
    ├── .claude/
    │   ├── rules/                  # 11 个带 files 的规则
    │   ├── skills/                 # 3 个可复用工作流
    │   ├── agents/                 # 2 个子代理
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
| **路径限定语法** | `applyTo: "**/*.java"` | `files: ["**/*.java"]` |
| **可复用工作流** | 内联在主文件（Prompt Templates 表格） | 独立目录 `skills/<name>/SKILL.md` |
| **子代理** | 不支持 | `agents/code-reviewer.md`、`agents/security-auditor.md` |
| **权限控制** | 无 | `.claude/settings.json`（allow/deny 列表） |
| **文件导入** | 不支持，用 Markdown 链接引用 | `@.claude/rules/xxx.md` 语法 |

---

## 核心 AI 配置内容

### 11 个知识模块（两平台内容一致，组织方式不同）

| 模块 | 作用 |
|------|------|
| **architecture** | 四层架构定义（Domain → Application → Infrastructure → Interfaces） |
| **tech-stack** | 技术栈版本约束（Java 21, Spring Boot 3.5.x, Spring Cloud Contract 4.3.0） |
| **api-conventions** | URL 命名、HTTP 方法语义、统一响应格式 `ApiResponse<T>` |
| **service-conventions** | `@Transactional(readOnly = true)` 类级默认、接口+实现模式 |
| **db-conventions** | Flyway 迁移命名、实体规则、MySQL/H2 兼容对照表 |
| **test-conventions** | 测试分层（API/Integration/Contract）、测试数据准备 |
| **downstream-conventions** | 下游接口在 domain、实现在 infrastructure、WireMock 测试规范 |
| **tdd-workflow** | 七步 TDD 流程（Red → Green → Refactor → Contract → Docs） |
| **contract-test** | Spring Cloud Contract Groovy DSL 模板和检查清单 |
| **db-migration** | Flyway 目录结构、破坏性变更处理流程 |
| **code-review** | 审查检查清单（JavaDoc、构造器注入、DTO record、Contract 覆盖） |

### Claude 独有的高级功能

| 功能 | 文件 | 用途 |
|------|------|------|
| **Feature 工作流** | `skills/implement-feature/SKILL.md` | 从需求到实现的完整 TDD 流程（含下游集成步骤） |
| **Add Endpoint 工作流** | `skills/add-endpoint/SKILL.md` | 添加新 API 端点的契约优先流程 |
| **Refactor 工作流** | `skills/refactor-module/SKILL.md` | 安全重构的增量变更流程 |
| **代码审查代理** | `agents/code-reviewer.md` | 独立子代理执行架构合规和代码质量审查 |
| **安全审计代理** | `agents/security-auditor.md` | 独立子代理执行 OWASP 安全检查 |

---

## 项目代码概况

这是一个四层架构的 Spring Boot 3 REST API 项目，用户管理模块为示例场景。

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
- **集成测试**：TestRestTemplate + `@SpringBootTest(RANDOM_PORT)` + H2 + WireMock
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
docker-compose up -d
```

### 本地开发
```bash
# 1. 启动 MySQL
docker-compose up -d mysql

# 2. 初始化数据库
./scripts/setup-dev-env.sh

# 3. 运行应用
mvn spring-boot:run
```

### 测试
```bash
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
| `README.md` | 本文档（项目总览 + AI 配置说明） |
| `copilot/` | GitHub Copilot 完整项目副本（代码 + AI 配置） |
| `claude/` | Claude Code 完整项目副本（代码 + AI 配置） |
| `docs/` | 项目文档（需求、设计 ADR、API 规范、领域模型、规范、模板） |
| `scripts/` | 便捷脚本（fast-test、full-ci、contract-tests、setup-dev、generate-openapi） |
| `src/` | 源代码（Domain + Application + Infrastructure + Interfaces + Tests） |
