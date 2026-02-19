package com.eazybytes;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;

import javax.crypto.SecretKey;

/**
 * Replaces the production {@link JwtDecoder} — which contacts Keycloak's
 * JWK Set URI — with one that validates tokens signed by the test HMAC key
 * defined in {@link TestTokenUtil#SECRET_KEY}.
 * <p>
 * This configuration is imported by {@link EazyBankIntegrationRestClientTest}
 * via {@code @Import} so it does not affect any other test class.
 */
@TestConfiguration
class TestJwtDecoderConfig {

    @Bean
    JwtDecoder testJwtDecoder() {
        SecretKey key = TestTokenUtil.SECRET_KEY;
        return NimbusJwtDecoder.withSecretKey(key).build();
    }
}
