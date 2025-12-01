package com.meetcha.auth.dto;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class LoginRequestDto {
    @NotBlank(message = "Google 인증 코드는 필수입니다.")
    private String code;
    @NotBlank(message = "Redirect URI는 필수입니다.")
    private String redirectUri;
}
