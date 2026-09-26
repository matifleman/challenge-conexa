package com.conexa.starwars.vehicles.mapper;

import com.conexa.starwars.common.swapi.dto.SwapiResource;
import com.conexa.starwars.common.swapi.dto.SwapiVehicle;
import com.conexa.starwars.vehicles.dto.VehicleDto;
import org.springframework.stereotype.Component;

/**
 * Translates SWAPI vehicle resources into {@link VehicleDto}, keeping SWAPI's model
 * out of this API's public contract.
 */
@Component
public class VehicleMapper {

    public VehicleDto toDto(SwapiResource<SwapiVehicle> resource) {
        SwapiVehicle vehicle = resource.properties();
        return new VehicleDto(
                resource.uid(),
                vehicle.name(),
                vehicle.model(),
                vehicle.manufacturer(),
                vehicle.vehicleClass(),
                vehicle.costInCredits(),
                vehicle.length(),
                vehicle.crew(),
                vehicle.passengers(),
                vehicle.cargoCapacity(),
                vehicle.consumables(),
                vehicle.maxAtmospheringSpeed());
    }
}
