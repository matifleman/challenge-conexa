package com.conexa.starwars.common.exception;

import java.net.ConnectException;
import java.net.http.HttpTimeoutException;

import com.conexa.starwars.people.controller.PeopleController;
import com.conexa.starwars.people.service.PeopleService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Verifies the error contract through a real controller; any controller would do, since the handler is global.
 */
@WebMvcTest(PeopleController.class)
@WithMockUser
class GlobalExceptionHandlerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PeopleService peopleService;

    @Test
    void invalidParametersReturnProblemDetailWithErrorsList() throws Exception {
        mockMvc.perform(get("/api/v1/people").param("page", "0").param("size", "101"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.detail").value("Invalid request parameters"))
                .andExpect(jsonPath("$.errors.length()").value(2))
                .andExpect(jsonPath("$.errors[?(@.parameter == 'page')].message")
                        .value("must be greater than or equal to 1"))
                .andExpect(jsonPath("$.errors[?(@.parameter == 'size')].message")
                        .value("must be less than or equal to 100"));
    }

    @Test
    void nonNumericIdReturnsProblemDetail() throws Exception {
        mockMvc.perform(get("/api/v1/people/abc"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void unknownRouteReturnsProblemDetail() throws Exception {
        mockMvc.perform(get("/api/v1/unknown"))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void swapiServerErrorReturnsBadGateway() throws Exception {
        when(peopleService.findById(1)).thenThrow(HttpServerErrorException.create(
                HttpStatus.SERVICE_UNAVAILABLE, "Service Unavailable", HttpHeaders.EMPTY, null, null));

        mockMvc.perform(get("/api/v1/people/1"))
                .andExpect(status().isBadGateway())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.detail").value("The Star Wars API responded with an error"));
    }

    @Test
    void swapiRateLimitReturnsServiceUnavailableWithRetryAfter() throws Exception {
        HttpHeaders swapiHeaders = new HttpHeaders();
        swapiHeaders.set(HttpHeaders.RETRY_AFTER, "120");
        when(peopleService.findById(1)).thenThrow(HttpClientErrorException.create(
                HttpStatus.TOO_MANY_REQUESTS, "Too Many Requests", swapiHeaders, null, null));

        mockMvc.perform(get("/api/v1/people/1"))
                .andExpect(status().isServiceUnavailable())
                .andExpect(header().string(HttpHeaders.RETRY_AFTER, "120"))
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.detail")
                        .value("The Star Wars API is temporarily limiting requests. Try again later."));
    }

    @Test
    void swapiRateLimitWithoutRetryAfterOmitsTheHeader() throws Exception {
        when(peopleService.findById(1)).thenThrow(HttpClientErrorException.create(
                HttpStatus.TOO_MANY_REQUESTS, "Too Many Requests", HttpHeaders.EMPTY, null, null));

        mockMvc.perform(get("/api/v1/people/1"))
                .andExpect(status().isServiceUnavailable())
                .andExpect(header().doesNotExist(HttpHeaders.RETRY_AFTER));
    }

    @Test
    void swapiClientErrorReturnsBadGateway() throws Exception {
        when(peopleService.findById(1)).thenThrow(HttpClientErrorException.create(
                HttpStatus.BAD_REQUEST, "Bad Request", HttpHeaders.EMPTY, null, null));

        mockMvc.perform(get("/api/v1/people/1"))
                .andExpect(status().isBadGateway())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.detail").value("The Star Wars API responded with an error"));
    }

    @Test
    void swapiTimeoutReturnsGatewayTimeout() throws Exception {
        when(peopleService.findById(1)).thenThrow(
                new ResourceAccessException("I/O error", new HttpTimeoutException("request timed out")));

        mockMvc.perform(get("/api/v1/people/1"))
                .andExpect(status().isGatewayTimeout())
                .andExpect(jsonPath("$.detail").value("The Star Wars API did not respond in time"));
    }

    @Test
    void swapiConnectionFailureReturnsServiceUnavailable() throws Exception {
        when(peopleService.findById(1)).thenThrow(
                new ResourceAccessException("I/O error", new ConnectException("Connection refused")));

        mockMvc.perform(get("/api/v1/people/1"))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.detail").value("The Star Wars API is currently unavailable"));
    }

    @Test
    void alreadyExistingResourceReturnsConflict() throws Exception {
        when(peopleService.findById(1)).thenThrow(new ResourceAlreadyExistsException("User", "luke"));

        mockMvc.perform(get("/api/v1/people/1"))
                .andExpect(status().isConflict())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.detail").value("User 'luke' already exists"));
    }

    @Test
    void badCredentialsReturnUnauthorizedWithGenericMessage() throws Exception {
        when(peopleService.findById(1)).thenThrow(new BadCredentialsException("User luke not found"));

        mockMvc.perform(get("/api/v1/people/1"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.detail").value("Invalid username or password"));
    }

    @Test
    void otherAuthenticationFailuresReturnUnauthorized() throws Exception {
        when(peopleService.findById(1)).thenThrow(new InsufficientAuthenticationException("no token"));

        mockMvc.perform(get("/api/v1/people/1"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.detail").value("Authentication is required to access this resource"));
    }

    @Test
    void unexpectedErrorReturnsGenericInternalServerError() throws Exception {
        when(peopleService.findById(1)).thenThrow(new IllegalStateException("database password is secret"));

        mockMvc.perform(get("/api/v1/people/1"))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.detail").value("An unexpected error occurred"))
                .andExpect(content().string(not(containsString("secret"))));
    }
}
