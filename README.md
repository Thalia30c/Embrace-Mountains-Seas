# 卡牌竞技游戏

微信小程序 + Java 后端的卡牌对战游戏框架。

```
卡牌游戏/
├── backend/          Java 后端（Spring Boot 3.2 + MyBatis 风格自研 DAO 层）
├── miniprogram/      微信小程序前端（原生框架）
└── docs/             架构与接口文档
```

---

## 一、技术选型

| 层 | 选型 | 说明 |
|---|---|---|
| 前端 | 微信小程序原生框架 | 无需 npm 构建，开发者工具直接导入 |
| 后端 | Spring Boot 3.2.5 / Java 17 | IDEA 2025.3.1 可直接运行 |
| 持久层 | 自研 `BaseDao` 统一接口 + Spring JdbcTemplate | 所有 SQL 收敛在一处，见下方说明 |
| 数据库 | MySQL 8.0 | 账号密码自行填写 |
| 鉴权 | JWT | 拦截器统一校验 |

---

## 二、快速开始

### 1. 初始化数据库

`backend/src/main/resources/db/schema.sql` 里有完整建表语句 + 11 张初始卡牌 + 2 个测试账号。

**命令行方式：**
```bash
mysql -uroot -p < backend/src/main/resources/db/schema.sql
```

**Navicat / Workbench 方式：** 打开 `schema.sql` 文件，全选执行。

> 脚本里包含 `CREATE DATABASE IF NOT EXISTS card_game`，会自动建库。

### 2. 填写数据库账号密码

编辑 `backend/src/main/resources/application.yml`：

```yaml
spring:
  datasource:
    url: jdbc:mysql://${DB_HOST:localhost}:${DB_PORT:3306}/${DB_NAME:card_game}?...
    username: ${DB_USERNAME:root}      # ← 改成你的账号
    password: ${DB_PASSWORD:root}      # ← 改成你的密码
```

也可以不��文件，直接设环境变量 `DB_USERNAME` / `DB_PASSWORD`（IDEA 里在 Run Configuration 的 Environment variables 填）。

详见 `docs/database-config.md`。

### 3. 启动后端

**IDEA 方式（推荐）：**
1. `File` → `Open` → 选中 `D:\竞赛\卡牌游戏\backend` 目录
2. 等待 Maven 导入完成（右下角进度条）
3. 确认 Project SDK 为 JDK 17 或更高（`File` → `Project Structure` → `Project SDK`）
4. 打开 `CardGameApplication.java`，点 `main` 方法旁边的绿色三角 → `Run 'CardGameApplication'`
5. 控制台出现 `Started CardGameApplication` 即启动成功，端口 8080

**命令行方式：**
```bash
cd backend
mvn spring-boot:run
```

### 4. 运行小程序

1. 打开微信开发者工具 → `导入项目`
2. 目录选 `D:\竞赛\卡牌游戏\miniprogram`
3. AppID 选「测试号」（没有正式 AppID 也能开发调试）
4. 详情 → 本地设置 → 勾选「不校验合法域名、web-view（业务域名）、TLS 版本以及 HTTPS 证书」
5. 编译运行

**关键：** `miniprogram/utils/config.js` 里的 `BASE_URL` 要改成你后端的地址。

- 开发者工具本机调试：`http://localhost:8080/api`
- 手机真机预览：改成电脑局域网 IP，如 `http://192.168.1.100:8080/api`（IP 用 `ipconfig` 查）

---

## 三、测试账号

初始化脚本自带两个账号：

| 用户名 | 密码 | 说明 |
|---|---|---|
| admin | admin123 | 房主大佬，1000 金币 |
| guest | guest123 | 路人甲，500 金币 |

小程序登录页也可以直接注册新账号，或用「微信一键登录」。

---

## 四、核心设计：统一的数据库对外接口

这是本框架的重点。**所有数据库调用只有一个出口**，Service 层不允许直接用 `JdbcTemplate`。

```
Controller  →  Service  →  BaseDao<T>（统一对外接口）  →  BaseDaoImpl<T>  →  JdbcTemplate
                                              ↑
                              各业务 Dao 继承它，只写业务特有查询
```

**`dao/BaseDao.java` —— 全项目唯一的对外数据访问接口：**

```java
public interface BaseDao<T> {
    int insert(T entity);
    int insertBatch(List<T> entities);
    int updateById(T entity);
    int deleteById(Object id);
    int deleteBy(QueryWrapper<T> wrapper);
    T selectById(Object id);
    T selectOne(QueryWrapper<T> wrapper);
    List<T> selectList(QueryWrapper<T> wrapper);
    long count(QueryWrapper<T> wrapper);
    PageResult<T> selectPage(QueryWrapper<T> wrapper, int page, int size);
    List<T> selectBySql(String sql, Class<T> entityClass, Object... args);
}
```

**实体类用注解声明表结构，不写 SQL 字符串：**

```java
@Table("t_user")
public class User {
    @Column(id = true)  private Long id;
    @Column             private String username;   // 自动映射到 username 列
    @Column             private LocalDateTime createTime;  // → create_time
}
```

**查询条件用链式 API 构造：**

```java
QueryWrapper<User> qw = QueryWrapper.create();
qw.eq("username", "admin").like("nickname", "张").orderByDesc("win_count").limit(0, 10);
```

**安全性：** `QueryWrapper` 里用到的列名在执行前会和实体注解比对，未定义的列直接抛异常 —— 从根上杜绝 SQL 注入，不用靠字符串拼接。

**各业务 DAO 示例：**

```java
@Repository
public interface UserDao extends BaseDao<User> {
    User findByUsername(String username);          // 只声明业务特有查询
    List<User> findTopByWinCount(int limit);
}

@Repository
public class UserDaoImpl extends BaseDaoImpl<User> implements UserDao {
    public UserDaoImpl(JdbcTemplate jdbcTemplate) {
        super(jdbcTemplate, User.class);
    }
    @Override
    public User findByUsername(String username) {
        return selectOne(QueryWrapper.<User>create().eq("username", username));
    }
}
```

---

## 五、目录结构

### 后端 `backend/src/main/java/com/cardgame/`

| 包 | 职责 |
|---|---|
| `common` | 统一响应 `Result`、错误码 `ResultCode`、`BizException`、全局异常处理、分页封装 |
| `config` | 跨域配置、JWT 鉴权拦截器、拦截器注册 |
| `controller` | REST 接口，只做参数接收和结果包装 |
| `service` / `service.impl` | 业务逻辑，事务边界在这一层 |
| `dao` | **统一数据访问层**，SQL 全部在这里 |
| `entity` | 数据库实体，带 `@Table` / `@Column` 注解 |
| `dto` | 请求/响应对象 |
| `util` | JWT 工具、通用工具 |

### 小程序 `miniprogram/`

| 路径 | 职责 |
|---|---|
| `utils/request.js` | **统一请求封装**：自动带令牌、解析 `{code,msg,data}`、令牌失效跳登录 |
| `utils/api.js` | 接口清单，页面只调用这里，不直接拼 URL |
| `pages/index` | 首页入口 |
| `pages/login` | 登录注册 |
| `pages/hall` | 对战大厅、创建/加入房间 |
| `pages/room` | 房间等待 |
| `pages/battle` | 对战界面（手牌、出牌、血量） |
| `pages/deck` | 牌组配置 |
| `pages/profile` | 个人中心、排行榜 |

---

## 六、接口一览

所有接口前缀 `/api`，统一返回 `{ code, msg, data, timestamp }`，`code=0` 表示成功。

### 用户

| 方法 | 路径 | 说明 | 鉴权 |
|---|---|---|---|
| POST | `/user/login` | 用户名密码登录 | 否 |
| POST | `/user/register` | 注册 | 否 |
| POST | `/user/wxLogin` | 微信 code 登录 | 否 |
| GET | `/user/info` | 当前用户信息 | 是 |
| POST | `/user/profile` | 改昵称头像 | 是 |
| GET | `/user/ranking` | 排行榜 | 是 |

### 卡牌

| 方法 | 路径 | 说明 | 鉴权 |
|---|---|---|---|
| GET | `/card/page` | 卡牌图鉴分页（`type`/`rarity` 筛选） | 否 |
| GET | `/card/list` | 全部卡牌 | 否 |
| GET | `/card/{id}` | 卡牌详情 | 否 |
| GET | `/card/mine` | 我拥有的卡牌 | 是 |

### 牌组

| 方法 | 路径 | 说明 |
|---|---|---|
| GET | `/deck/list` | 我的牌组（含卡牌明细） |
| POST | `/deck/create` | 创建牌组 |
| DELETE | `/deck/{id}` | 删除牌组 |
| POST | `/deck/{id}/default` | 设为默认 |
| POST | `/deck/{id}/card?cardId=` | 牌组加卡 |
| DELETE | `/deck/{id}/card/{cardId}` | 牌组移除卡 |

### 房间

| 方法 | 路径 | 说明 |
|---|---|---|
| GET | `/room/list` | 大厅房间列表 |
| POST | `/room/create` | 创建房间 |
| POST | `/room/join?roomNo=&password=` | 加入房间 |
| POST | `/room/{id}/leave` | 离开房间 |
| POST | `/room/{id}/start` | 房主开局 |
| GET | `/room/{id}` | 房间详情 |

### 对战

| 方法 | 路径 | 说明 |
|---|---|---|
| GET | `/battle/{id}/state` | 对局实时状态 |
| POST | `/battle/play` | 出牌 |
| POST | `/battle/{id}/draw` | 抽牌 |
| POST | `/battle/{id}/pass` | 过牌 |
| POST | `/battle/{id}/surrender` | 认输 |
| GET | `/battle/{id}/details` | 对局回放明细 |

---

## 七、游戏规则（当前实现）

- 每位玩家初始血量 30，初始手牌 5 张
- 每回合费用上限 5 点，出牌扣除对应费用
- 攻击卡对对手造成 `attack` 伤害，防御卡回复自身血量，技能卡回复生命
- 对手血量归零即获胜，最多 30 回合后判平局
- 胜者奖励 30 金币，败者 10 金币

---

## 八、后续可扩展方向

当前框架已可跑通完整对局流程，后续可在此基础上扩展：

1. **实时对战**：把 `pages/battle` 的 2 秒轮询换成 WebSocket 推送，页面结构不用动
2. **房间状态持久化**：现在房间成员存在内存 `Map` 中，多实例部署需换成 Redis
3. **微信真实登录**：`UserServiceImpl.wxLogin` 目前把 code 当 openid 用，接开放平台时改成调 `jscode2session`
4. **卡牌效果系统**：`BattleServiceImpl.playCard` 里目前是固定规则，可抽象成策略模式
5. **匹配系统**：`room/list` 是手动建房，可加快速匹配队列
