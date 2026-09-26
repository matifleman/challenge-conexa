package com.conexa.starwars.films.controller;

import com.conexa.starwars.common.dto.PageResponse;
import com.conexa.starwars.films.dto.FilmDto;
import com.conexa.starwars.films.service.FilmsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST endpoints for Star Wars films. The HTTP contract is documented via OpenAPI.
 */
@Tag(name = "Films", description = "Star Wars films")
@RestController
@RequestMapping("/api/v1/films")
public class FilmsController {

    private final FilmsService filmsService;

    public FilmsController(FilmsService filmsService) {
        this.filmsService = filmsService;
    }

    @Operation(summary = "List films",
            description = "Returns a page of films, optionally filtered by title.")
    @ApiResponse(responseCode = "200", description = "Page of films (empty content if the page is out of range)")
    @ApiResponse(responseCode = "400", description = "Invalid pagination parameters",
            content = @Content(mediaType = "application/problem+json",
                    schema = @Schema(implementation = ProblemDetail.class)))
    @GetMapping
    public PageResponse<FilmDto> findAll(
            @Parameter(description = "Page number, starting at 1", example = "1")
            @RequestParam(defaultValue = "1") @Min(1) int page,
            @Parameter(description = "Number of elements per page", example = "10")
            @RequestParam(defaultValue = "10") @Min(1) @Max(100) int size,
            @Parameter(description = "Title filter (partial, case-insensitive match)", example = "hope")
            @RequestParam(required = false) String title) {
        return filmsService.findAll(page, size, title);
    }

    @Operation(summary = "Get a film by id")
    @ApiResponse(responseCode = "200", description = "The film")
    @ApiResponse(responseCode = "400", description = "The id is not a number",
            content = @Content(mediaType = "application/problem+json",
                    schema = @Schema(implementation = ProblemDetail.class)))
    @ApiResponse(responseCode = "404", description = "No film with the given id",
            content = @Content(mediaType = "application/problem+json",
                    schema = @Schema(implementation = ProblemDetail.class)))
    @GetMapping("/{id}")
    public FilmDto findById(@Parameter(description = "Film id", example = "1") @PathVariable int id) {
        return filmsService.findById(id);
    }
}
