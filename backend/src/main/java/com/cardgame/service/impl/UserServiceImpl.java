package com.cardgame.service.impl;

import com.cardgame.common.BizException;
import com.cardgame.common.ResultCode;
import com.cardgame.dao.QueryWrapper;
import com.cardgame.dao.UserDao;
import com.cardgame.dto.LoginDTO;
import com.cardgame.dto.LoginVO;
import com.cardgame.dto.RegisterDTO;
import com.cardgame.dto.WxLoginDTO;
import com.cardgame.entity.StaticUserData;
import com.cardgame.entity.User;
import com.cardgame.service.UserService;
import com.cardgame.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 用户服务实现。
 *
 * <p><b>两种运行模式，由 card.use-static-user 开关控制：</b></p>
 * <ul>
 *     <li><b>静态模式（默认，true）</b>：数据只来自 {@link StaticUserData} 的内存存储，
 *         <b>完全不访问数据库</b>。数据库还没配好时，用它把前端流程先跑通。</li>
 *     <li><b>数据库模式（false）</b>：全部走真实 MySQL 数据。</li>
 * </ul>
 *
 * <p>两条路径互斥，静态模式下不会产生任何数据库连接。</p>
 */
@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserDao userDao;

    @Autowired
    private JwtUtil jwtUtil;

    /**
     * 是否使用静态用户数据。
     * true：只用内存数据，不访问数据库；false：只用数据库。
     */
    @Value("${card.use-static-user:true}")
    private boolean useStaticUser;

    @Override
    public LoginVO login(LoginDTO dto) {
        User user = findByUsername(dto.getUsername());
        if (user == null || !dto.getPassword().equals(user.getPassword())) {
            throw new BizException(ResultCode.LOGIN_FAILED);
        }
        checkBanned(user);
        return buildLoginVO(user);
    }

    @Override
    public LoginVO register(RegisterDTO dto) {
        if (findByUsername(dto.getUsername()) != null) {
            throw new BizException(ResultCode.USER_EXISTS);
        }
        User user = new User();
        user.setUsername(dto.getUsername());
        user.setPassword(dto.getPassword());
        user.setNickname(dto.getNickname() == null || dto.getNickname().isBlank()
                ? dto.getUsername() : dto.getNickname());
        user.setCoins(100);
        user.setWinCount(0);
        user.setLoseCount(0);
        user.setRank("青铜");
        user.setBanned(0);
        user.setCreateTime(LocalDateTime.now());
        user.setUpdateTime(LocalDateTime.now());
        save(user);
        return buildLoginVO(user);
    }

    @Override
    public LoginVO wxLogin(WxLoginDTO dto) {
        // 演示工程：未接入微信开放平台时，把 code 当作 openid 使用
        // 正式接入时改为调用 https://api.weixin.qq.com/sns/jscode2session 换取 openid
        String openid = "wx_" + dto.getCode();
        User user = findByOpenid(openid);
        if (user == null) {
            user = new User();
            user.setOpenid(openid);
            user.setNickname(dto.getNickname() == null || dto.getNickname().isBlank()
                    ? "玩家" + openid.substring(openid.length() - 4) : dto.getNickname());
            user.setAvatar(dto.getAvatar());
            user.setCoins(100);
            user.setWinCount(0);
            user.setLoseCount(0);
            user.setRank("青铜");
            user.setBanned(0);
            user.setCreateTime(LocalDateTime.now());
            user.setUpdateTime(LocalDateTime.now());
            save(user);
        } else {
            if (dto.getNickname() != null && !dto.getNickname().isBlank()) {
                user.setNickname(dto.getNickname());
            }
            if (dto.getAvatar() != null && !dto.getAvatar().isBlank()) {
                user.setAvatar(dto.getAvatar());
            }
            user.setUpdateTime(LocalDateTime.now());
            update(user);
        }
        return buildLoginVO(user);
    }

    @Override
    public User getById(Long userId) {
        User user = findById(userId);
        if (user == null) {
            throw new BizException(ResultCode.NOT_FOUND, "用户不存在");
        }
        return user;
    }

    @Override
    public User updateProfile(Long userId, String nickname, String avatar) {
        User user = getById(userId);
        if (nickname != null && !nickname.isBlank()) {
            user.setNickname(nickname);
        }
        if (avatar != null && !avatar.isBlank()) {
            user.setAvatar(avatar);
        }
        user.setUpdateTime(LocalDateTime.now());
        update(user);
        return user;
    }

    @Override
    public List<User> ranking(int limit) {
        int size = limit <= 0 ? 20 : Math.min(limit, 100);
        List<User> list = findAll();
        list.sort((a, b) -> {
            int win = Integer.compare(
                    b.getWinCount() == null ? 0 : b.getWinCount(),
                    a.getWinCount() == null ? 0 : a.getWinCount());
            if (win != 0) {
                return win;
            }
            return Integer.compare(
                    b.getCoins() == null ? 0 : b.getCoins(),
                    a.getCoins() == null ? 0 : a.getCoins());
        });
        return list.size() > size ? list.subList(0, size) : list;
    }

    // ------------------------------------------------------------------ 数据访问（两种模式互斥）

    /** 按用户名查用户 */
    private User findByUsername(String username) {
        if (username == null) {
            return null;
        }
        if (useStaticUser) {
            return StaticUserData.findByUsername(username);
        }
        return userDao.findByUsername(username);
    }

    /** 按 openid 查用户 */
    private User findByOpenid(String openid) {
        if (openid == null) {
            return null;
        }
        if (useStaticUser) {
            return StaticUserData.findByOpenid(openid);
        }
        return userDao.findByOpenid(openid);
    }

    /** 按 id 查用户 */
    private User findById(Long id) {
        if (id == null) {
            return null;
        }
        if (useStaticUser) {
            return StaticUserData.findById(id);
        }
        return userDao.selectById(id);
    }

    /** 查全部用户 */
    private List<User> findAll() {
        if (useStaticUser) {
            return StaticUserData.listAll();
        }
        List<User> list = userDao.selectList(QueryWrapper.<User>create().orderByDesc("win_count"));
        return list == null ? new ArrayList<>() : list;
    }

    /** 新增用户 */
    private void save(User user) {
        if (useStaticUser) {
            StaticUserData.add(user);
            return;
        }
        userDao.insert(user);
    }

    /** 更新用户 */
    private void update(User user) {
        if (useStaticUser) {
            StaticUserData.update(user);
            return;
        }
        userDao.updateById(user);
    }

    // ------------------------------------------------------------------ 内部工具

    private void checkBanned(User user) {
        if (user.getBanned() != null && user.getBanned() == 1) {
            throw new BizException(ResultCode.FORBIDDEN, "账号已被封禁");
        }
    }

    private LoginVO buildLoginVO(User user) {
        LoginVO vo = new LoginVO();
        vo.setUserId(user.getId());
        vo.setToken(jwtUtil.generateToken(user.getId()));
        vo.setExpireSeconds(jwtUtil.getExpireSeconds());
        vo.setNickname(user.getNickname());
        vo.setAvatar(user.getAvatar());
        vo.setCoins(user.getCoins());
        vo.setRank(user.getRank());
        return vo;
    }
}
