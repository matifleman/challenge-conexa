package com.conexa.starwars.planets.service;

import java.util.List;

import com.conexa.starwars.common.config.CacheConfig;
import com.conexa.starwars.common.dto.PageResponse;
import com.conexa.starwars.common.exception.ResourceNotFoundException;
import com.conexa.starwars.common.swapi.SwapiPlanetsClient;
import com.conexa.starwars.common.swapi.dto.SwapiPageResponse;
import com.conexa.starwars.common.swapi.dto.SwapiPlanet;
import com.conexa.starwars.planets.dto.PlanetDto;
import com.conexa.starwars.planets.mapper.PlanetMapper;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;

/**
 * Retrieves Star Wars planets from SWAPI and exposes them in this API's paginated format.
 */
@Service
public class PlanetsService {

    private final SwapiPlanetsClient swapiPlanetsClient;
    private final PlanetMapper planetMapper;

    public PlanetsService(SwapiPlanetsClient swapiPlanetsClient, PlanetMapper planetMapper) {
        this.swapiPlanetsClient = swapiPlanetsClient;
        this.planetMapper = planetMapper;
    }

    /**
     * Returns a page of planets, optionally filtered by name (case-insensitive, partial match).
     * <p>
     * Without a filter, pagination is delegated to SWAPI. With a filter, SWAPI returns every match
     * unpaginated, so the page is sliced in memory. Results are cached; SWAPI errors are not.
     *
     * @param page 1-based page number
     * @param size page size
     * @param name optional name filter; blank means no filter
     */
    @Cacheable(cacheNames = CacheConfig.PLANETS_LIST, sync = true)
    public PageResponse<PlanetDto> findAll(int page, int size, String name) {
        if (name == null || name.isBlank()) {
            return listPage(page, size);
        }
        List<PlanetDto> matches = swapiPlanetsClient.findByName(name.trim()).result().stream()
                .map(planetMapper::toDto)
                .toList();
        return PageResponse.fromList(matches, page, size);
    }

    /**
     * Returns a single planet by id. Results are cached; SWAPI errors are not.
     *
     * @throws ResourceNotFoundException if SWAPI has no planet with the given id
     */
    @Cacheable(cacheNames = CacheConfig.PLANETS_BY_ID, sync = true)
    public PlanetDto findById(int id) {
        try {
            return planetMapper.toDto(swapiPlanetsClient.findById(id).result());
        } catch (HttpClientErrorException.NotFound ex) {
            throw new ResourceNotFoundException("Planet", id);
        }
    }

    private PageResponse<PlanetDto> listPage(int page, int size) {
        SwapiPageResponse<SwapiPlanet> response = swapiPlanetsClient.findAll(page, size, true);
        // SWAPI answers out-of-range pages with the last page's data instead of an empty list
        List<PlanetDto> content = page > response.totalPages()
                ? List.of()
                : response.results().stream().map(planetMapper::toDto).toList();
        return PageResponse.of(content, page, size, response.totalRecords());
    }
}
