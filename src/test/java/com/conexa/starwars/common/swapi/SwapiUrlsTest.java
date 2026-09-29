package com.conexa.starwars.common.swapi;

import java.util.List;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SwapiUrlsTest {

    @Test
    void idsOfExtractsTheLastPathSegmentPreservingOrder() {
        List<String> ids = SwapiUrls.idsOf(List.of(
                "https://www.swapi.tech/api/films/2",
                "https://www.swapi.tech/api/films/1",
                "https://www.swapi.tech/api/films/6"));

        assertThat(ids).containsExactly("2", "1", "6");
    }

    @Test
    void idsOfReturnsEmptyListForEmptyInput() {
        assertThat(SwapiUrls.idsOf(List.of())).isEmpty();
    }

    @Test
    void idsOfReturnsEmptyListWhenSwapiOmitsTheRelation() {
        assertThat(SwapiUrls.idsOf(null)).isEmpty();
    }

    @Test
    void idsOfSkipsUrlsThatPointToNoResource() {
        List<String> ids = SwapiUrls.idsOf(List.of(
                "https://www.swapi.tech/api/planets/null",
                "https://www.swapi.tech/api/planets/1"));

        assertThat(ids).containsExactly("1");
    }

    @Test
    void idOfExtractsTheLastPathSegment() {
        assertThat(SwapiUrls.idOf("https://www.swapi.tech/api/planets/14")).isEqualTo("14");
    }

    @Test
    void idOfReturnsNullWhenSwapiOmitsTheRelation() {
        assertThat(SwapiUrls.idOf(null)).isNull();
    }

    @Test
    void idOfReturnsNullWhenTheUrlPointsToNoResource() {
        assertThat(SwapiUrls.idOf("https://www.swapi.tech/api/planets/null")).isNull();
    }
}
