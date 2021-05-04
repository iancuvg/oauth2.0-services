package com.example.oauth2.services.dto;

import lombok.Value;

@Value
public class JwtAuthenticationResponse {

    String accessToken;
    UserInfo user;
}
