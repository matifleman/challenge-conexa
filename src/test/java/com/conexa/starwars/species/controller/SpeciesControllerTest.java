package com.conexa.starwars.species.controller;

import java.util.List;

import com.conexa.starwars.common.dto.PageResponse;
import com.conexa.starwars.common.exception.ResourceNotFoundException;
import com.conexa.starwars.species.dto.SpeciesDto;
import com.conexa.starwars.species.service.SpeciesService;
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

@WebMvcTest(SpeciesController.class)
@WithMockUser
class SpeciesControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private SpeciesService speciesService;

    @Test
    void findAllUsesDefaultPaginationAndSerializesSpeciesFields() throws Exception {
        when(speciesService.findAll(1, 10, null))
                .thenReturn(PageResponse.of(List.of(wookie()), 1, 10, 37));

        mockMvc.perform(get("/api/v1/species"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].name").value("Wookie"))
                .andExpect(jsonPath("$.content[0].averageLifespan").value("400"))
                .andExpect(jsonPath("$.content[0].eyeColors").value("blue, green, yellow, brown, golden, red"))
                .andExpect(jsonPath("$.page").value(1))
                .andExpect(jsonPath("$.size").value(10))
                .andExpect(jsonPath("$.totalElements").value(37))
                .andExpect(jsonPath("$.totalPages").value(4));
    }

    @Test
    void findAllPassesPaginationAndNameFilterToService() throws Exception {
        when(speciesService.findAll(2, 5, "wook"))
                .thenReturn(PageResponse.of(List.of(), 2, 5, 1));

        mockMvc.perform(get("/api/v1/species").param("page", "2").param("size", "5").param("name", "wook"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isEmpty());
    }

    @Test
    void findAllRejectsOutOfRangePagination() throws Exception {
        mockMvc.perform(get("/api/v1/species").param("page", "0"))
                .andExpect(status().isBadRequest());
        mockMvc.perform(get("/api/v1/species").param("size", "101"))
                .andExpect(status().isBadRequest());

        verify(speciesService, never()).findAll(anyInt(), anyInt(), any());
    }

    @Test
    void findByIdReturnsSpecies() throws Exception {
        when(speciesService.findById(3)).thenReturn(wookie());

        mockMvc.perform(get("/api/v1/species/3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("3"))
                .andExpect(jsonPath("$.name").value("Wookie"))
                .andExpect(jsonPath("$.language").value("Shyriiwook"))
                .andExpect(jsonPath("$.characterIds[1]").value("80"))
                .andExpect(jsonPath("$.homeworldId").value("14"));
    }

    @Test
    void findByIdReturnsProblemDetailWhenSpeciesDoesNotExist() throws Exception {
        when(speciesService.findById(999)).thenThrow(new ResourceNotFoundException("Species", 999));

        mockMvc.perform(get("/api/v1/species/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.detail").value("Species with id 999 not found"));
    }

    @Test
    void findByIdRejectsNonNumericId() throws Exception {
        mockMvc.perform(get("/api/v1/species/abc"))
                .andExpect(status().isBadRequest());
    }

    private static SpeciesDto wookie() {
        return new SpeciesDto("3", "Wookie", "mammal", "sentient", "210", "gray", "black, brown",
                "blue, green, yellow, brown, golden, red", "400", "Shyriiwook", List.of("13", "80"), "14");
    }
}
