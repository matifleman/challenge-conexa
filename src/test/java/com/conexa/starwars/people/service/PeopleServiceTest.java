package com.conexa.starwars.people.service;

import java.util.List;

import com.conexa.starwars.common.dto.PageResponse;
import com.conexa.starwars.common.exception.ResourceNotFoundException;
import com.conexa.starwars.common.swapi.SwapiPeopleClient;
import com.conexa.starwars.common.swapi.dto.SwapiItemResponse;
import com.conexa.starwars.common.swapi.dto.SwapiListResponse;
import com.conexa.starwars.common.swapi.dto.SwapiPageResponse;
import com.conexa.starwars.common.swapi.dto.SwapiPerson;
import com.conexa.starwars.common.swapi.dto.SwapiResource;
import com.conexa.starwars.people.dto.PersonDto;
import com.conexa.starwars.people.mapper.PersonMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PeopleServiceTest {

    @Mock
    private SwapiPeopleClient swapiPeopleClient;

    private PeopleService peopleService;

    @BeforeEach
    void setUp() {
        peopleService = new PeopleService(swapiPeopleClient, new PersonMapper());
    }

    @Test
    void findAllWithoutNameDelegatesPaginationToSwapi() {
        when(swapiPeopleClient.findAll(2, 1, true))
                .thenReturn(new SwapiPageResponse<>(82, 82, List.of(person("2", "C-3PO"))));

        PageResponse<PersonDto> result = peopleService.findAll(2, 1, null);

        assertThat(result.content()).extracting(PersonDto::name).containsExactly("C-3PO");
        assertThat(result.page()).isEqualTo(2);
        assertThat(result.totalElements()).isEqualTo(82);
        assertThat(result.totalPages()).isEqualTo(82);
        verify(swapiPeopleClient, never()).findByName(any());
    }

    @Test
    void findAllBeyondLastPageReturnsEmptyContent() {
        when(swapiPeopleClient.findAll(100, 10, true))
                .thenReturn(new SwapiPageResponse<>(82, 9, List.of(person("81", "Tion Medon"))));

        PageResponse<PersonDto> result = peopleService.findAll(100, 10, null);

        assertThat(result.content()).isEmpty();
        assertThat(result.totalElements()).isEqualTo(82);
    }

    @Test
    void findAllWithNameSearchesAndPaginatesInMemory() {
        when(swapiPeopleClient.findByName("sky")).thenReturn(new SwapiListResponse<>(List.of(
                person("1", "Luke Skywalker"),
                person("11", "Anakin Skywalker"),
                person("43", "Shmi Skywalker"))));

        PageResponse<PersonDto> result = peopleService.findAll(2, 2, " sky ");

        assertThat(result.content()).extracting(PersonDto::name).containsExactly("Shmi Skywalker");
        assertThat(result.totalElements()).isEqualTo(3);
        assertThat(result.totalPages()).isEqualTo(2);
        verify(swapiPeopleClient, never()).findAll(anyInt(), anyInt(), anyBoolean());
    }

    @Test
    void findAllWithBlankNameListsWithoutFilter() {
        when(swapiPeopleClient.findAll(1, 10, true))
                .thenReturn(new SwapiPageResponse<>(0, 0, List.of()));

        peopleService.findAll(1, 10, "  ");

        verify(swapiPeopleClient, never()).findByName(any());
    }

    @Test
    void findByIdReturnsMappedPerson() {
        when(swapiPeopleClient.findById(1))
                .thenReturn(new SwapiItemResponse<>(person("1", "Luke Skywalker")));

        PersonDto result = peopleService.findById(1);

        assertThat(result.id()).isEqualTo("1");
        assertThat(result.name()).isEqualTo("Luke Skywalker");
        assertThat(result.hairColor()).isEqualTo("blond");
        assertThat(result.filmIds()).containsExactly("1", "2");
        assertThat(result.starshipIds()).containsExactly("12");
        assertThat(result.vehicleIds()).containsExactly("14");
        assertThat(result.homeworldId()).isEqualTo("1");
    }

    @Test
    void findByIdThrowsNotFoundWhenSwapiReturns404() {
        when(swapiPeopleClient.findById(999)).thenThrow(
                HttpClientErrorException.create(HttpStatus.NOT_FOUND, "Not Found", HttpHeaders.EMPTY, null, null));

        assertThatThrownBy(() -> peopleService.findById(999))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Person with id 999 not found");
    }

    private static SwapiResource<SwapiPerson> person(String uid, String name) {
        return new SwapiResource<>(uid,
                new SwapiPerson(name, "172", "77", "blond", "fair", "blue", "19BBY", "male",
                        List.of("https://www.swapi.tech/api/films/1", "https://www.swapi.tech/api/films/2"),
                        List.of("https://www.swapi.tech/api/starships/12"),
                        List.of("https://www.swapi.tech/api/vehicles/14"),
                        "https://www.swapi.tech/api/planets/1"));
    }
}
