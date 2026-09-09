package com.luxeride.taxistfg.jwt;

import lombok.Builder;
import lombok.Getter;

@Getter
public class AuthResponse {
    private String token;
    private String message;

    public AuthResponse(String token, String message) {
        this.token = token;
        this.message = message;
    }

    @Builder
    public static AuthResponse withTokenAndMessage(String token, String message) {
        return new AuthResponse(token, message);
    }
}
