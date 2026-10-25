package com.cardgame.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 注册请求。
 */
public class RegisterDTO {

    @NotBlank(message = "不能为空")
    @Size(min = 3, max = 20, message = "长度需在 3-20 之间")
    private String username;

    @NotBlank(message = "不能为空")
    @Size(min = 6, max = 20, message = "长度需在 6-20 之间")
    private String password;

    /** 昵称，不填默认用用户名 */
    private String nickname;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }
}
