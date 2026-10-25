package com.cardgame.util;

import java.util.concurrent.ThreadLocalRandom;

/**
 * 通用工具：房间号生成、字符串判空等。
 */
public final class CommonUtil {

    private CommonUtil() {
    }

    /**
     * 生成 6 位数字房间号。
     */
    public static String generateRoomNo() {
        return String.valueOf(ThreadLocalRandom.current().nextInt(100000, 1000000));
    }

    /**
     * 判空：null、空字符串、纯空白都算空。
     */
    public static boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }

    /**
     * 昵称脱敏，排行榜展示用。
     */
    public static String maskNickname(String nickname) {
        if (isBlank(nickname)) {
            return "匿名玩家";
        }
        if (nickname.length() <= 1) {
            return nickname;
        }
        return nickname.charAt(0) + "**" + nickname.charAt(nickname.length() - 1);
    }
}
