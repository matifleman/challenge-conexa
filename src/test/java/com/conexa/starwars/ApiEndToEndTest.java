package com.conexa.starwars;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.UUID;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.restclient.test.autoconfigure.AutoConfigureMockRestServiceServer;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.ExpectedCount;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Runs requests through the whole application: security with real tokens, PostgreSQL, controllers, services,
 * the {@code @HttpExchange} clients and the parsing of real SWAPI responses. Only the network call to SWAPI is
 * simulated, with responses captured from the real API.
 */
@SpringBootTest(properties = "swapi.base-url=https://swapi.test/api")
@AutoConfigureMockMvc
@AutoConfigureMockRestServiceServer
@Import(TestcontainersConfiguration.class)
class ApiEndToEndTest {

    private static final String SWAPI = "https://swapi.test/api";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private MockRestServiceServer swapi;

    @Autowired
    private CacheManager cacheManager;

    private String bearerToken;

    @BeforeEach
    void clearCaches() {
        // The application context, and with it the caches, is shared by every tesat in this class
        cacheManager.getCacheNames().stream()
                .map(cacheManager::getCache)
                .filter(Objects::nonNull)
                .forEach(Cache::clear);
    }

    @BeforeEach
    void logIn() throws Exception {
        String credentials = """
                {"username": "%s", "password": "password123"}
                """.formatted("e2e_" + UUID.randomUUID().toString().substring(0, 8));

        mockMvc.perform(post("/api/v1/auth/register").contentType(MediaType.APPLICATION_JSON).content(credentials))
                .andExpect(status().isCreated());
        String response = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON).content(credentials))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        bearerToken = "Bearer " + JsonPath.read(response, "$.accessToken");
    }

    @AfterEach
    void verifySwapiCalls() {
        swapi.verify();
    }

    @Test
    void listsPeoplePaginatedBySwapi() throws Exception {
        swapi.expect(requestTo(SWAPI + "/people?page=1&limit=2&expanded=true"))
                .andRespond(withSuccess(fixture("people-page.json"), MediaType.APPLICATION_JSON));

        mockMvc.perform(get("/api/v1/people").param("size", "2").header(HttpHeaders.AUTHORIZATION, bearerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.content[0].name").value("Luke Skywalker"))
                .andExpect(jsonPath("$.content[0].hairColor").value("blond"))
                .andExpect(jsonPath("$.totalElements").value(82))
                .andExpect(jsonPath("$.totalPages").value(41));
    }

    @Test
    void filtersFilmsByTitle() throws Exception {
        swapi.expect(requestTo(SWAPI + "/films?title=hope"))
                .andRespond(withSuccess(fixture("films-search.json"), MediaType.APPLICATION_JSON));

        mockMvc.perform(get("/api/v1/films").param("title", "hope").header(HttpHeaders.AUTHORIZATION, bearerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].title").value("A New Hope"))
                .andExpect(jsonPath("$.content[0].episodeId").value(4))
                .andExpect(jsonPath("$.content[0].releaseDate").value("1977-05-25"))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    void returnsStarshipDetail() throws Exception {
        swapi.expect(requestTo(SWAPI + "/starships/9"))
                .andRespond(withSuccess(fixture("starship.json"), MediaType.APPLICATION_JSON));

        mockMvc.perform(get("/api/v1/starships/9").header(HttpHeaders.AUTHORIZATION, bearerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("9"))
                .andExpect(jsonPath("$.name").value("Death Star"))
                .andExpect(jsonPath("$.mglt").value("10"));
    }

    @Test
    void filtersVehiclesByNameAndPaginatesTheResult() throws Exception {
        swapi.expect(requestTo(SWAPI + "/vehicles?name=speeder"))
                .andRespond(withSuccess(fixture("vehicles-search.json"), MediaType.APPLICATION_JSON));

        mockMvc.perform(get("/api/v1/vehicles").param("name", "speeder").param("size", "5")
                        .header(HttpHeaders.AUTHORIZATION, bearerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(5))
                .andExpect(jsonPath("$.content[0].name").value("X-34 landspeeder"))
                .andExpect(jsonPath("$.totalElements").value(8))
                .andExpect(jsonPath("$.totalPages").value(2));
    }

    @Test
    void resourceMissingInSwapiReturnsNotFound() throws Exception {
        swapi.expect(requestTo(SWAPI + "/people/999")).andRespond(withStatus(HttpStatus.NOT_FOUND));

        mockMvc.perform(get("/api/v1/people/999").header(HttpHeaders.AUTHORIZATION, bearerToken))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.detail").value("Person with id 999 not found"));
    }

    @Test
    void swapiServerErrorReturnsBadGateway() throws Exception {
        swapi.expect(requestTo(SWAPI + "/starships/1")).andRespond(withServerError());

        mockMvc.perform(get("/api/v1/starships/1").header(HttpHeaders.AUTHORIZATION, bearerToken))
                .andExpect(status().isBadGateway())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON));
    }

    @Test
    void repeatedRequestIsServedFromCache() throws Exception {
        swapi.expect(ExpectedCount.once(), requestTo(SWAPI + "/starships/9"))
                .andRespond(withSuccess(fixture("starship.json"), MediaType.APPLICATION_JSON));

        for (int i = 0; i < 2; i++) {
            mockMvc.perform(get("/api/v1/starships/9").header(HttpHeaders.AUTHORIZATION, bearerToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.name").value("Death Star"));
        }
    }

    @Test
    void swapiErrorsAreNotCached() throws Exception {
        swapi.expect(ExpectedCount.twice(), requestTo(SWAPI + "/people/999"))
                .andRespond(withStatus(HttpStatus.NOT_FOUND));

        for (int i = 0; i < 2; i++) {
            mockMvc.perform(get("/api/v1/people/999").header(HttpHeaders.AUTHORIZATION, bearerToken))
                    .andExpect(status().isNotFound());
        }
    }

    @Test
    void requestWithoutTokenNeverReachesSwapi() throws Exception {
        mockMvc.perform(get("/api/v1/vehicles/4"))
                .andExpect(status().isUnauthorized());
    }

    private static String fixture(String name) throws IOException {
        return new ClassPathResource("swapi/" + name).getContentAsString(StandardCharsets.UTF_8);
    }
}
