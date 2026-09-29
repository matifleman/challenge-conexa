package com.conexa.starwars.common.swapi;

import java.util.List;
import java.util.Objects;

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
                .map(SwapiUrls::idOf)
                .filter(Objects::nonNull)
                .toList();
    }

    /**
     * Extracts the id from a single SWAPI resource URL.
     *
     * @param url SWAPI resource URL; {@code null} when SWAPI omits the relation
     * @return the id, or {@code null} if {@code url} is {@code null} or points to no resource
     */
    public static String idOf(String url) {
        if (url == null) {
            return null;
        }
        String id = url.substring(url.lastIndexOf('/') + 1);
        // SWAPI points relations without a target to ".../null" (e.g. the droids' homeworld), see ADR 0024
        return id.isEmpty() || "null".equals(id) ? null : id;
    }
}
