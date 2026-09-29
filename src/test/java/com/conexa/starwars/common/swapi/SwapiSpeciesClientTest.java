package com.conexa.starwars.common.swapi;

import java.util.List;

import com.conexa.starwars.common.swapi.dto.SwapiItemResponse;
import com.conexa.starwars.common.swapi.dto.SwapiListResponse;
import com.conexa.starwars.common.swapi.dto.SwapiPageResponse;
import com.conexa.starwars.common.swapi.dto.SwapiSpecies;
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
class SwapiSpeciesClientTest {

    @Autowired
    private SwapiSpeciesClient client;

    @Autowired
    private MockRestServiceServer server;

    @Test
    void findAllSendsPaginationParamsAndParsesPage() {
        server.expect(requestTo("https://swapi.test/api/species?page=1&limit=2&expanded=true"))
                .andExpect(method(GET))
                .andRespond(withSuccess(fixture("species-page.json"), MediaType.APPLICATION_JSON));

        SwapiPageResponse<SwapiSpecies> response = client.findAll(1, 2, true);

        assertThat(response.totalRecords()).isEqualTo(37);
        assertThat(response.totalPages()).isEqualTo(19);
        assertThat(response.results())
                .extracting(resource -> resource.properties().name())
                .containsExactly("Human", "Droid");
        assertThat(response.results().get(1).properties().homeworld())
                .isEqualTo("https://www.swapi.tech/api/planets/null");
        server.verify();
    }

    @Test
    void findByNameSendsNameParamAndParsesResultList() {
        server.expect(requestTo("https://swapi.test/api/species?name=wook"))
                .andRespond(withSuccess(fixture("species-search.json"), MediaType.APPLICATION_JSON));

        SwapiListResponse<SwapiSpecies> response = client.findByName("wook");

        assertThat(response.result())
                .extracting(resource -> resource.properties().name())
                .containsExactly("Wookie");
        server.verify();
    }

    @Test
    void findByIdParsesAllSpeciesAttributes() {
        server.expect(requestTo("https://swapi.test/api/species/3"))
                .andRespond(withSuccess(fixture("species.json"), MediaType.APPLICATION_JSON));

        SwapiItemResponse<SwapiSpecies> response = client.findById(3);

        assertThat(response.result().uid()).isEqualTo("3");
        assertThat(response.result().properties()).isEqualTo(new SwapiSpecies(
                "Wookie",
                "mammal",
                "sentient",
                "210",
                "gray",
                "black, brown",
                "blue, green, yellow, brown, golden, red",
                "400",
                "Shyriiwook",
                List.of("https://www.swapi.tech/api/people/13", "https://www.swapi.tech/api/people/80"),
                "https://www.swapi.tech/api/planets/14"));
        server.verify();
    }

    @Test
    void findByIdThrowsNotFoundWhenSwapiReturns404() {
        server.expect(requestTo("https://swapi.test/api/species/999"))
                .andRespond(withStatus(HttpStatus.NOT_FOUND));

        assertThatThrownBy(() -> client.findById(999))
                .isInstanceOf(HttpClientErrorException.NotFound.class);
        server.verify();
    }

    private static ClassPathResource fixture(String name) {
        return new ClassPathResource("swapi/" + name);
    }
}
