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
}
