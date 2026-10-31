package com.cardgame.dto;

import com.fasterxml.jackson.annotation.JsonAlias;

/**
 * 注册入参，对应小程序 api.register(userAccount, userPwd, nickname) 的请求体。
 *
 * <p>请求体：{"username":"zhangsan","password":"123456","nickname":"张三"}
 * 其中 username 即登录账号 user_account，nickname 即用户名 username。</p>
 */
public class RegisterDTO {

    /** 登录账号，兼容 userAccount / user_account / account */
    @JsonAlias({"userAccount", "user_account", "account"})
    private String username;

    /** 登录密码，兼容 userPwd / user_pwd */
    @JsonAlias({"userPwd", "user_pwd"})
    private String password;

    /** 用户名（昵称），兼容 nickName / user_name */
    @JsonAlias({"nickName", "user_name"})
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
