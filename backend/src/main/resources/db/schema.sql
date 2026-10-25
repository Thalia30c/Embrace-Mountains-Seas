-- ============================================================
--  卡牌竞技游戏 数据库初始化脚本
--  MySQL 8.0+  /  utf8mb4
--  使用方式：mysql -uroot -p < schema.sql
-- ============================================================

CREATE DATABASE IF NOT EXISTS `card_game`
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_general_ci;

USE `card_game`;

-- ------------------------------------------------------------ 用户表
DROP TABLE IF EXISTS `t_user`;
CREATE TABLE `t_user` (
    `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    `openid`      VARCHAR(64)           DEFAULT NULL COMMENT '微信 openid',
    `username`    VARCHAR(64)           DEFAULT NULL COMMENT '登录用户名',
    `password`    VARCHAR(128)          DEFAULT NULL COMMENT '登录密码',
    `nickname`    VARCHAR(64)           DEFAULT NULL COMMENT '昵称',
    `avatar`      VARCHAR(255)          DEFAULT NULL COMMENT '头像地址',
    `coins`       INT                   DEFAULT 0    COMMENT '金币数',
    `win_count`   INT                   DEFAULT 0    COMMENT '胜场数',
    `lose_count`  INT                   DEFAULT 0    COMMENT '负场数',
    `rank`        VARCHAR(32)           DEFAULT '青铜' COMMENT '段位',
    `banned`      TINYINT               DEFAULT 0    COMMENT '是否封禁 0正常 1封禁',
    `create_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_openid` (`openid`),
    UNIQUE KEY `uk_username` (`username`)
) ENGINE = InnoDB COMMENT ='用户表';

-- ------------------------------------------------------------ 卡牌图鉴表
DROP TABLE IF EXISTS `t_card`;
CREATE TABLE `t_card` (
    `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    `name`        VARCHAR(64)  NOT NULL COMMENT '卡牌名称',
    `description` VARCHAR(255)          DEFAULT NULL COMMENT '卡牌描述',
    `type`        VARCHAR(16)  NOT NULL DEFAULT 'attack' COMMENT '类型 attack/defense/skill',
    `rarity`      VARCHAR(16)  NOT NULL DEFAULT 'common' COMMENT '稀有度 common/rare/epic/legend',
    `attack`      INT                   DEFAULT 0    COMMENT '攻击力',
    `defense`     INT                   DEFAULT 0    COMMENT '防御力',
    `cost`        INT                   DEFAULT 1    COMMENT '费用',
    `image`       VARCHAR(255)          DEFAULT NULL COMMENT '图片地址',
    `weight`      INT                   DEFAULT 10   COMMENT '随机发牌权重',
    PRIMARY KEY (`id`),
    KEY `idx_type` (`type`),
    KEY `idx_rarity` (`rarity`)
) ENGINE = InnoDB COMMENT ='卡牌图鉴表';

-- ------------------------------------------------------------ 用户卡牌收藏表
DROP TABLE IF EXISTS `t_user_card`;
CREATE TABLE `t_user_card` (
    `id`          BIGINT   NOT NULL AUTO_INCREMENT COMMENT '主键',
    `user_id`     BIGINT   NOT NULL COMMENT '用户 id',
    `card_id`     BIGINT   NOT NULL COMMENT '卡牌 id',
    `count`       INT      NOT NULL DEFAULT 1 COMMENT '拥有数量',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '获得时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_card` (`user_id`, `card_id`),
    KEY `idx_user` (`user_id`)
) ENGINE = InnoDB COMMENT ='用户卡牌收藏表';

-- ------------------------------------------------------------ 卡组表
DROP TABLE IF EXISTS `t_deck`;
CREATE TABLE `t_deck` (
    `id`          BIGINT      NOT NULL AUTO_INCREMENT COMMENT '主键',
    `user_id`     BIGINT      NOT NULL COMMENT '所属用户 id',
    `name`        VARCHAR(64) NOT NULL COMMENT '牌组名称',
    `is_default`  TINYINT     NOT NULL DEFAULT 0 COMMENT '是否默认牌组 0否 1是',
    `create_time` DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_user` (`user_id`)
) ENGINE = InnoDB COMMENT ='卡组表';

-- ------------------------------------------------------------ 牌组卡牌关联表
DROP TABLE IF EXISTS `t_deck_card`;
CREATE TABLE `t_deck_card` (
    `id`      BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    `deck_id` BIGINT NOT NULL COMMENT '牌组 id',
    `card_id` BIGINT NOT NULL COMMENT '卡牌 id',
    `count`   INT    NOT NULL DEFAULT 1 COMMENT '卡牌数量',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_deck_card` (`deck_id`, `card_id`)
) ENGINE = InnoDB COMMENT ='牌组卡牌关联表';

-- ------------------------------------------------------------ 房间表
DROP TABLE IF EXISTS `t_room`;
CREATE TABLE `t_room` (
    `id`             BIGINT      NOT NULL AUTO_INCREMENT COMMENT '主键',
    `room_no`        VARCHAR(16) NOT NULL COMMENT '房间号',
    `owner_id`       BIGINT      NOT NULL COMMENT '房主用户 id',
    `owner_name`     VARCHAR(64)          DEFAULT NULL COMMENT '房主昵称',
    `name`           VARCHAR(64)          DEFAULT NULL COMMENT '房间名称',
    `mode`           VARCHAR(16) NOT NULL DEFAULT 'casual' COMMENT '模式 rank/casual',
    `status`         VARCHAR(16) NOT NULL DEFAULT 'waiting' COMMENT '状态 waiting/playing/finished',
    `max_player`     INT         NOT NULL DEFAULT 2 COMMENT '人数上限',
    `current_player` INT         NOT NULL DEFAULT 0 COMMENT '当前人数',
    `password`       VARCHAR(32)          DEFAULT NULL COMMENT '房间密码',
    `battle_id`      BIGINT               DEFAULT NULL COMMENT '对局 id',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_room_no` (`room_no`),
    KEY `idx_status` (`status`)
) ENGINE = InnoDB COMMENT ='房间表';

-- ------------------------------------------------------------ 对局表
DROP TABLE IF EXISTS `t_battle`;
CREATE TABLE `t_battle` (
    `id`          BIGINT      NOT NULL AUTO_INCREMENT COMMENT '主键',
    `room_id`     BIGINT      NOT NULL COMMENT '房间 id',
    `mode`        VARCHAR(16) NOT NULL DEFAULT 'casual' COMMENT '模式',
    `status`      VARCHAR(16) NOT NULL DEFAULT 'playing' COMMENT '状态 playing/finished',
    `winner_id`   BIGINT               DEFAULT NULL COMMENT '获胜者 id',
    `winner_name` VARCHAR(64)          DEFAULT NULL COMMENT '获胜者昵称',
    `rounds`      INT                  DEFAULT 0    COMMENT '回合数',
    `player_count` INT                 DEFAULT 0    COMMENT '参与人数',
    `reward`      INT                  DEFAULT 0    COMMENT '奖励金币',
    `start_time`  DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '开始时间',
    `end_time`    DATETIME             DEFAULT NULL COMMENT '结束时间',
    PRIMARY KEY (`id`),
    KEY `idx_room` (`room_id`)
) ENGINE = InnoDB COMMENT ='对局表';

-- ------------------------------------------------------------ 对局明细表
DROP TABLE IF EXISTS `t_battle_detail`;
CREATE TABLE `t_battle_detail` (
    `id`         BIGINT      NOT NULL AUTO_INCREMENT COMMENT '主键',
    `battle_id`  BIGINT      NOT NULL COMMENT '对局 id',
    `user_id`    BIGINT      NOT NULL COMMENT '操作用户 id',
    `user_name`  VARCHAR(64)          DEFAULT NULL COMMENT '操作人昵称',
    `round`      INT         NOT NULL DEFAULT 1 COMMENT '回合序号',
    `action`     VARCHAR(16) NOT NULL DEFAULT 'play' COMMENT '动作 play/draw/skill/pass',
    `card_id`    BIGINT               DEFAULT NULL COMMENT '卡牌 id',
    `card_name`  VARCHAR(64)          DEFAULT NULL COMMENT '卡牌名称',
    `score`      INT                  DEFAULT 0    COMMENT '本步得分',
    `create_time` DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_battle` (`battle_id`)
) ENGINE = InnoDB COMMENT ='对局明细表';

-- ============================================================
--  初始化基础数据：卡牌图鉴 + 测试账号
-- ============================================================
INSERT INTO `t_card` (`name`, `description`, `type`, `rarity`, `attack`, `defense`, `cost`, `weight`) VALUES
('烈焰斩',    '造成 30 点火焰伤害',         'attack',  'rare',   30, 0,  3, 10),
('寒冰箭',    '造成 15 点伤害并降低攻速',   'attack',  'common', 15, 0,  2, 12),
('破甲击',    '无视对方 50% 防御力',        'attack',  'epic',   25, 0,  4,  8),
('雷霆一击',  '高概率造成暴击',             'attack',  'legend', 45, 0,  6,  4),
('石肤术',    '获得 25 点护甲',             'defense', 'common', 0,  25, 2, 12),
('圣光壁垒',  '获得 40 点护甲并回复生命',   'defense', 'rare',   0,  40, 4,  9),
('不屈意志',  '抵挡一次致命伤害',           'defense', 'epic',   0,  30, 5,  6),
('闪电链',    '同时攻击所有敌方角色',       'skill',   'rare',   20, 0,  3,  8),
('治疗术',    '回复 30 点生命值',           'skill',   'common', 0,  0,  3, 11),
('时间静止',  '跳过对手下一回合',           'skill',   'legend', 0,  0,  8,  3),
('嘲讽',      '强制对手下一回合只能攻击自己','skill',  'epic',   0,  15, 4,  7);

INSERT INTO `t_user` (`username`, `password`, `nickname`, `coins`, `rank`) VALUES
('admin', 'admin123', '房主大佬', 1000, '黄金'),
('guest', 'guest123', '路人甲',   500, '青铜');

INSERT INTO `t_user_card` (`user_id`, `card_id`, `count`) VALUES
(1, 1, 5), (1, 2, 5), (1, 5, 5), (1, 6, 3), (1, 9, 3),
(2, 1, 3), (2, 3, 2), (2, 7, 2), (2, 10, 1);
