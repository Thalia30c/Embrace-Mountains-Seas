package com.cardgame.service;

import com.cardgame.dto.LoginDTO;
import com.cardgame.dto.LoginVO;
import com.cardgame.dto.RegisterDTO;
import com.cardgame.dto.WxLoginDTO;
import com.cardgame.entity.User;

/**
 * 认证与用户服务。
 */
public interface UserService {

    /** 用户名密码登录 */
    LoginVO login(LoginDTO dto);

    /** 注册并直接返回登录态 */
    LoginVO register(RegisterDTO dto);

    /** 微信小程序 code 登录（未接微信后台时按 code 生成测试账号） */
    LoginVO wxLogin(WxLoginDTO dto);

    /** 查询用户详情 */
    User getById(Long userId);

    /** 更新昵称头像 */
    User updateProfile(Long userId, String nickname, String avatar);

    /** 排行榜 */
    java.util.List<User> ranking(int limit);
}
