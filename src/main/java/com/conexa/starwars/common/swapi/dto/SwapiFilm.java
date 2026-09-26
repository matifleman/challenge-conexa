package com.conexa.starwars.common.swapi.dto;

import java.time.LocalDate;

import tools.jackson.databind.PropertyNamingStrategies;
import tools.jackson.databind.annotation.JsonNaming;

/**
 * Mirror of the {@code properties} object of a SWAPI film.
 * Unlike other resources, SWAPI sends {@code episode_id} as a number and {@code release_date}
 * as an ISO date, so they are mapped to typed fields.
 */
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record SwapiFilm(String title, int episodeId, String director, String producer,
        LocalDate releaseDate, String openingCrawl) {
}
