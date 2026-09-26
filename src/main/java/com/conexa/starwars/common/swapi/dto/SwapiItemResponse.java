package com.conexa.starwars.common.swapi.dto;

/**
 * Mirror of SWAPI's single-resource response ({@code GET /{resource}/{id}}).
 *
 * @param <T> the entity-specific properties type
 */
public record SwapiItemResponse<T>(SwapiResource<T> result) {
}
