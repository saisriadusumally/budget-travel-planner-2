package com.budgettravel.controller;

import com.budgettravel.model.*;
import com.budgettravel.repo.*;
import com.budgettravel.security.JwtCookieFilter;
import com.budgettravel.security.JwtService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.Duration;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class AuthController {
    private final TravelerRepository travelers;
    private final TravelPreferenceRepository prefs;
    private final BCryptPasswordEncoder encoder;
    private final JwtService jwt;
    private final boolean secureCookie;

    public AuthController(TravelerRepository t, TravelPreferenceRepository p, BCryptPasswordEncoder e,
                          JwtService jwt, @Value("${app.cookie.secure:false}") boolean secureCookie) {
        this.travelers = t; this.prefs = p; this.encoder = e; this.jwt = jwt; this.secureCookie = secureCookie;
    }

    private void setCookie(HttpServletResponse res, String value, Duration age) {
        ResponseCookie c = ResponseCookie.from(JwtCookieFilter.COOKIE, value)
            .httpOnly(true).secure(secureCookie).sameSite("Strict").path("/").maxAge(age).build();
        res.addHeader(HttpHeaders.SET_COOKIE, c.toString());
    }

    @PostMapping("/auth/register")
    public Traveler register(@RequestBody Map<String, String> b, HttpServletResponse res) {
        String email = b.getOrDefault("email", "").trim().toLowerCase();
        String password = b.getOrDefault("password", "");
        if (email.isEmpty() || password.length() < 6)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Enter an email and a password of at least 6 characters");
        if (travelers.findByEmail(email).isPresent())
            throw new ResponseStatusException(HttpStatus.CONFLICT, "An account with this email already exists");
        Traveler t = new Traveler();
        t.name = b.getOrDefault("name", "Traveler");
        t.email = email;
        t.passwordHash = encoder.encode(password);
        t.budgetPreference = b.getOrDefault("budgetPreference", "MEDIUM");
        t = travelers.save(t);
        TravelPreference p = new TravelPreference();
        p.travelerId = t.id;
        p.travelStyle = b.getOrDefault("travelStyle", "Relaxed");
        p.interests = b.getOrDefault("interests", "");
        prefs.save(p);
        setCookie(res, jwt.create(t.id), Duration.ofSeconds(jwt.ttlSeconds()));
        return t;
    }

    @PostMapping("/auth/login")
    public Traveler login(@RequestBody Map<String, String> b, HttpServletResponse res) {
        Traveler t = travelers.findByEmail(b.getOrDefault("email", "").trim().toLowerCase()).orElse(null);
        if (t == null || !encoder.matches(b.getOrDefault("password", ""), t.passwordHash))
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Email or password is incorrect");
        setCookie(res, jwt.create(t.id), Duration.ofSeconds(jwt.ttlSeconds()));
        return t;
    }

    @PostMapping("/auth/logout")
    public Map<String, String> logout(HttpServletResponse res) {
        setCookie(res, "", Duration.ZERO);   // expires the cookie immediately
        return Map.of("status", "logged out");
    }

    @GetMapping("/auth/me")
    public Traveler me(@AuthenticationPrincipal Long userId) {
        return travelers.findById(userId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Please log in again"));
    }

    @GetMapping("/travelers/{id}")
    public Traveler profile(@PathVariable Long id, @AuthenticationPrincipal Long userId) {
        if (!id.equals(userId)) throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not your profile");
        return me(userId);
    }
}
