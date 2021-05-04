package com.example.oauth2.services.service;

import com.example.oauth2.services.dto.LocalUser;
import com.example.oauth2.services.dto.SignUpRequest;
import com.example.oauth2.services.exception.UserAlreadyExistAuthenticationException;
import com.example.oauth2.services.model.User;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.OidcUserInfo;

import java.util.Map;
import java.util.Optional;

public interface UserService {

    User registerNewUser(SignUpRequest signUpRequest) throws UserAlreadyExistAuthenticationException;

    User findUserByEmail(String email);

    Optional<User> findUserById(Long id);

    LocalUser processUserRegistration(String registrationId,
                                      Map<String, Object> attributes,
                                      OidcIdToken idToken,
                                      OidcUserInfo userInfo);
}
