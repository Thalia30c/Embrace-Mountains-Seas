-- ============================================================
--  新增一个「拥有全部 1~24 号卡牌」的测试玩家
--  用法：mysql -u root -p -P 3307 < add_allcards_player.sql
--  特性：可重复执行（已存在则跳过，不会产生重复数据）
-- ============================================================
USE mountains_and_seas;

-- 1) 新建仓库：拥有全部 24 张卡（1,2,...,24）
SET @ALL_CARDS = '1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,23,24';

INSERT INTO warehouse (card_list, Attack, Defense, magic, backup)
SELECT @ALL_CARDS, '', '', '', ''
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM warehouse WHERE card_list = @ALL_CARDS);

-- 2) 新建玩家，绑定上面那个仓库
SET @WID = (SELECT warehouse_id FROM warehouse WHERE card_list = @ALL_CARDS ORDER BY warehouse_id LIMIT 1);

INSERT INTO user (username, user_account, user_pwd, game_level, score, warehouse_id)
SELECT '全卡君', 'allcards', 'allcards123', 1, 0, @WID
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM user WHERE user_account = 'allcards');

-- 3) 校验输出
SELECT u.uid, u.username, u.user_account, u.user_pwd, u.game_level, u.score,
       w.warehouse_id, w.card_list
FROM user u JOIN warehouse w ON u.warehouse_id = w.warehouse_id
WHERE u.user_account = 'allcards';

SELECT COUNT(*) AS 拥有卡数
FROM card c
WHERE FIND_IN_SET(c.id,
  (SELECT card_list FROM user u JOIN warehouse w ON u.warehouse_id = w.warehouse_id
   WHERE u.user_account = 'allcards')) > 0;
