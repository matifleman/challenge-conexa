package com.conexa.starwars.common.swapi.dto;

import java.time.LocalDate;
import java.util.List;

import tools.jackson.databind.PropertyNamingStrategies;
import tools.jackson.databind.annotation.JsonNaming;

/**
 * Mirror of the {@code properties} object of a SWAPI film.
 * Unlike other resources, SWAPI sends {@code episode_id} as a number and {@code release_date}
 * as an ISO date, so they are mapped to typed fields.
 * Relations are kept as SWAPI URLs; mappers convert them into ids (ADR 0022).
 */
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record SwapiFilm(String title, int episodeId, String director, String producer,
        LocalDate releaseDate, String openingCrawl,
        List<String> characters, List<String> starships, List<String> vehicles, List<String> species) {
}
