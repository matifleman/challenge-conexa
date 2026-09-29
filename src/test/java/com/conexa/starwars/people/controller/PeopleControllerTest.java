package com.conexa.starwars.people.controller;

import java.util.List;

import com.conexa.starwars.common.dto.PageResponse;
import com.conexa.starwars.common.exception.ResourceNotFoundException;
import com.conexa.starwars.people.dto.PersonDto;
import com.conexa.starwars.people.service.PeopleService;
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

@WebMvcTest(PeopleController.class)
@WithMockUser
class PeopleControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PeopleService peopleService;

    @Test
    void findAllUsesDefaultPaginationWhenNoParamsAreGiven() throws Exception {
        when(peopleService.findAll(1, 10, null))
                .thenReturn(PageResponse.of(List.of(luke()), 1, 10, 82));

        mockMvc.perform(get("/api/v1/people"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].name").value("Luke Skywalker"))
                .andExpect(jsonPath("$.content[0].birthYear").value("19BBY"))
                .andExpect(jsonPath("$.page").value(1))
                .andExpect(jsonPath("$.size").value(10))
                .andExpect(jsonPath("$.totalElements").value(82))
                .andExpect(jsonPath("$.totalPages").value(9));
    }

    @Test
    void findAllPassesPaginationAndNameFilterToService() throws Exception {
        when(peopleService.findAll(2, 5, "sky"))
                .thenReturn(PageResponse.of(List.of(), 2, 5, 3));

        mockMvc.perform(get("/api/v1/people").param("page", "2").param("size", "5").param("name", "sky"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isEmpty());
    }

    @Test
    void findAllRejectsOutOfRangePagination() throws Exception {
        mockMvc.perform(get("/api/v1/people").param("page", "0"))
                .andExpect(status().isBadRequest());
        mockMvc.perform(get("/api/v1/people").param("size", "101"))
                .andExpect(status().isBadRequest());

        verify(peopleService, never()).findAll(anyInt(), anyInt(), any());
    }

    @Test
    void findByIdReturnsPerson() throws Exception {
        when(peopleService.findById(1)).thenReturn(luke());

        mockMvc.perform(get("/api/v1/people/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("1"))
                .andExpect(jsonPath("$.name").value("Luke Skywalker"))
                .andExpect(jsonPath("$.filmIds[3]").value("6"))
                .andExpect(jsonPath("$.starshipIds[0]").value("12"))
                .andExpect(jsonPath("$.vehicleIds[1]").value("30"));
    }

    @Test
    void findByIdReturnsProblemDetailWhenPersonDoesNotExist() throws Exception {
        when(peopleService.findById(999)).thenThrow(new ResourceNotFoundException("Person", 999));

        mockMvc.perform(get("/api/v1/people/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.detail").value("Person with id 999 not found"));
    }

    @Test
    void findByIdRejectsNonNumericId() throws Exception {
        mockMvc.perform(get("/api/v1/people/abc"))
                .andExpect(status().isBadRequest());
    }

    private static PersonDto luke() {
        return new PersonDto("1", "Luke Skywalker", "172", "77", "blond", "fair", "blue", "19BBY", "male",
                List.of("1", "2", "3", "6"), List.of("12", "22"), List.of("14", "30"));
    }
}
