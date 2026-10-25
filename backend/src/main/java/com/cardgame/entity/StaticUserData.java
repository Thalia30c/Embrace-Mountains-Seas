package com.cardgame.entity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 静态用户数据（内存存储）。
 *
 * <p>数据库还没配好时，用它提供测试账号，让前端能先把登录流程跑起来。
 * 开启后 <b>完全不访问数据库</b>，数据只存在于本进程内存中，重启即恢复初始状态。</p>
 *
 * <p>内置账号：</p>
 * <ul>
 *     <li>admin / admin123 —— 房主账号</li>
 *     <li>guest / guest123 —— 普通账号</li>
 * </ul>
 */
public final class StaticUserData {

    private StaticUserData() {
    }

    /** 内存中的用户列表 */
    private static final List<User> USERS = new ArrayList<>();

    /** 新增用户时用的 id 序列 */
    private static final AtomicLong ID_SEQ = new AtomicLong(2);

    static {
        USERS.add(build(1L, "admin", "admin123", "房主大佬", 1000, "黄金", 12, 3));
        USERS.add(build(2L, "guest", "guest123", "路人甲", 500, "青铜", 3, 8));
    }

    /**
     * 构造一个用户。
     */
    private static User build(Long id, String username, String password, String nickname,
                              int coins, String rank, int winCount, int loseCount) {
        User user = new User();
        user.setId(id);
        user.setUsername(username);
        user.setPassword(password);
        user.setNickname(nickname);
        user.setCoins(coins);
        user.setRank(rank);
        user.setWinCount(winCount);
        user.setLoseCount(loseCount);
        user.setBanned(0);
        user.setCreateTime(LocalDateTime.now());
        user.setUpdateTime(LocalDateTime.now());
        return user;
    }

    /**
     * 复制一份用户对象，避免外部改动影响内存数据。
     */
    private static User copyOf(User source) {
        User clone = new User();
        clone.setId(source.getId());
        clone.setOpenid(source.getOpenid());
        clone.setUsername(source.getUsername());
        clone.setPassword(source.getPassword());
        clone.setNickname(source.getNickname());
        clone.setAvatar(source.getAvatar());
        clone.setCoins(source.getCoins());
        clone.setWinCount(source.getWinCount());
        clone.setLoseCount(source.getLoseCount());
        clone.setRank(source.getRank());
        clone.setBanned(source.getBanned());
        clone.setCreateTime(source.getCreateTime());
        clone.setUpdateTime(source.getUpdateTime());
        return clone;
    }

    /**
     * 取出全部用户（副本）。
     */
    public static synchronized List<User> listAll() {
        List<User> copy = new ArrayList<>();
        for (User user : USERS) {
            copy.add(copyOf(user));
        }
        return copy;
    }

    /**
     * 按用户名查找，找不到返回 null。
     */
    public static synchronized User findByUsername(String username) {
        if (username == null) {
            return null;
        }
        for (User user : USERS) {
            if (username.equals(user.getUsername())) {
                return copyOf(user);
            }
        }
        return null;
    }

    /**
     * 按 openid 查找，找不到返回 null。
     */
    public static synchronized User findByOpenid(String openid) {
        if (openid == null) {
            return null;
        }
        for (User user : USERS) {
            if (openid.equals(user.getOpenid())) {
                return copyOf(user);
            }
        }
        return null;
    }

    /**
     * 按 id 查找，找不到返回 null。
     */
    public static synchronized User findById(Long id) {
        if (id == null) {
            return null;
        }
        for (User user : USERS) {
            if (id.equals(user.getId())) {
                return copyOf(user);
            }
        }
        return null;
    }

    /**
     * 用户名是否已存在。
     */
    public static synchronized boolean existsByUsername(String username) {
        return findByUsername(username) != null;
    }

    /**
     * 新增一个用户到内存中，返回存入后的对象。
     */
    public static synchronized User add(User user) {
        if (user.getId() == null) {
            user.setId(ID_SEQ.incrementAndGet());
        } else {
            ID_SEQ.updateAndGet(cur -> Math.max(cur, user.getId()));
        }
        USERS.add(user);
        return copyOf(user);
    }

    /**
     * 更新内存中的用户信息，按 id 匹配。
     */
    public static synchronized void update(User user) {
        for (int i = 0; i < USERS.size(); i++) {
            if (USERS.get(i).getId().equals(user.getId())) {
                USERS.set(i, copyOf(user));
                return;
            }
        }
    }
}
