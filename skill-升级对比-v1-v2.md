# java-backend-architecture skill 版本对比(v1 → v2)

> 日期:2026-10-19
> v1:同日上午基于 goldlion 工程**原始状态**封装
> v2:同日随工程四轮优化(13 项修复 → 6 项重构 → 版本升级 → 测试+部署模板)**同步升级**后的版本
> 存放:源副本 `JavaArchitecture\.claude\skills\java-backend-architecture\`,用户级副本 `~\.claude\skills\java-backend-architecture\`(两处已同步为 v2,`diff` 校验一致)
> 注:v1 从未单独存档(git 亦未提交),本文档即 v1→v2 的完整变更追溯

---

## 一、两版的定位差异(本质区别)

| | v1 | v2 |
|---|---|---|
| 沉淀对象 | 工程**当时的样子**(含瑕疵:查询缓存全表读穿、编译 skip、零测试) | 工程**优化后的目标态**(修复/重构/升级/测试/部署模板全部落地) |
| 约束记录 | 无 | 显式标注踩过的坑与解冻条件:MP 3.5.11 上限、springdoc 2.8.17 覆盖、Nacos server 须 3.x、Windows bind mount 教训 |
| 质量要求 | compile 通过即可 | `mvn test` 门禁 + 自动装配约定 + 部署模板复制即用 |
| 指导意义 | "照着原来写" | "照着**改好后**的写,并且别把修好的问题改回去" |

**一句话**:v1 是快照,v2 是规范。用 v1 指导新项目,会把当天刚修掉的问题(如"先查后扣库存""无限重入队死信")原样复刻到新工程。

---

## 二、逐文件变更清单

### 1. SKILL.md(入口)

| 位置 | v1 | v2 |
|---|---|---|
| 版本标识 | 无 | v2.0 标注 + 指向本对比文档,声明"以 v2 为准" |
| Step 3(core 模块) | 仅"照蓝图生成" | 明确 core **自带 Spring 自动装配**(AutoConfiguration.imports)+ **单测基线** |
| Step 5(部署) | 手写 Dockerfile/Jenkinsfile | **复制 `deploy/` 标准模板**替换占位符 |
| Step 6(验证) | `mvn compile` 通过 | `mvn test` 全绿(**单测是门禁**)+ 启动验证 |
| 规范速查-缓存 | "默认读穿写删" | 仅 id 维度;列表/分页直查库;扣减用 `conditionalDecrease` 原子模板 |
| 规范速查-MQ | 无持久化说明 | 消息持久化 + 发送回报 |
| 规范速查-异常 | 无外泄约束 | 500 统一兜底文案,不回传原始异常 |

### 2. references/tech-stack.md

- **版本矩阵整体升级**:Boot 3.3.10→3.5.16、SC 2023.0.6→2025.0.3、SCA 2023.0.1.0→2025.0.0.0、MPJ 1.5.3→1.5.6、knife4j 4.5 + **springdoc 2.8.17 显式覆盖**、Hutool 5.8.47、Redisson 3.49.0、Druid 1.2.28、fastexcel 1.2.0、SAS 1.5.8
- **新增 MP 版本上限约束**(实测结论):MP 3.5.12+ 改包名,MPJ 全系不兼容 → MP 不可超 3.5.11,附解冻条件(MPJ 适配)
- **新增 Boot 4 暂缓结论**:SCA/knife4j 生态未就绪,不满足稳定标准
- skip 警告改写(参考工程已移除 skip,CI 可用)
- 部署节指向 `deploy/` 模板 + 单测门禁

### 3. references/base-classes.md

| 主题 | v1 描述 | v2 描述 |
|---|---|---|
| 缓存实现 | search/getOne/分页全走查询缓存 | **仅 id 维度**读穿;查询直查库;删除在**事务提交后**;空值不回写;SCAN 禁 KEYS;hash 按 TTL 续期 |
| update 语义 | 全量覆盖(null 也拷) | **部分更新**(忽略 null,内存状态与落库一致;置空走专用端点) |
| BaseEntity | — | id 由 `IdGenerator` 生成,workerId/datacenterId 可配(防容器 MAC 撞号) |
| MybatisPlusServiceImpl | — | 新增 **conditionalDecrease** 原子扣减模板;getOne 多行转业务异常 |
| GlobalExceptionHandler | "基类无注解,由启动模块注册" | **DefaultGlobalExceptionHandler 自动装配注册**;500 掩码;DuplicateKeyException 友好映射 |
| CrudController | 查空结果行为未定义 | 空 id/one 查询返回 400 业务提示 |
| Excel | 无行数限制说明 | 2 万行上限(可配可覆写)+ 不做整表二次拷贝 |
| MQ 生产 | CorrelationData 自动 ID | **PERSISTENT** + confirm 回报(异常/nack 打日志) |
| 死信监听 | 异常重新入队 | **丢弃 + 消息体落日志**(重入队会无限循环且不累计 x-death);禁止手动 close channel |
| OAuth2 | 仅 access_token 参数 | **Authorization Bearer 头优先**,参数兼容 |

### 4. references/conventions.md

- **新增"慢查询与索引"节**:高频筛选字段随建表建索引、动态条件新字段同步评估、p6spy 仅 dev、禁 N+1、扣减一律 conditionalDecrease
- 缓存行:读穿写删 → 仅 id 维度
- MQ 行:生产持久化 + confirm 回报

---

## 三、v2 的优势

1. **与工程真实状态一致**:skill 触发后生成的代码 = 当前最优实现,不会把已修复的 13 类问题(缓存时序、KEYS、异常泄露、超卖风险……)带进新项目
2. **约束显式化**:MP 3.5.11 上限、springdoc 覆盖、Nacos 3.x 依赖、Boot 4 暂缓——全部是本次实测/官方求证过的结论,写成"约束 + 解冻条件",防止后人重蹈或误升
3. **质量门禁内建**:单测从"可选项"变为 scaffold 与流水线的必经步骤,skill 产出的每个新工程自带回归能力
4. **部署开箱即用**:新服务部署从"自己写 Dockerfile/Jenkinsfile"降为"复制模板改占位符"
5. **安全默认值前移**:异常掩码、注入面白名单、扣减原语、token 走 header——这些不再是"记得配"而是"框架默认"
6. **版本升级路径有记录**:Boot 3.5→4.x 的跳板关系、SCA 版本映射、Nacos client/server 兼容规则,下次升级不用重新调研

## 四、v2 的劣势/风险(如实列举)

1. **维护成本更高**:内容与具体版本号(3.5.16、2.8.17…)耦合,依赖升级后需回写 skill;"约束+解冻条件"缓解但不消除该成本
2. **上下文略增**:比 v1 约多 15% 内容,skill 触发时加载略大(已用 references 按需加载缓解,SKILL.md 本体仍精简)
3. **双副本人工同步**:源副本与用户级副本靠 `cp` + `diff` 同步,存在漂移风险;建议固定流程"改源 → cp → diff 校验"(本次已执行)
4. **语义断代**:update 部分更新、列表不缓存、死信丢弃等是**破坏性语义变化**——老代码/老习惯与 v2 冲突时以 v2 为准并回改代码,存在一次性迁移成本
5. **单测门禁的反作用**:规范要求新工程带测试,赶工期时可能被人为跳过(与 v1 无此约束相比,多了一条需要团队自律的规则)
6. **conditionalDecrease 双轨风险**:存量业务若已自写扣减逻辑,新老两种写法并存,需在代码评审中收敛

## 五、使用与维护建议

1. **源副本是唯一编辑入口**(JavaArchitecture 下),改完必须 `cp` 到用户级并 `diff` 校验。
2. 工程后续演进时,**"同步更新 skill"作为优化的收尾步骤**固定下来(本次四轮均是如此执行)。
3. 遇到 v1/v2 语义冲突:查本文档第二节对照表,以 v2 为准。
4. 版本号随里程碑推进(v2.0 → 下次结构性变更升 v3),并继续追加对比文档。

---

## 附:v2 当前文件清单

```
.claude/skills/java-backend-architecture/
├── SKILL.md                      # 入口:原则 + 搭建六步 + 规范速查(v2.0)
└── references/
    ├── tech-stack.md             # 3.5.16 版本矩阵 + MP 上限约束 + 部署模板指引
    ├── base-classes.md           # 基类蓝图(含 conditionalDecrease/缓存策略/异常/死信新语义)
    └── conventions.md            # 命名/REST/校验/事务/缓存/MQ/慢查询索引/九件套清单
```
