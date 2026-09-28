package com.conexa.starwars.auth.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Credentials to exchange for an access token. Only presence is checked: the format rules belong to registration,
 * and applying them here would lock out existing users if those rules ever change.
 */
public record LoginRequest(@NotBlank String username, @NotBlank String password) {
}
