package com.conexa.starwars.films.controller;

import java.time.LocalDate;
import java.util.List;

import com.conexa.starwars.common.dto.PageResponse;
import com.conexa.starwars.common.exception.ResourceNotFoundException;
import com.conexa.starwars.films.dto.FilmDto;
import com.conexa.starwars.films.service.FilmsService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(FilmsController.class)
@WithMockUser
class FilmsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private FilmsService filmsService;

    @Test
    void findAllUsesDefaultPaginationAndSerializesFilmFields() throws Exception {
        when(filmsService.findAll(1, 10, null))
                .thenReturn(PageResponse.of(List.of(aNewHope()), 1, 10, 6));

        mockMvc.perform(get("/api/v1/films"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].title").value("A New Hope"))
                .andExpect(jsonPath("$.content[0].episodeId").value(4))
                .andExpect(jsonPath("$.content[0].releaseDate").value("1977-05-25"))
                .andExpect(jsonPath("$.page").value(1))
                .andExpect(jsonPath("$.size").value(10))
                .andExpect(jsonPath("$.totalElements").value(6))
                .andExpect(jsonPath("$.totalPages").value(1));
    }

    @Test
    void findAllPassesPaginationAndTitleFilterToService() throws Exception {
        when(filmsService.findAll(2, 5, "hope"))
                .thenReturn(PageResponse.of(List.of(), 2, 5, 1));

        mockMvc.perform(get("/api/v1/films").param("page", "2").param("size", "5").param("title", "hope"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isEmpty());
    }

    @Test
    void findAllRejectsOutOfRangePagination() throws Exception {
        mockMvc.perform(get("/api/v1/films").param("page", "0"))
                .andExpect(status().isBadRequest());
        mockMvc.perform(get("/api/v1/films").param("size", "101"))
                .andExpect(status().isBadRequest());

        verify(filmsService, never()).findAll(anyInt(), anyInt(), any());
    }

    @Test
    void findByIdReturnsFilm() throws Exception {
        when(filmsService.findById(1)).thenReturn(aNewHope());

        mockMvc.perform(get("/api/v1/films/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("1"))
                .andExpect(jsonPath("$.title").value("A New Hope"))
                .andExpect(jsonPath("$.characterIds[1]").value("2"))
                .andExpect(jsonPath("$.starshipIds[0]").value("2"))
                .andExpect(jsonPath("$.vehicleIds[0]").value("4"))
                .andExpect(jsonPath("$.speciesIds[2]").value("3"))
                .andExpect(jsonPath("$.planetIds[0]").value("1"));
    }

    @Test
    void findByIdReturnsProblemDetailWhenFilmDoesNotExist() throws Exception {
        when(filmsService.findById(99)).thenThrow(new ResourceNotFoundException("Film", 99));

        mockMvc.perform(get("/api/v1/films/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.detail").value("Film with id 99 not found"));
    }

    @Test
    void findByIdRejectsNonNumericId() throws Exception {
        mockMvc.perform(get("/api/v1/films/abc"))
                .andExpect(status().isBadRequest());
    }

    private static FilmDto aNewHope() {
        return new FilmDto("1", "A New Hope", 4, "George Lucas", "Gary Kurtz, Rick McCallum",
                LocalDate.of(1977, 5, 25), "It is a period of civil war.",
                List.of("1", "2"), List.of("2", "3"), List.of("4"),
                List.of("1", "2", "3"), List.of("1", "2", "3"));
    }
}
