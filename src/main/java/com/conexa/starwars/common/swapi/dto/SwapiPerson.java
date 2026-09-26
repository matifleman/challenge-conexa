package com.conexa.starwars.common.swapi.dto;

import tools.jackson.databind.PropertyNamingStrategies;
import tools.jackson.databind.annotation.JsonNaming;

/**
 * Mirror of the {@code properties} object of a SWAPI person.
 * Numeric-looking values are kept as strings because SWAPI returns them as text and may use "unknown".
 */
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record SwapiPerson(String name, String height, String mass, String hairColor,
        String skinColor, String eyeColor, String birthYear, String gender) {
}
