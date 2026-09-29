package com.conexa.starwars.common.swapi.dto;

import tools.jackson.databind.PropertyNamingStrategies;
import tools.jackson.databind.annotation.JsonNaming;

/**
 * Mirror of the {@code properties} object of a SWAPI planet.
 * Values are kept as strings because SWAPI returns free-form text (e.g. "1 standard", "N/A", "unknown").
 * Unlike its documentation, SWAPI sends no relations for planets ({@code residents}, {@code films}); see ADR 0024.
 */
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record SwapiPlanet(String name, String diameter, String rotationPeriod, String orbitalPeriod,
        String gravity, String population, String climate, String terrain, String surfaceWater) {
}
