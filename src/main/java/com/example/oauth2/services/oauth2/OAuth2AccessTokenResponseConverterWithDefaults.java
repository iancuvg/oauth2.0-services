package com.example.oauth2.services.oauth2;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.endpoint.OAuth2AccessTokenResponse;
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class OAuth2AccessTokenResponseConverterWithDefaults implements Converter<Map<String, String>, OAuth2AccessTokenResponse> {
    private static final Set<String> TOKEN_RESPONSE_PARAMETER_NAMES = Stream
            .of(OAuth2ParameterNames.ACCESS_TOKEN, OAuth2ParameterNames.TOKEN_TYPE, OAuth2ParameterNames.EXPIRES_IN, OAuth2ParameterNames.REFRESH_TOKEN, OAuth2ParameterNames.SCOPE)
            .collect(Collectors.toSet());

    private OAuth2AccessToken.TokenType defaultAccessTokenType = OAuth2AccessToken.TokenType.BEARER;

    @Override
    public OAuth2AccessTokenResponse convert(Map<String, String> tokenResponseParameters) {
        // Google (or any social) provider sent back to our app a token response parameters which holds:
            // 1. access_token = parameter in which the authorization server accesses the token
            // 2. token_type = specifies the type of token. This type of token is assigned by the authorization server.
            // 3. scope = restricts the client app. Let's say, the client app may be granted READ and WRITE access to protected resources, or just READ access.
            //              this is also a good reason for implementing a custom Identity Provider...
            // 4. id_token = it is a JSON Web Token which contains claims about the user authentication and other claims
            // 5. expires_in = access token expiry
        // In this method, I'm going to build an OAuth2AccessTokenResponse, using tokenResponseParameters fields, which I'll send back to Spring API
        String accessToken = tokenResponseParameters.get(OAuth2ParameterNames.ACCESS_TOKEN);

        OAuth2AccessToken.TokenType accessTokenType = this.defaultAccessTokenType;
        if (OAuth2AccessToken.TokenType.BEARER.getValue().equalsIgnoreCase(tokenResponseParameters.get(OAuth2ParameterNames.TOKEN_TYPE))) {
            accessTokenType = OAuth2AccessToken.TokenType.BEARER;
        }

        long expiresIn = 0;
        if (tokenResponseParameters.containsKey(OAuth2ParameterNames.EXPIRES_IN)) {
            try {
                expiresIn = Long.valueOf(tokenResponseParameters.get(OAuth2ParameterNames.EXPIRES_IN));
            } catch (NumberFormatException ex) {
            }
        }

        Set<String> scopes = Collections.emptySet();
        if (tokenResponseParameters.containsKey(OAuth2ParameterNames.SCOPE)) {
            String scope = tokenResponseParameters.get(OAuth2ParameterNames.SCOPE);
            scopes = Arrays.stream(StringUtils.delimitedListToStringArray(scope, " ")).collect(Collectors.toSet());
        }

        Map<String, Object> additionalParameters = new LinkedHashMap<>();
        tokenResponseParameters.entrySet().stream().filter(e -> !TOKEN_RESPONSE_PARAMETER_NAMES.contains(e.getKey()))
                .forEach(e -> additionalParameters.put(e.getKey(), e.getValue()));

        return OAuth2AccessTokenResponse
                .withToken(accessToken)
                .tokenType(accessTokenType)
                .expiresIn(expiresIn)
                .scopes(scopes)
                .additionalParameters(additionalParameters)
                .build();
    }                       // todo: (4)

    public final void setDefaultAccessTokenType(OAuth2AccessToken.TokenType defaultAccessTokenType) {
        Assert.notNull(defaultAccessTokenType, "defaultAccessTokenType cannot be null");
        this.defaultAccessTokenType = defaultAccessTokenType;
    }
}
