package com.conexa.starwars.common.config;

import com.conexa.starwars.TestcontainersConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Verifies the generated OpenAPI document, which is the contract published through Swagger UI.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
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
                .andExpect(jsonPath("$.paths", hasKey("/api/v1/species/{id}")))
                .andExpect(jsonPath("$.paths", hasKey("/api/v1/planets/{id}")))
                .andExpect(jsonPath("$.paths['/api/v1/people'].get.responses", hasKey("502")))
                .andExpect(jsonPath("$.paths['/api/v1/people'].get.responses", hasKey("504")))
                .andExpect(jsonPath("$.components.schemas", hasKey("ProblemDetail")));
    }

    @Test
    void apiDocsIncludeEndpointDescriptionsAndSpecificErrors() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paths['/api/v1/people'].get.summary").value("List people"))
                .andExpect(jsonPath("$.paths['/api/v1/people'].get.parameters[?(@.name == 'size')].description")
                        .value("Number of elements per page"))
                .andExpect(jsonPath("$.paths['/api/v1/people'].get.parameters[?(@.name == 'size')].schema.maximum")
                        .value(100))
                .andExpect(jsonPath("$.paths['/api/v1/people'].get.responses", hasKey("400")))
                .andExpect(jsonPath("$.paths['/api/v1/films'].get.parameters[?(@.name == 'title')]").exists())
                .andExpect(jsonPath("$.paths['/api/v1/vehicles/{id}'].get.responses", hasKey("404")))
                .andExpect(jsonPath("$.paths['/api/v1/starships'].get.responses['200'].content").exists());
    }

    @Test
    void apiDocsDescribeBearerAuthentication() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.components.securitySchemes.bearerAuth.scheme").value("bearer"))
                .andExpect(jsonPath("$.components.securitySchemes.bearerAuth.bearerFormat").value("JWT"))
                .andExpect(jsonPath("$.security[0]", hasKey("bearerAuth")))
                .andExpect(jsonPath("$.paths['/api/v1/people'].get.responses", hasKey("401")));
    }

    @Test
    void apiDocsMarkAuthenticationEndpointsAsPublic() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paths['/api/v1/auth/login'].post.security").isEmpty())
                .andExpect(jsonPath("$.paths['/api/v1/auth/register'].post.security").isEmpty())
                .andExpect(jsonPath("$.paths['/api/v1/auth/register'].post.responses", hasKey("409")))
                .andExpect(jsonPath("$.paths['/api/v1/auth/register'].post.responses", not(hasKey("401"))))
                .andExpect(jsonPath("$.paths['/api/v1/auth/login'].post.responses", not(hasKey("502"))));
    }

    @Test
    void swaggerUiIsServed() throws Exception {
        mockMvc.perform(get("/swagger-ui/index.html"))
                .andExpect(status().isOk());
    }
}
