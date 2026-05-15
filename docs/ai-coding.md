

# AI Coding Agent 动态上下文组织与生成机制深度研究

## 1. 核心架构与信息整合决策流程

### 1.1 代理循环（Agentic Loop）机制

#### 1.1.1 三阶段工作流：上下文收集 → 行动执行 → 结果验证

AI coding agent 的核心运作范式建立在持续运转的代理循环之上，这一架构从根本上重塑了信息整合与决策流程的动态性。以 Claude Code 为例，其系统被明确设计为分层代理系统，而非单一助手通过超长提示进行对话的简化模型  [(Powered Pentest Tool.)](https://www.penligent.ai/hackinglabs/inside-claude-code-the-architecture-behind-tools-memory-hooks-and-mcp/) 。代理循环作为系统中心，采用统一的 `queryLoop()` 迭代异步生成器，协调上下文组装、模型调用、工具执行和状态更新等关键环节  [(arXiv.org)](https://arxiv.org/pdf/2604.14228) 。无论用户通过交互式终端、无头 CLI、Agent SDK 还是 IDE 集成进行交互，核心执行逻辑保持一致，仅渲染层和用户交互层存在差异  [(arXiv.org)](https://arxiv.org/pdf/2604.14228) 。

该循环的三阶段工作流可精确分解为：**上下文收集（Context Ingestion）**、**行动执行（Execution）**与**结果验证（Feedback Loop Closure）**。在上下文收集阶段，系统从九个有序来源构建完整的上下文窗口：系统提示（含输出风格修改）、环境信息（git 状态等）、CLAUDE.md 层级指令、路径作用域规则、自动记忆、工具元数据（技能描述、MCP 工具名称、延迟工具定义）、对话历史、工具执行结果以及压缩摘要  [(arXiv.org)](https://arxiv.org/html/2604.14228v1) 。值得注意的是，初始组装后，相关记忆预取、MCP 指令增量、代理列表增量和后台代理任务通知等来源会在轮次进行中后期注入，使得上下文窗口在组装时并非静态，而是在整个交互回合中持续生长  [(arXiv.org)](https://arxiv.org/html/2604.14228v1) 。

行动执行阶段的核心特征是工具调用的并行化与序列化智能调度。Claude Code 采用 `StreamingToolExecutor` 作为主要执行路径，在模型响应流式传输时即开始执行工具，显著降低多工具响应的延迟  [(arXiv.org)](https://arxiv.org/html/2604.14228v1) 。系统通过 `partitionToolCalls()` 将工具分类为并发安全型（concurrent-safe）或独占型（exclusive）：只读操作可并行执行，而状态修改操作（如 shell 命令）则被序列化  [(arXiv.org)](https://arxiv.org/html/2604.14228v1) 。Codex CLI 则强调自主性，其系统提示明确要求"一旦用户给出方向，主动收集上下文、计划、实施、测试和完善，无需在每个步骤等待额外提示"  [(openai.com)](https://developers.openai.com/cookbook/examples/gpt-5/codex_prompting_guide) 。

结果验证阶段通过多重机制确保反馈闭环的完整性。Claude Code 采用 append-only 的 JSONL 转录本作为持久状态表示，仅在有明确清理需求时执行重写  [(arXiv.org)](https://arxiv.org/pdf/2604.14228) 。工具执行结果以特定格式（`assistant → tool_use → user → tool_result` 的消息链）注入上下文，驱动下一轮决策  [(arXiv.org)](https://arxiv.org/pdf/2604.14228) 。系统还实现了多种恢复机制：当响应触及输出 token 上限时，可尝试提升限制进行恢复（最多 3 次尝试）；当上下文接近容量时，触发反应式压缩（reactive compact）；若 API 返回 `prompt_too_long` 错误，则依次尝试上下文折叠溢出恢复和反应式压缩，仅在失败后终止循环  [(arXiv.org)](https://arxiv.org/pdf/2604.14228) 。

| 阶段 | 核心功能 | 关键机制 | 上下文影响 |
|:---|:---|:---|:---|
| 上下文收集 | 多源信息聚合与优先级排序 | 九来源组装、延迟注入、Hooks 扩展 | 确定模型可见的信息边界 |
| 行动执行 | 工具选择与调用、代码生成 | 并行/串行调度、权限检查、沙箱执行 | 产生外部效应，改变环境状态 |
| 结果验证 | 输出评估、错误恢复、状态更新 | 消息链注入、检查点机制、反应式压缩 | 反馈闭环，驱动迭代优化 |

#### 1.1.2 迭代式决策：基于前序步骤动态调整下一步行动

代理循环的迭代特性体现在每一步决策都严格依赖前序步骤的输出状态，而非遵循预设的固定计划。这种动态调整机制在多个层面展开：首先是**工具调用的链式反应**，当模型读取文件后发现新的依赖关系，可能触发额外的文件读取；执行测试失败后，可能转向调试或代码修复。研究表明，coding agent 在不同任务类型上会自发涌现差异化的处理策略——在 BrowseComp-Plus 等多跳检索任务中，agent 表现出"迭代搜索-精炼"模式，基于初始查询发现实体，提取新关系，构建针对下一推理跳的精炼查询，最终形成六跳推理链，且该行为完全自主涌现，无需显式指令  [(arXiv.org)](https://arxiv.org/html/2603.20432v1) 。

其次是**计划工具的动态更新**。Codex 的规划工具（plan tool）要求"完成计划的一个子任务后更新计划"，并强调"计划指导你的编辑；交付物是可工作的代码"  [(openai.com)](https://developers.openai.com/cookbook/examples/gpt-5/codex_prompting_guide) 。Claude Code 则通过 TODO 项目和计划状态的实时维护，在上下文中嵌入当前工作进度，使模型能够基于"已完成/进行中/待办"的状态矩阵做出合理决策。当检测到自身陷入循环（如反复读取或编辑同一文件而无明确进展）时，系统会触发终止条件——Codex 的系统提示明确要求："如果你发现自己在没有明确进展的情况下反复读取或重新编辑相同的文件，停止并以简洁的摘要结束本轮，以及任何需要的澄清问题"  [(openai.com)](https://developers.openai.com/cookbook/examples/gpt-5/codex_prompting_guide) 。

第三是**错误恢复与策略转换**。Claude Code 的 `queryLoop()` 实现了精细的错误处理：当 API 返回 `prompt_too_long` 时，依次尝试上下文折叠溢出恢复和反应式压缩；当工具执行失败时，通过 `PostToolUseFailure` 钩子事件允许系统注入重试逻辑或降级策略  [(Claude Code Docs)](https://code.claude.com/docs/en/hooks) 。这种迭代适应性使得 agent 能够在复杂任务中逐步推进，而非试图一次性解决所有问题。2026 年 4 月披露的一个严重缓存优化 bug 揭示了反馈闭环完整性的关键性：本意是在会话闲置超过一小时后清除旧思考段落以降低成本，但实现错误导致该清除在会话剩余时间内每轮都执行，造成"遗忘、重复和奇怪的工具选择"  [(Anthropic)](https://www.anthropic.com/engineering/april-23-postmortem) 。修复后，系统更加谨慎地保护推理历史的完整性，确保跨轮次的决策一致性。

#### 1.1.3 模型切换策略：根据任务复杂度自动选择不同能力模型

现代 AI coding agent 普遍支持多模型后端，其上下文生成机制需要适配不同模型的能力特征。Claude Code 提供从 Haiku 到 Sonnet 再到 Opus 的模型谱系，各自具有不同的上下文窗口、推理深度和成本结构  [(稀土掘金)](https://juejin.cn/post/7627035938669756451) 。模型切换的决策逻辑通常基于任务复杂度估计：简单查询或文件操作可能路由至轻量级模型以降低成本和延迟；涉及架构设计、跨文件重构或复杂调试的任务则升级至高性能模型。

| 模型 | 最佳场景 | 上下文窗口 | 特性与权衡 |
|:---|:---|:---|:---|
| **Sonnet 4.6** | 日常编码、实现、大多数任务 | 200K | 默认选择，平衡速度与智能 |
| **Opus 4.6/4.7** | 复杂多文件推理、架构规划 | 200K-1M | 更昂贵但推理深度更强，支持 xhigh effort tier |
| **Haiku 4.5** | 只读子代理、快速探索 | 200K | 成本最低，适合并行执行 |

Claude Code 引入了 **Opusplan Mode**，自动使用 Opus 进行规划，然后切换回 Sonnet 执行实现，这种"大脑-双手"分离策略可降低 60-70% 的成本  [(稀土掘金)](https://juejin.cn/post/7627035938669756451) 。模型切换的上下文影响需要谨慎管理：不同模型的上下文窗口大小、token 计费方式和缓存行为存在差异，切换时需确保任务状态、关键决策等核心上下文被正确传递至新模型实例。技能系统支持在技能元数据中指定特定模型（如 `claude-opus-4-20250514`），使得复杂任务（如代码审查）可以自动调用更强能力的模型，而常规任务则使用默认模型以平衡成本与延迟  [(leehanchung.github.io)](https://leehanchung.github.io/blogs/2025/10/26/claude-skills-deep-dive/) 。这种动态模型切换不仅优化了资源利用，也确保了关键决策获得足够的上下文处理能力。

### 1.2 多源信息融合框架

#### 1.2.1 用户输入解析：意图识别与查询增强（Query Augmentation）

用户输入是代理循环的触发源，但原始输入往往存在意图模糊、领域术语缺失、约束条件不完整等问题。Claude Code 采用"目标-约束-完成标准"（Goal-Constraint-Done）框架对任务进行结构化解析  [(Claude Code Prompt)](https://www.claudedirectory.org/blog/context-engineering-claude-code) 。以"添加订单状态变更的邮件通知"为例，弱提示仅陈述功能需求，而强提示明确界定：目标（订单转为"已发货"或"已送达"时向客户发送邮件）、约束（使用 Resend 库、通过 Bull 任务队列异步发送、仅在用户启用通知偏好时触发）、完成标准（状态转换触发邮件任务、使用现有品牌模板、测试覆盖正常路径及通知禁用场景、其他状态转换不发送邮件） [(Claude Code Prompt)](https://www.claudedirectory.org/blog/context-engineering-claude-code) 。

查询增强策略还包括"引用替代重复"（References Over Repetition）原则：不通过自然语言描述代码模式，而是直接指向代码库中的现有示例文件，如"参照 `src/app/api/users/[id]/route.ts` 的模式添加 PATCH 端点，采用相同的验证方法、错误处理、响应格式" [(Claude Code Prompt)](https://www.claudedirectory.org/blog/context-engineering-claude-code) 。Claude Code 的上下文工程层（Context Engineering Layer）包含 QueryProcessor 和 ContextPicker 组件，前者解析用户查询，后者负责上下文组装  [(arXiv.org)](https://arxiv.org/html/2603.05344v1) 。这种分离设计允许系统在不改变用户表达的情况下，自动附加相关环境信息（如当前工作目录、git 分支、最近修改文件等）。

更高级的查询增强体现在角色定位上。当用户将 Claude Code 定位为"联合创始人"或"开发经理"角色时，中央 AI 会自然地对用户的粗糙想法进行理解和提炼，再将优化后的提示委派给子代理  [(Claude Fast)](https://claudefa.st/blog/guide/mechanics/context-engineering) 。这一过程中，中央 AI 实际上承担了查询增强器的角色，将非结构化的用户意图转化为结构化的任务描述。例如，用户可能仅说"修复那个登录问题"，而中央 AI 需要基于已有上下文推断出具体的问题表现、相关文件位置、以及可能的修复方向，形成增强后的查询再传递给专门负责调试的子代理。

#### 1.2.2 任务状态追踪：当前目标、已完成工作与待办事项的实时维护

长程任务的有效执行要求 agent 维护精确的任务状态表示。Claude Code 通过多层次的待办事项系统实现这一目标：用户可见的 TODO 列表、模型内部的计划状态、以及压缩摘要中的结构化任务追踪。在九部分结构化摘要中，"待办事项"（TODO items）、"当前工作状态"（What's currently being worked on）和"建议下一步"（Suggested next steps）三个部分专门用于任务状态的持久化  [(justin3go.com)](https://justin3go.com/en/posts/2026/04/09-context-compaction-in-codex-claude-code-and-opencode) 。

任务状态追踪的深度体现在 Claude Code 的"计划模式"（Plan Mode）中。当通过 `Shift+Tab` 激活计划模式时，系统分离思考阶段与执行阶段：首先分析请求、提出结构化行动计划，然后仅在用户验证后执行  [(sfeir.com)](https://institute.sfeir.com/en/claude-code/claude-code-context-management/) 。这种模式将上下文消耗降低 25-45%，因为它避免了读取无关文件的浪费——在标准模式下，Claude 可能读取 15-25 个不必要的文件，而计划模式下仅需 3-8 个  [(sfeir.com)](https://institute.sfeir.com/en/claude-code/claude-code-context-management/) 。

更高级的状态管理采用**文件化笔记系统**：在会话结束前，要求 agent 将当前计划/进度保存到 markdown 文件（如 `plan.md`、`context.md`、`tasks.md`），然后使用 `/clear` 重置会话，新会话开始时读取该文件继续开发  [(Chudi Nnorukam)](https://chudi.dev/blog/claude-code-complete-guide) 。这种"文档与清除"模式（Document and Clear Pattern）实现了状态的外部持久化，减轻了上下文窗口的维护负担。Anthropic 的 Pokémon 游戏 agent 案例生动展示了记忆对长程任务的 transformative 作用：agent 在数千游戏步骤中精确追踪目标状态——"过去 1,234 步在 Route 1 训练 Pokémon，Pikachu 已提升 8 级，目标 10 级"——并自主发展出探索区域地图、关键成就解锁记录、针对不同对手的战斗策略笔记  [(Anthropic)](https://www.anthropic.com/engineering/effective-context-engineering-for-ai-agents) 。上下文重置后，agent 读取自身笔记即可继续多小时的训练序列或地牢探索，这种跨摘要步骤的连贯性使得纯上下文窗口无法支撑的长程策略成为可能。

#### 1.2.3 工具输出整合：文件读取、代码执行、网络搜索等结果的动态注入

工具输出是上下文增长的主要来源，也是信息融合的关键挑战。Claude Code 的上下文窗口组装明确区分了"初始组装"和"后期注入"两类来源：文件读取、命令输出、子代理摘要和压缩摘要等属于在交互过程中动态注入的内容  [(arXiv.org)](https://arxiv.org/html/2604.14228v1) 。这种延迟注入策略避免了预加载大量可能无关的工具信息，而是根据实际执行流程按需追加。

对于超大规模输出，系统采用智能截断策略。OpenDev 系统的工具结果优化层设定 8,000 字符（约 2,000 token）的阈值：低于此值的输出直接摘要后进入对话历史；超过阈值的输出被卸载（offload）到会话特定的暂存目录（`~/.opendev/scratch/<session_id>/`），上下文中仅保留 500 字符预览加引用路径  [(arXiv.org)](https://arxiv.org/html/2603.05344v1) 。Claude Code 则采用 10,000 字符的阈值，将完整输出保存为文件，上下文中仅保留预览片段与文件路径  [(Github)](https://github.com/humanlayer/advanced-context-engineering-for-coding-agents/blob/main/ace-fca.md) 。这种"指针化"处理既保留了信息的可访问性，又避免了上下文被单个大输出淹没。

网络搜索结果的整合具有特殊挑战，因为搜索结果可能包含大量不相关页面。Claude Code 的 `web_search` 工具返回结构化摘要而非原始 HTML，这些摘要经过模型处理后被压缩为关键发现点，减少噪声注入。Codex CLI 则采用更激进的策略：工具结果在压缩阶段被完全清除，仅保留用户消息和模型决策摘要  [(justin3go.com)](https://justin3go.com/en/posts/2026/04/09-context-compaction-in-codex-claude-code-and-opencode) 。这种差异反映了不同产品在信息完整性与上下文经济性之间的不同权衡。

| 输出类型 | 处理方式 | 阈值 | 上下文保留内容 |
|:---|:---|:---|:---|
| 常规工具输出 | 直接注入消息链 | 无 | 完整结果 |
| 大型文件读取 | 文件化 + 预览保留 | 10,000 字符 | 前 N 行预览 + 文件路径 |
| 命令执行输出 | 文件化 + 预览保留 | 10,000 字符 | 关键片段 + 完整路径 |
| 网络搜索结果 | 结构化摘要 | 无 | 关键发现点列表 |
| 子代理输出 | 结构化摘要返回 | 无 | 结果摘要 + 详细路径 |

#### 1.2.4 内部状态聚合：对话历史、技能库、记忆系统的协同调用

内部状态的聚合涉及多个持久化系统的协同，这些系统在不同时间尺度和作用域上维持信息。Claude Code 的三层记忆层次架构为跨会话信息持久化提供了结构化框架  [(aayushmnit.com)](https://aayushmnit.com/posts/2026-01-24_ClaudeCode/ClaudeCode.html) ：

**对话历史**提供即时的交互记忆，但受限于上下文窗口大小，会经历压缩和修剪。其优势在于细粒度的可追溯性——模型可以回顾具体的推理步骤和决策依据；劣势在于易失性，压缩后大量细节丢失。

**技能库（Skills）**提供结构化的领域知识，按需加载以避免上下文膨胀。技能在会话启动时仅加载描述信息（名称、触发条件），完整指令体仅在技能被触发时注入上下文  [(Claude Code Docs)](https://code.claude.com/docs/en/features-overview) 。这种"元数据预热、内容按需"的设计实现了上下文效率与功能丰富度的平衡。

**记忆系统（Memory）**提供跨会话的持久化学习，使 agent 能够在多次交互中积累项目特定的知识。Claude Code 的 Auto Memory 自动捕获用户修正中的偏好模式，存储于 `~/.claude/projects/<project>/memory/MEMORY.md`，下次会话自动加载前 200 行  [(shareuhack.com)](https://www.shareuhack.com/en/posts/claude-memory-feature-guide-2026) 。这与用户显式编写的 `CLAUDE.md` 形成互补：前者是"Claude 观察到的关于你的信息"，后者是"你的要求"  [(shareuhack.com)](https://www.shareuhack.com/en/posts/claude-memory-feature-guide-2026) 。

这些系统的协同调用遵循特定的优先级和加载时机。系统提示（含工具定义、技能索引）固定于序列前端；CLAUDE.md 及记忆文件紧随其后；对话历史按时间排列，但可能被压缩或修剪；最新用户输入位于末端，确保模型优先关注当前请求  [(Powered Pentest Tool.)](https://www.penligent.ai/hackinglabs/inside-claude-code-the-architecture-behind-tools-memory-hooks-and-mcp/) 。技能发现遵循三级优先级：项目本地技能（`.claude/skills/`）最高，用户全局技能（`~/.claude/skills/`）次之，内置技能最低，允许项目特定约定覆盖默认行为  [(arXiv.org)](https://arxiv.org/html/2603.05344v2) 。

### 1.3 上下文生成与决策输出

#### 1.3.1 行动选择机制：基于综合上下文决定工具调用或文本回复

在每一轮代理循环中，模型面临的核心决策是：继续执行工具调用以获取更多环境信息，还是基于已有信息生成文本回复（可能是代码、解释或向用户的澄清问题）。这一选择并非简单的二元判断，而是涉及对多种因素的综合权衡：当前上下文中信息的充分性、信息的不确定性、任务的紧急程度、以及工具调用的成本效益分析  [(Claude Code Docs)](https://code.claude.com/docs/en/how-claude-code-works) 。

Claude Code 的决策架构被描述为"deny-first evaluation"（拒绝优先评估）——系统默认对潜在风险操作持保守态度，通过权限模式（7 种权限模式）、ML 分类器和可选的沙箱执行等多层机制逐步建立信任  [(arXiv.org)](https://arxiv.org/pdf/2604.14228) 。这种安全导向的决策哲学同样体现在上下文生成中：敏感操作的相关上下文会被特别标注，权限状态作为显式变量参与决策。Codex CLI 则采用更激进的自主性策略，其系统提示明确编码了"自主性高级工程师"的角色期望："一旦用户给出方向，主动收集上下文、计划、实施、测试和完善"  [(openai.com)](https://developers.openai.com/cookbook/examples/gpt-5/codex_prompting_guide) 。

行动选择还受到"上下文焦虑"（Context Anxiety）现象的微妙影响：当模型感知到上下文窗口接近极限时，其行为模式会发生变化——从积极的并行操作转向保守的串行执行，即使实际剩余空间充足  [(inkeep.com)](https://inkeep.com/blog/context-anxiety) 。Cognition 团队发现，启用 1M token 上下文窗口但将实际使用上限设为 200K，可使模型保持"充足跑道"的心理状态，避免焦虑驱动的次优捷径  [(inkeep.com)](https://inkeep.com/blog/context-anxiety) 。这种心理效应揭示了上下文管理不仅是技术问题，也涉及模型行为的认知层面。

#### 1.3.2 上下文注入策略：将决策依据和预期结果编码为模型可见上下文

上下文注入不仅包含原始信息，还包含对信息的结构化组织与优先级标注。Claude Code 的系统提示构建展示了复杂的条件逻辑：某些组件始终包含（实心蓝点标记），其他组件条件包含（空心蓝点标记），且组件可能存在变体——例如"使用你的工具"部分仅包含与可用工具相关的信息  [(dbreunig.com)](https://www.dbreunig.com/2026/04/04/how-claude-code-builds-a-system-prompt.html) 。这种动态组装使得上下文结构能够适应会话状态的变化，避免无关信息干扰。

一个关键设计是**系统提示与用户上下文的结构分离**。Claude Code 通过 `asSystemPrompt(appendSystemContext(systemPrompt, systemContext))()` 组合系统上下文与基础提示，而用户上下文（CLAUDE.md 和日期）通过 `prependUserContext()` 前置到消息数组  [(arXiv.org)](https://arxiv.org/html/2604.14228v1) 。这种分离意味着 CLAUDE.md 内容在 API 请求中占据与系统提示不同的结构位置，可能影响模型的注意力模式——早期指令可能比后期指令获得更高的权重。

上下文注入策略还涉及"渐进式披露"（Progressive Disclosure）：对于大型功能，不将所有信息一次性注入，而是分层构建——第一提示要求分析现有 WebSocket 设置与文档模型并报告方案，第二提示基于确认的方案实现服务端事件处理，第三提示添加 React 组件与协作 UI  [(Claude Code Prompt)](https://www.claudedirectory.org/blog/context-engineering-claude-code) 。每一层构建在前一层积累的上下文之上，使得到达 UI 实现阶段时，Claude 已对自建的数据层有深度理解。

#### 1.3.3 反馈闭环构建：执行结果回传至上下文，驱动下一轮决策

反馈闭环的完整性取决于工具结果如何被整合回上下文。Claude Code 采用严格的消息链格式：`assistant` 消息包含 `tool_use` 块（工具调用请求），`user` 消息包含 `tool_result` 块（工具执行结果），形成清晰的调用-响应配对  [(arXiv.org)](https://arxiv.org/pdf/2604.14228) 。这种结构确保模型能够明确区分"我的决策"与"世界反馈"，在后续推理中正确归因成功或失败。

对于成功的工具调用，结果可能直接注入；对于失败的调用（如编译错误、测试失败、文件未找到），系统可能添加错误分类标签，帮助 agent 快速识别问题类型并选择适当的修复策略  [(Claude Code Docs)](https://code.claude.com/docs/en/how-claude-code-works) 。这种反馈闭环的设计质量直接影响 agent 的错误恢复能力——一个设计良好的闭环能够使 agent 从工具执行失败中快速学习并调整策略，而设计不良的闭环则可能导致 agent 陷入重复尝试相同错误操作的循环。

反馈闭环还涉及对人类干预的响应机制。用户可以在任何时刻中断 agent 的执行循环，提供额外上下文、纠正错误假设或要求改变方向。Claude Code 通过支持 `/steer` 命令（向进行中的 turn 追加用户输入）和 `/interrupt` 命令（请求取消正在执行的 turn）来实现精细的人类控制  [(openai.com)](https://developers.openai.com/codex/app-server) 。这些机制本质上是对标准代理循环的扩展，允许外部智能体（人类用户）动态注入新的目标或约束。

## 2. 上下文管理与优化：渐进式压缩体系

### 2.1 上下文作为稀缺资源的设计哲学

#### 2.1.1 上下文窗口的硬约束与软退化：200K token 上限与 80% 警戒线

上下文窗口是 AI coding agent 面临的最根本约束之一，其限制不仅体现在物理的 token 数量上限，更体现在性能的实际软退化。Claude Code 基于 Claude 3.5 Sonnet、Claude 3.7 Sonnet 及 Claude Opus 4 系列模型，标准上下文上限为 200K token，Opus 4.6 在 beta 阶段可扩展至 1M token  [(mindstudio.ai)](https://www.mindstudio.ai/blog/context-window-claude-code-manage-consistent-results/) 。然而，这一数字仅表征硬件容量的上限，而非有效工作能力的保证。

实际可用空间因系统开销而大幅缩减：新鲜会话启动即消耗约 20K token 加载系统提示、工具定义和 CLAUDE.md 文件；每个 MCP 服务器添加的工具模式永久占用上下文空间，实用上限约为 5-8 个服务器  [(DataCamp)](https://www.datacamp.com/tutorial/claude-code-best-practices) 。更为关键的是"软退化"现象：当上下文填充超过 20-40% 时，模型输出质量开始下降；超过 60% 时，多数从业者观察到明显的性能衰减；接近 80% 时，复杂任务（如跨文件重构）的成功率急剧降低  [(DataCamp)](https://www.datacamp.com/tutorial/claude-code-best-practices) 。Chroma 于 2025 年 7 月发布的技术报告对 18 个领先模型进行了跨长度评估，结果揭示了一个令人警醒的规律：性能并非随上下文增长而线性下降，而是在特定阈值后出现"灾难性"衰减——部分模型在 10K token 处即显现退化迹象，超过 50K 后加速恶化  [(RockCyber Musings)](https://www.rockcybermusings.com/p/the-context-window-trap-why-1m-tokens) 。

这种"迷失在中间"（lost in the middle）现象意味着上下文管理不仅是容量问题，更是质量问题——简单地填充到物理上限往往导致性能劣化，而非有效利用。多位从业者独立得出相同阈值：**不要让上下文超过 60% 容量**  [(DataCamp)](https://www.datacamp.com/tutorial/claude-code-best-practices) 。Claude Code 在状态栏显示当前上下文使用率，使用户能够直观监控这一关键资源  [(Mintlify)](https://www.mintlify.com/saurav-shakya/Claude_Code-_Source_Code/advanced/context-management) 。更精细的管理可能涉及任务类型感知的动态阈值——需要深度架构理解的 refactoring 任务可能需要保留更多余量，而独立的文档更新任务则可以容忍更高的上下文密度。

| 上下文使用率 | 性能特征 | 建议操作 |
|:---|:---|:---|
| 0-40% | 最佳工作区间，注意力分配均匀 | 正常执行，无需特殊干预 |
| 40-60% | 早期退化迹象，边缘信息召回下降 | 监控使用趋势，准备压缩预案 |
| 60-80% | 显著性能衰减，复杂任务成功率下降 | 主动压缩或任务分块，避免启动高复杂度操作 |
| 80-100% | 临界状态，错误率激增，可能触发自动压缩 | 立即压缩或 `/clear`，保存关键状态后重启 |

#### 2.1.2 上下文污染与腐烂（Context Rot）：信息过时导致的性能衰减

上下文腐烂是长会话中逐渐累积的隐性性能杀手。随着对话轮次的增加，上下文中积累的信息并非都保持同等价值：早期的工具结果可能已被后续操作失效（如读取后修改的文件），过时的假设可能已被证伪，临时的探索路径可能已被放弃。这些"腐烂"信息不仅浪费宝贵的 token 预算，更可能通过引入矛盾信号干扰当前决策  [(Blog)](https://sfailabs.com/guides/how-to-manage-context-when-developing-with-ai) 。

Anthropic 的工程博客明确指出了四种可预测的失败模式  [(Claude Fast)](https://claudefa.st/blog/guide/mechanics/context-engineering) ：

| 失败模式 | 机制描述 | 典型症状 |
|:---|:---|:---|
| **上下文污染（Poisoning）** | 幻觉信息进入上下文后被反复引用，形成错误级联 | 基于过时事实的错误决策，且错误自我强化 |
| **上下文分心（Distraction）** | 上下文过大导致模型过度聚焦历史，忽视当前任务 | 重复询问已确认信息，对早期指令的过度依赖 |
| **上下文混淆（Confusion）** | 不相关的工具或文档误导代理方向 | 可用工具越多性能越差，无关信息淹没信号 |
| **上下文冲突（Clash）** | 上下文不同部分存在直接矛盾 | 自相矛盾的输出，决策摇摆不定 |

上下文腐烂的表现形式多样：agent 开始重复之前已回答的问题、生成的代码与早期建立的架构决策冲突、对项目结构的描述与实际情况不符等。Claude Code 用户报告的"Claude drifted"（Claude 漂移）抱怨，经分析往往实质是上下文预算问题——早期指令在压缩过程中丢失，而非模型能力退化  [(Powered Pentest Tool.)](https://www.penligent.ai/hackinglabs/inside-claude-code-the-architecture-behind-tools-memory-hooks-and-mcp/) 。这种渐进式退化比硬性截断更具破坏性，因为它难以被即时察觉，导致用户在不知不觉中接受质量递减的输出。

应对上下文腐烂需要区分不同类型的信息时效性。任务目标和高层次架构决策通常具有长期价值，应在压缩中优先保留；具体的工具输出（如文件内容、搜索结果）往往具有短期价值，可以通过占位符替换或重新调用来"刷新"；而中间的推理过程（如失败的尝试、临时的假设）则可能具有负价值，主动清除反而有助于提高决策清晰度  [(justin3go.com)](https://justin3go.com/en/posts/2026/04/09-context-compaction-in-codex-claude-code-and-opencode) 。

#### 2.1.3 渐进式管理原则：从最小破坏到最大压缩的层级升级

面对上下文压力，最成熟的 agent 系统采用渐进式（tiered）管理策略，而非一刀切的激进压缩。这一原则的核心是"能修剪不摘要，能摘要不忘"——优先尝试对模型推理影响最小的清理操作，仅在必要时才升级到更激进的压缩层级  [(justin3go.com)](https://justin3go.com/en/posts/2026/04/09-context-compaction-in-codex-claude-code-and-opencode) 。

Claude Code 的五层压缩管道（five-layer compaction pipeline）是这一原则的工程典范  [(arXiv.org)](https://arxiv.org/pdf/2604.14228) ：

| 层级 | 名称 | 机制 | 成本 | 破坏性 | 触发条件 |
|:---|:---|:---|:---|:---|:---|
| L1 | Budget Reduction | 工具结果预算限制，超阈值持久化到磁盘 | 零 API 调用 | 极低：内容可重新获取 | 单个工具输出超出大小限制 |
| L2 | History Snip | 轻量级旧历史修剪，基于功能标志 | 零 API 调用 | 低：仅移除旧片段 | 时间深度过大的历史 |
| L3 | Microcompact | 细粒度时间路径 + 可选缓存感知路径 | 零 API 调用 | 中：替换为占位符 | 缓存开销反应 |
| L4 | Context Collapse | 读取时虚拟投影，非破坏性折叠 | 零 API 调用 | 中高：视图压缩，源保留 | 极长历史 |
| L5 | Auto-compact | 完整模型生成摘要，分叉子代理 | 一次 API 调用 | 高：不可逆摘要 | 语义压缩最后手段 |

渐进式管理的优势在于风险可控性和恢复能力。轻量级清理操作（如替换工具结果占位符）是可逆的——agent 意识到需要具体数据时可以重新调用工具获取；而重量级摘要操作则可能导致信息永久丢失。通过分层设计，系统能够在大多数场景下仅依赖轻量级操作维持上下文健康，将昂贵的 LLM 摘要保留为最后手段。这种设计哲学与计算机系统中的内存管理层次结构（从寄存器到缓存到主存到磁盘）异曲同工，都是在稀缺资源约束下实现性能与成本的动态平衡。

### 2.2 Claude Code 三级"精准遗忘"机制

#### 2.2.1 第一层：工具结果修剪（Tool Result Trimming）

##### 2.2.1.1 零 LLM 成本的本地规则引擎

Claude Code 的第一层压缩是其最具特色的设计之一，它实现了"选择性遗忘"而非"全面失忆"的精妙平衡。这一层完全由本地规则引擎驱动，不涉及任何 LLM API 调用，因此具有**零额外成本和即时执行**的特性  [(justin3go.com)](https://justin3go.com/en/posts/2026/04/09-context-compaction-in-codex-claude-code-and-opencode) 。规则引擎的核心逻辑简洁而高效：识别并保护最近工具调用的结果（"活跃数据"不可删除），将超出保护窗口的旧工具结果替换为标准化占位符。

保护窗口的大小是一个关键的设计参数，需要在上下文节省与重新调用开销之间权衡。窗口过小会导致频繁重新获取相同数据，增加 API 成本和延迟；窗口过大则削弱压缩效果。Claude Code 的具体实现细节未完全公开，但社区分析表明其采用动态保护策略，可能基于工具类型（文件读取 vs. 网络搜索 vs. 代码执行）和调用时间戳综合判断保护优先级  [(Part 2)](https://zane-portfolio.kiyo-n-zane.com/blog/by/developer/learn_from_claude_code_context_compaction/) 。

##### 2.2.1.2 旧工具结果替换为占位符：`[Old tool result content cleared]`

占位符替换是第一层压缩的具体实现机制。当规则引擎判定某个工具结果超出保护窗口时，它不删除整个工具调用-结果消息对，而是仅替换结果部分的内容为 `[Old tool result content cleared]` 占位符，保留工具调用的元数据（工具名称、参数、调用时间等） [(justin3go.com)](https://justin3go.com/en/posts/2026/04/09-context-compaction-in-codex-claude-code-and-opencode) 。这种细粒度处理的优势在于维持了对话结构的完整性——模型仍然可以看到完整的工具使用历史序列，理解 agent 曾经采取过哪些探索步骤，只是无法直接访问过时的具体数据。

以具体的登录 bug 修复场景为例：假设 agent 在第 4 步执行了 `grep "auth" --include="*.ts"` 搜索，获得约 2,000 token 的搜索结果；在第 7 步执行了 `read_file "src/auth.ts"`，获得约 3,500 token 的文件内容。经过多轮后续操作后，这些早期工具结果可能被修剪，但消息序列中仍然保留 `#4 Tool Call: grep "auth"...` 和 `#7 Tool Call: read_file "src/auth.ts"...` 的记录，只是对应的结果部分变为 `[Old tool result content cleared]`  [(justin3go.com)](https://justin3go.com/en/posts/2026/04/09-context-compaction-in-codex-claude-code-and-opencode) 。如果后续推理需要确认某个具体函数是否在 `src/auth.ts` 中，agent 可以基于保留的调用记忆决定重新执行 `read_file`，而非盲目猜测或基于可能已过时的缓存内容推理。

##### 2.2.1.3 保留工具调用记忆，支持按需重新调用

第一层设计的精妙之处在于其对人类认知模式的模拟：我们记得自己曾经读过某本书、查过某个资料，但通常不记得具体内容，需要时可以重新查阅。这种"元记忆"（meta-memory）——关于信息来源和获取路径的记忆——往往比具体信息本身更具持久价值，特别是在信息可能动态变化的环境中  [(justin3go.com)](https://justin3go.com/en/posts/2026/04/09-context-compaction-in-codex-claude-code-and-opencode) 。对于代码库这一典型动态环境，文件内容可能在 agent 操作过程中被修改（包括被 agent 自己修改），因此保留获取路径比保留具体快照更具实用价值。

按需重新调用机制还自然实现了"缓存失效"处理。当 agent 意识到需要某个被修剪的具体数据时，它重新执行的工具调用将获取最新状态，而非基于可能过时的历史结果推理。这在长时间 refactoring 会话中尤为重要——agent 可能在会话早期读取了某文件，经过多轮修改后该文件已显著变化，此时基于占位符触发的重新调用将自动获取当前版本，避免基于陈旧信息做出错误决策。

Anthropic 官方将此机制称为"工具使用清理"（Tool Use Clearing），并于 2025 年 9 月将其作为 Claude Developer Platform 的正式功能推出，定位为"最安全的轻触式压缩形式" [(Anthropic)](https://www.anthropic.com/engineering/effective-context-engineering-for-ai-agents) 。该功能的核心价值在于"自动清除陈旧工具调用和结果，同时保留对话流程"，有效延长了 agent 的无干预运行时间。

#### 2.2.2 第二层：缓存友好策略（Cache-Friendly Strategy）

##### 2.2.2.1 消息序列前缀保护，尾部渐进修剪

Claude Code 的第二层压缩是其区别于其他 agent 系统的独特优势，核心在于对 Anthropic API Prompt Cache 特性的深度优化。Prompt Cache 允许当新请求的消息前缀与之前请求匹配时，服务器复用先前的计算结果（KV cache），从而显著降低延迟和成本  [(justin3go.com)](https://justin3go.com/en/posts/2026/04/09-context-compaction-in-codex-claude-code-and-opencode) 。为最大化这一优势，Claude Code 在需要进一步修剪上下文时，采取"保护前缀、修剪尾部"的策略，而非直观的"删除最旧消息"。

具体而言，假设经过第一层工具结果修剪后，消息序列 #1-#26 仍然超出上下文限制。一种"朴素"的修剪策略会从最早的消息开始删除，这似乎合理因为旧信息可能价值较低。但 Claude Code 避免这种做法，因为删除早期消息会改变整个消息序列的前缀，导致 Prompt Cache 完全失效，下一请求必须从头计算  [(justin3go.com)](https://justin3go.com/en/posts/2026/04/09-context-compaction-in-codex-claude-code-and-opencode) 。相反，它宁愿修剪较少的内容，也要确保消息序列的前缀部分与前一请求保持绝对一致，从而使 API cache 命中。

这种策略的代价是压缩效率的轻微降低——保护前缀意味着可能需要保留一些理论上可删除的早期内容。但对于长时运行任务（如协助重构整个模块），这种代价被 cache 带来的显著成本节约所抵消。因为大多数 API 请求的内容可以命中 cache，用户只需为新增的尾部内容付费，这在经济性上远优于追求最大压缩但完全丧失 cache 优势的策略  [(justin3go.com)](https://justin3go.com/en/posts/2026/04/09-context-compaction-in-codex-claude-code-and-opencode) 。

##### 2.2.2.2 Anthropic 提示缓存的高命中率优化

Prompt Cache 的命中率优化是第二层策略的核心目标。Anthropic 的 API 实现细节未完全公开，但基本原理是：当请求的前 N 个 token 与之前处理的请求前缀匹配时，可以复用已计算的 key-value 表示，仅需处理新增的后缀部分。这意味着消息序列的早期部分（系统提示、初始用户消息、早期工具定义等）具有最高的 cache 价值，因为它们在多次请求中保持不变；而近期消息（最新的用户输入、刚执行的工具结果）则必然变化，无法从 cache 获益  [(justin3go.com)](https://justin3go.com/en/posts/2026/04/09-context-compaction-in-codex-claude-code-and-opencode) 。

Claude Code 的修剪算法因此优先保护高 cache 价值区域。系统提示（包含工具定义、技能索引、CLAUDE.md 内容等）通常位于消息序列最前端，是 cache 命中的关键，几乎从不被修剪。初始的用户任务描述和早期规划对话次之，尽可能保留。修剪操作集中在序列尾部，即最近的几轮交互，这些区域 cache 价值最低且信息时效性最高（可能包含已过时或已失效的内容） [(Part 2)](https://zane-portfolio.kiyo-n-zane.com/blog/by/developer/learn_from_claude_code_context_compaction/) 。这种"头重脚轻"的保护策略与信息价值分布高度吻合——早期消息通常包含任务的核心定义和关键约束，价值持久；尾部消息则包含具体执行细节，时效性强且可能已失效。

##### 2.2.2.3 成本与延迟的双重控制

缓存友好策略的经济效益可观。Claude Code 源码中的注释记录了一项 2026 年 1 月的实验："false path is 98% cache miss, costs ~0.76% of fleet cache_creation"  [(arXiv.org)](https://arxiv.org/html/2604.14228v1) 。这表明非缓存优化路径导致极高的缓存未命中率，而正确的缓存策略可将缓存创建成本控制在舰队总成本的 0.76% 以下。对于长时运行任务（如运行数小时、涉及数百次 API 调用的复杂任务），cache 优化可能将总成本降低一个数量级。

延迟控制同样重要。LLM 推理的首次 token 时间（time to first token）与需要处理的上下文长度密切相关。通过 cache 命中避免重复处理长前缀，可以显著改善响应延迟，使 agent 在长时间会话中保持接近初始状态的响应速度，而非逐渐变慢。这对于维持开发者体验至关重要——人类用户对交互系统的延迟高度敏感，延迟增加会显著降低 perceived productivity，即使绝对等待时间仍在可接受范围内  [(justin3go.com)](https://justin3go.com/en/posts/2026/04/09-context-compaction-in-codex-claude-code-and-opencode) 。

#### 2.2.3 第三层：九部分结构化 LLM 摘要（9-Section Structured LLM Summary）

##### 2.2.3.1 摘要结构与生成机制

当前两层压缩仍不足以将上下文控制在限制内时，Claude Code 触发第三层——基于 LLM 的结构化摘要。这是最具侵入性的压缩操作，因为它涉及对历史信息的语义理解和选择性保留，而非简单的格式化处理。与 Codex 的"交接备忘录"不同，Claude Code 采用更精细的九部分结构化模板，强制 LLM 在特定信息槽位中填充内容，防止关键信息的系统性遗漏  [(justin3go.com)](https://justin3go.com/en/posts/2026/04/09-context-compaction-in-codex-claude-code-and-opencode) 。

九部分结构的具体内容基于社区对 Claude Code 泄露源代码的分析  [(Part 2)](https://zane-portfolio.kiyo-n-zane.com/blog/by/developer/learn_from_claude_code_context_compaction/) ：

| 部分编号 | 内容类别 | 功能定位 | 保留优先级 |
|---------|---------|---------|-----------|
| 1 | 用户原始意图（User's original intent） | 锚定任务根本目标，防止长期偏离 | 最高 |
| 2 | 核心技术概念（Core technical concepts） | 保留领域特定的知识框架 | 最高 |
| 3 | 关键文件与代码（Files and code of interest） | 记录关键代码位置，支持快速重新定位 | 高 |
| 4 | 错误与修复（Errors encountered and how they were fixed） | 保存调试经验，避免重复踩坑 | 高 |
| 5 | 问题解决逻辑链（Problem-solving logic chain） | 保留推理轨迹，支持中断后恢复 | 高 |
| 6 | 用户消息摘要（Summary of all user messages） | 压缩对话历史，保留用户意图演变 | 中 |
| 7 | 待办事项（TODO items） | 明确未完成工作，驱动后续执行 | 高 |
| 8 | 当前工作状态（What's currently being worked on） | 标记当前焦点，支持上下文恢复 | 最高 |
| 9 | 建议下一步（Suggested next steps） | 提供行动指南，降低重新启动成本 | 中 |

九部分结构的触发阈值经过精确计算：`effective context window - 13,000 tokens`，其中 effective window = model context window - min(max output tokens, 20,000)  [(justin3go.com)](https://justin3go.com/en/posts/2026/04/09-context-compaction-in-codex-claude-code-and-opencode) 。这一预留空间确保压缩后有足够余量完成当前轮次。

值得注意的是，系统并非直接跳到 LLM 摘要。当自动压缩触发时，首先尝试"会话记忆压缩"（Session Memory Compact）——利用会话记忆中已有的结构化信息替代完整 LLM 调用  [(justin3go.com)](https://justin3go.com/en/posts/2026/04/09-context-compaction-in-codex-claude-code-and-opencode) 。这意味着大多数自动压缩甚至不需要 LLM 调用，仅在会话记忆路径不可用或不足时才回退到传统 LLM 摘要流程  [(justin3go.com)](https://justin3go.com/en/posts/2026/04/09-context-compaction-in-codex-claude-code-and-opencode) 。这种"增量维护、快速输出"的设计将压缩延迟从数分钟降低到近乎即时，显著改善了用户体验。

##### 2.2.3.2 防漂移设计：直接引用原文关键短语

九部分摘要的提示要求极其严格：模型必须**直接引用原文关键短语，而非过度改写**（directly quote key phrases from the original text rather than paraphrasing everything） [(justin3go.com)](https://justin3go.com/en/posts/2026/04/09-context-compaction-in-codex-claude-code-and-opencode) 。这一"防漂移"（anti-drift）设计防止了摘要过程中模型微妙偏离原意的风险——当模型用自身语言重新表述时，可能无意改变技术细节、数值或约束条件，导致后续推理基于扭曲的信息。

直接引用的实现通过提示工程完成：压缩提示明确要求模型在摘要中保留原始文本的关键片段，特别是代码片段、错误消息、文件路径和配置值  [(Part 2)](https://zane-portfolio.kiyo-n-zane.com/blog/by/developer/learn_from_claude_code_context_compaction/) 。这种设计牺牲了摘要的流畅性，换取了技术准确性的保障。例如，对于错误信息，摘要应保留原始错误代码和关键日志片段；对于技术决策，应引用原始论证中的核心语句。这种"引用优先"策略将信息损失降至最低，确保压缩后的上下文能够支持精确的代码生成和调试决策。

##### 2.2.3.3 状态重建：会话引导注入与自动重读

压缩后的状态重建是确保 agent 能够无缝继续工作的关键环节。Claude Code 在生成结构化摘要后，执行一系列恢复操作  [(justin3go.com)](https://justin3go.com/en/posts/2026/04/09-context-compaction-in-codex-claude-code-and-opencode) ：

1. **会话引导注入**：将摘要作为系统级别的"会话引导"注入上下文，确保模型首先看到压缩后的状态概览。典型引导语为"This session continues from a previous conversation..."，为模型建立时间连续性认知。

2. **最近编辑文件自动重读**：系统自动重新读取最近编辑的文件（最多 5 个文件，总预算 50,000 tokens，每文件 5,000 tokens），确保 agent 立即获得当前代码的最新状态。这些文件可能在压缩触发后被修改，或摘要中的代码片段不足以支持后续编辑。

3. **工具与技能重新定义**：重新声明可用的工具和技能，恢复 agent 的操作能力。压缩过程可能清除了相关元数据，重建确保 agent 知晓完整的工具集。

4. **项目规范保护**：`CLAUDE.md` 中的项目规范作为系统提示的一部分永久驻留，不受压缩影响  [(justin3go.com)](https://justin3go.com/en/posts/2026/04/09-context-compaction-in-codex-claude-code-and-opencode) 。

这种"摘要+具体状态"的混合恢复策略兼顾了效率与准确性。摘要提供了高层次的任务上下文和历史决策记录，防止 agent 重复已完成的探索或偏离已确立的架构方向；自动重读的具体文件则提供了编码所需的精确语法和语义信息，确保生成的代码与现有代码库兼容。对于特别长的会话，可能还需要额外的恢复措施，如重新执行关键的工具调用来刷新环境状态，或向用户确认压缩后的理解是否准确。

### 2.3 Codex CLI 的"交接备忘录"机制

#### 2.3.1 保留用户消息原文，删除模型回复与工具结果

OpenAI Codex CLI 采用与 Claude Code 截然不同的压缩哲学，其核心是"交接备忘录"（Handoff Memo）概念——一种模拟工作场景中同事间任务交接的直观机制  [(justin3go.com)](https://justin3go.com/en/posts/2026/04/09-context-compaction-in-codex-claude-code-and-opencode) 。Codex 的压缩流程首先提取最近的用户消息（硬上限约 20,000 token），然后发送一个简短的摘要提示给 LLM，要求生成"让另一位 LLM 能够继续任务的交接摘要"。关键设计决策在于对用户消息的保留策略：Codex 选择**物理保留所有用户消息的原文**，但删除所有 assistant 回复和工具相关消息，然后插入一个包含结构化摘要的 fabricated assistant 消息  [(justin3go.com)](https://justin3go.com/en/posts/2026/04/09-context-compaction-in-codex-claude-code-and-opencode) 。

这种"用户消息保护"策略体现了对"用户意图神圣性"的尊重——用户明确表达的需求和反馈被认为是最不可丢失的信息，因为它们是任务目标的最终来源和验证标准。相比之下，模型的回复被视为可再生的中间产物，只要摘要保留了关键决策和结果，具体措辞和推理过程可以丢弃。工具结果同样被视为可重新获取的临时数据，无需在压缩后保留。

以 15,400 token 的登录 bug 修复场景为例，压缩前包含 26 条消息的完整对话历史被压缩为仅 4 条消息：系统提示、两条保留的用户消息（初始请求和可能的后续澄清）、以及一条包含结构化摘要的 assistant 消息  [(justin3go.com)](https://justin3go.com/en/posts/2026/04/09-context-compaction-in-codex-claude-code-and-opencode) 。摘要内容包括任务目标、已完成项目、关键架构决策和剩余 TODO，足以让新的模型实例"立即进入工作状态"（hit the ground running），而无需了解具体的搜索过程或测试输出细节。

#### 2.3.2 结构化摘要的直观性与信息丢失风险

Codex 的交接备忘录设计具有显著的直观性优势——"工作交接"是人类开发者普遍理解的概念，使得压缩行为的可解释性很强。用户可以想象 agent 在"换班"时向接替者做简要汇报，这种心智模型有助于理解和信任压缩过程  [(justin3go.com)](https://justin3go.com/en/posts/2026/04/09-context-compaction-in-codex-claude-code-and-opencode) 。然而，这种直观性伴随着结构性风险：其"全有或全无"（all-or-nothing）的特性意味着所有模型回复和工具结果都被统一替换为单一摘要，如果摘要遗漏了某个关键细节（如特定的错误模式、探索过的失败路径、或用户给出的微妙约束），该信息将永久丢失，无法通过任何机制恢复。

这种信息丢失风险在复杂调试场景中尤为突出。假设 agent 在压缩前尝试了多种修复方案，前几种都失败了并产生了特定的错误模式，最终找到正确方案。交接备忘录可能仅记录"问题已修复"，而丢失关于失败尝试的具体信息——这些信息对于理解问题的根本原因、避免类似问题复发、或处理相关边缘情况可能至关重要。Claude Code 的分层修剪策略通过保留更多原始信息（至少保留工具调用记忆）和更精细的摘要结构来缓解这一风险，但代价是更复杂的实现和更高的压缩成本  [(justin3go.com)](https://justin3go.com/en/posts/2026/04/09-context-compaction-in-codex-claude-code-and-opencode) 。

社区开发者 Sigalovskinick 针对 Codex 的极简默认提示（仅 6 行"总结对话"）提出了"扩展压缩提示"方案，通过 10 条规则增强压缩保真度  [(GitHub Gist)](https://gist.github.com/sigalovskinick/e2e329bb37ecc74b9f15d5ba74ee1ee5) 。关键规则包括：历史线程规则（识别并累积保留先前的压缩摘要）、累积路线图规则（强制摘要回答"会话的完整旅程"而非仅"近期事件"）、关键决策与原因规则（强调保留决策推理而非仅结果）。测试表明，Codex 的默认压缩在 2 次压缩后所有推理消失，3 次压缩后高层方向模糊，agent 在最长工作时段反而最无用；而扩展提示显著改善了这一问题  [(GitHub Gist)](https://gist.github.com/sigalovskinick/e2e329bb37ecc74b9f15d5ba74ee1ee5) 。

#### 2.3.3 `/responses/compact` 端点：加密内容保存模型潜在理解

Codex CLI 的压缩机制还通过 `/responses/compact` API 端点实现一种更高级的压缩形式，该端点生成"加密内容"（encrypted_content）项，可以被传递到后续请求中  [(openai.com)](https://developers.openai.com/cookbook/examples/gpt-5/codex_prompting_guide) 。这种压缩表示被优化为重建保真度（reconstruction fidelity），而非人类可读性，实现了极高的压缩率（据报道可达 99.3%） [(zenml.io)](https://www.zenml.io/llmops-database/evaluating-context-compression-strategies-for-long-running-ai-agent-sessions) 。与 Claude Code 的结构化摘要不同，这种压缩表示是"不透明的"——用户无法阅读它来验证保留了什么信息，模型也无法直接解释其内容，但据称能够在后续推理中恢复关键状态。

2026 年 3 月的安全研究通过提示注入技术揭示了 `compact()` API 的内部机制：服务器端确实存在一个 compactor LLM，使用隐藏的 system prompt 处理输入，其系统提示、压缩提示和交接提示与开源版本几乎相同  [(simzhou.com)](https://simzhou.com/en/posts/2026/how-codex-compacts-context/) 。加密 blob 可能携带超出简单摘要的额外信息（如工具结果的特定压缩和恢复方式），但具体细节未公开。对于状态less 的输入数组链式调用，将输出项目（包括压缩项目）追加到下一次输入；对于 `previous_response_id` 链式调用，每轮仅传递新用户消息，压缩项目自动携带必要上下文  [(openai.com)](https://developers.openai.com/api/docs/guides/compaction) 。

这种"潜在空间压缩"方法代表了上下文管理的另一种极端：将压缩视为表示学习问题而非摘要问题。其优势是可能保留比显式摘要更多的信息，特别是那些难以用语言表达的微妙模式（如代码风格偏好、隐式约束、或复杂的依赖关系）；其风险则是完全丧失可解释性和可验证性，用户和开发者无法诊断压缩是否导致了信息丢失，也无法在出现问题时进行针对性修复  [(zenml.io)](https://www.zenml.io/llmops-database/evaluating-context-compression-strategies-for-long-running-ai-agent-sessions) 。OpenAI 官方强烈推荐使用其精确的 `apply_patch` 实现，因为模型已针对这种 diff 格式进行训练以优化性能  [(openai.com)](https://developers.openai.com/cookbook/examples/gpt-5/codex_prompting_guide) 。

### 2.4 压缩触发与手动干预

#### 2.4.1 自动压缩阈值：33K token 预留缓冲区的动态管理

上下文压缩的自动触发机制需要在"过早压缩浪费上下文容量"和"过晚压缩导致性能危机"之间取得平衡。Claude Code 的自动压缩触发基于动态计算的阈值：`effective context window - 13,000 tokens`，其中 effective window = model context window - min(max output tokens, 20,000)  [(justin3go.com)](https://justin3go.com/en/posts/2026/04/09-context-compaction-in-codex-claude-code-and-opencode) 。以标准 200K 上下文窗口为例，若模型最大输出设为 8K，则 effective window = 200K - 8K = 192K，触发阈值为 192K - 13K = 179K。这意味着系统在上下文达到约 93% 容量时启动预防性压缩。

13K token 的预留缓冲区具有多重功能：为模型生成响应保留空间，避免在压缩边缘因输出溢出而失败；为压缩操作本身的开销（摘要生成、状态重建）提供余量；以及为用户的即时输入保留响应能力。这一数值 presumably 源于 Anthropic 对模型输出分布与压缩操作成本的统计优化。

Codex CLI 的自动压缩在 token 使用率接近模型上下文窗口限制时触发，默认阈值约为模型窗口的 90%（可配置），并预留基线开销（BASELINE_TOKENS=12,000）以确保 UI 显示的"剩余百分比"反映用户可控空间而非固定提示/工具开销  [(OpenAI API Community Forum)](https://community.openai.com/t/best-practices-for-cost-efficient-high-quality-context-management-in-long-ai-chats/1373996) 。Claude Code 则采用更保守的策略，据报道在 75-92% 利用率区间触发自动压缩，并预留 33K token 的缓冲区用于复杂操作  [(Claude Fast)](https://claudefa.st/blog/guide/mechanics/context-management) 。

阈值选择的差异反映了不同的设计哲学。Codex 的高阈值（90%）倾向于最大化单次会话的连续工作长度，减少压缩频率，但增加了在压缩触发时面临紧急压力的风险——如果压缩本身消耗大量 token（特别是需要 LLM 生成摘要时），可能剩余空间不足以完成压缩操作。Claude Code 的较低阈值和较大预留缓冲区则优先考虑操作安全性和压缩质量，允许在触发后有充足空间执行完整的压缩流程，包括可能的 LLM 摘要生成和状态恢复操作  [(Claude Fast)](https://claudefa.st/blog/guide/mechanics/context-management) 。

#### 2.4.2 `/compact` 命令：自定义指令引导压缩焦点

手动压缩命令为用户提供了对自动压缩行为的精细控制。Claude Code 的 `/compact` 命令支持自定义指令来引导压缩焦点，例如 `/compact focus on the database schema decisions and migration plan` 指示系统在压缩时优先保留与数据库架构相关的决策信息  [(Blog)](https://sfailabs.com/guides/how-to-manage-context-when-developing-with-ai) 。这种"目标导向压缩"使用户能够根据当前任务的特定需求调整压缩策略，而非接受一刀切的全局摘要。

自定义指令的实现机制 presumably 通过修改摘要提示的权重或注入额外的约束条件，影响 LLM 在信息选择与编码过程中的优先级。这与默认的九部分结构形成互补：固定结构确保最低限度的信息完整性，而自定义指令允许针对特定会话的弹性调整。例如，在进行 API 重构时，用户可指令压缩保留接口契约信息，牺牲 UI 调整或文档更新的细节；在调试安全漏洞时，可指令保留完整的漏洞分析轨迹和修复验证结果。

Codex CLI 同样支持 `/compact` 命令，但其行为更标准化——由于采用交接备忘录机制，压缩输出格式相对固定，用户自定义空间较小  [(openai.com)](https://developers.openai.com/codex/learn/best-practices) 。不过，用户可以通过在 `AGENTS.md` 中配置持久化指令来间接影响压缩行为，例如指定"在压缩时始终保留安全相关的决策记录"等偏好。

#### 2.4.3 `/clear` 命令：任务切换时的硬重置策略

当任务完全切换或上下文严重污染时，硬重置（hard reset）比渐进压缩更为高效。Claude Code 的 `/clear` 命令清除所有对话上下文，从零开始新会话，同时保留通过 `CLAUDE.md` 和记忆系统持久化的项目知识  [(Mintlify)](https://www.mintlify.com/saurav-shakya/Claude_Code-_Source_Code/advanced/context-management) 。这种"核选项"适用于多种场景：完成一个离散任务后开始无关工作、注意到性能退化信号（如 agent 开始自相矛盾或重复提问）、或经过 1-2 小时连续工作后无论任务是否完成都进行预防性清理  [(TurboAI)](https://www.turboai.dev/blog/claude-code-context-window-management) 。

`/clear` 与 `/compact` 的关键区别在于信息保留范围。压缩试图在缩减 token 占用的同时维持任务连续性，适合同一任务的不同阶段；清除则完全放弃连续性，适合任务边界或严重上下文污染的场景。专业用户发展出"一个任务，一个上下文"（one task, one context）的工作模式，在任务间积极使用 `/clear`，甚至为不同关注点运行并行的 Claude Code 实例（如一个处理后端实现、一个处理前端工作、一个进行代码审查），每个实例保持独立的新鲜上下文  [(TurboAI)](https://www.turboai.dev/blog/claude-code-context-window-management) 。

最佳实践建议采用"文档与清除"（Document & Clear）模式：将当前进度保存至 Markdown 文件，执行 `/clear`，然后以该文件作为新会话的初始上下文——既获得清洁的上下文窗口，又保留了必要的连续性  [(DataCamp)](https://www.datacamp.com/tutorial/claude-code-best-practices) 。自定义 `/catchup` 命令可平滑过渡：清除后读取当前分支相对 main 的所有修改文件，使 Claude 无需旧对话历史即可接续工作  [(DataCamp)](https://www.datacamp.com/tutorial/claude-code-best-practices) 。这种"保存状态-运行 30 分钟-接受或重新开始"的工作模式，往往比试图纠正退化上下文中的错误更为高效  [(anthropic.com)](https://www-cdn.anthropic.com/58284b19e702b49db9302d5b6f135ad8871e7658.pdf) 。

#### 2.4.4 会话记忆路径与自动摘要机制

Claude Code 通过会话记忆文件实现压缩操作的加速和优化。该文件位于 `~/.claude/projects/[project]/[session]/session_memory` 路径，是一个在会话过程中持续更新的结构化 markdown 文档，自动记录会话标题、当前状态、已完成工作、讨论要点和开放问题  [(Claude Fast)](https://claudefa.st/blog/guide/mechanics/context-management) 。这种"持续维护"的设计使得 `/compact` 命令无需在触发时从头生成摘要——它可以直接加载已维护的会话记忆，进行必要的精简和格式化后注入新上下文。

自动摘要的更新机制是增量式的：每次消息交互后，系统更新会话记忆中的相关部分（如添加新完成的步骤、更新待办事项状态、记录新的讨论要点），而非重新生成整个文档。这种增量维护的代价是持续的轻微开销，但收益是压缩时的即时响应——从社区报告看，Claude Code 的压缩操作在 v2.0.64 版本后已实现"即时完成"，无需等待数分钟的 LLM 摘要生成  [(Claude Fast)](https://claudefa.st/blog/guide/mechanics/context-management) 。这与 Codex CLI 的压缩形成对比，后者需要在压缩触发时执行完整的 LLM 调用以生成交接备忘录，具有显著的延迟成本  [(justin3go.com)](https://justin3go.com/en/posts/2026/04/09-context-compaction-in-codex-claude-code-and-opencode) 。

会话记忆与 CLAUDE.md 的差异在于：前者记录动态执行历史（做了什么、遇到什么、如何解决），后者规定静态行为规范（应该怎么做、遵循什么规则）。两者协同构成了完整的项目认知体系——CLAUDE.md 提供不变的决策基准，会话记忆提供变化的执行轨迹。

## 3. 技能与工具的动态调用体系

### 3.1 技能系统（Skills System）架构

#### 3.1.1 声明式、基于提示的发现与调用机制

Claude Code 的技能系统采用独特的声明式架构，其核心洞察是将技能视为"动态上下文修饰器"而非"可执行代码" [(leehanchung.github.io)](https://leehanchung.github.io/blogs/2025/10/26/claude-skills-deep-dive/) 。技能以 Markdown 文件形式存在，包含 YAML frontmatter（元数据）和指令主体（instructional body）。系统通过两阶段处理实现高效的按需加载：在会话启动时，SkillLoader 扫描所有技能目录，仅解析 YAML frontmatter 提取名称、描述和其他元数据，构建轻量级索引；这个索引以格式化的技能列表形式纳入系统提示，使 agent 始终知晓可用技能的概览，但无需承担加载完整指令的 token 成本  [(arXiv.org)](https://arxiv.org/html/2603.05344v2) 。

技能发现机制的设计遵循"Claude 搜索优化"约定——每个技能的描述以"Use when..."开头，明确指定触发条件（如"Use when writing bash scripts that need to wait for external conditions"），优化 agent 基于文本描述匹配用户意图的能力  [(arXiv.org)](https://arxiv.org/html/2603.05344v2) 。当用户发送请求时，Claude 接收三类信息：用户消息、可用工具列表（Read、Write、Bash 等）、以及包含所有可用技能描述的 Skill 工具。Claude 基于其原生语言理解能力，将用户意图与技能描述进行匹配，决定是否调用 `invoke_skill` 工具并指定技能名称  [(leehanchung.github.io)](https://leehanchung.github.io/blogs/2025/10/26/claude-skills-deep-dive/) 。

这种设计的精妙之处在于决策的完全内生性——没有算法级的技能选择或 AI 驱动的意图检测代码，所有决策发生在模型的推理过程中。这避免了传统意图分类系统的脆弱性（需要维护分类器、处理边界情况、适应新技能），同时利用了大型语言模型强大的语义匹配能力。技能的调用不是通过函数调用或 HTTP 请求，而是通过上下文注入——当技能被触发时，其完整 Markdown 内容（剥离 frontmatter）被注入对话上下文，修改 agent 的行为约束和知识基础  [(leehanchung.github.io)](https://leehanchung.github.io/blogs/2025/10/26/claude-skills-deep-dive/) 。

#### 3.1.2 技能作为动态上下文修饰器：注入领域专用指令

技能的本质是"专业化提示模板"，它们在调用时动态修改 agent 的上下文环境，而非执行预定义的操作序列  [(leehanchung.github.io)](https://leehanchung.github.io/blogs/2025/10/26/claude-skills-deep-dive/) 。这种设计使技能具有极高的灵活性——同一个技能可以被不同模型版本以不同方式解释和执行，技能作者无需预测所有可能的执行路径，只需提供高质量的领域指导和约束。例如，一个"代码审查"技能可能包含审查清单、常见反模式示例、以及团队特定的质量标准，当调用时这些信息被注入上下文，使 agent 在后续代码分析中应用这些标准。

技能系统支持三级优先级覆盖：项目本地技能（`.claude/skills/` 或 `.opendev/skills/`）最高，用户全局技能（`~/.claude/skills/` 或 `~/.opendev/skills/`）次之，内置技能最低，允许项目特定约定覆盖默认行为  [(arXiv.org)](https://arxiv.org/html/2603.05344v2) 。当同名技能存在于多个层级时，高优先级来源自动生效，这种设计支持渐进式定制——从使用内置技能开始，逐步添加个人和项目特定的覆盖。

技能对执行上下文的影响远超简单的文本注入。当技能被调用时，它可以修改工具权限（限制或扩展 agent 可用的工具集）、覆盖模型参数（如调整思考 token 预算）、以及注入生命周期钩子（在技能执行前后运行特定操作） [(towardsai.net)](https://pub.towardsai.net/claude-code-agent-skills-2-0-from-custom-instructions-to-programmable-agents-ab6e4563c176) 。这种"上下文修饰器"能力使技能成为强大的工作流编排机制——例如，一个"安全审计"技能可能禁用文件写入工具以防止意外修改，同时启用增强的网络搜索工具以获取最新的安全公告。

#### 3.1.3 技能元数据：名称、描述、模型指定、禁用自动调用、模式命令分类

技能的 YAML frontmatter 包含丰富的元数据，不仅用于发现，还用于控制技能的执行环境。关键元数据字段包括  [(anthropic.com)](https://resources.anthropic.com/hubfs/The-Complete-Guide-to-Building-Skill-for-Claude.pdf) ：

| 字段 | 功能 | 上下文影响 |
|:---|:---|:---|
| `name` | 技能标识，用于 `/name` 调用 | 无直接上下文影响 |
| `description` | 自动触发时的匹配依据 | 每会话加载描述，占用上下文 |
| `model` | 指定执行模型（sonnet/opus） | 触发时切换模型，改变上下文处理能力 |
| `disable-model-invocation` | 隐藏技能直到手动调用 | 降为零上下文成本，直到显式使用 |
| `disable-auto-call` | 禁止自动调用，仅用户手动触发 | 防止高影响技能被意外调用 |
| `user-invocable` | 用户可见性控制 | `false` 时仅 Claude 可自动调用 |
| `context` | 执行上下文（current/fork） | fork 时创建隔离上下文，保护主会话 |
| `allowed-tools` | 限制技能可用工具集 | 减少工具池大小，降低选择负担 |
| `mode` | 模式命令分类 | 在技能列表顶部特殊区域展示 |

模型指定字段允许为技能绑定特定的底层模型变体——复杂架构技能可指定 Opus，简单格式化技能可使用 Sonnet。这种动态路由在单一 agent 会话内实现了"多模型协作"，无需用户手动切换。`disable-model-invocation` 和 `user-invocable` 的组合使用实现了精细的调用控制：当两者都设为 `false` 时，技能等效禁用；当 `disable-model-invocation: true` 时，技能仅响应显式用户触发，适合有副作用的工作流；当 `user-invocable: false` 时，技能仅 Claude 可自动调用，用于背景知识注入  [(vanja.io)](https://vanja.io/claude-code-skills-guide/) 。

模式命令分类（`mode: true`）将技能从普通工具提升为环境修饰器，在 UI 的"模式命令"特殊区域展示，区别于常规实用技能  [(leehanchung.github.io)](https://leehanchung.github.io/blogs/2025/10/26/claude-skills-deep-dive/) 。这种 UI 层面的显著性反映了其功能层面的重要性：模式命令建立特定的操作上下文或工作流，如 `debug-mode`、`expert-mode`、`review-mode` 等行为切换。模式切换实质上是批量上下文重构，使 agent 从一种认知框架快速转换至另一种。

### 3.2 技能调用的上下文影响

#### 3.2.1 对话上下文修改：指令提示注入

技能激活的首要影响是对话上下文的修改。技能定义中的指令文本被注入到当前对话中，通常作为系统级别的补充提示或特殊的 assistant 消息。这种注入不是简单的追加——系统可能将技能内容放置在特定位置以优化其影响力，例如紧跟在核心系统提示之后、但在用户消息之前，确保技能约束能够覆盖默认行为但又被用户明确指令所覆盖  [(leehanchung.github.io)](https://leehanchung.github.io/blogs/2025/10/26/claude-skills-deep-dive/) 。

技能注入的上下文具有会话内持久性——一旦加载，技能约束通常在整个会话中保持有效，除非被显式卸载或覆盖。这与普通工具调用的一次性结果注入形成对比，后者仅影响当前推理步骤。技能持久性的设计使得 agent 能够在长时间任务中保持行为一致性，例如当"测试驱动开发"技能被加载后，agent 在后续所有代码生成步骤中都会优先考虑可测试性。

具体的消息注入机制如下：当 Claude 决定调用技能时，系统会生成一个 `Skill` 工具的 `tool_use` 请求，随后返回的 `tool_result` 中包含技能元数据（对用户可见），同时额外注入一条包含完整技能提示的隐藏 user 消息（对 UI 不可见但发送给 Claude） [(leehanchung.github.io)](https://leehanchung.github.io/blogs/2025/10/26/claude-skills-deep-dive/) 。这种"可见元数据 + 隐藏完整提示"的双层结构，既保证了用户对技能调用的感知，又实现了对模型的深度指令注入。技能调用的 token 开销显著高于普通工具——每轮约 1,500+ token，相比普通工具的约 100 token  [(leehanchung.github.io)](https://leehanchung.github.io/blogs/2025/10/26/claude-skills-deep-dive/) 。

#### 3.2.2 执行上下文变更：工具权限调整、模型切换、思考 token 参数重设

技能对执行上下文的影响更为深远。技能 frontmatter 中的 `allowed-tools` 字段可动态限制可用工具集，实现细粒度的权限控制——例如，为安全审查技能配置只读工具权限，防止审计过程中的意外修改；相反，为"重构助手"技能可能启用通常被禁用的批量重命名工具  [(towardsai.net)](https://pub.towardsai.net/claude-code-agent-skills-2-0-from-custom-instructions-to-programmable-agents-ab6e4563c176) 。模型切换能力使技能能够根据任务特征优化推理资源分配——简单技能保持默认模型，复杂技能升级到更强模型，而快速查询技能甚至可以降级到更轻量模型以降低成本。

思考 token 参数重设是更精细的控制机制，影响模型在生成回复前的内部推理深度。某些技能可能需要更深的思考（如架构设计技能需要更多 token 进行权衡分析），而其他技能可能需要更直接的响应（如代码格式化技能应避免过度思考）。通过技能级别的参数控制，系统能够在单次会话中动态调整模型的"思考深度"，而非全局固定  [(towardsai.net)](https://pub.towardsai.net/claude-code-agent-skills-2-0-from-custom-instructions-to-programmable-agents-ab6e4563c176) 。这种调整 presumably 通过 API 请求中的 `thinking` 或 `effort` 参数实现，如 Anthropic 的 xhigh effort tier  [(catdoes.com)](https://catdoes.com/blog/claude-code-vs-codex) 。

| 维度 | 普通工具 | 技能 |
|:---|:---|:---|
| 执行机制 | 直接调用外部功能 | 修改模型上下文，由模型自主行动 |
| 上下文影响 | 返回结果注入上下文 | 注入指令，可能持续多轮 |
| 用户可见性 | 明确的功能调用 | 隐式的行为改变 |
| 可组合性 | 原子操作，结果确定 | 灵活解释，依赖模型判断 |
| 错误处理 | 工具级异常 | 模型级误解或忽略 |
| 适用场景 | 精确操作（文件读写、命令执行） | 模糊指导（风格规范、工作流程） |
| Token 开销 | ~100 token | ~1,500+ token/轮 |
| 持久性 | 单次调用 | 激活期间持续生效 |

#### 3.2.3 技能与普通工具的差异对比：直接执行器 vs. 提示注入+上下文修饰器

技能与普通工具在架构上存在本质差异，理解这种差异对于有效使用 agent 系统至关重要。普通工具（如 `read_file`、`bash`）是**直接执行器**——它们执行具体的操作并返回结果，具有明确的输入输出契约和副作用。技能则是**提示注入加上下文修饰器**——它们不直接执行操作，而是通过修改 agent 的上下文环境来影响后续行为，其实际效果依赖于模型对注入指令的解释和执行  [(leehanchung.github.io)](https://leehanchung.github.io/blogs/2025/10/26/claude-skills-deep-dive/) 。

这种差异带来了使用模式的不同。普通工具由 agent 在推理过程中自主调用，用户通常不直接干预具体选择；技能则可以由用户显式触发（通过 `/skill-name`）或由 agent 基于描述自动匹配。普通工具的结果直接可见（文件内容、命令输出），技能的效果则体现在 agent 后续行为的改变上，可能不那么直观。普通工具的错误通常表现为操作失败（文件不存在、命令返回错误码），技能的错误则可能表现为行为偏离（agent 未遵循技能指令、或误解了技能意图） [(leehanchung.github.io)](https://leehanchung.github.io/blogs/2025/10/26/claude-skills-deep-dive/) 。

### 3.3 外部工具的智能加载与发现

#### 3.3.1 工具搜索工具（Tool Search Tool）：按需发现替代全量加载

随着 agent 系统支持的工具数量增长，将所有工具定义一次性加载到上下文中的开销变得不可忽视。Claude Code 通过"工具搜索工具"（Tool Search Tool）机制实现按需发现，允许 agent 在需要时搜索可用工具而非预先加载全部定义  [(arXiv.org)](https://arxiv.org/html/2603.05344v2) 。这种设计将工具发现从"广播"模式转变为"查询"模式，显著降低了大型工具集的上下文占用。

工具搜索的实现依赖于轻量级的元数据索引。与技能发现类似，系统维护一个包含所有可用工具名称、描述和分类的索引，agent 可以基于当前任务特征查询相关工具子集。例如，当 agent 处理数据库相关任务时，它可以搜索"database"或"SQL"关键词发现相关工具，而非加载所有 50+ 工具定义。这种按需发现机制与渐进式上下文加载哲学高度一致——仅在需要时承担信息加载的成本。

传统方式预加载所有 MCP 工具定义（50+ MCP 工具约 72K token），对话历史和系统提示竞争剩余空间，总消耗约 77K token 才开始工作。工具搜索工具模式下，仅预加载工具搜索工具本身（约 500 token），按需发现相关工具（3-5 个，约 3K token），总消耗降至约 8.7K token，**保留 95% 上下文窗口**，实现 **85% 的 token 使用减少**  [(Anthropic)](https://www.anthropic.com/engineering/advanced-tool-use) 。内部测试显示显著精度提升：Opus 4 在 MCP 评估中从 49% 提升至 74%，Opus 4.5 从 79.5% 提升至 88.1%  [(Anthropic)](https://www.anthropic.com/engineering/advanced-tool-use) 。

#### 3.3.2 MCP 工具动态加载：从 55K token 到 8.7K token 的 85% 上下文节省

MCP（Model Context Protocol）为外部工具集成提供了开放标准，Claude Code 和 Codex 均支持通过 MCP 连接外部服务  [(LinkedIn)](https://www.linkedin.com/blog/engineering/ai/contextual-agent-playbooks-and-tools-how-linkedin-gave-ai-coding-agents-organizational-context) 。MCP 工具的动态加载实现了显著的上下文节省——传统集成可能需要将外部 API 的完整 schema 纳入系统提示，而 MCP 通过标准化接口允许 agent 在需要时动态发现工具能力。社区报告显示，通过 MCP 的动态加载机制，工具相关的上下文占用可以从 55K token 降低到 8.7K token，实现 85% 的节省  [(MCP Servers)](https://mcpmarket.com/tools/skills/context-optimization-for-ai-agents) 。

MCP 集成的另一优势是跨 agent 兼容性。遵循 MCP 标准构建的工具可以被任何支持的 agent 系统使用，避免了为每个 agent 单独适配的重复工作。对于企业环境，这意味着可以构建统一的内部工具集成层，供 Claude Code、Codex、Cursor 等多种 agent 共享  [(LinkedIn)](https://www.linkedin.com/blog/engineering/ai/contextual-agent-playbooks-and-tools-how-linkedin-gave-ai-coding-agents-organizational-context) 。LinkedIn 的 CAPT（Contextual Agent Playbooks & Tools）框架就是这一理念的实践，通过 MCP 为工程师提供对内部系统（代码搜索、数据平台、可观测性工具）的标准化访问  [(LinkedIn)](https://www.linkedin.com/blog/engineering/ai/contextual-agent-playbooks-and-tools-how-linkedin-gave-ai-coding-agents-organizational-context) 。

| 加载策略 | 上下文成本 | 适用场景 | 风险 |
|:---|:---|:---|:---|
| 全量加载 | ~55K tokens | 工具频繁切换的复杂工作流 | 上下文快速耗尽 |
| 智能过滤 | ~8.7K tokens | 大多数开发场景 | 首次调用新工具时有延迟 |
| 延迟 schema | 名称级开销 | 工具众多但调用稀疏 | 模型可能不知晓完整功能 |

#### 3.3.3 延迟加载与函数级发现：减少 LLM 学习负担

延迟加载延伸至函数级别。在大型代码库中，Claude Code 不会一次性加载所有函数定义，而是通过 `grep`、`search_files` 等工具按需发现相关函数。这种"函数级发现"策略将代码库的上下文表示从"完整地图"转为"渐进探索"，显著降低初始认知负荷  [(Anthropic)](https://www.anthropic.com/engineering/advanced-tool-use) 。

更细粒度的优化涉及**函数级延迟加载**（Function-Level Lazy Loading）。在大型 MCP 服务器（如企业内部的数百个 API 端点）中，单个服务器可能暴露数十至数百个操作。函数级发现允许模型仅加载特定操作（如"查询用户表"）的定义，而非整个服务器的全部 API。这种分层发现模式将 LLM 的"学习负担"降至最低——模型无需在单次上下文中理解数百个工具的差异，只需逐步探索相关子集  [(Anthropic)](https://www.anthropic.com/engineering/advanced-tool-use) 。

设计良好的函数应当利用 LLM 已有的训练知识，而非要求模型学习全新的抽象。Claude Code 的终端函数是典范：每个 LLM 都理解终端使用方式（存在于训练数据中），因此无需冗长描述  [(EclipseSource)](https://eclipsesource.com/blogs/2026/01/22/mcp-context-overload/) 。该函数功能强大（可搜索、重命名、执行、检查），同时直观易懂。另一种模式是将发现内建于函数设计：Theia IDE 中，代理仅通过两个函数执行用户定义任务——一个搜索相关任务，一个执行——在函数级别镜像了延迟加载方法  [(EclipseSource)](https://eclipsesource.com/blogs/2026/01/22/mcp-context-overload/) 。

### 3.4 工具执行结果的上下文整合

#### 3.4.1 结果注入格式：assistant → tool_use → user → tool_result 的消息链

工具执行结果的上下文整合遵循严格的消息链格式，确保因果可追溯性。`assistant` 消息包含 `tool_use` 块（工具调用请求），`user` 消息包含 `tool_result` 块（工具执行结果），形成清晰的调用-响应配对  [(Powered Pentest Tool.)](https://www.penligent.ai/hackinglabs/inside-claude-code-the-architecture-behind-tools-memory-hooks-and-mcp/) 。这种链式结构使得模型能够明确区分"我的决策"与"世界反馈"，在后续推理中正确归因成功或失败。

消息链的完整性是上下文连贯的基础：缺失 `tool_use` 或 `tool_result` 都会导致模型困惑，可能产生幻觉性的工具调用或忽略实际结果。对于多工具并行调用，多个 `tool_use` 块可共存于同一 assistant 消息，对应多个 `tool_result` 块在后续 user 消息中返回  [(arXiv.org)](https://arxiv.org/html/2604.14228v1) 。结果按工具请求顺序缓冲和发射，即使工具并行执行，输出顺序仍与请求一致  [(arXiv.org)](https://arxiv.org/html/2604.14228v1) 。

#### 3.4.2 大结果截断处理：超 10,000 字符保存为文件，上下文保留预览与路径

对于超大规模工具输出，系统采用智能截断策略。超过 10,000 字符的结果保存为临时文件，上下文中仅保留预览片段与文件路径  [(Powered Pentest Tool.)](https://www.penligent.ai/hackinglabs/inside-claude-code-the-architecture-behind-tools-memory-hooks-and-mcp/) 。这种"指针化"处理既保留了信息的可访问性，又避免了上下文被单个大输出淹没。预览片段的设计包含关键决策信息：文件路径（支持重新获取）、内容类型标识、前 2000 字节样本。这使模型能够评估是否需要完整内容，避免无意义的重新加载。

OpenDev 系统的工具结果优化层设定 8,000 字符（约 2,000 token）的阈值：低于此值的输出直接摘要后进入对话历史；超过阈值的输出被卸载（offload）到会话特定的暂存目录（`~/.opendev/scratch/<session_id>/`），上下文中仅保留 500 字符预览加引用路径  [(arXiv.org)](https://arxiv.org/html/2603.05344v1) 。这种设计创建了自然的分层系统：agent 看到足够理解内容摘要的信息，可在需要时通过 `read_file` 按需获取完整输出，且该读取操作本身也受相同阈值约束。对于具备子代理能力的 agent，卸载提示还会智能建议恢复策略——如"委托 Code Explorer 子代理通过搜索和读取工具处理完整输出"或"使用带 offset/limit 参数的搜索工具增量处理"——避免 agent 尝试超出其能力集的恢复操作  [(arXiv.org)](https://arxiv.org/html/2603.05344v1) 。

#### 3.4.3 技能执行后的上下文持久化：跨轮次的工具权限与模型配置保持

技能激活期间的上下文修改（工具权限、模型配置等）需要谨慎的持久化管理。Claude Code 通过运行时状态（runtime state）管理这些持久化配置，而非依赖上下文的隐式记忆  [(arXiv.org)](https://arxiv.org/html/2604.14228v1) 。这种设计确保即使上下文经历压缩，关键配置仍通过状态重建恢复。

技能执行后的上下文持久化涉及跨轮次的状态保持。当技能修改了工具权限、切换了模型、或调整了思考参数后，这些变更需要在技能的持续使用期间保持生效。Claude Code 通过将技能提示持续注入每轮上下文来实现这种持久性——这也是技能工具相比普通工具有更高 token 开销的原因  [(leehanchung.github.io)](https://leehanchung.github.io/blogs/2025/10/26/claude-skills-deep-dive/) 。这种设计类似于操作系统中的环境变量或 shell 中的 `export` 命令，设置了持续生效的配置状态，直至显式变更或会话结束。

技能的去激活（deactivation）同样需要上下文清理。当用户切换模式或显式卸载技能时，系统需移除对应的指令注入、恢复先前的工具权限、以及重置模型配置。这种"作用域控制"类似于编程语言中的变量作用域——局部修改不污染全局状态，确保系统的可预测性和稳定性。`disable-model-invocation: true` 等设置确保技能调用的副作用不会意外传播，而 `context: fork` 创建的隔离环境在执行后自动清理  [(vanja.io)](https://vanja.io/claude-code-skills-guide/) 。

## 4. 特定类型上下文的创建、管理与使用

### 4.1 静态上下文：CLAUDE.md 与项目规范

#### 4.1.1 创建机制：项目根目录下的持久化指令文件

`CLAUDE.md` 是 Claude Code 静态上下文的核心载体，采用四级层次结构  [(Github)](https://github.com/VILA-Lab/Dive-into-Claude-Code) ：

| 层级 | 位置 | 作用域 | 优先级 | 典型内容 |
|:---|:---|:---|:---|:---|
| Managed | `/etc/claude/` | 系统级，所有项目 | 最低 | 企业安全策略、合规要求 |
| User | `~/.claude/` | 用户级，跨项目 | 低 | 个人编码风格、常用工具偏好 |
| Project | `./CLAUDE.md`, `.claude/rules/` | 项目级，当前目录及子目录 | 高 | 技术栈、架构决策、团队约定 |
| Local | `./CLAUDE.local.md`（gitignored） | 本地级，个人偏好 | 最高 | 本地调试配置、个人工作流 |

这种层次化设计允许规范的分层继承和覆盖。项目级 `CLAUDE.md` 存储团队共享的约定（技术栈、架构决策、代码规范），而 `CLAUDE.local.md` 容纳个人偏好（如特定的 IDE 设置、本地调试配置），避免污染版本控制  [(Github)](https://github.com/VILA-Lab/Dive-into-Claude-Code) 。创建通常通过 `/init` 命令触发，Claude 分析代码库结构、技术栈、测试配置等，生成初始版本，用户随后精炼  [(Vibehackers)](https://vibehackers.io/blog/context-engineering-guide) 。

`CLAUDE.md` 的本质是"项目知识的持久化"——将团队规范、架构模式、常见陷阱编码为模型始终可见的上下文。与动态生成的上下文不同，`CLAUDE.md` 是显式的人工制品，其内容经过人类审核与迭代优化，代表了"我们希望 agent 如何工作"的正式规范。

#### 4.1.2 加载策略：会话启动时自动读取，作为系统提示的一部分

`CLAUDE.md` 的加载是强制且自动的：每个 Claude Code 会话启动时，文件内容被读取并注入系统提示的固定位置  [(nathanonn.com)](https://www.nathanonn.com/context-engineering-with-claude-code-explained/) 。这种"始终在线"特性是 `CLAUDE.md` 的力量所在——无需用户显式引用，模型始终知晓项目规范；也是其风险所在——文件过大时贡献上下文污染，模型开始忽略细节  [(nathanonn.com)](https://www.nathanonn.com/context-engineering-with-claude-code-explained/) 。

加载位置的选择影响规范的优先级。作为系统提示的一部分，`CLAUDE.md` 位于消息序列的最前端，享有最高的注意力权重与最持久的记忆效果。这与用户消息的动态注入形成对比：后者虽具即时性，但在长会话中可能因位置后移而遭受注意力衰减。`CLAUDE.md` 通过 `prependUserContext()` 前置到消息数组，与通过 `asSystemPrompt()` 组合的系统提示占据不同的结构位置  [(arXiv.org)](https://arxiv.org/html/2604.14228v1) 。这种分离可能影响模型的注意力模式——早期指令可能比后期指令获得更高的权重，使得项目特定规则能够覆盖或细化通用系统提示。

#### 4.1.3 内容设计原则：高信号、低冗余，聚焦"Claude 否则会猜测"的规则

有效的 `CLAUDE.md` 设计遵循"高信号、低冗余"原则，每节回答一个 Claude 否则会猜测的问题  [(Claude Code Prompt)](https://www.claudedirectory.org/blog/context-engineering-claude-code) 。Anthropic 官方推荐保持文件"简洁且人类可读"（concise and human-readable），初始版本聚焦核心通用规则，通过技能或动态加载扩展详细内容  [(nathanonn.com)](https://www.nathanonn.com/context-engineering-with-claude-code-explained/) 。实践中 400 token 的精要文件优于 4,000 token 的全面覆盖  [(packmind.com)](https://packmind.com/context-engineering-ai-coding/context-engineering-best-practices/) 。

典型结构包括：

| 内容类型 | 功能定位 | 示例 |
|:---|:---|:---|
| 命令速查 | 消除执行猜测 | `npm run dev` — 启动开发服务器 |
| 架构决策 | 防止技术栈漂移 | State management: Zustand, not Redux. We migrated in Q1. |
| 代码约定 | 统一风格模式 | All new components must be React Server Components unless... |
| 禁止项 | 阻断反模式 | Do NOT use `any` type. Use `unknown` and narrow. |
| 测试规范 | 确保质量实践 | Unit tests go next to the file they test: `foo.ts` → `foo.test.ts` |

"Things to Avoid"节尤其强大，它预防 Claude 落入不符合项目的常见模式  [(Claude Code Prompt)](https://www.claudedirectory.org/blog/context-engineering-claude-code) 。这种否定性规范与肯定性规范结合，形成完整的约束空间。元数据块（最后更新日期、所有者、范围、审核者）帮助 AI 理解文件时效性和权威性，也便于人类维护  [(packmind.com)](https://packmind.com/context-engineering-ai-coding/context-engineering-best-practices/) 。

#### 4.1.4 跨会话不变性：压缩与清理的免疫特权

`CLAUDE.md` 享有特殊的上下文地位：在自动压缩和手动 `/clear` 操作中，其内容被重新加载，确保跨会话的规范一致性  [(justin3go.com)](https://justin3go.com/en/posts/2026/04/09-context-compaction-in-codex-claude-code-and-opencode) 。这种"免疫特权"基于设计意图——项目规范不应随对话历史被压缩丢弃，而应作为持久基础设施存在。唯一的例外是技能列表：压缩后技能列表不自动恢复，需用户重新激活或模型按需发现  [(Powered Pentest Tool.)](https://www.penligent.ai/hackinglabs/inside-claude-code-the-architecture-behind-tools-memory-hooks-and-mcp/) 。

免疫特权的实现机制是物理分离：`CLAUDE.md` 存储于磁盘，每次请求时重新读取，而非保存在可变上下文中  [(Code With Mukesh)](https://codewithmukesh.com/blog/anatomy-claude-code-session/) 。这种"外部引用"模式使其免受上下文修剪的影响，但也意味着其内容变更（如团队更新规范）会立即生效，无需重启会话。用户被建议保持 `CLAUDE.md` 的精简："CLAUDE.md 中的每个 token 都是无法用于对话的 token"，仅记录每会话必需的内容，而非项目的全部信息  [(Claude Fast)](https://claudefa.st/blog/guide/mechanics/context-management) 。

### 4.2 动态上下文：运行时信息与状态

#### 4.2.1 代码库实时状态：文件系统、当前工作目录、最近访问文件

动态上下文的核心是代码库的实时状态。Claude Code 作为终端原生 agent，直接读取本地文件系统，获取当前工作目录、文件树结构、最近访问文件、Git 状态等信息  [(mindstudio.ai)](https://www.mindstudio.ai/blog/codex-vs-claude-code-2026/) 。这种"环境即上下文"的设计使 agent 能够感知与人类开发者相同的操作环境，实现真正的结对编程体验。与云端沙箱方案（如 Codex）不同，本地执行避免了代码上传的延迟与隐私风险，但也要求 agent 具备处理本地环境复杂性的能力（如系统特定路径、环境变量、本地服务依赖）。

最近访问文件的追踪在压缩后的状态重建中尤为关键。Claude Code 的自动重读机制在压缩后读取最多 5 个最近编辑文件，总预算 50K tokens，单个文件上限 5K tokens  [(justin3go.com)](https://justin3go.com/en/posts/2026/04/09-context-compaction-in-codex-claude-code-and-opencode) 。这一设计确保 agent 在"遗忘"大部分历史后，仍掌握当前工作对象的最新内容——文件可能在压缩间隔期间被外部编辑（如用户在其他编辑器中修改），自动重读同步了这些变更。

#### 4.2.2 会话内动态注入：Hooks 的 `additionalContext` 与 `systemMessage` 输出

Hooks 是 Claude Code 的高级扩展机制，允许在特定生命周期事件点执行自定义脚本，其输出可动态扩展 agent 上下文  [(Claude Code Docs)](https://code.claude.com/docs/en/hooks) 。关键钩子类型包括：

| Hook 类型 | 触发时机 | 上下文调整能力 | 典型应用 |
|:---|:---|:---|:---|
| `PreToolUse` | 工具调用前 | 修改参数、阻止调用、注入额外上下文 | 安全警告、合规检查 |
| `PostToolUse` | 工具调用后 | 处理结果、触发后续动作、更新状态 | 结果分析、自动重试 |
| `UserPromptSubmit` | 用户消息提交时 | 扩展查询、附加背景、预处理 | 查询增强、意图识别 |
| `PreCompact` | 压缩前 | 自定义压缩逻辑、保护特定信息 | 关键数据保留 |
| `PostCompact` | 压缩后 | 状态恢复、验证完整性 | 上下文重建 |

`additionalContext` 和 `systemMessage` 输出允许 Hooks 向当前上下文动态注入信息。例如，`UserPromptExpansion` hook 可自动将"修复登录问题"扩展为包含当前分支、最近相关提交和已知问题的结构化查询  [(Claude Code Docs)](https://code.claude.com/docs/en/hooks) 。`PostCompact` hook 在多 agent 场景中尤为重要，可注入团队状态警告："[!] POST-COMPACT TEAM CHECK: Context was compacted... There are 5 LIVE agents from before compaction..."，防止压缩后 orchestrator 错误地重复创建 agent  [(GitHub Gist)](https://gist.github.com/sigalovskinick/e2e329bb37ecc74b9f15d5ba74ee1ee5) 。

#### 4.2.3 条件上下文加载：基于工作目录、最近文件、用户显式指示的项目切换

多项目工作流中的上下文管理需要条件加载机制。当前行为是 `/add-dir` 累积所有上下文，导致项目间模式冲突和认知过载  [(Github)](https://github.com/anthropics/claude-code/issues/4689) 。社区提出的改进方案是动态上下文切换：`Active Context = Global CLAUDE.md + Current Project CLAUDE.md`，基于当前工作目录、最近访问文件、用户显式指示自动检测活跃项目，切换时卸载前一项目上下文、加载新项目上下文、保留全局上下文  [(Github)](https://github.com/anthropics/claude-code/issues/4689) 。

这种条件加载将"始终全部可用"转变为"按需精确可用"，显著减少多项目场景下的上下文污染。`.claude/rules/*.md` 文件支持按文件路径模式匹配激活，例如仅在编辑 `server/api/` 目录下的文件时加载后端特定规则  [(eventuallymaking.io)](https://eventuallymaking.io/p/ai-s-impact-on-the-state-of-the-art-in-software-engineering-in-2026) 。这种"上下文随工作焦点自动调整"的能力，避免了在所有会话中加载所有项目规则的低效，实现了上下文与任务焦点的精准对齐。

### 4.3 记忆层次与持久化机制

#### 4.3.1 三层记忆架构：用户级、项目级、动态导入级

Claude Code 的记忆系统采用分层设计，平衡跨会话连续性与上下文隔离  [(aayushmnit.com)](https://aayushmnit.com/posts/2026-01-24_ClaudeCode/ClaudeCode.html) ：

| 层级 | 载体 | 范围 | 持久性 | 更新频率 | 典型内容 |
|:---|:---|:---|:---|:---|:---|
| 用户级 | `~/.claude/` | 全局跨项目 | 跨会话持久 | ~24 小时自动合成 | 职业、语言偏好、常用工具、沟通风格 |
| 项目级 | `.claude/memory/` | 项目隔离 | 跨会话持久 | 会话内实时 | 项目特定规范、技术栈、团队约定、调试洞察 |
| 动态导入级 | `.claude/rules/*.md` | 路径条件激活 | 会话内临时 | 按需加载 | 模块特定规则、临时参考文档、技能定义 |

用户级记忆通过"记忆合成"自动处理对话，每约 24 小时提取长期价值信息，存储于记忆配置文件并自动加载到未来对话  [(shareuhack.com)](https://www.shareuhack.com/en/posts/claude-memory-feature-guide-2026) 。项目级记忆每个 Project 拥有独立的隔离记忆空间，偏好和上下文仅适用于该 Project 内的对话。动态导入级支持从 ChatGPT、Gemini、Grok 迁移记忆，处理约需 24 小时  [(shareuhack.com)](https://www.shareuhack.com/en/posts/claude-memory-feature-guide-2026) 。

#### 4.3.2 跨会话学习：Memory Tool 的文件化笔记系统（`/memories` 目录）

Claude 4 系列模型引入的 Memory Tool 支持通过文件化系统在外部存储和查询信息  [(Anthropic)](https://www.anthropic.com/engineering/effective-context-engineering-for-ai-agents) 。Agent 可构建随时间积累的知识库，跨会话维护项目状态，引用先前工作而无需全部保留在上下文中。具体实现上，Claude Code 在 `/memories` 目录维护结构化笔记，包含任务进度、关键决策、待办事项、学习到的用户偏好  [(aayushmnit.com)](https://aayushmnit.com/posts/2026-01-24_ClaudeCode/ClaudeCode.html) 。

Auto Memory 的工作流程具有清晰的阶段性  [(shareuhack.com)](https://www.shareuhack.com/en/posts/claude-memory-feature-guide-2026) ：
- **观察阶段**：在会话中，Claude 注意用户修正中的偏好模式（如"总是使用单引号"、"优先使用函数组件"）
- **记录阶段**：主动将观察到的模式写入 `~/.claude/projects/<project>/memory/MEMORY.md`
- **加载阶段**：下次会话自动加载前 200 行或 25KB
- **应用阶段**：遇到类似场景时直接应用已学模式，无需重新推导

例如，在代码审查场景中，Claude 在会话 1 识别出多线程修改 `self.results` 的竞争条件，将线程安全模式存储于 `/memories/concurrency_patterns/thread_safety.md`，在会话 2 审查异步代码时直接应用该模式  [(Claude Docs)](https://platform.claude.com/cookbook/tool-use-memory-cookbook) 。这种设计将人类"记笔记-复习-应用"的学习循环形式化为机器可执行的机制。

当前实现存在 200 行/25KB 的索引上限，超出后底部条目被静默截断，导致主题文件孤立  [(Github)](https://github.com/anthropics/claude-code/issues/40614) 。社区已提出自平衡树结构作为改进方案，通过按类型分组和层级分裂实现可扩展的记忆组织  [(Github)](https://github.com/anthropics/claude-code/issues/40614) 。

#### 4.3.3 上下文编辑策略：工具使用清理与思考管理

上下文编辑是主动管理信息时效性的高级机制。Claude 4 模型原生支持两种上下文编辑策略  [(Claude Docs)](https://platform.claude.com/cookbook/tool-use-memory-cookbook) ：

| 策略 | 机制 | 功能 | 配置方式 |
|:---|:---|:---|:---|
| `clear_tool_uses_20250919` | 工具使用清理 | 自动在上下文增长时清除旧的工具结果 | 可配置触发条件和保留策略 |
| `clear_thinking_20251015` | 思考管理 | 在启用扩展思考模式时管理思考块 | 可配置保留策略 |

这些策略具有可配置的触发条件和保留策略，允许开发者根据应用场景定制压缩行为。与 Claude Code 的客户端压缩不同，这些模型原生的上下文编辑发生在 API 层面，可能对模型内部的状态维护有更精细的控制。

2026 年 4 月的质量事件揭示了思考管理的重要性：缓存优化 bug 导致推理历史被错误清除，造成"遗忘、重复和奇怪的工具选择"  [(Anthropic)](https://www.anthropic.com/engineering/april-23-postmortem) 。修复后的系统更加谨慎地保护推理历史的完整性，确保跨轮次的决策一致性。这一事件凸显了上下文编辑的微妙平衡——过度清理导致信息丢失，清理不足导致上下文膨胀，找到正确的阈值是持续的工程挑战。

### 4.4 条件上下文与作用域控制

#### 4.4.1 子代理上下文隔离：专用工具集与独立上下文的委派模式

子代理（Sub-agents）是解决上下文膨胀问题的架构性方案。Claude Code 从单代理演进为多代理架构，2026 年 1 月实验性引入 **Agent Teams** 模式  [(株式会社オブライト)](https://www.oflight.co.jp/en/columns/claude-code-complete-guide-2026) 。该模式包含：

| 角色 | 功能 | 上下文特征 |
|:---|:---|:---|
| **Team Lead** | 分析任务，分配子任务 | 完整项目上下文，全局视图 |
| **Teammate Agents** | 独立上下文执行子任务 | 任务特定上下文，干净启动 |
| **集成验证** | Team Lead 汇总结果 | 结果摘要，非完整上下文 |

子代理拥有专用工具集——例如，安全审计子代理可访问静态分析工具但无文件写入权限，UI 实现子代理可访问浏览器测试工具但无数据库操作权限。这种隔离不仅是上下文空间的隔离，更是能力边界和安全边界的隔离。结合 Git worktrees，子代理可实现并行工作流：后端 API 代理、前端组件代理、集成测试代理同时运行，完成后合并结果  [(来源)](https://alexmerced.blog/blog/2026/2026-03-07-context-management-strategies-for-openai-codex-a-complete-guide-across-browser-cli-and-app.html) 。

**上下文隔离的实现方式**：
- **Forked 执行**（`context: fork`）：完全隔离，无对话历史访问，结果摘要返回。技能元数据中的 `context: fork` 字段触发此模式  [(batsov.com)](https://batsov.com/articles/2026/03/11/essential-claude-code-skills-and-commands/) 
- **专用工具集**：子代理可配置不同的 `allowed-tools`，实现最小权限原则
- **模型差异化**：Team Lead 使用 Opus 进行规划，Teammate 使用 Sonnet 执行，实现"大脑-双手"分离  [(稀土掘金)](https://juejin.cn/post/7627035938669756451) 

然而，需要审慎评估的是上下文隔离的实际程度：Claude Code 的子代理目前仍与主代理共享相同的工具集  [(EclipseSource)](https://eclipsesource.com/blogs/2026/01/22/mcp-context-overload/) ，这与真正独立的上下文存在差距。完全隔离的子代理应当拥有专用的工具集和独立的上下文窗口，仅通过结构化的任务描述和结果返回与主代理交互——这种设计在 Theia AI 等系统中已有演示  [(EclipseSource)](https://eclipsesource.com/blogs/2026/01/22/mcp-context-overload/) 。

#### 4.4.2 模式命令（Mode Commands）：debug-mode、expert-mode、review-mode 的行为切换

模式命令通过批量激活相关技能实现行为模式的快速切换。Claude Code 内置的 `/debug`、`/review`、`/simplify` 等技能实质是预定义的行为模式，每种模式注入不同的指令集与评估标准  [(batsov.com)](https://batsov.com/articles/2026/03/11/essential-claude-code-skills-and-commands/) 。用户也可创建自定义模式，如 `expert-mode` 注入深度技术审查清单，`security-mode` 注入安全审计规范。模式切换不仅改变提示内容，还可能调整模型选择（如审查任务自动切换至更严谨的模型）、工具权限（如安全模式禁用网络工具）和输出格式（如调试模式强制结构化错误报告）。

模式命令的元数据控制通过技能的 `mode` 字段实现。当设置为 `true` 时，技能在技能列表的"模式命令"特殊区域展示，区别于普通实用技能  [(leehanchung.github.io)](https://leehanchung.github.io/blogs/2025/10/26/claude-skills-deep-dive/) 。这种 UI 层面的显著性反映了其功能层面的重要性：模式命令建立特定的操作上下文或工作流，其影响是全局性的，持续生效直至新的模式命令覆盖或会话结束。与单次技能调用的临时性形成对比，模式切换是"环境切换"——类似于 IDE 的透视图（Perspective）切换，整体工作空间重新配置。

#### 4.4.3 权限与沙箱边界：允许/拒绝规则对上下文可见性的动态调控

权限系统通过允许/拒绝规则动态调控上下文可见性。Claude Code 的七级权限模式（Permission Modes）决定 agent 可尝试的操作范围  [(Powered Pentest Tool.)](https://www.penligent.ai/hackinglabs/inside-claude-code-the-architecture-behind-tools-memory-hooks-and-mcp/) ：

| 权限级别 | 行为特征 | 上下文影响 |
|:---|:---|:---|
| 建议模式（Ask） | 每项操作人工确认 | 工具保留但需授权，上下文完整可见 |
| 自动编辑模式（Auto Edit） | 允许文件修改，禁止命令执行 | 编辑工具直接可用，Bash 工具需确认 |
| 完全自动模式（Full Auto） | 信任验证后放开限制 | 全部工具可用，上下文无额外过滤 |

这些规则不仅控制 agent 能执行什么操作，也间接控制其能"看到"什么上下文——被禁止的工具从上下文中不可见，模型甚至不会考虑调用它们。沙箱边界进一步隔离文件系统访问、网络访问、环境变量可见性，形成"上下文中的上下文"——agent 在受限视角内推理，无法感知或利用边界外的资源。

多层独立安全层（权限规则、PreToolUse hooks、自动模式分类器、可选 shell 沙箱）并行应用，任何一层均可阻断动作  [(arXiv.org)](https://arxiv.org/pdf/2604.14228) 。这种"可见性即安全性"的设计防止敏感信息（如 `.env` 文件中的密钥）意外进入模型上下文，即使模型被诱导请求此类信息。安全敏感的 Hooks 可以配置为：当 Claude 尝试读取 `.env` 文件或凭证存储时，自动拦截并在上下文中替换为警告信息，而非原始内容。

### 4.5 上下文工程的最佳实践框架

#### 4.5.1 四支柱模型：技能、上下文工程、工具设计最佳实践、子代理配置

综合 Anthropic 官方文档和社区实践，有效的 agent 上下文管理建立在四支柱之上  [(aayushmnit.com)](https://aayushmnit.com/posts/2026-01-24_ClaudeCode/ClaudeCode.html) ：

| 支柱 | 核心功能 | 上下文效益 | 关键机制 |
|:---|:---|:---|:---|
| **技能系统** | 封装可复用的领域知识 | 按需加载，避免前置膨胀 | 声明式定义、自动发现、动态注入 |
| **上下文工程** | 优化信息组织与压缩 | 最大化信号密度，最小化噪声 | 渐进压缩、缓存优化、结构化摘要 |
| **工具设计最佳实践** | 确保高效的结果格式 | 降低处理负担，提高决策精度 | 模式清晰、延迟加载、结果分层 |
| **子代理配置** | 实现任务分解与隔离 | 控制单窗口复杂度，防止污染 | 独立上下文、专用工具、模型差异化 |

四支柱相互支撑：技能提供知识模块，上下文工程决定模块的组装方式，工具设计影响注入内容的格式与体积，子代理配置控制模块的作用域边界。2026 年的核心趋势是从"提示工程"向"上下文工程"的范式转移—— leverage point 已从"说什么"转向"加载什么"，系统提示、文件、记忆、示例的结构比任务措辞本身更重要  [(The AI Corner)](https://www.the-ai-corner.com/p/claude-best-practices-power-user-guide-2026) 。

#### 4.5.2 任务分块策略：研究-计划-实施三阶段与 80/20 规则

HumanLayer 团队提出的"频繁有意压缩"（Frequent Intentional Compaction）工作流将上下文管理融入整个开发流程，保持利用率在 40%-60% 范围  [(humanlayer.dev)](https://www.humanlayer.dev/blog/advanced-context-engineering) 。具体实施分为三阶段：

| 阶段 | 核心目标 | 输出产物 | 上下文管理要点 |
|:---|:---|:---|:---|
| **研究（Session 1）** | 理解代码库，定位相关组件 | `SPEC.md` 规范文档 | 广泛收集，容忍噪音；创建结构化研究文件 |
| **计划（Session 2）** | 构建变更路线图，明确测试策略 | `PLAN.md` 实施计划 | 生成可审查的实施计划，确保清晰正确 |
| **实施（Session 3+）** | 编写和审查代码 | 代码变更 + 测试 | 保持窗口效率 < 40%，标记完成任务，聚焦剩余工作 |

80/20 规则指导上下文投资：将 80% 的上下文预算投入当前核心任务，保留 20% 用于相关背景；避免为"以防万一"加载大量可能无关的上下文。这种分块与预算分配策略，使得 agent 能够在有限上下文内处理远超窗口大小的复杂项目。

Dex 提出的"研究-计划-实施"方法论强调：在研究阶段投入时间创建结构化的"研究文件"，记录关键文件名、行号和系统概览；在计划阶段生成明确的实施路线图，包含文件变更、代码片段和测试策略；在实施阶段保持上下文窗口效率（建议低于 40% 容量），并在关键检查点引入人工审查  [(latitude.so)](https://latitude.so/blog/context-engineering-guide-coding-agents) 。这种"分而治之"策略的代价是会话间的显式信息传递（通常通过 CLAUDE.md 或人工摘要），但收益是避免了长会话中累积噪声的干扰。

#### 4.5.3 交接笔记（Handoff Notes）：跨会话连续性的人工保障机制

尽管自动压缩和记忆系统日益完善，显式的人工交接仍是最可靠的跨会话知识传递方式。实践模式包括：会话结束前要求 agent 生成结构化交接笔记，记录已完成工作、关键决策、待解决问题、下次会话建议起点；将笔记保存至 `decisions.md` 或 `context-handoff.md`；新会话启动时首先读取该笔记  [(mindstudio.ai)](https://www.mindstudio.ai/blog/context-rot-ai-coding-agents-how-to-prevent/) 。

Claude Code 的 `/transfer-context` 技能将此流程自动化：会话退化时生成结构化交接文件，包含已完成工作、开放决策、需避免的陷阱和相关文件路径；新会话读取该文件后，在全新上下文中接续工作，保留了关键信息而排除了对话膨胀  [(DataCamp)](https://www.datacamp.com/tutorial/claude-code-best-practices) 。这种"人机协同"的记忆管理，结合了自动系统的便利性与人类判断的准确性，是当前技术条件下最稳健的跨会话连续性方案。

更自动化的变体是**决策日志（decisions.md）**和 **context-handoff.md**：每会话结束更新，记录完成内容、决策和下一步起点，下会话优先读取  [(mindstudio.ai)](https://www.mindstudio.ai/blog/context-rot-ai-coding-agents-how-to-prevent/) 。对于追求更高自动化的团队，Remy 等 spec-driven 方法在架构层面规范了交接格式与验证流程，减少了对手动纪律的依赖  [(mindstudio.ai)](https://www.mindstudio.ai/blog/context-rot-ai-coding-agents-how-to-prevent/) 。

高级用户甚至可以建立系统化的交接模板，涵盖架构决策、已建立模式、当前进度、以及已知陷阱，形成项目特定的"上下文恢复协议"。这种"文档与清除"（Document & Clear）模式的核心洞察是：重新开始往往比修复退化上下文中的错误更为高效——保存状态、运行 30 分钟、接受或重新开始，这种"老虎机"式策略避免了在沉没成本中持续投入  [(anthropic.com)](https://www-cdn.anthropic.com/58284b19e702b49db9302d5b6f135ad8871e7658.pdf) 。

