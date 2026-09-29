package com.conexa.starwars.species.mapper;

import com.conexa.starwars.common.swapi.SwapiUrls;
import com.conexa.starwars.common.swapi.dto.SwapiResource;
import com.conexa.starwars.common.swapi.dto.SwapiSpecies;
import com.conexa.starwars.species.dto.SpeciesDto;
import org.springframework.stereotype.Component;

/**
 * Translates SWAPI species resources into {@link SpeciesDto}, keeping SWAPI's model
 * out of this API's public contract.
 */
@Component
public class SpeciesMapper {

    public SpeciesDto toDto(SwapiResource<SwapiSpecies> resource) {
        SwapiSpecies species = resource.properties();
        return new SpeciesDto(
                resource.uid(),
                species.name(),
                species.classification(),
                species.designation(),
                species.averageHeight(),
                species.skinColors(),
                species.hairColors(),
                species.eyeColors(),
                species.averageLifespan(),
                species.language(),
                SwapiUrls.idsOf(species.people()),
                SwapiUrls.idOf(species.homeworld()));
    }
}
