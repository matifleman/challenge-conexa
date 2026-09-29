package com.conexa.starwars.common.swapi.dto;

import java.util.List;

import tools.jackson.databind.PropertyNamingStrategies;
import tools.jackson.databind.annotation.JsonNaming;

/**
 * Mirror of the {@code properties} object of a SWAPI species.
 * Values are kept as strings because SWAPI returns free-form text (e.g. "n/a", "indefinite"), and colors
 * come as comma-separated lists in a single string.
 * Relations are kept as SWAPI URLs; mappers convert them into ids (ADR 0022).
 */
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record SwapiSpecies(String name, String classification, String designation, String averageHeight,
        String skinColors, String hairColors, String eyeColors, String averageLifespan, String language,
        List<String> people, String homeworld) {
}
