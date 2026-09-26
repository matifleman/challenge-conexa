package com.conexa.starwars.common.swapi;

import com.conexa.starwars.common.swapi.dto.SwapiItemResponse;
import com.conexa.starwars.common.swapi.dto.SwapiListResponse;
import com.conexa.starwars.common.swapi.dto.SwapiPageResponse;
import com.conexa.starwars.common.swapi.dto.SwapiStarship;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;

/**
 * Declarative client for SWAPI's starship endpoints. Implemented at runtime by a proxy
 * created in {@link SwapiClientConfig}.
 */
@HttpExchange("/starships")
public interface SwapiStarshipsClient {

    @GetExchange
    SwapiPageResponse<SwapiStarship> findAll(@RequestParam int page, @RequestParam int limit,
            @RequestParam boolean expanded);

    @GetExchange
    SwapiListResponse<SwapiStarship> findByName(@RequestParam String name);

    @GetExchange("/{id}")
    SwapiItemResponse<SwapiStarship> findById(@PathVariable int id);
}
