package com.example.oauth2.services.dto;

public enum SocialProvider {

    GOOGLE("google"),
    VINC("VInc"),
    LOCAL("local");

    private String providerType;

    public String getProviderType() {
        return providerType;
    }

    SocialProvider(final String providerType) {
        this.providerType = providerType;
    }
}
