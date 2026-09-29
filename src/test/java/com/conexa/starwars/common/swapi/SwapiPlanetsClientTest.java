package com.conexa.starwars.common.swapi;

import com.conexa.starwars.common.swapi.dto.SwapiItemResponse;
import com.conexa.starwars.common.swapi.dto.SwapiListResponse;
import com.conexa.starwars.common.swapi.dto.SwapiPageResponse;
import com.conexa.starwars.common.swapi.dto.SwapiPlanet;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.restclient.test.autoconfigure.RestClientTest;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.HttpClientErrorException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

@RestClientTest(properties = "swapi.base-url=https://swapi.test/api")
@Import(SwapiClientConfig.class)
class SwapiPlanetsClientTest {

    @Autowired
    private SwapiPlanetsClient client;

    @Autowired
    private MockRestServiceServer server;

    @Test
    void findAllSendsPaginationParamsAndParsesPage() {
        server.expect(requestTo("https://swapi.test/api/planets?page=1&limit=2&expanded=true"))
                .andExpect(method(GET))
                .andRespond(withSuccess(fixture("planets-page.json"), MediaType.APPLICATION_JSON));

        SwapiPageResponse<SwapiPlanet> response = client.findAll(1, 2, true);

        assertThat(response.totalRecords()).isEqualTo(60);
        assertThat(response.totalPages()).isEqualTo(30);
        assertThat(response.results())
                .extracting(resource -> resource.properties().name())
                .containsExactly("Tatooine", "Alderaan");
        server.verify();
    }

    @Test
    void findByNameSendsNameParamAndParsesResultList() {
        server.expect(requestTo("https://swapi.test/api/planets?name=tat"))
                .andRespond(withSuccess(fixture("planets-search.json"), MediaType.APPLICATION_JSON));

        SwapiListResponse<SwapiPlanet> response = client.findByName("tat");

        assertThat(response.result())
                .extracting(resource -> resource.properties().name())
                .containsExactly("Tatooine");
        server.verify();
    }

    @Test
    void findByIdParsesAllPlanetAttributes() {
        server.expect(requestTo("https://swapi.test/api/planets/1"))
                .andRespond(withSuccess(fixture("planet.json"), MediaType.APPLICATION_JSON));

        SwapiItemResponse<SwapiPlanet> response = client.findById(1);

        assertThat(response.result().uid()).isEqualTo("1");
        assertThat(response.result().properties()).isEqualTo(new SwapiPlanet(
                "Tatooine",
                "10465",
                "23",
                "304",
                "1 standard",
                "200000",
                "arid",
                "desert",
                "1"));
        server.verify();
    }

    @Test
    void findByIdThrowsNotFoundWhenSwapiReturns404() {
        server.expect(requestTo("https://swapi.test/api/planets/999"))
                .andRespond(withStatus(HttpStatus.NOT_FOUND));

        assertThatThrownBy(() -> client.findById(999))
                .isInstanceOf(HttpClientErrorException.NotFound.class);
        server.verify();
    }

    private static ClassPathResource fixture(String name) {
        return new ClassPathResource("swapi/" + name);
    }
}
