package com.conexa.starwars.auth.controller;

import com.conexa.starwars.auth.config.JwtConfig;
import com.conexa.starwars.auth.config.SecurityConfig;
import com.conexa.starwars.auth.service.AuthService;
import com.conexa.starwars.common.exception.ResourceAlreadyExistsException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Runs with the real security configuration, so it also verifies the endpoints are reachable without a token.
 */
@WebMvcTest(AuthController.class)
@Import({SecurityConfig.class, JwtConfig.class})
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthService authService;

    @Test
    void registerCreatesUserWithoutAuthentication() throws Exception {
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username": "luke", "password": "password123"}
                                """))
                .andExpect(status().isCreated());

        verify(authService).register("luke", "password123");
    }

    @Test
    void registerRejectsTakenUsername() throws Exception {
        doThrow(new ResourceAlreadyExistsException("User", "luke")).when(authService).register("luke", "password123");

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username": "luke", "password": "password123"}
                                """))
                .andExpect(status().isConflict())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.detail").value("User 'luke' already exists"));
    }

    @Test
    void registerRejectsInvalidBodyWithErrorsList() throws Exception {
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username": "ab", "password": "short"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.detail").value("Invalid request body"))
                .andExpect(jsonPath("$.errors.length()").value(2))
                .andExpect(jsonPath("$.errors[?(@.parameter == 'username')].message")
                        .value("size must be between 3 and 50"))
                .andExpect(jsonPath("$.errors[?(@.parameter == 'password')].message")
                        .value("size must be between 8 and 72"));

        verify(authService, never()).register(any(), any());
    }

    @Test
    void registerRejectsUsernameWithInvalidCharacters() throws Exception {
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username": "luke skywalker", "password": "password123"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[0].message")
                        .value("may only contain letters, digits, '.', '_' and '-'"));
    }
}
