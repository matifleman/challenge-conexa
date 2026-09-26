package com.conexa.starwars.common.swapi;

import java.time.LocalDate;

import com.conexa.starwars.common.swapi.dto.SwapiFilm;
import com.conexa.starwars.common.swapi.dto.SwapiItemResponse;
import com.conexa.starwars.common.swapi.dto.SwapiListResponse;
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
class SwapiFilmsClientTest {

    @Autowired
    private SwapiFilmsClient client;

    @Autowired
    private MockRestServiceServer server;

    @Test
    void findAllRequestsAllFilmsAndParsesList() {
        server.expect(requestTo("https://swapi.test/api/films"))
                .andExpect(method(GET))
                .andRespond(withSuccess(fixture("films.json"), MediaType.APPLICATION_JSON));

        SwapiListResponse<SwapiFilm> response = client.findAll();

        assertThat(response.result()).hasSize(6);
        assertThat(response.result())
                .extracting(resource -> resource.properties().episodeId())
                .containsExactly(4, 5, 6, 1, 2, 3);
        server.verify();
    }

    @Test
    void findByTitleSendsTitleParamAndParsesResultList() {
        server.expect(requestTo("https://swapi.test/api/films?title=hope"))
                .andRespond(withSuccess(fixture("films-search.json"), MediaType.APPLICATION_JSON));

        SwapiListResponse<SwapiFilm> response = client.findByTitle("hope");

        assertThat(response.result())
                .extracting(resource -> resource.properties().title())
                .containsExactly("A New Hope");
        server.verify();
    }

    @Test
    void findByIdParsesAllFilmAttributes() {
        server.expect(requestTo("https://swapi.test/api/films/1"))
                .andRespond(withSuccess(fixture("film.json"), MediaType.APPLICATION_JSON));

        SwapiItemResponse<SwapiFilm> response = client.findById(1);

        SwapiFilm film = response.result().properties();
        assertThat(response.result().uid()).isEqualTo("1");
        assertThat(film.title()).isEqualTo("A New Hope");
        assertThat(film.episodeId()).isEqualTo(4);
        assertThat(film.director()).isEqualTo("George Lucas");
        assertThat(film.producer()).isEqualTo("Gary Kurtz, Rick McCallum");
        assertThat(film.releaseDate()).isEqualTo(LocalDate.of(1977, 5, 25));
        assertThat(film.openingCrawl()).startsWith("It is a period of civil war.");
        server.verify();
    }

    @Test
    void findByIdThrowsNotFoundWhenSwapiReturns404() {
        server.expect(requestTo("https://swapi.test/api/films/99"))
                .andRespond(withStatus(HttpStatus.NOT_FOUND));

        assertThatThrownBy(() -> client.findById(99))
                .isInstanceOf(HttpClientErrorException.NotFound.class);
        server.verify();
    }

    private static ClassPathResource fixture(String name) {
        return new ClassPathResource("swapi/" + name);
    }
}
