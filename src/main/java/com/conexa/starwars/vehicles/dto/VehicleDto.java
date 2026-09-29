package com.conexa.starwars.vehicles.dto;

import java.util.List;

/**
 * Public representation of a Star Wars vehicle exposed by this API.
 * Attributes are strings because the source reports them as free-form text (e.g. "n/a", "unknown").
 * Relations are ids of this API, resolvable through the endpoint of the related resource
 * (e.g. {@code pilotIds} through {@code /api/v1/people/{id}}).
 */
public record VehicleDto(String id, String name, String model, String manufacturer, String vehicleClass,
        String costInCredits, String length, String crew, String passengers, String cargoCapacity,
        String consumables, String maxAtmospheringSpeed,
        List<String> pilotIds, List<String> filmIds) {
}
