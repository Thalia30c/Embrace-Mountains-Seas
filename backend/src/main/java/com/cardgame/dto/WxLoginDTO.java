package com.cardgame.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * 微信小程序登录请求：携带 wx.login 拿到的 code。
 */
public class WxLoginDTO {

    @NotBlank(message = "不能为空")
    private String code;

    /** 昵称，可选 */
    private String nickname;

    /** 头像，可选 */
    private String avatar;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }
}
