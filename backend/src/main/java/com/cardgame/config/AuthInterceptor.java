package com.cardgame.config;

import com.cardgame.common.BizException;
import com.cardgame.common.ResultCode;
import com.cardgame.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 登录校验拦截器：从请求头 Authorization 中解析 JWT，解析出的用户 id 放入 request 属性。
 */
@Component
public class AuthInterceptor implements HandlerInterceptor {

    @Autowired
    private JwtUtil jwtUtil;

    /** 请求属性 key，Controller 可通过 @RequestAttribute("userId") 取出 */
    public static final String USER_ID_KEY = "userId";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // 放行预检请求
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }
        String token = request.getHeader("Authorization");
        if (token == null || token.isBlank()) {
            throw new BizException(ResultCode.UNAUTHORIZED);
        }
        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        Long userId = jwtUtil.parseUserId(token);
        if (userId == null) {
            throw new BizException(ResultCode.UNAUTHORIZED, "登录已过期，请重新登录");
        }
        request.setAttribute(USER_ID_KEY, userId);
        return true;
    }
}
