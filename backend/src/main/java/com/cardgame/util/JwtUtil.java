package com.cardgame.util;

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
 * JWT 工具类：生成登录令牌、解析令牌中的用户 id。
 */
@Component
public class JwtUtil {

    private final SecretKey key;
    private final long expireSeconds;

    public JwtUtil(@Value("${card.jwt.secret}") String secret,
                   @Value("${card.jwt.expire-seconds:604800}") long expireSeconds) {
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        // HMAC-SHA256 要求密钥至少 256 位，不足时补齐
        if (keyBytes.length < 32) {
            byte[] padded = new byte[32];
            System.arraycopy(keyBytes, 0, padded, 0, keyBytes.length);
            keyBytes = padded;
        }
        this.key = Keys.hmacShaKeyFor(keyBytes);
        this.expireSeconds = expireSeconds;
    }

    /**
     * 为指定用户生成令牌。
     */
    public String generateToken(Long userId) {
        Date now = new Date();
        Date expire = new Date(now.getTime() + expireSeconds * 1000);
        return Jwts.builder()
                .setSubject(String.valueOf(userId))
                .claim("userId", userId)
                .setIssuedAt(now)
                .setExpiration(expire)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * 解析令牌中的用户 id，令牌无效或过期时返回 null。
     */
    public Long parseUserId(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
            Object userId = claims.get("userId");
            return userId == null ? null : Long.valueOf(String.valueOf(userId));
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 取令牌有效期（秒），返回给小程序用于本地缓存判断。
     */
    public long getExpireSeconds() {
        return expireSeconds;
    }
}
