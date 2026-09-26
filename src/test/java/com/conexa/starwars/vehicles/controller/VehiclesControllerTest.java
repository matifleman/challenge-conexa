package com.conexa.starwars.vehicles.controller;

import java.util.List;

import com.conexa.starwars.common.dto.PageResponse;
import com.conexa.starwars.common.exception.ResourceNotFoundException;
import com.conexa.starwars.vehicles.dto.VehicleDto;
import com.conexa.starwars.vehicles.service.VehiclesService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
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

@WebMvcTest(VehiclesController.class)
class VehiclesControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private VehiclesService vehiclesService;

    @Test
    void findAllUsesDefaultPaginationAndSerializesVehicleFields() throws Exception {
        when(vehiclesService.findAll(1, 10, null))
                .thenReturn(PageResponse.of(List.of(sandCrawler()), 1, 10, 39));

        mockMvc.perform(get("/api/v1/vehicles"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].name").value("Sand Crawler"))
                .andExpect(jsonPath("$.content[0].vehicleClass").value("wheeled"))
                .andExpect(jsonPath("$.content[0].maxAtmospheringSpeed").value("30"))
                .andExpect(jsonPath("$.page").value(1))
                .andExpect(jsonPath("$.size").value(10))
                .andExpect(jsonPath("$.totalElements").value(39))
                .andExpect(jsonPath("$.totalPages").value(4));
    }

    @Test
    void findAllPassesPaginationAndNameFilterToService() throws Exception {
        when(vehiclesService.findAll(2, 5, "speeder"))
                .thenReturn(PageResponse.of(List.of(), 2, 5, 8));

        mockMvc.perform(get("/api/v1/vehicles").param("page", "2").param("size", "5").param("name", "speeder"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isEmpty());
    }

    @Test
    void findAllRejectsOutOfRangePagination() throws Exception {
        mockMvc.perform(get("/api/v1/vehicles").param("page", "0"))
                .andExpect(status().isBadRequest());
        mockMvc.perform(get("/api/v1/vehicles").param("size", "101"))
                .andExpect(status().isBadRequest());

        verify(vehiclesService, never()).findAll(anyInt(), anyInt(), any());
    }

    @Test
    void findByIdReturnsVehicle() throws Exception {
        when(vehiclesService.findById(4)).thenReturn(sandCrawler());

        mockMvc.perform(get("/api/v1/vehicles/4"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("4"))
                .andExpect(jsonPath("$.name").value("Sand Crawler"));
    }

    @Test
    void findByIdReturnsProblemDetailWhenVehicleDoesNotExist() throws Exception {
        when(vehiclesService.findById(1)).thenThrow(new ResourceNotFoundException("Vehicle", 1));

        mockMvc.perform(get("/api/v1/vehicles/1"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.detail").value("Vehicle with id 1 not found"));
    }

    @Test
    void findByIdRejectsNonNumericId() throws Exception {
        mockMvc.perform(get("/api/v1/vehicles/abc"))
                .andExpect(status().isBadRequest());
    }

    private static VehicleDto sandCrawler() {
        return new VehicleDto("4", "Sand Crawler", "Digger Crawler", "Corellia Mining Corporation", "wheeled",
                "150000", "36.8", "46", "30", "50000", "2 months", "30");
    }
}
