package com.budgettravel.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/** Reads the HttpOnly login cookie and authenticates the request. */
public class JwtCookieFilter extends OncePerRequestFilter {
    public static final String COOKIE = "btp_token";
    private final JwtService jwt;

    public JwtCookieFilter(JwtService jwt) { this.jwt = jwt; }

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
            throws ServletException, IOException {
        if (req.getCookies() != null) {
            for (Cookie c : req.getCookies()) {
                if (COOKIE.equals(c.getName())) {
                    Long id = jwt.parse(c.getValue());
                    if (id != null) {
                        SecurityContextHolder.getContext().setAuthentication(
                            new UsernamePasswordAuthenticationToken(id, null, List.of()));
                    }
                }
            }
        }
        chain.doFilter(req, res);
    }
}
