package com.conexa.starwars.common.swapi;

import java.util.List;

import com.conexa.starwars.common.swapi.dto.SwapiItemResponse;
import com.conexa.starwars.common.swapi.dto.SwapiListResponse;
import com.conexa.starwars.common.swapi.dto.SwapiPageResponse;
import com.conexa.starwars.common.swapi.dto.SwapiVehicle;
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
class SwapiVehiclesClientTest {

    @Autowired
    private SwapiVehiclesClient client;

    @Autowired
    private MockRestServiceServer server;

    @Test
    void findAllSendsPaginationParamsAndParsesPage() {
        server.expect(requestTo("https://swapi.test/api/vehicles?page=1&limit=2&expanded=true"))
                .andExpect(method(GET))
                .andRespond(withSuccess(fixture("vehicles-page.json"), MediaType.APPLICATION_JSON));

        SwapiPageResponse<SwapiVehicle> response = client.findAll(1, 2, true);

        assertThat(response.totalRecords()).isEqualTo(39);
        assertThat(response.totalPages()).isEqualTo(20);
        assertThat(response.results())
                .extracting(resource -> resource.properties().name())
                .containsExactly("Sand Crawler", "X-34 landspeeder");
        server.verify();
    }

    @Test
    void findByNameSendsNameParamAndParsesResultList() {
        server.expect(requestTo("https://swapi.test/api/vehicles?name=speeder"))
                .andRespond(withSuccess(fixture("vehicles-search.json"), MediaType.APPLICATION_JSON));

        SwapiListResponse<SwapiVehicle> response = client.findByName("speeder");

        assertThat(response.result())
                .extracting(resource -> resource.properties().name())
                .contains("X-34 landspeeder", "Snowspeeder");
        server.verify();
    }

    @Test
    void findByIdParsesAllVehicleAttributes() {
        server.expect(requestTo("https://swapi.test/api/vehicles/4"))
                .andRespond(withSuccess(fixture("vehicle.json"), MediaType.APPLICATION_JSON));

        SwapiItemResponse<SwapiVehicle> response = client.findById(4);

        assertThat(response.result().uid()).isEqualTo("4");
        assertThat(response.result().properties()).isEqualTo(new SwapiVehicle(
                "Sand Crawler",
                "Digger Crawler",
                "Corellia Mining Corporation",
                "wheeled",
                "150000",
                "36.8 ",
                "46",
                "30",
                "50000",
                "2 months",
                "30",
                List.of(),
                List.of("https://www.swapi.tech/api/films/1", "https://www.swapi.tech/api/films/5")));
        server.verify();
    }

    @Test
    void findByIdThrowsNotFoundWhenSwapiReturns404() {
        server.expect(requestTo("https://swapi.test/api/vehicles/1"))
                .andRespond(withStatus(HttpStatus.NOT_FOUND));

        assertThatThrownBy(() -> client.findById(1))
                .isInstanceOf(HttpClientErrorException.NotFound.class);
        server.verify();
    }

    private static ClassPathResource fixture(String name) {
        return new ClassPathResource("swapi/" + name);
    }
}
