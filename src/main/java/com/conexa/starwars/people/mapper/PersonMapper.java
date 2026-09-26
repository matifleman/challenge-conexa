package com.conexa.starwars.people.mapper;

import com.conexa.starwars.common.swapi.dto.SwapiPerson;
import com.conexa.starwars.common.swapi.dto.SwapiResource;
import com.conexa.starwars.people.dto.PersonDto;
import org.springframework.stereotype.Component;

/**
 * Translates SWAPI person resources into {@link PersonDto}, keeping SWAPI's model
 * out of this API's public contract.
 */
@Component
public class PersonMapper {

    public PersonDto toDto(SwapiResource<SwapiPerson> resource) {
        SwapiPerson person = resource.properties();
        return new PersonDto(
                resource.uid(),
                person.name(),
                person.height(),
                person.mass(),
                person.hairColor(),
                person.skinColor(),
                person.eyeColor(),
                person.birthYear(),
                person.gender());
    }
}
