package com.conexa.starwars.common.swapi.dto;

import java.util.List;

import tools.jackson.databind.PropertyNamingStrategies;
import tools.jackson.databind.annotation.JsonNaming;

/**
 * Mirror of the {@code properties} object of a SWAPI person.
 * Numeric-looking values are kept as strings because SWAPI returns them as text and may use "unknown".
 * Relations are kept as SWAPI URLs; mappers convert them into ids (ADR 0022).
 */
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record SwapiPerson(String name, String height, String mass, String hairColor,
        String skinColor, String eyeColor, String birthYear, String gender,
        List<String> films, List<String> starships, List<String> vehicles, String homeworld) {
}
