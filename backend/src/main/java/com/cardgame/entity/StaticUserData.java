package com.cardgame.entity;

import java.util.ArrayList;
import java.util.List;

/**
 * 静态用户数据（内存存储）。
 *
 * <p>数据库还没配好账号密码时，用它提供测试账号，让小程序能先把登录流程跑通。
 * 开启后 <b>完全不访问数据库</b>，数据只存在于本进程内存中，重启即恢复初始状态。</p>
 *
 * <p>开关：{@code card.use-static-user=true}（application.yml），默认开启。
 * 数据库配好后设为 false，即完全走真实数据。</p>
 *
 * <p>内置账号与 {@code resources/db/schema.sql} 中的初始数据一致：</p>
 * <ul>
 *     <li>zhangsan / zhangsan123 —— 张三，山稚三段，积分 0</li>
 *     <li>lisi / lisi456 —— 李四，山精二段，积分 220</li>
 *     <li>wangwu / wangwu789 —— 王五，山巫二段，积分 700</li>
 * </ul>
 */
public final class StaticUserData {

    private StaticUserData() {
    }

    /** 内存中的用户列表 */
    private static final List<User> USERS = new ArrayList<>();

    static {
        USERS.add(build(1, "张三", "zhangsan", "zhangsan123", 1, 0, 1));
        USERS.add(build(2, "李四", "lisi", "lisi456", 3, 220, 2));
        USERS.add(build(3, "王五", "wangwu", "wangwu789", 6, 700, 3));
    }

    /** 构造一个用户 */
    private static User build(Integer uid, String username, String userAccount, String userPwd,
                              Integer gameLevel, Integer score, Integer warehouseId) {
        User user = new User();
        user.setUid(uid);
        user.setUsername(username);
        user.setUserAccount(userAccount);
        user.setUserPwd(userPwd);
        user.setGameLevel(gameLevel);
        user.setScore(score);
        user.setWarehouseId(warehouseId);
        return user;
    }

    /** 复制一份用户，避免外部改动影响内存数据 */
    private static User copyOf(User source) {
        User clone = new User();
        clone.setUid(source.getUid());
        clone.setUsername(source.getUsername());
        clone.setUserAccount(source.getUserAccount());
        clone.setUserPwd(source.getUserPwd());
        clone.setGameLevel(source.getGameLevel());
        clone.setScore(source.getScore());
        clone.setWarehouseId(source.getWarehouseId());
        return clone;
    }

    /** 全部用户（副本） */
    public static synchronized List<User> listAll() {
        List<User> copy = new ArrayList<>();
        for (User user : USERS) {
            copy.add(copyOf(user));
        }
        return copy;
    }

    /** 按登录账号查找，找不到返回 null */
    public static synchronized User findByAccount(String userAccount) {
        if (userAccount == null) {
            return null;
        }
        String target = userAccount.trim();
        for (User user : USERS) {
            if (target.equals(user.getUserAccount())) {
                return copyOf(user);
            }
        }
        return null;
    }

    /** 按 id 查找，找不到返回 null */
    public static synchronized User findById(Integer uid) {
        if (uid == null) {
            return null;
        }
        for (User user : USERS) {
            if (uid.equals(user.getUid())) {
                return copyOf(user);
            }
        }
        return null;
    }
}
