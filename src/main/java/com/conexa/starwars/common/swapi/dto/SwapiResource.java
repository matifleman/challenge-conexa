package com.conexa.starwars.common.swapi.dto;

/**
 * A single SWAPI resource envelope: SWAPI nests entity attributes under {@code properties}
 * and exposes the identifier separately as {@code uid}.
 *
 * @param <T> the entity-specific properties type
 */
public record SwapiResource<T>(String uid, T properties) {
}
