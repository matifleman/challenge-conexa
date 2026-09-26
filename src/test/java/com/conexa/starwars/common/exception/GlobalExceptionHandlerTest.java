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
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Verifies the error contract through a real controller; any controller would do, since the handler is global.
 */
@WebMvcTest(PeopleController.class)
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
}
