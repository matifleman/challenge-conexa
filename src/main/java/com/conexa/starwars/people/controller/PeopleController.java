package com.conexa.starwars.people.controller;

import com.conexa.starwars.common.dto.PageResponse;
import com.conexa.starwars.people.dto.PersonDto;
import com.conexa.starwars.people.service.PeopleService;
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
 * REST endpoints for Star Wars characters. The HTTP contract is documented via OpenAPI.
 */
@Tag(name = "People", description = "Star Wars characters")
@RestController
@RequestMapping("/api/v1/people")
public class PeopleController {

    private final PeopleService peopleService;

    public PeopleController(PeopleService peopleService) {
        this.peopleService = peopleService;
    }

    @Operation(summary = "List people",
            description = "Returns a page of characters, optionally filtered by name.")
    @ApiResponse(responseCode = "200", description = "Page of characters (empty content if the page is out of range)")
    @ApiResponse(responseCode = "400", description = "Invalid pagination parameters",
            content = @Content(mediaType = "application/problem+json",
                    schema = @Schema(implementation = ProblemDetail.class)))
    @GetMapping
    public PageResponse<PersonDto> findAll(
            @Parameter(description = "Page number, starting at 1", example = "1")
            @RequestParam(defaultValue = "1") @Min(1) int page,
            @Parameter(description = "Number of elements per page", example = "10")
            @RequestParam(defaultValue = "10") @Min(1) @Max(100) int size,
            @Parameter(description = "Name filter (partial, case-insensitive match)", example = "sky")
            @RequestParam(required = false) String name) {
        return peopleService.findAll(page, size, name);
    }

    @Operation(summary = "Get a person by id")
    @ApiResponse(responseCode = "200", description = "The character")
    @ApiResponse(responseCode = "400", description = "The id is not a number",
            content = @Content(mediaType = "application/problem+json",
                    schema = @Schema(implementation = ProblemDetail.class)))
    @ApiResponse(responseCode = "404", description = "No character with the given id",
            content = @Content(mediaType = "application/problem+json",
                    schema = @Schema(implementation = ProblemDetail.class)))
    @GetMapping("/{id}")
    public PersonDto findById(@Parameter(description = "Character id", example = "1") @PathVariable int id) {
        return peopleService.findById(id);
    }
}
