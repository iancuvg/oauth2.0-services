package com.example.oauth2.services.dto;

import lombok.Value;

import java.util.List;

@Value
public class UserInfo {
    String id, displayName, email;
    List<String> roles;
}
