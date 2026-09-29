package com.conexa.starwars.species.dto;

import java.util.List;

/**
 * Public representation of a Star Wars species exposed by this API.
 * Attributes are strings because the source reports them as free-form text (e.g. "n/a", "indefinite");
 * colors are comma-separated lists kept as reported.
 * {@code characterIds} are ids of this API, resolvable through {@code /api/v1/people/{id}}.
 * The source does not assign a species to every character, so the list may be incomplete.
 */
public record SpeciesDto(String id, String name, String classification, String designation,
        String averageHeight, String skinColors, String hairColors, String eyeColors, String averageLifespan,
        String language, List<String> characterIds) {
}
