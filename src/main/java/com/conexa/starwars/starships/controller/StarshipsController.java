package com.conexa.starwars.starships.controller;

import com.conexa.starwars.common.dto.PageResponse;
import com.conexa.starwars.starships.dto.StarshipDto;
import com.conexa.starwars.starships.service.StarshipsService;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST endpoints for Star Wars starships. The HTTP contract is documented via OpenAPI.
 */
@RestController
@RequestMapping("/api/v1/starships")
public class StarshipsController {

    private final StarshipsService starshipsService;

    public StarshipsController(StarshipsService starshipsService) {
        this.starshipsService = starshipsService;
    }

    @GetMapping
    public PageResponse<StarshipDto> findAll(
            @RequestParam(defaultValue = "1") @Min(1) int page,
            @RequestParam(defaultValue = "10") @Min(1) @Max(100) int size,
            @RequestParam(required = false) String name) {
        return starshipsService.findAll(page, size, name);
    }

    @GetMapping("/{id}")
    public StarshipDto findById(@PathVariable int id) {
        return starshipsService.findById(id);
    }
}
