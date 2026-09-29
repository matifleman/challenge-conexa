package com.conexa.starwars.planets.dto;

/**
 * Public representation of a Star Wars planet exposed by this API.
 * Attributes are strings because the source reports them as free-form text (e.g. "1 standard", "N/A", "unknown");
 * climate and terrain are comma-separated lists kept as reported.
 * Planets carry no relations: the source does not report their residents or films.
 */
public record PlanetDto(String id, String name, String diameter, String rotationPeriod, String orbitalPeriod,
        String gravity, String population, String climate, String terrain, String surfaceWater) {
}
