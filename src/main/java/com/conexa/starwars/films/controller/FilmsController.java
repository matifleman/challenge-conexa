package com.conexa.starwars.films.controller;

import com.conexa.starwars.common.dto.PageResponse;
import com.conexa.starwars.films.dto.FilmDto;
import com.conexa.starwars.films.service.FilmsService;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST endpoints for Star Wars films. The HTTP contract is documented via OpenAPI.
 */
@RestController
@RequestMapping("/api/v1/films")
public class FilmsController {

    private final FilmsService filmsService;

    public FilmsController(FilmsService filmsService) {
        this.filmsService = filmsService;
    }

    @GetMapping
    public PageResponse<FilmDto> findAll(
            @RequestParam(defaultValue = "1") @Min(1) int page,
            @RequestParam(defaultValue = "10") @Min(1) @Max(100) int size,
            @RequestParam(required = false) String title) {
        return filmsService.findAll(page, size, title);
    }

    @GetMapping("/{id}")
    public FilmDto findById(@PathVariable int id) {
        return filmsService.findById(id);
    }
}
