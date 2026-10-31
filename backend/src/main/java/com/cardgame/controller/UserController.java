package com.cardgame.controller;

import com.cardgame.common.JwtUtil;
import com.cardgame.common.Result;
import com.cardgame.dao.GameRankDao;
import com.cardgame.dao.UserDao;
import com.cardgame.entity.GameRank;
import com.cardgame.entity.User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 用户中心接口：供小程序个人中心页读取当前登录用户信息。
 *
 * <p>鉴权方式：前端在请求头携带 {@code Authorization: Bearer <token>}，
 * token 为 {@link com.cardgame.common.JwtUtil} 登录时签发的 JWT，
 * 解析出 uid 后到 user 表查最新数据返回。</p>
 *
 * <p>token 缺失 / 无效 / 过期统一返回 code = 40002，
 * 小程序 utils/request.js 收到后会清除登录态并跳回登录页。</p>
 */
@RestController
@RequestMapping("/user")
public class UserController {

    private final UserDao userDao;
    private final GameRankDao gameRankDao;
    private final JwtUtil jwtUtil;

    public UserController(UserDao userDao, GameRankDao gameRankDao, JwtUtil jwtUtil) {
        this.userDao = userDao;
        this.gameRankDao = gameRankDao;
        this.jwtUtil = jwtUtil;
    }

    /**
     * 当前登录用户信息（个人中心数据源）。
     *
     * <p>返回：userId、userAccount、nickname、avatar、coins、score、
     * gameLevel、rank（段位全称）、warehouseId。</p>
     */
    @GetMapping("/info")
    public Result<Map<String, Object>> info(
            @RequestHeader(value = "Authorization", required = false) String authorization) {

        Integer uid = resolveUid(authorization);
        if (uid == null) {
            return Result.fail(Result.TOKEN_INVALID, "登录已失效，请重新登录");
        }

        User user;
        try {
            user = userDao.selectById(uid);
        } catch (Exception e) {
            // 数据库异常按令牌失效处理，让前端回到登录页，避免个人中心显示服务器出错
            return Result.fail(Result.TOKEN_INVALID, "登录已失效，请重新登录");
        }
        if (user == null) {
            return Result.fail(Result.TOKEN_INVALID, "用户不存在，请重新登录");
        }

        // 段位全称查询失败不影响主体信息返回
        String rankName = null;
        try {
            GameRank rank = gameRankDao.selectById(user.getGameLevel());
            rankName = rank == null ? null : rank.getFullName();
        } catch (Exception ignored) {
            // 段位名称仅用于展示
        }

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("userId", user.getUid());
        data.put("userAccount", user.getUserAccount());
        data.put("nickname", user.getUsername());
        data.put("avatar", "");
        // user 表暂无金币字段，金币暂取现有积分（score）
        data.put("coins", user.getScore());
        data.put("score", user.getScore());
        data.put("gameLevel", user.getGameLevel());
        data.put("rank", rankName);
        data.put("warehouseId", user.getWarehouseId());
        return Result.ok(data);
    }

    /** 从 Authorization 头解析 token 并取出 uid，失败返回 null */
    private Integer resolveUid(String authorization) {
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            return null;
        }
        return jwtUtil.parseUid(authorization.substring(7).trim());
    }

    /**
     * 上报对局积分变化（战斗结算时调用，写入 user 表）。
     *
     * <p>入参：{@code { "uid": 1, "delta": 30 }}，delta 为正加分、为负扣分，
     * 积分不会小于 0。</p>
     *
     * <p>返回用于段位可视化的全部数据：
     * prevScore / score、prevLevel / level、prevRank / rank（段位全称）、
     * nextRank（下一级段位名）、toNext（还差多少分升段）、
     * progress（本段位内进度 0~100）、changed（up / down / same）。</p>
     */
    @PostMapping("/score")
    public Result<Map<String, Object>> updateScore(@RequestBody Map<String, Object> body) {
        Integer uid = toInt(body == null ? null : body.get("uid"));
        if (uid == null) {
            return Result.fail(Result.PARAM_ERROR, "缺少 uid");
        }

        User user;
        try {
            user = userDao.selectById(uid);
        } catch (Exception e) {
            return Result.fail(Result.TOKEN_INVALID, "登录已失效，请重新登录");
        }
        if (user == null) {
            return Result.fail(Result.NOT_FOUND, "用户不存在");
        }

        Integer delta = toInt(body.get("delta"));
        int prevScore = user.getScore() == null ? 0 : user.getScore();
        int nextScore = Math.max(0, prevScore + (delta == null ? 0 : delta));

        GameRank prevRank = findRank(prevScore);
        GameRank newRank = findRank(nextScore);
        int prevLevel = prevRank == null ? 1 : prevRank.getRankId();
        int newLevel = newRank == null ? prevLevel : newRank.getRankId();

        user.setScore(nextScore);
        user.setGameLevel(newLevel);
        try {
            userDao.updateById(user);
        } catch (Exception e) {
            return Result.fail(Result.TOKEN_INVALID, "登录已失效，请重新登录");
        }

        // 升段进度：本段门槛 → 下一段门槛
        GameRank curRank = findRank(nextScore);
        GameRank upRank = nextRankOf(newLevel);
        int base = curRank == null ? 0 : curRank.getNeedTotalScore();
        int target = upRank == null ? base : upRank.getNeedTotalScore();
        int progress = upRank == null ? 100
                : (int) Math.round((nextScore - base) * 100.0 / Math.max(1, target - base));

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("prevScore", prevScore);
        data.put("score", nextScore);
        data.put("prevLevel", prevLevel);
        data.put("level", newLevel);
        data.put("prevRank", prevRank == null ? null : prevRank.getFullName());
        data.put("rank", curRank == null ? null : curRank.getFullName());
        data.put("nextRank", upRank == null ? "已是最高段位" : upRank.getFullName());
        data.put("nextNeed", target);
        data.put("toNext", upRank == null ? 0 : Math.max(0, target - nextScore));
        data.put("progress", Math.max(0, Math.min(100, progress)));
        data.put("changed", newLevel > prevLevel ? "up" : (newLevel < prevLevel ? "down" : "same"));
        return Result.ok(data);
    }

    /** 按积分反查段位（走 game_rank.need_total_score，失败返回 null 不影响主流程） */
    private GameRank findRank(int score) {
        try {
            return gameRankDao.findRankByScore(score);
        } catch (Exception ignored) {
            return null;
        }
    }

    /** 下一级段位，已是最高段位返回 null */
    private GameRank nextRankOf(int level) {
        try {
            return gameRankDao.findNextRank(level);
        } catch (Exception ignored) {
            return null;
        }
    }

    /** 前端传来的是数字或字符串，这里统一转成 Integer，无法转换返回 null */
    private Integer toInt(Object value) {
        if (value == null) {
            return null;
        }
        try {
            return Integer.valueOf(String.valueOf(value).trim());
        } catch (Exception e) {
            return null;
        }
    }
}
