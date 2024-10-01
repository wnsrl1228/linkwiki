package com.linkwiki.auth.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

@NoArgsConstructor()
@Getter
public class LoginRequest {

    @Length(min = 5, max = 30, message = "아이디는 5~30자까지 입력할 수 있습니다.")
    @NotBlank(message = "아이디을 입력해주세요.")
    private String username;

    @Length(min = 5, max = 30, message = "비밀번호는 5~30자까지 입력할 수 있습니다.")
    @NotBlank(message = "비밀번호를 입력해주세요.")
    private String password;

    public LoginRequest(String username, String password) {
        this.username = username;
        this.password = password;
    }
}
