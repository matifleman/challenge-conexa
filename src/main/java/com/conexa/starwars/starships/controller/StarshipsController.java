package com.conexa.starwars.starships.controller;

import com.conexa.starwars.common.dto.PageResponse;
import com.conexa.starwars.starships.dto.StarshipDto;
import com.conexa.starwars.starships.service.StarshipsService;
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
 * REST endpoints for Star Wars starships. The HTTP contract is documented via OpenAPI.
 */
@Tag(name = "Starships", description = "Star Wars starships")
@RestController
@RequestMapping("/api/v1/starships")
public class StarshipsController {

    private final StarshipsService starshipsService;

    public StarshipsController(StarshipsService starshipsService) {
        this.starshipsService = starshipsService;
    }

    @Operation(summary = "List starships",
            description = "Returns a page of starships, optionally filtered by name.")
    @ApiResponse(responseCode = "200", description = "Page of starships (empty content if the page is out of range)")
    @ApiResponse(responseCode = "400", description = "Invalid pagination parameters",
            content = @Content(mediaType = "application/problem+json",
                    schema = @Schema(implementation = ProblemDetail.class)))
    @GetMapping
    public PageResponse<StarshipDto> findAll(
            @Parameter(description = "Page number, starting at 1", example = "1")
            @RequestParam(defaultValue = "1") @Min(1) int page,
            @Parameter(description = "Number of elements per page", example = "10")
            @RequestParam(defaultValue = "10") @Min(1) @Max(100) int size,
            @Parameter(description = "Name filter (partial, case-insensitive match)", example = "star")
            @RequestParam(required = false) String name) {
        return starshipsService.findAll(page, size, name);
    }

    @Operation(summary = "Get a starship by id")
    @ApiResponse(responseCode = "200", description = "The starship")
    @ApiResponse(responseCode = "400", description = "The id is not a number",
            content = @Content(mediaType = "application/problem+json",
                    schema = @Schema(implementation = ProblemDetail.class)))
    @ApiResponse(responseCode = "404", description = "No starship with the given id",
            content = @Content(mediaType = "application/problem+json",
                    schema = @Schema(implementation = ProblemDetail.class)))
    @GetMapping("/{id}")
    public StarshipDto findById(@Parameter(description = "Starship id", example = "9") @PathVariable int id) {
        return starshipsService.findById(id);
    }
}
