package com.conexa.starwars.people.service;

import java.util.List;

import com.conexa.starwars.common.config.CacheConfig;
import com.conexa.starwars.common.dto.PageResponse;
import com.conexa.starwars.common.exception.ResourceNotFoundException;
import com.conexa.starwars.common.swapi.SwapiPeopleClient;
import com.conexa.starwars.common.swapi.dto.SwapiPageResponse;
import com.conexa.starwars.common.swapi.dto.SwapiPerson;
import com.conexa.starwars.people.dto.PersonDto;
import com.conexa.starwars.people.mapper.PersonMapper;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;

/**
 * Retrieves Star Wars characters from SWAPI and exposes them in this API's paginated format.
 */
@Service
public class PeopleService {

    private final SwapiPeopleClient swapiPeopleClient;
    private final PersonMapper personMapper;

    public PeopleService(SwapiPeopleClient swapiPeopleClient, PersonMapper personMapper) {
        this.swapiPeopleClient = swapiPeopleClient;
        this.personMapper = personMapper;
    }

    /**
     * Returns a page of people, optionally filtered by name (case-insensitive, partial match).
     * <p>
     * Without a filter, pagination is delegated to SWAPI. With a filter, SWAPI returns every match
     * unpaginated, so the page is sliced in memory. Results are cached; SWAPI errors are not.
     *
     * @param page 1-based page number
     * @param size page size
     * @param name optional name filter; blank means no filter
     */
    @Cacheable(cacheNames = CacheConfig.PEOPLE_LIST, sync = true)
    public PageResponse<PersonDto> findAll(int page, int size, String name) {
        if (name == null || name.isBlank()) {
            return listPage(page, size);
        }
        List<PersonDto> matches = swapiPeopleClient.findByName(name.trim()).result().stream()
                .map(personMapper::toDto)
                .toList();
        return PageResponse.fromList(matches, page, size);
    }

    /**
     * Returns a single person by id. Results are cached; SWAPI errors are not.
     *
     * @throws ResourceNotFoundException if SWAPI has no person with the given id
     */
    @Cacheable(cacheNames = CacheConfig.PEOPLE_BY_ID, sync = true)
    public PersonDto findById(int id) {
        try {
            return personMapper.toDto(swapiPeopleClient.findById(id).result());
        } catch (HttpClientErrorException.NotFound ex) {
            throw new ResourceNotFoundException("Person", id);
        }
    }

    private PageResponse<PersonDto> listPage(int page, int size) {
        SwapiPageResponse<SwapiPerson> response = swapiPeopleClient.findAll(page, size, true);
        // SWAPI answers out-of-range pages with the last page's data instead of an empty list
        List<PersonDto> content = page > response.totalPages()
                ? List.of()
                : response.results().stream().map(personMapper::toDto).toList();
        return PageResponse.of(content, page, size, response.totalRecords());
    }
}
