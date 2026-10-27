package com.cardgame.controller;

import com.cardgame.common.JwtUtil;
import com.cardgame.common.Result;
import com.cardgame.dto.LoginDTO;
import com.cardgame.work.LoginWork;
import com.cardgame.work.model.LoginResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 登录接口，路径与小程序 utils/api.js 中的 /user/login 对应，
 * 完整地址：http://localhost:8080/api/user/login
 */
@RestController
@RequestMapping("/user")
public class LoginController {

    private final LoginWork loginWork;
    private final JwtUtil jwtUtil;

    public LoginController(LoginWork loginWork, JwtUtil jwtUtil) {
        this.loginWork = loginWork;
        this.jwtUtil = jwtUtil;
    }

    /**
     * 账号密码登录。
     *
     * <p>请求体：{"username":"zhangsan","password":"zhangsan123"}
     * （也兼容 userAccount / userPwd 字段名）</p>
     *
     * <p>返回：code = 0 表示账号密码匹配，data.success = true 并带回用户信息 + token；
     * code = 40101 表示不匹配，data.success = false，msg 为失败原因。</p>
     */
    @PostMapping("/login")
    public Result<LoginResult> login(@RequestBody LoginDTO dto) {
        LoginResult result = loginWork.login(dto.getUsername(), dto.getPassword());
        if (result.isSuccess()) {
            // 登录成功，签发 JWT 令牌一并返回给小程序
            result.setToken(jwtUtil.generateToken(result.getUserId(), result.getUserAccount()));
            return Result.ok("登录成功", result);
        }
        return Result.fail(Result.LOGIN_FAIL, result.getMessage(), result);
    }
}
