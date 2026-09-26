package com.conexa.starwars.common.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.hasKey;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Verifies the generated OpenAPI document, which is the contract published through Swagger UI.
 */
@SpringBootTest
@AutoConfigureMockMvc
class OpenApiConfigTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void apiDocsDescribeEveryResourceAndCommonErrors() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.info.title").value("Star Wars API"))
                .andExpect(jsonPath("$.paths", hasKey("/api/v1/people")))
                .andExpect(jsonPath("$.paths", hasKey("/api/v1/films/{id}")))
                .andExpect(jsonPath("$.paths", hasKey("/api/v1/starships")))
                .andExpect(jsonPath("$.paths", hasKey("/api/v1/vehicles/{id}")))
                .andExpect(jsonPath("$.paths['/api/v1/people'].get.responses", hasKey("502")))
                .andExpect(jsonPath("$.paths['/api/v1/people'].get.responses", hasKey("504")))
                .andExpect(jsonPath("$.components.schemas", hasKey("ProblemDetail")));
    }

    @Test
    void swaggerUiIsServed() throws Exception {
        mockMvc.perform(get("/swagger-ui/index.html"))
                .andExpect(status().isOk());
    }
}
