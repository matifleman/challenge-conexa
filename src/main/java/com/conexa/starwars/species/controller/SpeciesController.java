package com.conexa.starwars.species.controller;

import com.conexa.starwars.common.dto.PageResponse;
import com.conexa.starwars.species.dto.SpeciesDto;
import com.conexa.starwars.species.service.SpeciesService;
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
 * REST endpoints for Star Wars species. The HTTP contract is documented via OpenAPI.
 */
@Tag(name = "Species", description = "Star Wars species")
@RestController
@RequestMapping("/api/v1/species")
public class SpeciesController {

    private final SpeciesService speciesService;

    public SpeciesController(SpeciesService speciesService) {
        this.speciesService = speciesService;
    }

    @Operation(summary = "List species",
            description = "Returns a page of species, optionally filtered by name.")
    @ApiResponse(responseCode = "200", description = "Page of species (empty content if the page is out of range)")
    @ApiResponse(responseCode = "400", description = "Invalid pagination parameters",
            content = @Content(mediaType = "application/problem+json",
                    schema = @Schema(implementation = ProblemDetail.class)))
    @GetMapping
    public PageResponse<SpeciesDto> findAll(
            @Parameter(description = "Page number, starting at 1", example = "1")
            @RequestParam(defaultValue = "1") @Min(1) int page,
            @Parameter(description = "Number of elements per page", example = "10")
            @RequestParam(defaultValue = "10") @Min(1) @Max(100) int size,
            @Parameter(description = "Name filter (partial, case-insensitive match)", example = "wook")
            @RequestParam(required = false) String name) {
        return speciesService.findAll(page, size, name);
    }

    @Operation(summary = "Get a species by id")
    @ApiResponse(responseCode = "200", description = "The species")
    @ApiResponse(responseCode = "400", description = "The id is not a number",
            content = @Content(mediaType = "application/problem+json",
                    schema = @Schema(implementation = ProblemDetail.class)))
    @ApiResponse(responseCode = "404", description = "No species with the given id",
            content = @Content(mediaType = "application/problem+json",
                    schema = @Schema(implementation = ProblemDetail.class)))
    @GetMapping("/{id}")
    public SpeciesDto findById(@Parameter(description = "Species id", example = "3") @PathVariable int id) {
        return speciesService.findById(id);
    }
}
