package com.conexa.starwars.common.swapi;

import com.conexa.starwars.common.swapi.dto.SwapiItemResponse;
import com.conexa.starwars.common.swapi.dto.SwapiListResponse;
import com.conexa.starwars.common.swapi.dto.SwapiPageResponse;
import com.conexa.starwars.common.swapi.dto.SwapiPerson;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;

/**
 * Declarative client for SWAPI's people endpoints. Implemented at runtime by a proxy
 * created in {@link SwapiClientConfig}.
 */
@HttpExchange("/people")
public interface SwapiPeopleClient {

    @GetExchange
    SwapiPageResponse<SwapiPerson> findAll(@RequestParam int page, @RequestParam int limit,
            @RequestParam boolean expanded);

    @GetExchange
    SwapiListResponse<SwapiPerson> findByName(@RequestParam String name);

    @GetExchange("/{id}")
    SwapiItemResponse<SwapiPerson> findById(@PathVariable int id);
}
