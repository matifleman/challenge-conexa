package com.conexa.starwars.films.mapper;

import com.conexa.starwars.common.swapi.SwapiUrls;
import com.conexa.starwars.common.swapi.dto.SwapiFilm;
import com.conexa.starwars.common.swapi.dto.SwapiResource;
import com.conexa.starwars.films.dto.FilmDto;
import org.springframework.stereotype.Component;

/**
 * Translates SWAPI film resources into {@link FilmDto}, keeping SWAPI's model
 * out of this API's public contract.
 */
@Component
public class FilmMapper {

    public FilmDto toDto(SwapiResource<SwapiFilm> resource) {
        SwapiFilm film = resource.properties();
        return new FilmDto(
                resource.uid(),
                film.title(),
                film.episodeId(),
                film.director(),
                film.producer(),
                film.releaseDate(),
                film.openingCrawl(),
                SwapiUrls.idsOf(film.characters()),
                SwapiUrls.idsOf(film.starships()),
                SwapiUrls.idsOf(film.vehicles()),
                SwapiUrls.idsOf(film.species()),
                SwapiUrls.idsOf(film.planets()));
    }
}
