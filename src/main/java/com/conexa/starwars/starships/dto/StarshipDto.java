package com.conexa.starwars.starships.dto;

import java.util.List;

/**
 * Public representation of a Star Wars starship exposed by this API.
 * Attributes are strings because the source reports them as free-form text (e.g. "n/a", "unknown", "30-165").
 * Relations are ids of this API, resolvable through the endpoint of the related resource
 * (e.g. {@code pilotIds} through {@code /api/v1/people/{id}}).
 */
public record StarshipDto(String id, String name, String model, String manufacturer, String starshipClass,
        String costInCredits, String length, String crew, String passengers, String cargoCapacity,
        String consumables, String maxAtmospheringSpeed, String hyperdriveRating, String mglt,
        List<String> pilotIds, List<String> filmIds) {
}
