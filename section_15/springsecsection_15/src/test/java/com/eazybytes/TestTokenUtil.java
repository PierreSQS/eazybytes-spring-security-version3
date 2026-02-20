package com.eazybytes;

import io.jsonwebtoken.Jwts;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Produces a self-signed HS256 JWT for use in integration tests.
 * <p>
 * The token carries the claims that {@link com.eazybytes.config.KeycloakRoleConverter}
 * expects so that the authenticated principal is granted {@code ROLE_USER}.
 * <p>
 * Because the token is signed with a symmetric key that is only known inside
 * this test helper, a matching {@link TestJwtDecoderConfig} replaces the real
 * Keycloak-backed {@link org.springframework.security.oauth2.jwt.JwtDecoder}
 * bean with one that accepts tokens signed by the same key.
 */
final class TestTokenUtil {

    /** Shared HMAC key used to both sign and verify the test JWT. */
    static final SecretKey SECRET_KEY = Jwts.SIG.HS256.key().build();

    /** A ready-to-use signed JWT that grants {@code ROLE_USER}. */
    static final String SIGNED_JWT = Jwts.builder()
            .subject("happy@example.com")
            .claim("preferred_username", "happy@example.com")
            .claim("realm_access", Map.of("roles", List.of("USER")))
            .issuedAt(Date.from(Instant.now().minus(1, ChronoUnit.MINUTES)))
            .expiration(Date.from(Instant.now().plus(1, ChronoUnit.HOURS)))
            .signWith(SECRET_KEY)
            .compact();

    private TestTokenUtil() {}
}
