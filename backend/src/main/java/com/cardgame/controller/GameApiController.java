package com.cardgame.controller;

import com.cardgame.common.PageResult;
import com.cardgame.common.Result;
import com.cardgame.dao.CardDao;
import com.cardgame.dao.GameRankDao;
import com.cardgame.dao.PassiveSkillDao;
import com.cardgame.dao.UserDao;
import com.cardgame.dao.WarehouseDao;
import com.cardgame.dto.DeckSaveDTO;
import com.cardgame.entity.Card;
import com.cardgame.entity.GameRank;
import com.cardgame.entity.PassiveSkill;
import com.cardgame.entity.User;
import com.cardgame.entity.Warehouse;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 统一对外接口类：前端与后端之间的唯一通道。
 *
 * <p><b>设计要点</b>：</p>
 * <ul>
 *     <li>前端<b>不直接连接</b>任何后端 Java 类，只认 HTTP 地址 + JSON 格式。
 *         后端内部怎么实现（Work / DAO / SQL）对前端完全透明。</li>
 *     <li>需要登录时，前端 POST 一段 JSON 到 {@code /api/user/login}，
 *         后端内部调用 {@code LoginWork} 完成校验，再把结果包成统一 JSON 返回。
 *         业务实现改了前端也不用动。</li>
 *     <li>所有响应统一为 {@code {code, msg, data, timestamp, success}}，
 *         前端只需判断 {@code code === 0}。</li>
 * </ul>
 *
 * <p>路径前缀 {@code /api} 由 application.yml 的 server.servlet.context-path 统一配置，
 * 本类只声明相对路径（与 {@link LoginController} 保持一致，不要再加 /api 前缀）。</p>
 *
 * <p>登录接口在 {@link LoginController}，本类负责除登录外的其它功能。</p>
 */
@RestController
public class GameApiController {

    private final UserDao userDao;
    private final CardDao cardDao;
    private final GameRankDao gameRankDao;
    private final PassiveSkillDao passiveSkillDao;
    private final WarehouseDao warehouseDao;

    public GameApiController(UserDao userDao, CardDao cardDao, GameRankDao gameRankDao,
                              PassiveSkillDao passiveSkillDao, WarehouseDao warehouseDao) {
        this.userDao = userDao;
        this.cardDao = cardDao;
        this.gameRankDao = gameRankDao;
        this.passiveSkillDao = passiveSkillDao;
        this.warehouseDao = warehouseDao;
    }

    // ================================================================
    //  用户
    // ================================================================

    /** 用户列表（分页） */
    @GetMapping("/user/list")
    public Result<PageResult<User>> listUsers(@RequestParam(defaultValue = "1") int page,
                                              @RequestParam(defaultValue = "20") int size) {
        return Result.ok(userDao.selectPage(page, size));
    }

    /** 用户详情 */
    @GetMapping("/user/{uid}")
    public Result<User> getUser(@PathVariable Integer uid) {
        User user = userDao.selectById(uid);
        if (user == null) {
            return Result.fail(Result.NOT_FOUND, "用户不存在");
        }
        return Result.ok(user);
    }

    /** 按段位查用户 */
    @GetMapping("/user/by-level")
    public Result<List<User>> usersByLevel(@RequestParam Integer gameLevel) {
        return Result.ok(userDao.findByGameLevel(gameLevel));
    }

    /** 积分排行榜 */
    @GetMapping("/user/ranking")
    public Result<List<User>> ranking(@RequestParam(defaultValue = "20") int limit) {
        return Result.ok(userDao.findTopByScore(limit));
    }

    /** 删除用户 */
    @DeleteMapping("/user/{uid}")
    public Result<Void> deleteUser(@PathVariable Integer uid) {
        userDao.deleteById(uid);
        return Result.ok();
    }

    // ================================================================
    //  卡牌
    // ================================================================

    /** 卡牌列表（可按品级过滤：黄 / 玄） */
    @GetMapping("/card/list")
    public Result<List<Card>> listCards(@RequestParam(required = false) String cLevel) {
        if (cLevel == null || cLevel.isBlank()) {
            return Result.ok(cardDao.selectAll("id ASC"));
        }
        return Result.ok(cardDao.findByLevel(cLevel));
    }

    /** 卡牌详情 */
    @GetMapping("/card/{id}")
    public Result<Card> getCard(@PathVariable Integer id) {
        Card card = cardDao.selectById(id);
        if (card == null) {
            return Result.fail(Result.NOT_FOUND, "卡牌不存在");
        }
        return Result.ok(card);
    }

    /** 按名称查卡牌 */
    @GetMapping("/card/by-name")
    public Result<Card> cardByName(@RequestParam String cName) {
        return Result.ok(cardDao.findByName(cName));
    }

    /** 新增卡牌 */
    @PostMapping("/card")
    public Result<Card> addCard(@RequestBody Card card) {
        cardDao.insert(card);
        return Result.ok(card);
    }

    /** 删除卡牌 */
    @DeleteMapping("/card/{id}")
    public Result<Void> deleteCard(@PathVariable Integer id) {
        cardDao.deleteById(id);
        return Result.ok();
    }

    /** 复合型卡牌（is_compound = 1，同时有主动和被动技能） */
    @GetMapping("/card/compound")
    public Result<List<Card>> compoundCards() {
        return Result.ok(cardDao.findCompoundCards());
    }

    /** 克制某张卡的卡牌列表 */
    @GetMapping("/card/{id}/countered-by")
    public Result<List<Card>> counteredBy(@PathVariable Integer id) {
        return Result.ok(cardDao.findCounterTo(id));
    }

    /** 克制链（沿 counter_id 追踪） */
    @GetMapping("/card/{id}/counter-chain")
    public Result<List<Card>> counterChain(@PathVariable Integer id,
                                           @RequestParam(defaultValue = "10") int maxDepth) {
        return Result.ok(cardDao.findCounterChain(id, maxDepth));
    }

    /** 带指定被动技能的卡牌 */
    @GetMapping("/card/by-passive")
    public Result<List<Card>> cardsByPassive(@RequestParam String passiveCode) {
        return Result.ok(cardDao.findByPassiveCode(passiveCode));
    }

    // ================================================================
    //  被动技能
    // ================================================================

    /** 被动技能列表（可按效果类型过滤：A/D/H/R/N/B/C/E） */
    @GetMapping("/skill/list")
    public Result<List<PassiveSkill>> listSkills(@RequestParam(required = false) String effectType) {
        if (effectType == null || effectType.isBlank()) {
            return Result.ok(passiveSkillDao.selectAll("code ASC"));
        }
        return Result.ok(passiveSkillDao.findByEffectType(effectType));
    }

    /** 被动技能详情 */
    @GetMapping("/skill/{code}")
    public Result<PassiveSkill> getSkill(@PathVariable String code) {
        PassiveSkill skill = passiveSkillDao.findByCode(code);
        if (skill == null) {
            return Result.fail(Result.NOT_FOUND, "被动技能不存在");
        }
        return Result.ok(skill);
    }

    /** 按作用目标查技能：S 己方 / E 对手 */
    @GetMapping("/skill/by-target")
    public Result<List<PassiveSkill>> skillsByTarget(@RequestParam String target) {
        return Result.ok(passiveSkillDao.findByTarget(target));
    }

    // ================================================================
    //  段位
    // ================================================================

    /** 段位列表（按段位顺序） */
    @GetMapping("/rank/list")
    public Result<List<GameRank>> listRanks() {
        return Result.ok(gameRankDao.findAllOrderByRankId());
    }

    /** 按积分查对应段位 */
    @GetMapping("/rank/by-score")
    public Result<GameRank> rankByScore(@RequestParam int score) {
        GameRank rank = gameRankDao.findRankByScore(score);
        if (rank == null) {
            return Result.fail(Result.NOT_FOUND, "积分未达到任何段位");
        }
        return Result.ok(rank);
    }

    /** 下一个段位 */
    @GetMapping("/rank/{rankId}/next")
    public Result<GameRank> nextRank(@PathVariable Integer rankId) {
        return Result.ok(gameRankDao.findNextRank(rankId));
    }

    // ================================================================
    //  仓库
    // ================================================================

    /** 仓库里的卡牌 id 列表 */
    @GetMapping("/warehouse/{id}/cards")
    public Result<List<Integer>> warehouseCards(@PathVariable Integer id) {
        return Result.ok(warehouseDao.findCardIds(id));
    }

    /**
     * 当前登录用户自己的仓库卡牌编号列表。
     *
     * <p>按前端传来的 uid 找到对应用户，再取其 warehouse_id 去 warehouse 表读 card_list，
     * 把逗号分隔的卡牌编号解析成列表返回。前端据此到 /photo/{id}.png 取对应图片。</p>
     */
    @GetMapping("/warehouse/mine")
    public Result<List<Integer>> myWarehouseCards(@RequestParam Integer uid) {
        User user = userDao.selectById(uid);
        if (user == null) {
            return Result.fail(Result.NOT_FOUND, "用户不存在");
        }
        Integer warehouseId = user.getWarehouseId();
        if (warehouseId == null) {
            return Result.ok(Collections.emptyList());
        }
        return Result.ok(warehouseDao.findCardIds(warehouseId));
    }

    /** 仓库详情 */
    @GetMapping("/warehouse/{id}")
    public Result<Warehouse> getWarehouse(@PathVariable Integer id) {
        Warehouse warehouse = warehouseDao.selectById(id);
        if (warehouse == null) {
            return Result.fail(Result.NOT_FOUND, "仓库不存在");
        }
        return Result.ok(warehouse);
    }

    /**
     * 读取当前登录用户的卡组编队：返回仓库全部卡牌（cardList）以及四组卡组
     * （进攻 / 防御 / 法术 / 备选）各自已放置的卡牌编号列表。
     *
     * <p>前端据此在进场时复原上次锁卡后的状态（若从未锁卡则四组为空）。</p>
     */
    @GetMapping("/warehouse/deck")
    public Result<Map<String, Object>> myDeck(@RequestParam Integer uid) {
        User user = userDao.selectById(uid);
        if (user == null) {
            return Result.fail(Result.NOT_FOUND, "用户不存在");
        }
        Integer warehouseId = user.getWarehouseId();
        if (warehouseId == null) {
            return Result.fail(Result.NOT_FOUND, "用户未绑定仓库");
        }
        Warehouse warehouse = warehouseDao.selectById(warehouseId);
        List<Integer> cardIds = warehouseDao.findCardIds(warehouseId);
        List<Map<String, Object>> cardList = new ArrayList<>();
        for (Integer cid : cardIds) {
            Card c = cardDao.selectById(cid);
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("card", cid);
            item.put("kind", cardKind(c));
            item.put("compound", c != null && c.compound());
            cardList.add(item);
        }
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("cardList", cardList);
        data.put("attack", parseCsv(warehouse == null ? null : warehouse.getAttack()));
        data.put("defense", parseCsv(warehouse == null ? null : warehouse.getDefense()));
        data.put("magic", parseCsv(warehouse == null ? null : warehouse.getMagic()));
        data.put("backup", parseCsv(warehouse == null ? null : warehouse.getBackup()));
        return Result.ok(data);
    }

    /**
     * 仓库页：返回该用户拥有的全部卡牌详情（含类型/等级，用于分类展示）
     */
    @GetMapping("/warehouse/cards")
    public Result<List<Map<String, Object>>> warehouseCardDetails(@RequestParam Integer uid) {
        User user = userDao.selectById(uid);
        if (user == null) {
            return Result.fail(Result.NOT_FOUND, "用户不存在");
        }
        Integer warehouseId = user.getWarehouseId();
        if (warehouseId == null) {
            return Result.fail(Result.NOT_FOUND, "用户未绑定仓库");
        }
        List<Integer> cardIds = warehouseDao.findCardIds(warehouseId);
        List<Map<String, Object>> cards = new ArrayList<>();
        Set<Integer> seen = new LinkedHashSet<>();
        for (Integer cid : cardIds) {
            if (!seen.add(cid)) {
                continue; // card_list 中可能重复出现同一张卡，去重展示
            }
            Card c = cardDao.selectById(cid);
            if (c == null) {
                continue;
            }
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("card", c.getId());
            item.put("cName", c.getCName());
            item.put("cLevel", c.getCLevel());
            item.put("kind", cardKind(c));
            item.put("compound", c.compound());
            item.put("hp", c.getHp());
            item.put("damageOrShield", c.getDamageOrShield());
            item.put("skillName", c.getSkillName());
            cards.add(item);
        }
        return Result.ok(cards);
    }

    /**
     * 锁卡：把四组卡组的准备结果写入 warehouse 表的 Attack / Defense / magic / backup 列。
     *
     * <p>空卡槽的校验（四组卡槽必须全部放置）由前端在点击“锁卡”时完成，
     * 后端只负责把已提交的数据落库。</p>
     */
    @PostMapping("/warehouse/deck")
    public Result<String> lockDeck(@RequestBody DeckSaveDTO dto) {
        User user = userDao.selectById(dto.getUid());
        if (user == null) {
            return Result.fail(Result.NOT_FOUND, "用户不存在");
        }
        Integer warehouseId = user.getWarehouseId();
        if (warehouseId == null) {
            return Result.fail(Result.NOT_FOUND, "用户未绑定仓库");
        }
        Warehouse warehouse = warehouseDao.selectById(warehouseId);
        if (warehouse == null) {
            return Result.fail(Result.NOT_FOUND, "仓库不存在");
        }
        warehouse.setAttack(joinCsv(dto.getAttack()));
        warehouse.setDefense(joinCsv(dto.getDefense()));
        warehouse.setMagic(joinCsv(dto.getMagic()));
        warehouse.setBackup(joinCsv(dto.getBackup()));
        warehouseDao.updateById(warehouse);
        return Result.ok("锁卡成功");
    }

    /** 把逗号分隔的编号字符串解析为列表，空串返回空列表 */
    private List<Integer> parseCsv(String csv) {
        List<Integer> ids = new ArrayList<>();
        if (csv == null || csv.trim().isEmpty()) {
            return ids;
        }
        for (String part : csv.split(",")) {
            String trimmed = part.trim();
            if (!trimmed.isEmpty()) {
                ids.add(Integer.valueOf(trimmed));
            }
        }
        return ids;
    }

    /** 把编号列表拼成逗号分隔字符串，null 返回空串 */
    private String joinCsv(List<Integer> ids) {
        if (ids == null || ids.isEmpty()) {
            return "";
        }
        List<String> parts = new ArrayList<>(ids.size());
        for (Integer id : ids) {
            parts.add(String.valueOf(id));
        }
        return String.join(",", parts);
    }

    /** 根据 card 表字段推导卡牌类型：数字伤害=attack，H 开头护盾=defense，null=法术 magic */
    private String cardKind(Card card) {
        if (card == null) return "magic";
        String dos = card.getDamageOrShield();
        if (dos == null) return "magic";
        if (card.shield()) return "defense";
        if (card.damage()) return "attack";
        return "magic";
    }

    // ================================================================
    //  健康检查
    // ================================================================

    /** 健康检查，前端可用来探测后端是否在线 */
    @GetMapping("/health")
    public Result<String> health() {
        return Result.ok("ok");
    }
}
