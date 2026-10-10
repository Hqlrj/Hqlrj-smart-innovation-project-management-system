package org.example.springbootproject.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

/**
 * JWT工具类
 * 用于生成和解析JWT令牌
 */
@Component
public class JwtUtil {

    // JWT密钥（生产环境应使用配置文件或环境变量）
    private static final String SECRET_KEY = "mySecretKeyForJWTTokenGenerationMustBeLongEnoughForHS256Algorithm";
    
    // Token过期时间：7天（单位：毫秒）
    private static final long EXPIRATION_TIME = 7 * 24 * 60 * 60 * 1000L;

    /**
     * 生成密钥
     */
    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(SECRET_KEY.getBytes());
    }

    /**
     * 生成JWT Token
     */
    public String generateToken(Integer userId, String username, String role) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + EXPIRATION_TIME);

        return Jwts.builder()
                .claim("userId", userId)
                .claim("username", username)
                .claim("role", role)
                .subject(username) // 主题：用户名
                .issuedAt(now) // 签发时间
                .expiration(expiryDate) // 过期时间
                .signWith(getSigningKey())
                .compact();
    }

    /**
     * 从Token中获取Claims（包含用户信息）
     */
    private Claims getClaimsFromToken(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * 从Token中获取用户ID
     */
    public Integer getUserIdFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        return claims.get("userId", Integer.class);
    }

    /**
     * 从Token中获取用户名
     */
    public String getUsernameFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        return claims.getSubject();
    }

    /**
     * 从Token中获取用户角色
     */
    public String getRoleFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        return claims.get("role", String.class);
    }

    /**
     * 验证Token是否有效
     */
    public boolean validateToken(String token) {
        try {
            Claims claims = getClaimsFromToken(token);
            // 检查Token是否过期
            return !claims.getExpiration().before(new Date());
        } catch (Exception e) {
            // Token无效（格式错误、签名错误、已过期等）
            return false;
        }
    }

    /**
     * 判断Token是否过期
     */
    public boolean isTokenExpired(String token) {
        try {
            Claims claims = getClaimsFromToken(token);
            Date expiration = claims.getExpiration();
            return expiration.before(new Date());
        } catch (Exception e) {
            return true;
        }
    }
}

