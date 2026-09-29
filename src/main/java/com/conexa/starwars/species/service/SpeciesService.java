package com.conexa.starwars.species.service;

import java.util.List;

import com.conexa.starwars.common.config.CacheConfig;
import com.conexa.starwars.common.dto.PageResponse;
import com.conexa.starwars.common.exception.ResourceNotFoundException;
import com.conexa.starwars.common.swapi.SwapiSpeciesClient;
import com.conexa.starwars.common.swapi.dto.SwapiPageResponse;
import com.conexa.starwars.common.swapi.dto.SwapiSpecies;
import com.conexa.starwars.species.dto.SpeciesDto;
import com.conexa.starwars.species.mapper.SpeciesMapper;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;

/**
 * Retrieves Star Wars species from SWAPI and exposes them in this API's paginated format.
 */
@Service
public class SpeciesService {

    private final SwapiSpeciesClient swapiSpeciesClient;
    private final SpeciesMapper speciesMapper;

    public SpeciesService(SwapiSpeciesClient swapiSpeciesClient, SpeciesMapper speciesMapper) {
        this.swapiSpeciesClient = swapiSpeciesClient;
        this.speciesMapper = speciesMapper;
    }

    /**
     * Returns a page of species, optionally filtered by name (case-insensitive, partial match).
     * <p>
     * Without a filter, pagination is delegated to SWAPI. With a filter, SWAPI returns every match
     * unpaginated, so the page is sliced in memory. Results are cached; SWAPI errors are not.
     *
     * @param page 1-based page number
     * @param size page size
     * @param name optional name filter; blank means no filter
     */
    @Cacheable(cacheNames = CacheConfig.SPECIES_LIST, sync = true)
    public PageResponse<SpeciesDto> findAll(int page, int size, String name) {
        if (name == null || name.isBlank()) {
            return listPage(page, size);
        }
        List<SpeciesDto> matches = swapiSpeciesClient.findByName(name.trim()).result().stream()
                .map(speciesMapper::toDto)
                .toList();
        return PageResponse.fromList(matches, page, size);
    }

    /**
     * Returns a single species by id. Results are cached; SWAPI errors are not.
     *
     * @throws ResourceNotFoundException if SWAPI has no species with the given id
     */
    @Cacheable(cacheNames = CacheConfig.SPECIES_BY_ID, sync = true)
    public SpeciesDto findById(int id) {
        try {
            return speciesMapper.toDto(swapiSpeciesClient.findById(id).result());
        } catch (HttpClientErrorException.NotFound ex) {
            throw new ResourceNotFoundException("Species", id);
        }
    }

    private PageResponse<SpeciesDto> listPage(int page, int size) {
        SwapiPageResponse<SwapiSpecies> response = swapiSpeciesClient.findAll(page, size, true);
        // SWAPI answers out-of-range pages with the last page's data instead of an empty list
        List<SpeciesDto> content = page > response.totalPages()
                ? List.of()
                : response.results().stream().map(speciesMapper::toDto).toList();
        return PageResponse.of(content, page, size, response.totalRecords());
    }
}
