package com.conexa.starwars.common.swapi;

import java.util.List;

/**
 * Converts SWAPI resource URLs into ids of this API.
 * <p>
 * SWAPI references related resources by URL (e.g. {@code https://www.swapi.tech/api/films/1}).
 * This API exposes the same resources under the same ids, so the last path segment is the id
 * a client can use against the corresponding endpoint (ADR 0022).
 */
public final class SwapiUrls {

    private SwapiUrls() {
    }

    /**
     * Extracts the id from each SWAPI resource URL, preserving order.
     *
     * @param urls SWAPI resource URLs; {@code null} when SWAPI omits the relation
     * @return the ids, or an empty list if {@code urls} is {@code null}
     */
    public static List<String> idsOf(List<String> urls) {
        if (urls == null) {
            return List.of();
        }
        return urls.stream()
                .map(url -> url.substring(url.lastIndexOf('/') + 1))
                .toList();
    }
}
