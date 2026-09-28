package com.conexa.starwars.auth.config;

import java.security.KeyPair;
import java.time.Duration;
import java.time.Instant;

import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.BadJwtException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwtValidationException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtConfigTest {

    private static final String ISSUER = "starwars-api";

    private final JwtConfig config = new JwtConfig();
    private final KeyPair keyPair = config.jwtSigningKeyPair();
    private final JwtEncoder encoder = config.jwtEncoder(keyPair);
    private final JwtDecoder decoder = config.jwtDecoder(keyPair, new JwtProperties(ISSUER, Duration.ofHours(1)));

    @Test
    void tokenIssuedByEncoderIsAcceptedByDecoder() {
        Jwt jwt = decoder.decode(token(encoder, ISSUER, Instant.now().plusSeconds(60)));

        assertThat(jwt.getSubject()).isEqualTo("luke");
        assertThat(jwt.getHeaders().get("alg")).isEqualTo("RS256");
    }

    @Test
    void tokenFromAnotherIssuerIsRejected() {
        String token = token(encoder, "another-api", Instant.now().plusSeconds(60));

        assertThatThrownBy(() -> decoder.decode(token)).isInstanceOf(JwtValidationException.class);
    }

    @Test
    void expiredTokenIsRejected() {
        String token = token(encoder, ISSUER, Instant.now().minus(Duration.ofMinutes(5)));

        assertThatThrownBy(() -> decoder.decode(token)).isInstanceOf(JwtValidationException.class);
    }

    @Test
    void tokenSignedWithAnotherKeyIsRejected() {
        JwtEncoder otherEncoder = config.jwtEncoder(config.jwtSigningKeyPair());
        String token = token(otherEncoder, ISSUER, Instant.now().plusSeconds(60));

        assertThatThrownBy(() -> decoder.decode(token)).isInstanceOf(BadJwtException.class);
    }

    private static String token(JwtEncoder encoder, String issuer, Instant expiresAt) {
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(issuer)
                .subject("luke")
                .issuedAt(expiresAt.minus(Duration.ofHours(1)))
                .expiresAt(expiresAt)
                .build();
        return encoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
    }
}
