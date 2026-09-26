package com.conexa.starwars.common.swapi.dto;

import java.util.List;

/**
 * Mirror of SWAPI's non-paginated list response, returned by name searches and by
 * resources SWAPI does not paginate (e.g. films). Note the singular {@code result} key.
 *
 * @param <T> the entity-specific properties type
 */
public record SwapiListResponse<T>(List<SwapiResource<T>> result) {
}
