package com.conexa.starwars.films.dto;

import java.time.LocalDate;
import java.util.List;

/**
 * Public representation of a Star Wars film exposed by this API.
 * Relations are ids of this API, resolvable through the endpoint of the related resource
 * (e.g. {@code characterIds} through {@code /api/v1/people/{id}}).
 */
public record FilmDto(String id, String title, int episodeId, String director, String producer,
                      LocalDate releaseDate, String openingCrawl,
                      List<String> characterIds, List<String> starshipIds, List<String> vehicleIds,
                      List<String> speciesIds) {
}
