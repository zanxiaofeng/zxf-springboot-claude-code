# AI 工具配置目录

本目录集中存放项目中所有 AI Coding 工具的配置文件和对应的项目代码副本。每个工具拥有完全独立的子目录，包含完整的项目代码 + AI 配置，便于独立测试和版本控制。

## 目录结构

```
.ai-config/
├── README.md                 # 本文件
├── copilot/                  # GitHub Copilot 完整项目副本
│   ├── copilot-instructions.md
│   ├── instructions/           # 11 个 .instructions.md 子指令
│   ├── .github/workflows/ci.yml
│   ├── docs/                   # 项目文档
│   ├── scripts/                # 构建脚本
│   ├── src/                    # 完整源代码
│   ├── pom.xml
│   ├── docker-compose.yml
│   └── README.md
└── claude/                     # Claude Code 完整项目副本
    ├── CLAUDE.md
    ├── settings.json           # 权限配置
    ├── rules/                  # 11 个路径限定规则
    ├── skills/                 # 3 个可复用工作流
    ├── agents/                 # 2 个子代理
    ├── .github/workflows/ci.yml
    ├── docs/                   # 项目文档
    ├── scripts/                # 构建脚本
    ├── src/                    # 完整源代码
    ├── pom.xml
    ├── docker-compose.yml
    └── README.md
```

## 工具标准路径说明

各 AI 工具有其特定的配置加载路径，以下路径必须保持才能使配置生效：

| 工具 | 配置内容 | 标准路径（必须保持） |
|------|----------|---------------------|
| **GitHub Copilot** | 主指令 | `.github/copilot-instructions.md` |
| **GitHub Copilot** | 子指令 | `.github/instructions/*.instructions.md` |
| **Claude Code** | 主指令 | 项目根目录 `CLAUDE.md` |
| **Claude Code** | 子规则 | `.claude/rules/*.md` |
| **Claude Code** | 技能 | `.claude/skills/<name>/SKILL.md` |
| **Claude Code** | 子代理 | `.claude/agents/*.md` |
| **Claude Code** | 设置 | `.claude/settings.json` |

## 配置同步

`.ai-config/` 是本项目的 AI 配置**单一可信来源**。标准路径下的文件应与此目录保持同步。

当需要修改配置时：
1. 先修改 `.ai-config/` 下对应文件
2. 再同步到标准路径（或直接编辑标准路径后反向同步到此处）

## Copilot 与 Claude 配置差异说明

由于两个平台的机制不同，部分内容在两个目录中的组织方式有所差异：

| 内容 | Copilot 位置 | Claude 位置 |
|------|-------------|---------------|
| Feature 实现流程 | `instructions/tdd-workflow.instructions.md` 内联 | `skills/implement-feature/SKILL.md` |
| Add Endpoint 流程 | `instructions/api-conventions.instructions.md` 内联 | `skills/add-endpoint/SKILL.md` |
| Refactor 流程 | `instructions/code-review.instructions.md` 内联 | `skills/refactor-module/SKILL.md` |
| Sprint 状态更新路径 | `.github/copilot-instructions.md` | `CLAUDE.md` |

这是两个平台机制差异导致的合理分化，不影响功能等价性。

## 快速参考

### Copilot 配置要点
- 子指令使用 `*.instructions.md` 命名，包含 `applyTo` frontmatter
- VS Code 自动加载 `.github/instructions/` 下的所有 `.instructions.md` 文件

### Claude Code 配置要点
- 主指令 `CLAUDE.md` 应保持精简（< 200 行，推荐 < 100 行）
- 子规则使用 `@.claude/rules/xxx.md` 语法在 CLAUDE.md 中引用
- 子规则使用 `files: ["glob"]` frontmatter 限定作用范围
- 技能使用 `skills/<name>/SKILL.md` 结构，包含 `name` + `description` frontmatter
- 子代理放在 `agents/` 下，可被技能调用
