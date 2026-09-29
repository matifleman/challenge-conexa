package com.conexa.starwars.common.swapi;

import java.util.List;

import com.conexa.starwars.common.swapi.dto.SwapiItemResponse;
import com.conexa.starwars.common.swapi.dto.SwapiListResponse;
import com.conexa.starwars.common.swapi.dto.SwapiPageResponse;
import com.conexa.starwars.common.swapi.dto.SwapiPerson;
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
class SwapiPeopleClientTest {

    @Autowired
    private SwapiPeopleClient client;

    @Autowired
    private MockRestServiceServer server;

    @Test
    void findAllSendsPaginationParamsAndParsesPage() {
        server.expect(requestTo("https://swapi.test/api/people?page=1&limit=2&expanded=true"))
                .andExpect(method(GET))
                .andRespond(withSuccess(fixture("people-page.json"), MediaType.APPLICATION_JSON));

        SwapiPageResponse<SwapiPerson> response = client.findAll(1, 2, true);

        assertThat(response.totalRecords()).isEqualTo(82);
        assertThat(response.totalPages()).isEqualTo(41);
        assertThat(response.results()).hasSize(2);
        assertThat(response.results().getFirst().uid()).isEqualTo("1");
        assertThat(response.results().getFirst().properties().name()).isEqualTo("Luke Skywalker");
        server.verify();
    }

    @Test
    void findByNameSendsNameParamAndParsesResultList() {
        server.expect(requestTo("https://swapi.test/api/people?name=sky"))
                .andRespond(withSuccess(fixture("people-search.json"), MediaType.APPLICATION_JSON));

        SwapiListResponse<SwapiPerson> response = client.findByName("sky");

        assertThat(response.result())
                .extracting(resource -> resource.properties().name())
                .containsExactly("Luke Skywalker", "Anakin Skywalker", "Shmi Skywalker");
        server.verify();
    }

    @Test
    void findByIdParsesAllPersonAttributes() {
        server.expect(requestTo("https://swapi.test/api/people/1"))
                .andRespond(withSuccess(fixture("person.json"), MediaType.APPLICATION_JSON));

        SwapiItemResponse<SwapiPerson> response = client.findById(1);

        assertThat(response.result().uid()).isEqualTo("1");
        assertThat(response.result().properties()).isEqualTo(new SwapiPerson(
                "Luke Skywalker", "172", "77", "blond", "fair", "blue", "19BBY", "male",
                List.of("https://www.swapi.tech/api/films/1", "https://www.swapi.tech/api/films/2",
                        "https://www.swapi.tech/api/films/3", "https://www.swapi.tech/api/films/6"),
                List.of("https://www.swapi.tech/api/starships/12", "https://www.swapi.tech/api/starships/22"),
                List.of("https://www.swapi.tech/api/vehicles/14", "https://www.swapi.tech/api/vehicles/30")));
        server.verify();
    }

    @Test
    void findByIdThrowsNotFoundWhenSwapiReturns404() {
        server.expect(requestTo("https://swapi.test/api/people/999"))
                .andRespond(withStatus(HttpStatus.NOT_FOUND));

        assertThatThrownBy(() -> client.findById(999))
                .isInstanceOf(HttpClientErrorException.NotFound.class);
        server.verify();
    }

    private static ClassPathResource fixture(String name) {
        return new ClassPathResource("swapi/" + name);
    }
}
