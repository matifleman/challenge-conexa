package com.conexa.starwars.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Credentials of a new user. The password rules follow NIST SP 800-63B (minimum length, no composition rules),
 * capped at 72 characters because BCrypt ignores anything longer (ADR 0015).
 */
public record RegisterRequest(
        @Schema(example = "luke")
        @NotBlank
        @Size(min = 3, max = 50)
        @Pattern(regexp = "[a-zA-Z0-9._-]+", message = "may only contain letters, digits, '.', '_' and '-'")
        String username,

        @Schema(example = "password123")
        @NotBlank
        @Size(min = 8, max = 72)
        String password) {
}
