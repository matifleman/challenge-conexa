package com.conexa.starwars.planets.service;

import java.util.List;

import com.conexa.starwars.common.dto.PageResponse;
import com.conexa.starwars.common.exception.ResourceNotFoundException;
import com.conexa.starwars.common.swapi.SwapiPlanetsClient;
import com.conexa.starwars.common.swapi.dto.SwapiItemResponse;
import com.conexa.starwars.common.swapi.dto.SwapiListResponse;
import com.conexa.starwars.common.swapi.dto.SwapiPageResponse;
import com.conexa.starwars.common.swapi.dto.SwapiPlanet;
import com.conexa.starwars.common.swapi.dto.SwapiResource;
import com.conexa.starwars.planets.dto.PlanetDto;
import com.conexa.starwars.planets.mapper.PlanetMapper;
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
class PlanetsServiceTest {

    @Mock
    private SwapiPlanetsClient swapiPlanetsClient;

    private PlanetsService planetsService;

    @BeforeEach
    void setUp() {
        planetsService = new PlanetsService(swapiPlanetsClient, new PlanetMapper());
    }

    @Test
    void findAllWithoutNameDelegatesPaginationToSwapi() {
        when(swapiPlanetsClient.findAll(2, 1, true))
                .thenReturn(new SwapiPageResponse<>(60, 60, List.of(planet("2", "Alderaan"))));

        PageResponse<PlanetDto> result = planetsService.findAll(2, 1, null);

        assertThat(result.content()).extracting(PlanetDto::name).containsExactly("Alderaan");
        assertThat(result.page()).isEqualTo(2);
        assertThat(result.totalElements()).isEqualTo(60);
        assertThat(result.totalPages()).isEqualTo(60);
        verify(swapiPlanetsClient, never()).findByName(any());
    }

    @Test
    void findAllBeyondLastPageReturnsEmptyContent() {
        when(swapiPlanetsClient.findAll(100, 10, true))
                .thenReturn(new SwapiPageResponse<>(60, 6, List.of(planet("60", "Umbara"))));

        PageResponse<PlanetDto> result = planetsService.findAll(100, 10, null);

        assertThat(result.content()).isEmpty();
        assertThat(result.totalElements()).isEqualTo(60);
    }

    @Test
    void findAllWithNameSearchesAndPaginatesInMemory() {
        when(swapiPlanetsClient.findByName("oo")).thenReturn(new SwapiListResponse<>(List.of(
                planet("1", "Tatooine"),
                planet("8", "Naboo"),
                planet("40", "Troiken"))));

        PageResponse<PlanetDto> result = planetsService.findAll(2, 2, " oo ");

        assertThat(result.content()).extracting(PlanetDto::name).containsExactly("Troiken");
        assertThat(result.totalElements()).isEqualTo(3);
        assertThat(result.totalPages()).isEqualTo(2);
        verify(swapiPlanetsClient, never()).findAll(anyInt(), anyInt(), anyBoolean());
    }

    @Test
    void findAllWithBlankNameListsWithoutFilter() {
        when(swapiPlanetsClient.findAll(1, 10, true))
                .thenReturn(new SwapiPageResponse<>(0, 0, List.of()));

        planetsService.findAll(1, 10, "  ");

        verify(swapiPlanetsClient, never()).findByName(any());
    }

    @Test
    void findByIdReturnsMappedPlanet() {
        when(swapiPlanetsClient.findById(1))
                .thenReturn(new SwapiItemResponse<>(planet("1", "Tatooine")));

        PlanetDto result = planetsService.findById(1);

        assertThat(result).isEqualTo(new PlanetDto("1", "Tatooine", "10465", "23", "304", "1 standard",
                "200000", "arid", "desert", "1"));
    }

    @Test
    void findByIdThrowsNotFoundWhenSwapiReturns404() {
        when(swapiPlanetsClient.findById(999)).thenThrow(
                HttpClientErrorException.create(HttpStatus.NOT_FOUND, "Not Found", HttpHeaders.EMPTY, null, null));

        assertThatThrownBy(() -> planetsService.findById(999))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Planet with id 999 not found");
    }

    private static SwapiResource<SwapiPlanet> planet(String uid, String name) {
        return new SwapiResource<>(uid, new SwapiPlanet(name, "10465", "23", "304", "1 standard", "200000",
                "arid", "desert", "1"));
    }
}
