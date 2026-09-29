package com.conexa.starwars.common.swapi.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import tools.jackson.databind.PropertyNamingStrategies;
import tools.jackson.databind.annotation.JsonNaming;

/**
 * Mirror of the {@code properties} object of a SWAPI starship.
 * Values are kept as strings because SWAPI returns free-form text (e.g. "343,342", "30-165", "n/a", "unknown").
 * {@code MGLT} is the only upper-case field in SWAPI, so it is mapped explicitly.
 * Relations are kept as SWAPI URLs; mappers convert them into ids (ADR 0022).
 */
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record SwapiStarship(String name, String model, String manufacturer, String starshipClass,
        String costInCredits, String length, String crew, String passengers, String cargoCapacity,
        String consumables, String maxAtmospheringSpeed, String hyperdriveRating,
        @JsonProperty("MGLT") String mglt,
        List<String> pilots, List<String> films) {
}
