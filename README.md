# 拥抱山海 · 卡牌竞技游戏

微信小程序 + Java 后端的山海主题卡牌对战游戏。

```
Embrace-Mountains-Seas/
├── backend/                Java 后端（Spring Boot 3.2 + 自研 BaseDao 通用数据访问层）
├── miniprogram/            微信小程序前端（原生框架）
└── mountains_and_seas/     数据库 SQL 脚本 + 数据库设计文档（database-design.md）
```

---

## 一、技术选型

| 层 | 选型 | 说明 |
|---|---|---|
| 前端 | 微信小程序原生框架 | 无需 npm 构建，开发者工具直接导入 |
| 后端 | Spring Boot 3.2.5 / Java 17 | IDEA 或 `mvn spring-boot:run` 直接运行 |
| 持久层 | 自研 `BaseDao` 通用接口 + Spring JdbcTemplate | 所有 SQL 收敛在一处，见第四节 |
| 数据库 | MySQL 8.0，库名 `mountains_and_seas` | 账号密码在 `application.yml` 填写 |
| 鉴权 | JWT | 登录后签发，`card.jwt` 配置 |

---

## 二、快速开始

### 1. 初始化数据库

SQL 脚本在项目根目录 `mountains_and_seas/` 下，按顺序执行：

| 脚本 | 内容 |
|---|---|
| `mountains_and_seas_user.sql` | 用户表 + 测试用户 |
| `mountains_and_seas_warehouse.sql` | 仓库表 |
| `mountains_and_seas_card.sql` | 卡牌表 + 24 张山海卡牌（含克制关系） |
| `mountains_and_seas_passive_skill.sql` | 被动技能表 |
| `mountains_and_seas_game_rank.sql` | 段位表 |
| `add_allcards_player.sql` | 补充数据 |

**命令行方式：**
```bash
mysql -uroot -p < mountains_and_seas/mountains_and_seas_user.sql
# 依次执行其余脚本
```

**Navicat / Workbench 方式：** 逐个打开脚本文件，全选执行。

> 数据表结构说明见 `mountains_and_seas/database-design.md`。

### 2. 填写数据库账号密码

编辑 `backend/src/main/resources/application.yml`：

```yaml
spring:
  datasource:
    url: jdbc:mysql://${DB_HOST:localhost}:${DB_PORT:3306}/${DB_NAME:mountains_and_seas}?...
    username: ${DB_USERNAME:root}      # ← 改成你的账号
    password: ${DB_PASSWORD:xxx}       # ← 改成你的密码
```

也可以不改文件，直接设环境变量 `DB_USERNAME` / `DB_PASSWORD`。

### 3. 启动后端

**IDEA 方式（推荐）：**
1. `File` → `Open` → 选中 `Embrace-Mountains-Seas/backend` 目录
2. 等待 Maven 导入完成
3. 确认 Project SDK 为 JDK 17 或更高
4. 运行 `CardGameApplication.java`，控制台出现 `Started CardGameApplication` 即成功，端口 8080

**命令行方式：**
```bash
cd backend
mvn spring-boot:run
```

启动后可访问 `http://localhost:8080/api/health` 验证。

### 4. 运行小程序

1. 微信开发者工具 → `导入项目` → 目录选 `Embrace-Mountains-Seas/miniprogram`
2. AppID 选「测试号」即可
3. 详情 → 本地设置 → 勾选「不校验合法域名…」

**后端地址配置：** `miniprogram/utils/config.js` 的 `HOSTS` 数组里维护了一组候选主机，小程序启动时会自动逐个探测，第一个连通的地址生效。

- 开发者工具本机调试：列表里有 `127.0.0.1` 兜底，无需改动
- 手机真机预览：把电脑当前局域网 IP（`ipconfig` 查看）加进 `HOSTS` 列表最前面，手机与电脑连同一 Wi-Fi

---

## 三、测试账号

数据库初始化脚本与静态兜底数据（`card.use-static-user: true` 时生效）内置：

| 用户名 | 密码 | 说明 |
|---|---|---|
| zhangsan | zhangsan123 | 张三，山稚三段，积分 0 |
| lisi | lisi456 | 李四，山精二段，积分 220 |
| wangwu | wangwu789 | 王五，山巫二段，积分 700 |

登录页也可以直接注册新账号。

> 静态用户开关说明：数据库不可用时登录自动回退到这批内存账号（重启即恢复初始状态）；数据库配好后建议保持 `false` 只走真实数据。

---

## 四、核心设计

### 1. 分层结构

```
Controller  →  Work（业务逻辑）  →  Dao  →  BaseDao / BaseDaoImpl（统一数据访问）  →  JdbcTemplate  →  MySQL
```

- `controller`：REST 接口，只做参数接收和结果包装
- `work`：业务逻辑（如 `LoginWork` 处理登录校验、静态用户回退）
- `dao`：自研通用数据访问层，SQL 全部收敛在这里

### 2. 统一数据访问层 `BaseDao`

实体类用 `@Table` / `@Column` 注解声明表结构，查询条件用链式 `QueryWrapper` 构造，列名执行前与实体注解比对、未定义的列直接抛异常，从根上杜绝 SQL 注入。各业务 Dao（`UserDao`、`CardDao` 等）继承它，只补充业务特有查询。

### 3. 统一响应格式

所有接口统一返回 `{ code, msg, data, timestamp }`，`code=0` 表示成功；异常由 `GlobalExceptionHandler` 统一转换为该格式。

---

## 五、目录结构

### 后端 `backend/src/main/java/com/cardgame/`

| 包 | 职责 |
|---|---|
| `common` | 统一响应 `Result`、JWT 工具、全局异常处理、分页封装 |
| `controller` | `LoginController`（登录）、`UserController`（注册/用户信息/积分）、`GameApiController`（卡牌/技能/段位/仓库/健康检查） |
| `work` | 登录等业务逻辑 |
| `dao` | **统一数据访问层**，`BaseDao` 通用接口 + 各业务 Dao |
| `entity` | 数据库实体（`User`、`Card`、`PassiveSkill`、`GameRank`、`Warehouse`、`StaticUserData`） |
| `dto` | 请求对象（`LoginDTO`、`RegisterDTO`、`DeckSaveDTO`） |

### 小程序 `miniprogram/`

| 路径 | 职责 |
|---|---|
| `utils/config.js` | 后端地址配置 + 启动时自动探测可用主机 |
| `utils/request.js` | **统一请求封装**：自动带令牌、解析 `{code,msg,data}`、令牌失效跳登录 |
| `utils/api.js` | 接口清单，页面只调用这里，不直接拼 URL |
| `utils/sync.js` | 断网兜底：对局积分变化先进本地队列，后端恢复后自动补传 |
| `pages/index` | 首页入口 |
| `pages/login` | 登录注册 |
| `pages/hall` | 对战大厅 |
| `pages/room` | 房间等待 |
| `pages/fight` | 对战 |
| `pages/battle` | 对战结算 |
| `pages/deck` | 牌组配置 |
| `pages/warehouse` | 卡牌仓库 |
| `pages/profile` | 个人中心、排行榜 |

---

## 六、接口一览

所有接口前缀 `/api`（由 `server.servlet.context-path` 提供），统一返回 `{ code, msg, data, timestamp }`。

### 用户

| 方法 | 路径 | 说明 |
|---|---|---|
| POST | `/user/login` | 用户名密码登录 |
| POST | `/user/register` | 注册 |
| GET | `/user/info` | 当前用户信息 |
| POST | `/user/score` | 上报积分变化 |
| GET | `/user/list` | 用户分页列表 |
| GET | `/user/{uid}` | 用户详情 |
| GET | `/user/by-level` | 按关卡查用户 |
| GET | `/user/ranking` | 排行榜 |
| DELETE | `/user/{uid}` | 删除用户 |

### 卡牌

| 方法 | 路径 | 说明 |
|---|---|---|
| GET | `/card/list` | 全部卡牌（可按 `cLevel` 筛选） |
| GET | `/card/{id}` | 卡牌详情 |
| GET | `/card/by-name` | 按名称查卡 |
| POST | `/card` | 新增卡牌 |
| DELETE | `/card/{id}` | 删除卡牌 |
| GET | `/card/compound` | 可合成的卡牌 |
| GET | `/card/{id}/countered-by` | 克制该卡的卡牌 |
| GET | `/card/{id}/counter-chain` | 克制链 |
| GET | `/card/by-passive` | 按被动技能查卡 |

### 被动技能

| 方法 | 路径 | 说明 |
|---|---|---|
| GET | `/skill/list` | 技能列表（可按 `effectType` 筛选） |
| GET | `/skill/{code}` | 技能详情 |
| GET | `/skill/by-target` | 按作用目标查技能 |

### 段位

| 方法 | 路径 | 说明 |
|---|---|---|
| GET | `/rank/list` | 段位列表 |
| GET | `/rank/by-score` | 按积分查对应段位 |
| GET | `/rank/{rankId}/next` | 下一阶段位 |

### 仓库 / 牌组

| 方法 | 路径 | 说明 |
|---|---|---|
| GET | `/warehouse/mine?uid=` | 我的仓库卡牌 id 列表 |
| GET | `/warehouse/{id}/cards` | 指定仓库卡牌 id 列表 |
| GET | `/warehouse/{id}` | 仓库详情 |
| GET | `/warehouse/deck?uid=` | 我的出战牌组 |
| GET | `/warehouse/cards?uid=` | 仓库卡牌明细（含属性） |
| POST | `/warehouse/deck` | 保存/锁定出战牌组 |

### 其他

| 方法 | 路径 | 说明 |
|---|---|---|
| GET | `/health` | 健康检查 |

---

## 七、后续可扩展方向

1. **卡牌效果系统**：把被动技能结算抽象成策略模式，支撑更多技能组合
2. **房间状态持久化**：房间/对局状态接入数据库或 Redis，支持多实例
3. **微信真实登录**：接开放平台 `jscode2session`，替换当前的账号密码登录
4. **实时对战**：对战页面轮询换成 WebSocket 推送
