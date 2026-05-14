package com.jotechhub.auth;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@RequestMapping("/api/auth/oauth2")
public class OAuth2StartController {

    public static final String GOOGLE_ACCOUNT_TYPE_COOKIE = "JTH_GOOGLE_ACCOUNT_TYPE";

    @GetMapping("/google")
    public void startGoogleLogin(
            @RequestParam(defaultValue = "STUDENT") String accountType,
            HttpServletResponse response
    ) throws IOException {

        String normalizedAccountType = normalizeAccountType(accountType);

        Cookie cookie = new Cookie(GOOGLE_ACCOUNT_TYPE_COOKIE, normalizedAccountType);
        cookie.setHttpOnly(true);
        cookie.setSecure(false); // خليها true لما تستخدم HTTPS بالإنتاج
        cookie.setPath("/");
        cookie.setMaxAge(180); // 3 minutes

        response.addCookie(cookie);
        response.sendRedirect("/oauth2/authorization/google");
    }

    private String normalizeAccountType(String accountType) {
        if (accountType == null) {
            return "STUDENT";
        }

        String value = accountType.trim().toUpperCase();

        if (value.equals("ORGANIZER")) {
            return "ORGANIZER";
        }

        return "STUDENT";
    }
}