package com.conexa.starwars.common.swapi.dto;

import java.util.List;

import tools.jackson.databind.PropertyNamingStrategies;
import tools.jackson.databind.annotation.JsonNaming;

/**
 * Mirror of SWAPI's paginated list response ({@code GET /{resource}?page=&limit=}).
 *
 * @param <T> the entity-specific properties type
 */
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record SwapiPageResponse<T>(int totalRecords, int totalPages, List<SwapiResource<T>> results) {
}
