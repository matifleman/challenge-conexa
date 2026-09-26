package com.conexa.starwars.starships.service;

import java.util.List;

import com.conexa.starwars.common.dto.PageResponse;
import com.conexa.starwars.common.exception.ResourceNotFoundException;
import com.conexa.starwars.common.swapi.SwapiStarshipsClient;
import com.conexa.starwars.common.swapi.dto.SwapiPageResponse;
import com.conexa.starwars.common.swapi.dto.SwapiStarship;
import com.conexa.starwars.starships.dto.StarshipDto;
import com.conexa.starwars.starships.mapper.StarshipMapper;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;

/**
 * Retrieves Star Wars starships from SWAPI and exposes them in this API's paginated format.
 */
@Service
public class StarshipsService {

    private final SwapiStarshipsClient swapiStarshipsClient;
    private final StarshipMapper starshipMapper;

    public StarshipsService(SwapiStarshipsClient swapiStarshipsClient, StarshipMapper starshipMapper) {
        this.swapiStarshipsClient = swapiStarshipsClient;
        this.starshipMapper = starshipMapper;
    }

    /**
     * Returns a page of starships, optionally filtered by name (case-insensitive, partial match).
     * <p>
     * Without a filter, pagination is delegated to SWAPI. With a filter, SWAPI returns every match
     * unpaginated, so the page is sliced in memory.
     *
     * @param page 1-based page number
     * @param size page size
     * @param name optional name filter; blank means no filter
     */
    public PageResponse<StarshipDto> findAll(int page, int size, String name) {
        if (name == null || name.isBlank()) {
            return listPage(page, size);
        }
        List<StarshipDto> matches = swapiStarshipsClient.findByName(name.trim()).result().stream()
                .map(starshipMapper::toDto)
                .toList();
        return PageResponse.fromList(matches, page, size);
    }

    /**
     * Returns a single starship by id.
     *
     * @throws ResourceNotFoundException if SWAPI has no starship with the given id
     */
    public StarshipDto findById(int id) {
        try {
            return starshipMapper.toDto(swapiStarshipsClient.findById(id).result());
        } catch (HttpClientErrorException.NotFound ex) {
            throw new ResourceNotFoundException("Starship", id);
        }
    }

    private PageResponse<StarshipDto> listPage(int page, int size) {
        SwapiPageResponse<SwapiStarship> response = swapiStarshipsClient.findAll(page, size, true);
        // SWAPI answers out-of-range pages with the last page's data instead of an empty list
        List<StarshipDto> content = page > response.totalPages()
                ? List.of()
                : response.results().stream().map(starshipMapper::toDto).toList();
        return PageResponse.of(content, page, size, response.totalRecords());
    }
}
