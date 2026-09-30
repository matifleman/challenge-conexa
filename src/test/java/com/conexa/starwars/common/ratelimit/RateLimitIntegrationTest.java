package com.conexa.starwars.common.ratelimit;

import com.conexa.starwars.people.controller.PeopleController;
import com.conexa.starwars.people.service.PeopleService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Verifies the limit is applied to real requests; the counting rules are covered by {@link RequestRateLimiterTest}.
 */
@WebMvcTest(PeopleController.class)
@TestPropertySource(properties = "rate-limit.max-requests=2")
class RateLimitIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PeopleService peopleService;

    @Test
    @WithMockUser("han")
    void requestOverTheLimitReturnsProblemDetailWithRetryAfter() throws Exception {
        mockMvc.perform(get("/api/v1/people/1")).andExpect(status().isOk());
        mockMvc.perform(get("/api/v1/people/1")).andExpect(status().isOk());

        mockMvc.perform(get("/api/v1/people/1"))
                .andExpect(status().isTooManyRequests())
                .andExpect(header().string(HttpHeaders.RETRY_AFTER, "60"))
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.detail").value("Too many requests. Try again later."));
    }
}
