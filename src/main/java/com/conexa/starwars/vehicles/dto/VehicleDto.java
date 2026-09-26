package com.conexa.starwars.vehicles.dto;

/**
 * Public representation of a Star Wars vehicle exposed by this API.
 * Attributes are strings because the source reports them as free-form text (e.g. "n/a", "unknown").
 */
public record VehicleDto(String id, String name, String model, String manufacturer, String vehicleClass,
        String costInCredits, String length, String crew, String passengers, String cargoCapacity,
        String consumables, String maxAtmospheringSpeed) {
}
