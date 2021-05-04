package com.example.oauth2.services.oauth2;

import com.example.oauth2.services.config.AppProperties;
import com.example.oauth2.services.exception.BadRequestException;
import com.example.oauth2.services.jwt.TokenProvider;
import com.example.oauth2.services.oauth2.HttpCookieOAuth2AuthorizationRequestRepository;
import com.example.oauth2.services.util.CookieUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URI;
import java.util.Optional;

import static com.example.oauth2.services.oauth2.HttpCookieOAuth2AuthorizationRequestRepository.REDIRECT_URI_PARAM_COOKIE_NAME;

@Component
public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {
 
    private TokenProvider tokenProvider;
 
    private AppProperties appProperties;
 
    private HttpCookieOAuth2AuthorizationRequestRepository httpCookieOAuth2AuthorizationRequestRepository;
 
    @Autowired
    OAuth2AuthenticationSuccessHandler(TokenProvider tokenProvider, AppProperties appProperties,
            HttpCookieOAuth2AuthorizationRequestRepository httpCookieOAuth2AuthorizationRequestRepository) {
        this.tokenProvider = tokenProvider;
        this.appProperties = appProperties;
        this.httpCookieOAuth2AuthorizationRequestRepository = httpCookieOAuth2AuthorizationRequestRepository;
    }
 
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        // this method is called when the authentication hits the success.
        // this method :
            // -> fetches the redirect_uri sent by the Angular client, and CHECKS if it validates against the list of allowed URIs mentioned in application.properties.
                // this kind of validation is made for unauthorized redirect_uris
            // -> creates a JWT authentication token
            // -> redirects the user to where redirect_uri says with the JWT added

        String targetUrl = determineTargetUrl(request, response, authentication);
 
        if (response.isCommitted()) {
            logger.debug("Response has already been committed. Unable to redirect to " + targetUrl);
            return;
        }
 
        clearAuthenticationAttributes(request, response);
        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }           //todo: (6)
 
    @Override
    protected String determineTargetUrl(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
        Optional<String> redirectUri = CookieUtils.getCookie(request, REDIRECT_URI_PARAM_COOKIE_NAME).map(Cookie::getValue);
 
        if (redirectUri.isPresent() && !isAuthorizedRedirectUri(redirectUri.get())) {
            throw new BadRequestException("Sorry! We've got an Unauthorized Redirect URI and can't proceed with the authentication");
        }
 
        String targetUrl = redirectUri.orElse(getDefaultTargetUrl());
 
        String token = tokenProvider.createToken(authentication);
 
        return UriComponentsBuilder.fromUriString(targetUrl).queryParam("token", token).build().toUriString();
    }           //todo: (7)
 
    protected void clearAuthenticationAttributes(HttpServletRequest request, HttpServletResponse response) {
        super.clearAuthenticationAttributes(request);
        httpCookieOAuth2AuthorizationRequestRepository.removeAuthorizationRequestCookies(request, response);
    }           //todo: (9)
 
    private boolean isAuthorizedRedirectUri(String uri) {
        URI clientRedirectUri = URI.create(uri);
 
        return appProperties.getOauth2().getAuthorizedRedirectUris().stream().anyMatch(authorizedRedirectUri -> {
            // Only validate host and port. Let the clients use different paths if they want
            // to
            URI authorizedURI = URI.create(authorizedRedirectUri);
            if (authorizedURI.getHost().equalsIgnoreCase(clientRedirectUri.getHost()) && authorizedURI.getPort() == clientRedirectUri.getPort()) {
                return true;
            }
            return false;
        });
    }       //todo: (8)
}