package com.conexa.starwars.auth.config;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

/**
 * Signs and verifies the API's access tokens with RS256 (ADR 0017).
 */
@Configuration
@EnableConfigurationProperties(JwtProperties.class)
public class JwtConfig {

    /**
     * Generated on every startup so no private key is stored in the repository or configuration;
     * as a consequence, tokens issued before a restart are no longer valid.
     */
    @Bean
    KeyPair jwtSigningKeyPair() {
        try {
            KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
            generator.initialize(2048);
            return generator.generateKeyPair();
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("RSA is not available in this JVM", ex);
        }
    }

    @Bean
    JwtEncoder jwtEncoder(KeyPair jwtSigningKeyPair) {
        return NimbusJwtEncoder
                .withKeyPair((RSAPublicKey) jwtSigningKeyPair.getPublic(), (RSAPrivateKey) jwtSigningKeyPair.getPrivate())
                .build();
    }

    @Bean
    JwtDecoder jwtDecoder(KeyPair jwtSigningKeyPair, JwtProperties properties) {
        NimbusJwtDecoder decoder = NimbusJwtDecoder.withPublicKey((RSAPublicKey) jwtSigningKeyPair.getPublic()).build();
        decoder.setJwtValidator(JwtValidators.createDefaultWithIssuer(properties.issuer()));
        return decoder;
    }
}
