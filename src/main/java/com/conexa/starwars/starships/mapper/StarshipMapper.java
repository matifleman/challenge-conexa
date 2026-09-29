package com.conexa.starwars.starships.mapper;

import com.conexa.starwars.common.swapi.SwapiUrls;
import com.conexa.starwars.common.swapi.dto.SwapiResource;
import com.conexa.starwars.common.swapi.dto.SwapiStarship;
import com.conexa.starwars.starships.dto.StarshipDto;
import org.springframework.stereotype.Component;

/**
 * Translates SWAPI starship resources into {@link StarshipDto}, keeping SWAPI's model
 * out of this API's public contract.
 */
@Component
public class StarshipMapper {

    public StarshipDto toDto(SwapiResource<SwapiStarship> resource) {
        SwapiStarship starship = resource.properties();
        return new StarshipDto(
                resource.uid(),
                starship.name(),
                starship.model(),
                starship.manufacturer(),
                starship.starshipClass(),
                starship.costInCredits(),
                starship.length(),
                starship.crew(),
                starship.passengers(),
                starship.cargoCapacity(),
                starship.consumables(),
                starship.maxAtmospheringSpeed(),
                starship.hyperdriveRating(),
                starship.mglt(),
                SwapiUrls.idsOf(starship.pilots()),
                SwapiUrls.idsOf(starship.films()));
    }
}
