package com.conexa.starwars.films.service;

import java.util.List;

import com.conexa.starwars.common.config.CacheConfig;
import com.conexa.starwars.common.dto.PageResponse;
import com.conexa.starwars.common.exception.ResourceNotFoundException;
import com.conexa.starwars.common.swapi.SwapiFilmsClient;
import com.conexa.starwars.common.swapi.dto.SwapiFilm;
import com.conexa.starwars.common.swapi.dto.SwapiListResponse;
import com.conexa.starwars.films.dto.FilmDto;
import com.conexa.starwars.films.mapper.FilmMapper;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;

/**
 * Retrieves Star Wars films from SWAPI and exposes them in this API's paginated format.
 * <p>
 * SWAPI never paginates films, so every listing is paginated in memory.
 */
@Service
public class FilmsService {

    private final SwapiFilmsClient swapiFilmsClient;
    private final FilmMapper filmMapper;

    public FilmsService(SwapiFilmsClient swapiFilmsClient, FilmMapper filmMapper) {
        this.swapiFilmsClient = swapiFilmsClient;
        this.filmMapper = filmMapper;
    }

    /**
     * Returns a page of films, optionally filtered by title (case-insensitive, partial match).
     * Results are cached; SWAPI errors are not.
     *
     * @param page  1-based page number
     * @param size  page size
     * @param title optional title filter; blank means no filter
     */
    @Cacheable(cacheNames = CacheConfig.FILMS_LIST, sync = true)
    public PageResponse<FilmDto> findAll(int page, int size, String title) {
        SwapiListResponse<SwapiFilm> response = (title == null || title.isBlank())
                ? swapiFilmsClient.findAll()
                : swapiFilmsClient.findByTitle(title.trim());
        List<FilmDto> films = response.result().stream()
                .map(filmMapper::toDto)
                .toList();
        return PageResponse.fromList(films, page, size);
    }

    /**
     * Returns a single film by id. Results are cached; SWAPI errors are not.
     *
     * @throws ResourceNotFoundException if SWAPI has no film with the given id
     */
    @Cacheable(cacheNames = CacheConfig.FILMS_BY_ID, sync = true)
    public FilmDto findById(int id) {
        try {
            return filmMapper.toDto(swapiFilmsClient.findById(id).result());
        } catch (HttpClientErrorException.NotFound ex) {
            throw new ResourceNotFoundException("Film", id);
        }
    }
}
