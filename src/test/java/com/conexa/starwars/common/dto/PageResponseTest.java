package com.conexa.starwars.common.dto;

import java.util.List;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PageResponseTest {

    private static final List<Integer> ELEMENTS = List.of(1, 2, 3, 4, 5);

    @Test
    void fromListReturnsFirstPage() {
        PageResponse<Integer> page = PageResponse.fromList(ELEMENTS, 1, 2);

        assertThat(page.content()).containsExactly(1, 2);
        assertThat(page.totalElements()).isEqualTo(5);
        assertThat(page.totalPages()).isEqualTo(3);
    }

    @Test
    void fromListReturnsPartialLastPage() {
        PageResponse<Integer> page = PageResponse.fromList(ELEMENTS, 3, 2);

        assertThat(page.content()).containsExactly(5);
    }

    @Test
    void fromListReturnsEmptyContentWhenPageIsOutOfRange() {
        PageResponse<Integer> page = PageResponse.fromList(ELEMENTS, 4, 2);

        assertThat(page.content()).isEmpty();
        assertThat(page.totalPages()).isEqualTo(3);
    }

    @Test
    void fromListHandlesEmptySource() {
        PageResponse<Integer> page = PageResponse.fromList(List.of(), 1, 10);

        assertThat(page.content()).isEmpty();
        assertThat(page.totalElements()).isZero();
        assertThat(page.totalPages()).isZero();
    }
}
