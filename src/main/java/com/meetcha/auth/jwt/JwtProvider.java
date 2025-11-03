package com.meetcha.auth.jwt;

import com.meetcha.global.exception.CustomException;
import com.meetcha.global.exception.ErrorCode;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.UUID;

@Component
public class JwtProvider {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.access-token-expiration}")
    private long accessTokenExpiration;

    @Value("${jwt.refresh-token-expiration}")
    private long refreshTokenExpiration;

    private Key key;

    @PostConstruct
    public void init() {
        this.key = Keys.hmacShaKeyFor(secret.getBytes());
    }

    public String createAccessToken(UUID userId, String email) {
        return createToken(userId, email, accessTokenExpiration);
    }

    public String createRefreshToken(UUID userId, String email) {
        return createToken(userId, email, refreshTokenExpiration);
    }

    private String createToken(UUID userId, String email, long expirationTimeMs) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + expirationTimeMs);

        return Jwts.builder()
                .setSubject(userId.toString())
                .claim("email", email)
                .setIssuedAt(now)
                .setExpiration(expiry)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            return true;
        } catch (ExpiredJwtException e) {
            // 만료된 토큰
            throw new CustomException(ErrorCode.EXPIRED_JWT);
        } catch (JwtException | IllegalArgumentException e) {
            // 형식 이상
            throw new CustomException(ErrorCode.MALFORMED_JWT);
        }
    }

    public String getEmail(String token) {
        try {
            return Jwts.parserBuilder().setSigningKey(key).build()
                    .parseClaimsJws(token)
                    .getBody()
                    .get("email", String.class);
        } catch (ExpiredJwtException e) {
            throw new CustomException(ErrorCode.EXPIRED_JWT);
        }
    }

    public UUID getUserId(String token) {
        try {
            String subject = Jwts.parserBuilder().setSigningKey(key).build()
                    .parseClaimsJws(token)
                    .getBody()
                    .getSubject();
            return UUID.fromString(subject);
        } catch (ExpiredJwtException e) {
            throw new CustomException(ErrorCode.EXPIRED_JWT);
        } catch (JwtException | IllegalArgumentException e) {
            throw new CustomException(ErrorCode.MALFORMED_JWT);
        }
    }
}
