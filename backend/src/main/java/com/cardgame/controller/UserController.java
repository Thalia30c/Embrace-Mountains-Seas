package com.cardgame.controller;

import com.cardgame.common.Result;
import com.cardgame.dto.LoginDTO;
import com.cardgame.dto.LoginVO;
import com.cardgame.dto.RegisterDTO;
import com.cardgame.dto.WxLoginDTO;
import com.cardgame.entity.User;
import com.cardgame.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 用户与认证接口。
 */
@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

    /** 用户名密码登录 */
    @PostMapping("/login")
    public Result<LoginVO> login(@Valid @RequestBody LoginDTO dto) {
        return Result.ok(userService.login(dto));
    }

    /** 注册 */
    @PostMapping("/register")
    public Result<LoginVO> register(@Valid @RequestBody RegisterDTO dto) {
        return Result.ok(userService.register(dto));
    }

    /** 微信小程序登录 */
    @PostMapping("/wxLogin")
    public Result<LoginVO> wxLogin(@Valid @RequestBody WxLoginDTO dto) {
        return Result.ok(userService.wxLogin(dto));
    }

    /** 获取当前登录用户信息 */
    @GetMapping("/info")
    public Result<User> info(@RequestAttribute("userId") Long userId) {
        return Result.ok(userService.getById(userId));
    }

    /** 修改昵称头像 */
    @PostMapping("/profile")
    public Result<User> updateProfile(@RequestAttribute("userId") Long userId,
                                      @RequestParam(required = false) String nickname,
                                      @RequestParam(required = false) String avatar) {
        return Result.ok(userService.updateProfile(userId, nickname, avatar));
    }

    /** 排行榜 */
    @GetMapping("/ranking")
    public Result<List<User>> ranking(@RequestParam(defaultValue = "20") int limit) {
        return Result.ok(userService.ranking(limit));
    }
}
