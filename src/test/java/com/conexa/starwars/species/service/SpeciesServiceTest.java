package com.conexa.starwars.species.service;

import java.util.List;

import com.conexa.starwars.common.dto.PageResponse;
import com.conexa.starwars.common.exception.ResourceNotFoundException;
import com.conexa.starwars.common.swapi.SwapiSpeciesClient;
import com.conexa.starwars.common.swapi.dto.SwapiItemResponse;
import com.conexa.starwars.common.swapi.dto.SwapiListResponse;
import com.conexa.starwars.common.swapi.dto.SwapiPageResponse;
import com.conexa.starwars.common.swapi.dto.SwapiResource;
import com.conexa.starwars.common.swapi.dto.SwapiSpecies;
import com.conexa.starwars.species.dto.SpeciesDto;
import com.conexa.starwars.species.mapper.SpeciesMapper;
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
class SpeciesServiceTest {

    @Mock
    private SwapiSpeciesClient swapiSpeciesClient;

    private SpeciesService speciesService;

    @BeforeEach
    void setUp() {
        speciesService = new SpeciesService(swapiSpeciesClient, new SpeciesMapper());
    }

    @Test
    void findAllWithoutNameDelegatesPaginationToSwapi() {
        when(swapiSpeciesClient.findAll(2, 1, true))
                .thenReturn(new SwapiPageResponse<>(37, 37, List.of(species("2", "Droid"))));

        PageResponse<SpeciesDto> result = speciesService.findAll(2, 1, null);

        assertThat(result.content()).extracting(SpeciesDto::name).containsExactly("Droid");
        assertThat(result.page()).isEqualTo(2);
        assertThat(result.totalElements()).isEqualTo(37);
        assertThat(result.totalPages()).isEqualTo(37);
        verify(swapiSpeciesClient, never()).findByName(any());
    }

    @Test
    void findAllBeyondLastPageReturnsEmptyContent() {
        when(swapiSpeciesClient.findAll(100, 10, true))
                .thenReturn(new SwapiPageResponse<>(37, 4, List.of(species("37", "Pau'an"))));

        PageResponse<SpeciesDto> result = speciesService.findAll(100, 10, null);

        assertThat(result.content()).isEmpty();
        assertThat(result.totalElements()).isEqualTo(37);
    }

    @Test
    void findAllWithNameSearchesAndPaginatesInMemory() {
        when(swapiSpeciesClient.findByName("an")).thenReturn(new SwapiListResponse<>(List.of(
                species("1", "Human"),
                species("5", "Hutt"),
                species("6", "Yoda's species"))));

        PageResponse<SpeciesDto> result = speciesService.findAll(2, 2, " an ");

        assertThat(result.content()).extracting(SpeciesDto::name).containsExactly("Yoda's species");
        assertThat(result.totalElements()).isEqualTo(3);
        assertThat(result.totalPages()).isEqualTo(2);
        verify(swapiSpeciesClient, never()).findAll(anyInt(), anyInt(), anyBoolean());
    }

    @Test
    void findAllWithBlankNameListsWithoutFilter() {
        when(swapiSpeciesClient.findAll(1, 10, true))
                .thenReturn(new SwapiPageResponse<>(0, 0, List.of()));

        speciesService.findAll(1, 10, "  ");

        verify(swapiSpeciesClient, never()).findByName(any());
    }

    @Test
    void findByIdReturnsMappedSpecies() {
        when(swapiSpeciesClient.findById(3))
                .thenReturn(new SwapiItemResponse<>(species("3", "Wookie")));

        SpeciesDto result = speciesService.findById(3);

        assertThat(result).isEqualTo(new SpeciesDto("3", "Wookie", "mammal", "sentient", "210", "gray",
                "black, brown", "blue, green, yellow, brown, golden, red", "400", "Shyriiwook",
                List.of("13", "80")));
    }

    @Test
    void findByIdThrowsNotFoundWhenSwapiReturns404() {
        when(swapiSpeciesClient.findById(999)).thenThrow(
                HttpClientErrorException.create(HttpStatus.NOT_FOUND, "Not Found", HttpHeaders.EMPTY, null, null));

        assertThatThrownBy(() -> speciesService.findById(999))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Species with id 999 not found");
    }

    private static SwapiResource<SwapiSpecies> species(String uid, String name) {
        return new SwapiResource<>(uid, new SwapiSpecies(name, "mammal", "sentient", "210", "gray",
                "black, brown", "blue, green, yellow, brown, golden, red", "400", "Shyriiwook",
                List.of("https://www.swapi.tech/api/people/13", "https://www.swapi.tech/api/people/80")));
    }
}
