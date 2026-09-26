package com.conexa.starwars.vehicles.service;

import java.util.List;

import com.conexa.starwars.common.dto.PageResponse;
import com.conexa.starwars.common.exception.ResourceNotFoundException;
import com.conexa.starwars.common.swapi.SwapiVehiclesClient;
import com.conexa.starwars.common.swapi.dto.SwapiItemResponse;
import com.conexa.starwars.common.swapi.dto.SwapiListResponse;
import com.conexa.starwars.common.swapi.dto.SwapiPageResponse;
import com.conexa.starwars.common.swapi.dto.SwapiResource;
import com.conexa.starwars.common.swapi.dto.SwapiVehicle;
import com.conexa.starwars.vehicles.dto.VehicleDto;
import com.conexa.starwars.vehicles.mapper.VehicleMapper;
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
class VehiclesServiceTest {

    @Mock
    private SwapiVehiclesClient swapiVehiclesClient;

    private VehiclesService vehiclesService;

    @BeforeEach
    void setUp() {
        vehiclesService = new VehiclesService(swapiVehiclesClient, new VehicleMapper());
    }

    @Test
    void findAllWithoutNameDelegatesPaginationToSwapi() {
        when(swapiVehiclesClient.findAll(2, 1, true))
                .thenReturn(new SwapiPageResponse<>(39, 39, List.of(vehicle("7", "X-34 landspeeder"))));

        PageResponse<VehicleDto> result = vehiclesService.findAll(2, 1, null);

        assertThat(result.content()).extracting(VehicleDto::name).containsExactly("X-34 landspeeder");
        assertThat(result.page()).isEqualTo(2);
        assertThat(result.totalElements()).isEqualTo(39);
        assertThat(result.totalPages()).isEqualTo(39);
        verify(swapiVehiclesClient, never()).findByName(any());
    }

    @Test
    void findAllBeyondLastPageReturnsEmptyContent() {
        when(swapiVehiclesClient.findAll(100, 10, true))
                .thenReturn(new SwapiPageResponse<>(39, 4, List.of(vehicle("76", "AT-AT"))));

        PageResponse<VehicleDto> result = vehiclesService.findAll(100, 10, null);

        assertThat(result.content()).isEmpty();
        assertThat(result.totalElements()).isEqualTo(39);
    }

    @Test
    void findAllWithNameSearchesAndPaginatesInMemory() {
        when(swapiVehiclesClient.findByName("speeder")).thenReturn(new SwapiListResponse<>(List.of(
                vehicle("7", "X-34 landspeeder"),
                vehicle("14", "Snowspeeder"),
                vehicle("30", "Imperial Speeder Bike"))));

        PageResponse<VehicleDto> result = vehiclesService.findAll(2, 2, " speeder ");

        assertThat(result.content()).extracting(VehicleDto::name).containsExactly("Imperial Speeder Bike");
        assertThat(result.totalElements()).isEqualTo(3);
        assertThat(result.totalPages()).isEqualTo(2);
        verify(swapiVehiclesClient, never()).findAll(anyInt(), anyInt(), anyBoolean());
    }

    @Test
    void findAllWithBlankNameListsWithoutFilter() {
        when(swapiVehiclesClient.findAll(1, 10, true))
                .thenReturn(new SwapiPageResponse<>(0, 0, List.of()));

        vehiclesService.findAll(1, 10, "  ");

        verify(swapiVehiclesClient, never()).findByName(any());
    }

    @Test
    void findByIdReturnsMappedVehicle() {
        when(swapiVehiclesClient.findById(4))
                .thenReturn(new SwapiItemResponse<>(vehicle("4", "Sand Crawler")));

        VehicleDto result = vehiclesService.findById(4);

        assertThat(result).isEqualTo(new VehicleDto("4", "Sand Crawler", "Digger Crawler",
                "Corellia Mining Corporation", "wheeled", "150000", "36.8", "46", "30", "50000",
                "2 months", "30"));
    }

    @Test
    void findByIdThrowsNotFoundWhenSwapiReturns404() {
        when(swapiVehiclesClient.findById(1)).thenThrow(
                HttpClientErrorException.create(HttpStatus.NOT_FOUND, "Not Found", HttpHeaders.EMPTY, null, null));

        assertThatThrownBy(() -> vehiclesService.findById(1))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Vehicle with id 1 not found");
    }

    private static SwapiResource<SwapiVehicle> vehicle(String uid, String name) {
        return new SwapiResource<>(uid, new SwapiVehicle(name, "Digger Crawler", "Corellia Mining Corporation",
                "wheeled", "150000", "36.8", "46", "30", "50000", "2 months", "30"));
    }
}
