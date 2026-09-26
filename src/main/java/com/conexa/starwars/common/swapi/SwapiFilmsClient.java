package com.conexa.starwars.common.swapi;

import com.conexa.starwars.common.swapi.dto.SwapiFilm;
import com.conexa.starwars.common.swapi.dto.SwapiItemResponse;
import com.conexa.starwars.common.swapi.dto.SwapiListResponse;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;

/**
 * Declarative client for SWAPI's film endpoints. Implemented at runtime by a proxy
 * created in {@link SwapiClientConfig}.
 * <p>
 * SWAPI never paginates films and searches them by {@code title} instead of {@code name}.
 */
@HttpExchange("/films")
public interface SwapiFilmsClient {

    @GetExchange
    SwapiListResponse<SwapiFilm> findAll();

    @GetExchange
    SwapiListResponse<SwapiFilm> findByTitle(@RequestParam String title);

    @GetExchange("/{id}")
    SwapiItemResponse<SwapiFilm> findById(@PathVariable int id);
}
