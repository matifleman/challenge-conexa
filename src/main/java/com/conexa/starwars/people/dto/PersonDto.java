package com.conexa.starwars.people.dto;

/**
 * Public representation of a Star Wars character exposed by this API.
 * Physical attributes are strings because the source may report them as "unknown".
 */
public record PersonDto(String id, String name, String height, String mass, String hairColor,
                        String skinColor, String eyeColor, String birthYear, String gender) {
}
