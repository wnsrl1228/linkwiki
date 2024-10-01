package com.linkwiki.auth.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor()
@Getter
public class AccessTokenRequest {
    private String refreshToken;
}
