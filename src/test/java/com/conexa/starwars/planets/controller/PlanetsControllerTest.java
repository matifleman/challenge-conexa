package com.conexa.starwars.planets.controller;

import java.util.List;

import com.conexa.starwars.common.dto.PageResponse;
import com.conexa.starwars.common.exception.ResourceNotFoundException;
import com.conexa.starwars.planets.dto.PlanetDto;
import com.conexa.starwars.planets.service.PlanetsService;
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

@WebMvcTest(PlanetsController.class)
@WithMockUser
class PlanetsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PlanetsService planetsService;

    @Test
    void findAllUsesDefaultPaginationAndSerializesPlanetFields() throws Exception {
        when(planetsService.findAll(1, 10, null))
                .thenReturn(PageResponse.of(List.of(tatooine()), 1, 10, 60));

        mockMvc.perform(get("/api/v1/planets"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].name").value("Tatooine"))
                .andExpect(jsonPath("$.content[0].rotationPeriod").value("23"))
                .andExpect(jsonPath("$.content[0].gravity").value("1 standard"))
                .andExpect(jsonPath("$.content[0].surfaceWater").value("1"))
                .andExpect(jsonPath("$.page").value(1))
                .andExpect(jsonPath("$.size").value(10))
                .andExpect(jsonPath("$.totalElements").value(60))
                .andExpect(jsonPath("$.totalPages").value(6));
    }

    @Test
    void findAllPassesPaginationAndNameFilterToService() throws Exception {
        when(planetsService.findAll(2, 5, "tat"))
                .thenReturn(PageResponse.of(List.of(), 2, 5, 1));

        mockMvc.perform(get("/api/v1/planets").param("page", "2").param("size", "5").param("name", "tat"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isEmpty());
    }

    @Test
    void findAllRejectsOutOfRangePagination() throws Exception {
        mockMvc.perform(get("/api/v1/planets").param("page", "0"))
                .andExpect(status().isBadRequest());
        mockMvc.perform(get("/api/v1/planets").param("size", "101"))
                .andExpect(status().isBadRequest());

        verify(planetsService, never()).findAll(anyInt(), anyInt(), any());
    }

    @Test
    void findByIdReturnsPlanet() throws Exception {
        when(planetsService.findById(1)).thenReturn(tatooine());

        mockMvc.perform(get("/api/v1/planets/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("1"))
                .andExpect(jsonPath("$.name").value("Tatooine"))
                .andExpect(jsonPath("$.climate").value("arid"))
                .andExpect(jsonPath("$.terrain").value("desert"));
    }

    @Test
    void findByIdReturnsProblemDetailWhenPlanetDoesNotExist() throws Exception {
        when(planetsService.findById(999)).thenThrow(new ResourceNotFoundException("Planet", 999));

        mockMvc.perform(get("/api/v1/planets/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.detail").value("Planet with id 999 not found"));
    }

    @Test
    void findByIdRejectsNonNumericId() throws Exception {
        mockMvc.perform(get("/api/v1/planets/abc"))
                .andExpect(status().isBadRequest());
    }

    private static PlanetDto tatooine() {
        return new PlanetDto("1", "Tatooine", "10465", "23", "304", "1 standard", "200000", "arid", "desert", "1");
    }
}
