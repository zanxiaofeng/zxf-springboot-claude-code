# Presentation Outline

## Page 1 [cover]
- **Title**: Spring Boot 3 REST API 项目规范
- **Content**: 版本 v1.0 | 适用：Claude Code AI Coding + Spring Boot 3.x + Java 21 | 规范即代码，文档即契约

## Page 2 [table_of_contents]
- **Title**: 目录
- **Content**: 
  1. 项目架构与配置 — 目录结构与Claude Code配置体系
  2. 编码规范 — Java/Spring编码与API设计规范
  3. 测试策略 — TDD流程、Contract Test与Builder模式
  4. 基础设施与工程实践 — 异常处理、Migration与CI/CD

## Page 3 [chapter]
- **Title**: 01 项目架构与配置
- **Content**: 结构化目录、Claude Code配置体系与上下文注入

## Page 4 [content]
- **Title**: 项目目录结构：分层与职责分离
- **Content**: 基于文档"1.项目目录结构"部分。核心信息：展示四层架构（Domain→Application→Infrastructure→Interfaces）的目录组织，强调 `.claude/` 配置目录（skills/context/prompts）、`docs/` 文档目录（requirements/design/conventions）、`src/test` 测试分层（unit/integration/contract/support）以及 `scripts/` 工具脚本。关键信息点：规范化的项目骨架是实现"规范即代码"的基础。

## Page 5 [content]
- **Title**: Claude Code 配置体系：上下文驱动的AI编程
- **Content**: 基于文档"2.Claude Code配置体系"部分。核心信息：三层配置入口——`.cursorrules`（全局行为规则）、`claude.md`（项目上下文锚点）、`.claude/config.json`（自动引用触发器）。关键机制：文件模式匹配自动加载对应规范（如Controller自动加载api-conventions.md和contract-test.md），关键词触发Skills激活（TDD/Contract/Database）。关键规则：新增功能先读需求、新增API先写Contract、数据库变更必须Flyway。

## Page 6 [chapter]
- **Title**: 02 编码规范
- **Content**: Java编码规范、Spring Boot分层约定与API设计标准

## Page 7 [content]
- **Title**: Java与Spring Boot编码核心规范
- **Content**: 基于文档"3.编码规范"的3.1和3.2节。核心信息：Java规范（PascalCase/camelCase/UPPER_SNAKE、构造器注入、Optional<T>禁止裸null、OffsetDateTime、不可变集合、BusinessException）；Spring Boot规范（Domain→Application→Infrastructure→Interfaces四层、@Transactional只放Service层、API路径/api/v{version}/{resources}复数名词、Controller统一返回ApiResponse<T>）。以表格形式呈现规则与要求。

## Page 8 [content]
- **Title**: API设计与响应体规范
- **Content**: 基于文档"3.3 API设计规范"和"3.4测试规范"。核心信息：HTTP Method语义表（GET/POST/PUT/PATCH/DELETE对应状态码）；统一响应体结构（code/data/message/timestamp/traceId）；错误响应包含errors数组（field/message）；测试三层分类（单元@Test/集成@IT/Contract@ContractTest）；强制规则（测试独立、@Sql准备数据、Arrange-Act-Assert、@DirtiesContext）。

## Page 9 [chapter]
- **Title**: 03 测试策略
- **Content**: TDD开发流程、Contract Test与TestDataBuilder模式

## Page 10 [content]
- **Title**: TDD开发流程：七步严格顺序
- **Content**: 基于文档"4.TDD开发流程"的4.1节。核心信息：七步流程——Step1需求分析（读取docs/requirements/）、Step2识别Builder、Step3编写失败测试（红）、Step4最小实现（绿）、Step5重构、Step6 Contract Test、Step7文档更新。流程图形式展示红绿循环。Builder使用规范：单元测试用纯Builder、集成测试用持久化Builder、Fixture复用预定义数据集。

## Page 11 [content]
- **Title**: Contract Test：契约即文档
- **Content**: 基于文档"5.Contract Test规范"。核心信息：目录规范（src/test/resources/contracts/{module}/）；Groovy DSL模板（request/response结构、regex匹配动态值）；ContractBaseTest基础类（@SpringBootTest + RestAssuredMockMvc）；强制检查清单（字段与api-spec-v1.md一致、regex匹配id/timestamp/email、错误场景必须覆盖400/404/500、生成Stub后运行run-contract-tests.sh通过）。

## Page 12 [content]
- **Title**: TestDataBuilder模式与测试数据管理
- **Content**: 基于文档"7.TestDataBuilder模式"。核心信息：三层Builder体系——TestDataBuilder<T>基础接口（build/buildAndPersist）、{Entity}Builder纯Builder（Randomizer生成默认值、链式withXxx方法、but()复制方法）、PersistableBuilder（代理+Repository持久化）；Randomizer工具类（nextId/uuid/username/email/password/now/past）；Fixture预定义数据集（如UserFixtures.activeUser/inactiveUser）。

## Page 13 [chapter]
- **Title**: 04 基础设施与工程实践
- **Content**: 全局异常处理、Flyway Migration与CI/CD Pipeline

## Page 14 [content]
- **Title**: GlobalExceptionHandler与Flyway Migration策略
- **Content**: 基于文档"6.GlobalExceptionHandler"和"8.Flyway Migration策略"。核心信息：统一响应包装ApiResponse<T>（含FieldError嵌套类）；ErrorCode枚举（6位编码+HttpStatus）；BusinessException领域异常基类；GlobalExceptionHandler三层处理（业务异常/参数验证/兜底异常）+ X-Trace-Id追踪。Flyway策略：目录结构（migration/mysql/test-migration）、H2兼容规则（AUTO_INCREMENT→IDENTITY等5条）、生产与测试配置分层、破坏性变更流程（新增→双写→下一版本清理）。

## Page 15 [content]
- **Title**: CI/CD Pipeline：分层质量门禁
- **Content**: 基于文档"9.CI/CD Pipeline配置"。核心信息：GitHub Actions四阶段流水线——Unit Tests（mvn test -Dtest="*Test"）、Contract Tests（needs unit, mvn verify -Pcontract-tests, 上传stubs）、Integration Tests（mvn test -Dtest="*IT"）、Publish Contracts（main分支, mvn deploy）；Maven Profile（contract-tests使用spring-cloud-contract-maven-plugin、fast-test跳过契约）；本地脚本（fast-test.sh/full-ci.sh）。

## Page 16 [final]
- **Title**: 规范即代码，文档即契约
- **Content**: 核心原则回顾：结构化目录支撑规模化协作、TDD+Contract Test保障API契约一致性、Flyway+CI/CD实现可重复交付。维护说明：本文档由Claude Code根据项目结构自动生成并维护，修改规范时同步更新.claude/skills/与docs/conventions/下对应文件，确保AI Coding上下文一致性。
