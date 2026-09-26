package com.conexa.starwars.people.controller;

import com.conexa.starwars.common.dto.PageResponse;
import com.conexa.starwars.people.dto.PersonDto;
import com.conexa.starwars.people.service.PeopleService;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST endpoints for Star Wars characters. The HTTP contract is documented via OpenAPI.
 */
@RestController
@RequestMapping("/api/v1/people")
public class PeopleController {

    private final PeopleService peopleService;

    public PeopleController(PeopleService peopleService) {
        this.peopleService = peopleService;
    }

    @GetMapping
    public PageResponse<PersonDto> findAll(
            @RequestParam(defaultValue = "1") @Min(1) int page,
            @RequestParam(defaultValue = "10") @Min(1) @Max(100) int size,
            @RequestParam(required = false) String name) {
        return peopleService.findAll(page, size, name);
    }

    @GetMapping("/{id}")
    public PersonDto findById(@PathVariable int id) {
        return peopleService.findById(id);
    }
}
