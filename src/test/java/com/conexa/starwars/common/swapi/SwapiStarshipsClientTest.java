package com.conexa.starwars.common.swapi;

import com.conexa.starwars.common.swapi.dto.SwapiItemResponse;
import com.conexa.starwars.common.swapi.dto.SwapiListResponse;
import com.conexa.starwars.common.swapi.dto.SwapiPageResponse;
import com.conexa.starwars.common.swapi.dto.SwapiStarship;
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
class SwapiStarshipsClientTest {

    @Autowired
    private SwapiStarshipsClient client;

    @Autowired
    private MockRestServiceServer server;

    @Test
    void findAllSendsPaginationParamsAndParsesPage() {
        server.expect(requestTo("https://swapi.test/api/starships?page=1&limit=2&expanded=true"))
                .andExpect(method(GET))
                .andRespond(withSuccess(fixture("starships-page.json"), MediaType.APPLICATION_JSON));

        SwapiPageResponse<SwapiStarship> response = client.findAll(1, 2, true);

        assertThat(response.totalRecords()).isEqualTo(36);
        assertThat(response.totalPages()).isEqualTo(18);
        assertThat(response.results())
                .extracting(resource -> resource.properties().name())
                .containsExactly("CR90 corvette", "Star Destroyer");
        server.verify();
    }

    @Test
    void findByNameSendsNameParamAndParsesResultList() {
        server.expect(requestTo("https://swapi.test/api/starships?name=star"))
                .andRespond(withSuccess(fixture("starships-search.json"), MediaType.APPLICATION_JSON));

        SwapiListResponse<SwapiStarship> response = client.findByName("star");

        assertThat(response.result())
                .extracting(resource -> resource.properties().name())
                .contains("Star Destroyer", "Death Star");
        server.verify();
    }

    @Test
    void findByIdParsesAllStarshipAttributes() {
        server.expect(requestTo("https://swapi.test/api/starships/9"))
                .andRespond(withSuccess(fixture("starship.json"), MediaType.APPLICATION_JSON));

        SwapiItemResponse<SwapiStarship> response = client.findById(9);

        assertThat(response.result().uid()).isEqualTo("9");
        assertThat(response.result().properties()).isEqualTo(new SwapiStarship(
                "Death Star",
                "DS-1 Orbital Battle Station",
                "Imperial Department of Military Research, Sienar Fleet Systems",
                "Deep Space Mobile Battlestation",
                "1000000000000",
                "120000",
                "342,953",
                "843,342",
                "1000000000000",
                "3 years",
                "n/a",
                "4.0",
                "10"));
        server.verify();
    }

    @Test
    void findByIdThrowsNotFoundWhenSwapiReturns404() {
        server.expect(requestTo("https://swapi.test/api/starships/1"))
                .andRespond(withStatus(HttpStatus.NOT_FOUND));

        assertThatThrownBy(() -> client.findById(1))
                .isInstanceOf(HttpClientErrorException.NotFound.class);
        server.verify();
    }

    private static ClassPathResource fixture(String name) {
        return new ClassPathResource("swapi/" + name);
    }
}
