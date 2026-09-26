package com.conexa.starwars.films.service;

import java.time.LocalDate;
import java.util.List;

import com.conexa.starwars.common.dto.PageResponse;
import com.conexa.starwars.common.exception.ResourceNotFoundException;
import com.conexa.starwars.common.swapi.SwapiFilmsClient;
import com.conexa.starwars.common.swapi.dto.SwapiFilm;
import com.conexa.starwars.common.swapi.dto.SwapiItemResponse;
import com.conexa.starwars.common.swapi.dto.SwapiListResponse;
import com.conexa.starwars.common.swapi.dto.SwapiResource;
import com.conexa.starwars.films.dto.FilmDto;
import com.conexa.starwars.films.mapper.FilmMapper;
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
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FilmsServiceTest {

    private static final SwapiListResponse<SwapiFilm> ALL_FILMS = new SwapiListResponse<>(List.of(
            film("1", "A New Hope", 4),
            film("2", "The Empire Strikes Back", 5),
            film("3", "Return of the Jedi", 6)));

    @Mock
    private SwapiFilmsClient swapiFilmsClient;

    private FilmsService filmsService;

    @BeforeEach
    void setUp() {
        filmsService = new FilmsService(swapiFilmsClient, new FilmMapper());
    }

    @Test
    void findAllWithoutTitlePaginatesAllFilmsInMemory() {
        when(swapiFilmsClient.findAll()).thenReturn(ALL_FILMS);

        PageResponse<FilmDto> result = filmsService.findAll(2, 2, null);

        assertThat(result.content()).extracting(FilmDto::title).containsExactly("Return of the Jedi");
        assertThat(result.totalElements()).isEqualTo(3);
        assertThat(result.totalPages()).isEqualTo(2);
        verify(swapiFilmsClient, never()).findByTitle(any());
    }

    @Test
    void findAllBeyondLastPageReturnsEmptyContent() {
        when(swapiFilmsClient.findAll()).thenReturn(ALL_FILMS);

        PageResponse<FilmDto> result = filmsService.findAll(5, 2, null);

        assertThat(result.content()).isEmpty();
        assertThat(result.totalElements()).isEqualTo(3);
    }

    @Test
    void findAllWithTitleSearchesByTrimmedTitle() {
        when(swapiFilmsClient.findByTitle("hope"))
                .thenReturn(new SwapiListResponse<>(List.of(film("1", "A New Hope", 4))));

        PageResponse<FilmDto> result = filmsService.findAll(1, 10, " hope ");

        assertThat(result.content()).extracting(FilmDto::title).containsExactly("A New Hope");
        verify(swapiFilmsClient, never()).findAll();
    }

    @Test
    void findAllWithBlankTitleListsAllFilms() {
        when(swapiFilmsClient.findAll()).thenReturn(ALL_FILMS);

        PageResponse<FilmDto> result = filmsService.findAll(1, 10, "  ");

        assertThat(result.content()).hasSize(3);
        verify(swapiFilmsClient, never()).findByTitle(any());
    }

    @Test
    void findByIdReturnsMappedFilm() {
        when(swapiFilmsClient.findById(1))
                .thenReturn(new SwapiItemResponse<>(film("1", "A New Hope", 4)));

        FilmDto result = filmsService.findById(1);

        assertThat(result.id()).isEqualTo("1");
        assertThat(result.title()).isEqualTo("A New Hope");
        assertThat(result.episodeId()).isEqualTo(4);
        assertThat(result.releaseDate()).isEqualTo(LocalDate.of(1977, 5, 25));
    }

    @Test
    void findByIdThrowsNotFoundWhenSwapiReturns404() {
        when(swapiFilmsClient.findById(99)).thenThrow(
                HttpClientErrorException.create(HttpStatus.NOT_FOUND, "Not Found", HttpHeaders.EMPTY, null, null));

        assertThatThrownBy(() -> filmsService.findById(99))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Film with id 99 not found");
    }

    private static SwapiResource<SwapiFilm> film(String uid, String title, int episodeId) {
        return new SwapiResource<>(uid, new SwapiFilm(title, episodeId, "George Lucas", "Gary Kurtz",
                LocalDate.of(1977, 5, 25), "It is a period of civil war."));
    }
}
