package com.linkwiki.auth.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TokenResponse {

    private String accessToken;

    public TokenResponse(String accessToken) {
        this.accessToken = accessToken;
    }
}
