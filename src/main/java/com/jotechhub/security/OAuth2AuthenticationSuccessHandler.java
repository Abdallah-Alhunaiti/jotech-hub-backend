package com.jotechhub.security;

import com.jotechhub.auth.GoogleLoginSuccessData;
import com.jotechhub.auth.GoogleOAuth2Service;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Component
@RequiredArgsConstructor
public class OAuth2AuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    private final GoogleOAuth2Service googleOAuth2Service;

    @Value("${app.frontend.oauth2-success-url}")
    private String frontendSuccessUrl;

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) throws IOException, ServletException {

        OAuth2User principal = (OAuth2User) authentication.getPrincipal();
        GoogleLoginSuccessData data = googleOAuth2Service.handleGoogleLogin(principal);

        String redirectUrl = frontendSuccessUrl
                + "#accessToken=" + encode(data.accessToken())
                + "&tokenType=" + encode(data.tokenType())
                + "&expiresIn=" + data.expiresIn()
                + "&userId=" + data.userId()
                + "&email=" + encode(data.email())
                + "&role=" + encode(data.role())
                + "&displayName=" + encode(data.displayName());

        response.sendRedirect(redirectUrl);
    }

    private String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}