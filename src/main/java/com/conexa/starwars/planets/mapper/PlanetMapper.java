package com.conexa.starwars.planets.mapper;

import com.conexa.starwars.common.swapi.dto.SwapiPlanet;
import com.conexa.starwars.common.swapi.dto.SwapiResource;
import com.conexa.starwars.planets.dto.PlanetDto;
import org.springframework.stereotype.Component;

/**
 * Translates SWAPI planet resources into {@link PlanetDto}, keeping SWAPI's model
 * out of this API's public contract.
 */
@Component
public class PlanetMapper {

    public PlanetDto toDto(SwapiResource<SwapiPlanet> resource) {
        SwapiPlanet planet = resource.properties();
        return new PlanetDto(
                resource.uid(),
                planet.name(),
                planet.diameter(),
                planet.rotationPeriod(),
                planet.orbitalPeriod(),
                planet.gravity(),
                planet.population(),
                planet.climate(),
                planet.terrain(),
                planet.surfaceWater());
    }
}
