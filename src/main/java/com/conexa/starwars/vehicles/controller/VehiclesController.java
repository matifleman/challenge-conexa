package com.conexa.starwars.vehicles.controller;

import com.conexa.starwars.common.dto.PageResponse;
import com.conexa.starwars.vehicles.dto.VehicleDto;
import com.conexa.starwars.vehicles.service.VehiclesService;
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
 * REST endpoints for Star Wars vehicles. The HTTP contract is documented via OpenAPI.
 */
@Tag(name = "Vehicles", description = "Star Wars vehicles")
@RestController
@RequestMapping("/api/v1/vehicles")
public class VehiclesController {

    private final VehiclesService vehiclesService;

    public VehiclesController(VehiclesService vehiclesService) {
        this.vehiclesService = vehiclesService;
    }

    @Operation(summary = "List vehicles",
            description = "Returns a page of vehicles, optionally filtered by name.")
    @ApiResponse(responseCode = "200", description = "Page of vehicles (empty content if the page is out of range)")
    @ApiResponse(responseCode = "400", description = "Invalid pagination parameters",
            content = @Content(mediaType = "application/problem+json",
                    schema = @Schema(implementation = ProblemDetail.class)))
    @GetMapping
    public PageResponse<VehicleDto> findAll(
            @Parameter(description = "Page number, starting at 1", example = "1")
            @RequestParam(defaultValue = "1") @Min(1) int page,
            @Parameter(description = "Number of elements per page", example = "10")
            @RequestParam(defaultValue = "10") @Min(1) @Max(100) int size,
            @Parameter(description = "Name filter (partial, case-insensitive match)", example = "speeder")
            @RequestParam(required = false) String name) {
        return vehiclesService.findAll(page, size, name);
    }

    @Operation(summary = "Get a vehicle by id")
    @ApiResponse(responseCode = "200", description = "The vehicle")
    @ApiResponse(responseCode = "400", description = "The id is not a number",
            content = @Content(mediaType = "application/problem+json",
                    schema = @Schema(implementation = ProblemDetail.class)))
    @ApiResponse(responseCode = "404", description = "No vehicle with the given id",
            content = @Content(mediaType = "application/problem+json",
                    schema = @Schema(implementation = ProblemDetail.class)))
    @GetMapping("/{id}")
    public VehicleDto findById(@Parameter(description = "Vehicle id", example = "4") @PathVariable int id) {
        return vehiclesService.findById(id);
    }
}
