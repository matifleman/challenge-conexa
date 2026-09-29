package com.conexa.starwars.planets.controller;

import com.conexa.starwars.common.dto.PageResponse;
import com.conexa.starwars.planets.dto.PlanetDto;
import com.conexa.starwars.planets.service.PlanetsService;
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
 * REST endpoints for Star Wars planets. The HTTP contract is documented via OpenAPI.
 */
@Tag(name = "Planets", description = "Star Wars planets")
@RestController
@RequestMapping("/api/v1/planets")
public class PlanetsController {

    private final PlanetsService planetsService;

    public PlanetsController(PlanetsService planetsService) {
        this.planetsService = planetsService;
    }

    @Operation(summary = "List planets",
            description = "Returns a page of planets, optionally filtered by name.")
    @ApiResponse(responseCode = "200", description = "Page of planets (empty content if the page is out of range)")
    @ApiResponse(responseCode = "400", description = "Invalid pagination parameters",
            content = @Content(mediaType = "application/problem+json",
                    schema = @Schema(implementation = ProblemDetail.class)))
    @GetMapping
    public PageResponse<PlanetDto> findAll(
            @Parameter(description = "Page number, starting at 1", example = "1")
            @RequestParam(defaultValue = "1") @Min(1) int page,
            @Parameter(description = "Number of elements per page", example = "10")
            @RequestParam(defaultValue = "10") @Min(1) @Max(100) int size,
            @Parameter(description = "Name filter (partial, case-insensitive match)", example = "tat")
            @RequestParam(required = false) String name) {
        return planetsService.findAll(page, size, name);
    }

    @Operation(summary = "Get a planet by id")
    @ApiResponse(responseCode = "200", description = "The planet")
    @ApiResponse(responseCode = "400", description = "The id is not a number",
            content = @Content(mediaType = "application/problem+json",
                    schema = @Schema(implementation = ProblemDetail.class)))
    @ApiResponse(responseCode = "404", description = "No planet with the given id",
            content = @Content(mediaType = "application/problem+json",
                    schema = @Schema(implementation = ProblemDetail.class)))
    @GetMapping("/{id}")
    public PlanetDto findById(@Parameter(description = "Planet id", example = "1") @PathVariable int id) {
        return planetsService.findById(id);
    }
}
