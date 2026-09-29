package com.conexa.starwars.people.dto;

import java.util.List;

/**
 * Public representation of a Star Wars character exposed by this API.
 * Physical attributes are strings because the source may report them as "unknown".
 * Relations are ids of this API, resolvable through the endpoint of the related resource
 * (e.g. {@code filmIds} through {@code /api/v1/films/{id}}).
 */
public record PersonDto(String id, String name, String height, String mass, String hairColor,
                        String skinColor, String eyeColor, String birthYear, String gender,
                        List<String> filmIds, List<String> starshipIds, List<String> vehicleIds) {
}
