package com.meetcha.auth.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.JdbcType;
import org.hibernate.type.descriptor.jdbc.BinaryJdbcType;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class UserEntity {
    @Id
    @GeneratedValue(generator = "uuid2")
    @GenericGenerator(name = "uuid2", strategy = "uuid2")
    @JdbcType(BinaryJdbcType.class)
    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "email", nullable = false, unique = true)
    private String email;

    //구글 액세스 토큰(구글 api 호출용, jwt 액세스토큰은 저장 X)
    @Column(name = "google_token", nullable = false)
    private String googleToken;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "profile_img_src")
    private String profileImgSrc;

    @Column(name = "google_refresh_token")
    private String googleRefreshToken;

    @Column(name = "google_token_expires_at")
    private LocalDateTime googleTokenExpiresAt;

    //구글 토큰(구글 api 호출용, 리프레쉬 토큰테이블에 저장된거는 jwt라 이거랑 다름)
    public void updateGoogleAccessToken(String accessToken, LocalDateTime expiresAt) {
        this.googleToken = accessToken;
        this.googleTokenExpiresAt = expiresAt;
    }

    public void updateGoogleAllTokens(String accessToken, String refreshToken, LocalDateTime expiresAt) {
        this.googleToken = accessToken;
        this.googleRefreshToken = refreshToken != null ? refreshToken : this.googleRefreshToken;
        this.googleTokenExpiresAt = expiresAt;
    }

    public void updateGoogleAccessToken(String accessToken) {
        this.googleToken = accessToken;
    }
}
