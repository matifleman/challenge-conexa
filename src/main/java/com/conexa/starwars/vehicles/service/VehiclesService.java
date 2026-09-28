package com.conexa.starwars.vehicles.service;

import java.util.List;

import com.conexa.starwars.common.config.CacheConfig;
import com.conexa.starwars.common.dto.PageResponse;
import com.conexa.starwars.common.exception.ResourceNotFoundException;
import com.conexa.starwars.common.swapi.SwapiVehiclesClient;
import com.conexa.starwars.common.swapi.dto.SwapiPageResponse;
import com.conexa.starwars.common.swapi.dto.SwapiVehicle;
import com.conexa.starwars.vehicles.dto.VehicleDto;
import com.conexa.starwars.vehicles.mapper.VehicleMapper;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;

/**
 * Retrieves Star Wars vehicles from SWAPI and exposes them in this API's paginated format.
 */
@Service
public class VehiclesService {

    private final SwapiVehiclesClient swapiVehiclesClient;
    private final VehicleMapper vehicleMapper;

    public VehiclesService(SwapiVehiclesClient swapiVehiclesClient, VehicleMapper vehicleMapper) {
        this.swapiVehiclesClient = swapiVehiclesClient;
        this.vehicleMapper = vehicleMapper;
    }

    /**
     * Returns a page of vehicles, optionally filtered by name (case-insensitive, partial match).
     * <p>
     * Without a filter, pagination is delegated to SWAPI. With a filter, SWAPI returns every match
     * unpaginated, so the page is sliced in memory. Results are cached; SWAPI errors are not.
     *
     * @param page 1-based page number
     * @param size page size
     * @param name optional name filter; blank means no filter
     */
    @Cacheable(cacheNames = CacheConfig.VEHICLES_LIST, sync = true)
    public PageResponse<VehicleDto> findAll(int page, int size, String name) {
        if (name == null || name.isBlank()) {
            return listPage(page, size);
        }
        List<VehicleDto> matches = swapiVehiclesClient.findByName(name.trim()).result().stream()
                .map(vehicleMapper::toDto)
                .toList();
        return PageResponse.fromList(matches, page, size);
    }

    /**
     * Returns a single vehicle by id. Results are cached; SWAPI errors are not.
     *
     * @throws ResourceNotFoundException if SWAPI has no vehicle with the given id
     */
    @Cacheable(cacheNames = CacheConfig.VEHICLES_BY_ID, sync = true)
    public VehicleDto findById(int id) {
        try {
            return vehicleMapper.toDto(swapiVehiclesClient.findById(id).result());
        } catch (HttpClientErrorException.NotFound ex) {
            throw new ResourceNotFoundException("Vehicle", id);
        }
    }

    private PageResponse<VehicleDto> listPage(int page, int size) {
        SwapiPageResponse<SwapiVehicle> response = swapiVehiclesClient.findAll(page, size, true);
        // SWAPI answers out-of-range pages with the last page's data instead of an empty list
        List<VehicleDto> content = page > response.totalPages()
                ? List.of()
                : response.results().stream().map(vehicleMapper::toDto).toList();
        return PageResponse.of(content, page, size, response.totalRecords());
    }
}
