package com.jotechhub.security;

import com.jotechhub.auth.GoogleLoginSuccessData;
import com.jotechhub.auth.GoogleOAuth2Service;
import com.jotechhub.auth.OAuth2StartController;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
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
import java.util.Arrays;

@Component
@RequiredArgsConstructor
public class OAuth2AuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    private final GoogleOAuth2Service googleOAuth2Service;
    @Value("${app.frontend.oauth2-onboarding-url}")
    private String frontendOnboardingUrl;
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

        String redirectUrl;

        if (data.requiresOnboarding()) {
            redirectUrl = frontendOnboardingUrl
                    + "#pendingToken=" + encode(data.pendingToken())
                    + "&email=" + encode(data.email())
                    + "&displayName=" + encode(data.displayName());
        } else {
            redirectUrl = frontendSuccessUrl
                    + "#accessToken=" + encode(data.accessToken())
                    + "&tokenType=" + encode(data.tokenType())
                    + "&expiresIn=" + data.expiresIn()
                    + "&userId=" + data.userId()
                    + "&email=" + encode(data.email())
                    + "&role=" + encode(data.role())
                    + "&displayName=" + encode(data.displayName());
        }

        response.sendRedirect(redirectUrl);
    }

    private String readCookie(HttpServletRequest request, String cookieName) {
        if (request.getCookies() == null) {
            return "STUDENT";
        }

        return Arrays.stream(request.getCookies())
                .filter(cookie -> cookieName.equals(cookie.getName()))
                .map(Cookie::getValue)
                .findFirst()
                .orElse("STUDENT");
    }

    private void clearCookie(HttpServletResponse response, String cookieName) {
        Cookie cookie = new Cookie(cookieName, "");
        cookie.setHttpOnly(true);
        cookie.setSecure(false);
        cookie.setPath("/");
        cookie.setMaxAge(0);
        response.addCookie(cookie);
    }

    private String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}