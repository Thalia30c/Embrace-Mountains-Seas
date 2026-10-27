package com.cardgame.dto;

import com.fasterxml.jackson.annotation.JsonAlias;

/**
 * 登录入参，对应小程序 api.login(username, password) 的请求体。
 *
 * <p>同时兼容后端的字段命名，传 userAccount / userPwd 或 username / password 都能解析。</p>
 */
public class LoginDTO {

    /** 登录账号，兼容 userAccount / user_account / account 三种字段名 */
    @JsonAlias({"userAccount", "user_account", "account"})
    private String username;

    /** 登录密码，兼容 userPwd / user_pwd / password 三种字段名 */
    @JsonAlias({"userPwd", "user_pwd"})
    private String password;

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
}
