package com.cardgame.work;

import com.cardgame.dao.GameRankDao;
import com.cardgame.dao.UserDao;
import com.cardgame.entity.GameRank;
import com.cardgame.entity.User;
import com.cardgame.work.model.LoginResult;
import org.springframework.stereotype.Service;

/**
 * 业务层 Work：登录业务。
 *
 * <p>职责：接收前端（小程序）传来的账号密码，作为条件到 user 表匹配，
 * 判断该用户是否存在、密码是否正确，并返回布尔判定结果（命中时附带用户信息）。</p>
 *
 * <p>本类只做业务判断，数据库访问全部交给 DAO 层（{@link UserDao} / {@link GameRankDao}）。</p>
 */
@Service
public class LoginWork {

    private final UserDao userDao;
    private final GameRankDao gameRankDao;

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
        User user = userDao.findByAccount(userAccount.trim());
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
        User user = userDao.findByAccount(userAccount.trim());
        if (user == null) {
            return LoginResult.fail("账号不存在");
        }
        if (!userPwd.equals(user.getUserPwd())) {
            return LoginResult.fail("密码错误");
        }
        // 段位表里查全称，如 game_level = 4 → 山精三段
        GameRank rank = gameRankDao.selectById(user.getGameLevel());
        return LoginResult.ok(user, rank == null ? null : rank.getFullName());
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
