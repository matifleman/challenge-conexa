package com.conexa.starwars.common.dto;

import java.util.List;

/**
 * Paginated response returned by every listing endpoint, independent of the upstream (SWAPI)
 * format so the public contract does not change if the data source does. Pages are 1-based.
 *
 * @param content       the elements of the requested page
 * @param page          the requested page number, starting at 1
 * @param size          the requested page size
 * @param totalElements the total number of elements across all pages
 * @param totalPages    the total number of pages
 * @param <T>           the element type
 */
public record PageResponse<T>(List<T> content, int page, int size, long totalElements, int totalPages) {

    public PageResponse {
        content = List.copyOf(content);
    }

    public static <T> PageResponse<T> of(List<T> content, int page, int size, long totalElements) {
        int totalPages = (int) Math.ceil((double) totalElements / size);
        return new PageResponse<>(content, page, size, totalElements, totalPages);
    }

    /**
     * Builds a page by slicing a complete in-memory list. Used when the upstream source does not
     * paginate (SWAPI name searches and films). A page beyond the last one yields empty content.
     */
    public static <T> PageResponse<T> fromList(List<T> all, int page, int size) {
        int from = (int) Math.min((long) (page - 1) * size, all.size());
        int to = Math.min(from + size, all.size());
        return of(all.subList(from, to), page, size, all.size());
    }
}
