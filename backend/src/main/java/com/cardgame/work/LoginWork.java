package com.cardgame.work;

import com.cardgame.dao.GameRankDao;
import com.cardgame.dao.UserDao;
import com.cardgame.entity.GameRank;
import com.cardgame.entity.StaticUserData;
import com.cardgame.entity.User;
import com.cardgame.work.model.LoginResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * 业务层 Work：登录业务。
 *
 * <p>职责：接收前端（小程序）传来的账号密码，作为条件到 user 表匹配，
 * 判断该用户是否存在、密码是否正确，并返回布尔判定结果（命中时附带用户信息）。</p>
 *
 * <p>本类只做业务判断，数据库访问全部交给 DAO 层（{@link UserDao} / {@link GameRankDao}）。</p>
 *
 * <p><b>静态数据回退：</b>数据库连不上时（账号密码没配、MySQL 没启动），
 * 自动回退到 {@link StaticUserData} 的内置测试账号，保证前端流程能先跑通。
 * 由 {@code card.use-static-user} 开关控制（默认 true），数据库配好后设为 false 即可。</p>
 */
@Service
public class LoginWork {

    private static final Logger log = LoggerFactory.getLogger(LoginWork.class);

    private final UserDao userDao;
    private final GameRankDao gameRankDao;

    /** 是否允许回退到静态用户数据 */
    @Value("${card.use-static-user:true}")
    private boolean useStaticUser;

    public LoginWork(UserDao userDao, GameRankDao gameRankDao) {
        this.userDao = userDao;
        this.gameRankDao = gameRankDao;
    }

    /**
     * 只判断用户是否存在：账号存在且密码匹配返回 true，否则 false。
     *
     * @param userAccount 登录账号
     * @param userPwd     登录密码
     */
    public boolean exists(String userAccount, String userPwd) {
        if (isBlank(userAccount) || isBlank(userPwd)) {
            return false;
        }
        User user = findUser(userAccount);
        return user != null && userPwd.equals(user.getUserPwd());
    }

    /**
     * 登录校验：返回布尔判定结果，命中用户时同时带回 uid、用户名、段位、积分、仓库编号。
     *
     * @param userAccount 登录账号
     * @param userPwd     登录密码
     */
    public LoginResult login(String userAccount, String userPwd) {
        if (isBlank(userAccount) || isBlank(userPwd)) {
            return LoginResult.fail("账号和密码不能为空");
        }
        User user = findUser(userAccount);
        if (user == null) {
            return LoginResult.fail("账号不存在");
        }
        if (!userPwd.equals(user.getUserPwd())) {
            return LoginResult.fail("密码错误");
        }
        // 段位表里查全称，如 game_level = 4 → 山精三段
        GameRank rank = findRank(user.getGameLevel());
        return LoginResult.ok(user, rank == null ? null : rank.getFullName());
    }

    /**
     * 按账号查用户：先查数据库，失败或查不到时回退静态数据。
     */
    private User findUser(String userAccount) {
        try {
            User user = userDao.findByAccount(userAccount.trim());
            if (user != null) {
                return user;
            }
        } catch (Exception e) {
            log.warn("数据库查询失败，回退到静态用户数据: {}", e.getMessage());
        }
        return useStaticUser ? StaticUserData.findByAccount(userAccount) : null;
    }

    /**
     * 按段位编号查段位：失败时返回 null（只影响段位名称展示，不影响登录成败）。
     */
    private GameRank findRank(Integer gameLevel) {
        if (gameLevel == null) {
            return null;
        }
        try {
            return gameRankDao.selectById(gameLevel);
        } catch (Exception e) {
            log.warn("段位查询失败，段位名称将不显示: {}", e.getMessage());
            return null;
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
