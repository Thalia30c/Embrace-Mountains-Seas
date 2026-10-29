package com.cardgame.common;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * JWT 工具：登录成功后签发令牌，供小程序端保存并在后续请求中携带。
 *
 * <p>密钥与有效期读取 application.yml 的 card.jwt.secret / card.jwt.expire-seconds。</p>
 */
@Component
public class JwtUtil {
    @Value("${card.jwt.secret}")
    private String secret;

    @Value("${card.jwt.expire-seconds:604800}")
    private long expireSeconds;

    private SecretKey key() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    /** 为用户签发 token，subject 放账号，claim 里带 uid 便于排查 */
    public String generateToken(Integer uid, String account) {
        long now = System.currentTimeMillis();
        return Jwts.builder()
                .setSubject(account)
                .claim("uid", uid)
                .setIssuedAt(new Date(now))
                .setExpiration(new Date(now + expireSeconds * 1000))
                .signWith(key(), SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * 解析 token：有效且未过期返回其中的 uid，无效 / 过期 / 格式错误返回 null。
     */
    public Integer parseUid(String token) {
        if (token == null || token.isBlank()) {
            return null;
        }
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(key())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
            Object uid = claims.get("uid");
            return uid == null ? null : Integer.valueOf(uid.toString());
        } catch (Exception e) {
            return null;
        }
    }
}
