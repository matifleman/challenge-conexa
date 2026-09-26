package com.conexa.starwars.common.swapi.dto;

import tools.jackson.databind.PropertyNamingStrategies;
import tools.jackson.databind.annotation.JsonNaming;

/**
 * Mirror of the {@code properties} object of a SWAPI vehicle.
 * Values are kept as strings because SWAPI returns free-form text (e.g. "n/a", "unknown").
 */
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record SwapiVehicle(String name, String model, String manufacturer, String vehicleClass,
        String costInCredits, String length, String crew, String passengers, String cargoCapacity,
        String consumables, String maxAtmospheringSpeed) {
}
