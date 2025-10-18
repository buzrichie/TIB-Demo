package com.amalitech.tib.config;

import jakarta.servlet.http.Cookie;
import org.springframework.stereotype.Service;

@Service
public class CookieUtils {

    public Cookie createHttpOnlyCookie(String name, String value, Long maxAgeSeconds) {
        Cookie cookie = new Cookie(name, value);
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setPath("/");
        cookie.setMaxAge(maxAgeSeconds.intValue() / 1000);
        return cookie;
    }

    public  Cookie createCookie(String name, String value, Long maxAgeSeconds, boolean httpOnly) {
        Cookie cookie = new Cookie(name, value);
        cookie.setHttpOnly(httpOnly);
        cookie.setSecure(true);
        cookie.setPath("/");
        cookie.setMaxAge(maxAgeSeconds.intValue() / 1000);
        return cookie;
    }

    public Cookie expireCookie() {

        Cookie expireCookie = new Cookie("refreshToken", null);
        expireCookie.setMaxAge(0);
        expireCookie.setHttpOnly(true);
        expireCookie.setSecure(true);
        expireCookie.setPath("/");
        return expireCookie;
    }
}
