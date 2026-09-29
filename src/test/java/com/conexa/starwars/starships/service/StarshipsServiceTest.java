package com.conexa.starwars.starships.service;

import java.util.List;

import com.conexa.starwars.common.dto.PageResponse;
import com.conexa.starwars.common.exception.ResourceNotFoundException;
import com.conexa.starwars.common.swapi.SwapiStarshipsClient;
import com.conexa.starwars.common.swapi.dto.SwapiItemResponse;
import com.conexa.starwars.common.swapi.dto.SwapiListResponse;
import com.conexa.starwars.common.swapi.dto.SwapiPageResponse;
import com.conexa.starwars.common.swapi.dto.SwapiResource;
import com.conexa.starwars.common.swapi.dto.SwapiStarship;
import com.conexa.starwars.starships.dto.StarshipDto;
import com.conexa.starwars.starships.mapper.StarshipMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StarshipsServiceTest {

    @Mock
    private SwapiStarshipsClient swapiStarshipsClient;

    private StarshipsService starshipsService;

    @BeforeEach
    void setUp() {
        starshipsService = new StarshipsService(swapiStarshipsClient, new StarshipMapper());
    }

    @Test
    void findAllWithoutNameDelegatesPaginationToSwapi() {
        when(swapiStarshipsClient.findAll(2, 1, true))
                .thenReturn(new SwapiPageResponse<>(36, 36, List.of(starship("3", "Star Destroyer"))));

        PageResponse<StarshipDto> result = starshipsService.findAll(2, 1, null);

        assertThat(result.content()).extracting(StarshipDto::name).containsExactly("Star Destroyer");
        assertThat(result.page()).isEqualTo(2);
        assertThat(result.totalElements()).isEqualTo(36);
        assertThat(result.totalPages()).isEqualTo(36);
        verify(swapiStarshipsClient, never()).findByName(any());
    }

    @Test
    void findAllBeyondLastPageReturnsEmptyContent() {
        when(swapiStarshipsClient.findAll(100, 10, true))
                .thenReturn(new SwapiPageResponse<>(36, 4, List.of(starship("75", "V-wing"))));

        PageResponse<StarshipDto> result = starshipsService.findAll(100, 10, null);

        assertThat(result.content()).isEmpty();
        assertThat(result.totalElements()).isEqualTo(36);
    }

    @Test
    void findAllWithNameSearchesAndPaginatesInMemory() {
        when(swapiStarshipsClient.findByName("star")).thenReturn(new SwapiListResponse<>(List.of(
                starship("3", "Star Destroyer"),
                starship("9", "Death Star"),
                starship("39", "Naboo Royal Starship"))));

        PageResponse<StarshipDto> result = starshipsService.findAll(2, 2, " star ");

        assertThat(result.content()).extracting(StarshipDto::name).containsExactly("Naboo Royal Starship");
        assertThat(result.totalElements()).isEqualTo(3);
        assertThat(result.totalPages()).isEqualTo(2);
        verify(swapiStarshipsClient, never()).findAll(anyInt(), anyInt(), anyBoolean());
    }

    @Test
    void findAllWithBlankNameListsWithoutFilter() {
        when(swapiStarshipsClient.findAll(1, 10, true))
                .thenReturn(new SwapiPageResponse<>(0, 0, List.of()));

        starshipsService.findAll(1, 10, "  ");

        verify(swapiStarshipsClient, never()).findByName(any());
    }

    @Test
    void findByIdReturnsMappedStarship() {
        when(swapiStarshipsClient.findById(9))
                .thenReturn(new SwapiItemResponse<>(starship("9", "Death Star")));

        StarshipDto result = starshipsService.findById(9);

        assertThat(result).isEqualTo(new StarshipDto("9", "Death Star", "DS-1 Orbital Battle Station",
                "Imperial Department of Military Research", "Deep Space Mobile Battlestation", "1000000000000",
                "120000", "342,953", "843,342", "1000000000000", "3 years", "n/a", "4.0", "10",
                List.of("13"), List.of("1")));
    }

    @Test
    void findByIdThrowsNotFoundWhenSwapiReturns404() {
        when(swapiStarshipsClient.findById(1)).thenThrow(
                HttpClientErrorException.create(HttpStatus.NOT_FOUND, "Not Found", HttpHeaders.EMPTY, null, null));

        assertThatThrownBy(() -> starshipsService.findById(1))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Starship with id 1 not found");
    }

    private static SwapiResource<SwapiStarship> starship(String uid, String name) {
        return new SwapiResource<>(uid, new SwapiStarship(name, "DS-1 Orbital Battle Station",
                "Imperial Department of Military Research", "Deep Space Mobile Battlestation", "1000000000000",
                "120000", "342,953", "843,342", "1000000000000", "3 years", "n/a", "4.0", "10",
                List.of("https://www.swapi.tech/api/people/13"), List.of("https://www.swapi.tech/api/films/1")));
    }
}
