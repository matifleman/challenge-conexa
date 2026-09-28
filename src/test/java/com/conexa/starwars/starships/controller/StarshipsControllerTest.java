package com.conexa.starwars.starships.controller;

import java.util.List;

import com.conexa.starwars.common.dto.PageResponse;
import com.conexa.starwars.common.exception.ResourceNotFoundException;
import com.conexa.starwars.starships.dto.StarshipDto;
import com.conexa.starwars.starships.service.StarshipsService;
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

@WebMvcTest(StarshipsController.class)
@WithMockUser
class StarshipsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private StarshipsService starshipsService;

    @Test
    void findAllUsesDefaultPaginationAndSerializesStarshipFields() throws Exception {
        when(starshipsService.findAll(1, 10, null))
                .thenReturn(PageResponse.of(List.of(deathStar()), 1, 10, 36));

        mockMvc.perform(get("/api/v1/starships"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].name").value("Death Star"))
                .andExpect(jsonPath("$.content[0].starshipClass").value("Deep Space Mobile Battlestation"))
                .andExpect(jsonPath("$.content[0].mglt").value("10"))
                .andExpect(jsonPath("$.page").value(1))
                .andExpect(jsonPath("$.size").value(10))
                .andExpect(jsonPath("$.totalElements").value(36))
                .andExpect(jsonPath("$.totalPages").value(4));
    }

    @Test
    void findAllPassesPaginationAndNameFilterToService() throws Exception {
        when(starshipsService.findAll(2, 5, "star"))
                .thenReturn(PageResponse.of(List.of(), 2, 5, 6));

        mockMvc.perform(get("/api/v1/starships").param("page", "2").param("size", "5").param("name", "star"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isEmpty());
    }

    @Test
    void findAllRejectsOutOfRangePagination() throws Exception {
        mockMvc.perform(get("/api/v1/starships").param("page", "0"))
                .andExpect(status().isBadRequest());
        mockMvc.perform(get("/api/v1/starships").param("size", "101"))
                .andExpect(status().isBadRequest());

        verify(starshipsService, never()).findAll(anyInt(), anyInt(), any());
    }

    @Test
    void findByIdReturnsStarship() throws Exception {
        when(starshipsService.findById(9)).thenReturn(deathStar());

        mockMvc.perform(get("/api/v1/starships/9"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("9"))
                .andExpect(jsonPath("$.name").value("Death Star"));
    }

    @Test
    void findByIdReturnsProblemDetailWhenStarshipDoesNotExist() throws Exception {
        when(starshipsService.findById(1)).thenThrow(new ResourceNotFoundException("Starship", 1));

        mockMvc.perform(get("/api/v1/starships/1"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.detail").value("Starship with id 1 not found"));
    }

    @Test
    void findByIdRejectsNonNumericId() throws Exception {
        mockMvc.perform(get("/api/v1/starships/abc"))
                .andExpect(status().isBadRequest());
    }

    private static StarshipDto deathStar() {
        return new StarshipDto("9", "Death Star", "DS-1 Orbital Battle Station",
                "Imperial Department of Military Research", "Deep Space Mobile Battlestation", "1000000000000",
                "120000", "342,953", "843,342", "1000000000000", "3 years", "n/a", "4.0", "10");
    }
}
