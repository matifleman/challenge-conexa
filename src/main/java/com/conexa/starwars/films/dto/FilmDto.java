package com.conexa.starwars.films.dto;

import java.time.LocalDate;
/**
 * Public representation of a Star Wars film exposed by this API.
 */
public record FilmDto(String id, String title, int episodeId, String director, String producer,
                      LocalDate releaseDate, String openingCrawl) {
}
