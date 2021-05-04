package com.example.oauth2.services.oauth2;

import com.example.oauth2.services.exception.OAuth2AuthenticationProcessingException;
import com.example.oauth2.services.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;

@Service
public class CustomOidcUserService extends OidcUserService {

    @Autowired
    private UserService userService;

    @Override
    public OidcUser loadUser(OidcUserRequest userRequest) throws OAuth2AuthenticationException {
        // this method is called after an access token is obtained from the OAuth2 provider
        // this method fetches the user's details from the OAuth2 provider.

        // If a user with the same email already exists in my database, this user will have his details updated
        // otherwise, the app will register a new user. This process is explained in the processUserRegistration method.
        OidcUser oidcUser = super.loadUser(userRequest);
        try {
            return userService.processUserRegistration(
                    userRequest.getClientRegistration().getRegistrationId(),
                    oidcUser.getAttributes(),
                    oidcUser.getIdToken(),
                    oidcUser.getUserInfo());
        } catch (AuthenticationException ex) {
            throw ex;
        } catch (Exception ex) {
            ex.printStackTrace();
            // Throwing an instance of AuthenticationException will trigger the
            // OAuth2AuthenticationFailureHandler
            throw new OAuth2AuthenticationProcessingException(ex.getMessage(), ex.getCause());
        }
    }       // todo: (5)
}
