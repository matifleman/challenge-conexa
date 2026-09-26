package com.conexa.starwars.vehicles.controller;

import com.conexa.starwars.common.dto.PageResponse;
import com.conexa.starwars.vehicles.dto.VehicleDto;
import com.conexa.starwars.vehicles.service.VehiclesService;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST endpoints for Star Wars vehicles. The HTTP contract is documented via OpenAPI.
 */
@RestController
@RequestMapping("/api/v1/vehicles")
public class VehiclesController {

    private final VehiclesService vehiclesService;

    public VehiclesController(VehiclesService vehiclesService) {
        this.vehiclesService = vehiclesService;
    }

    @GetMapping
    public PageResponse<VehicleDto> findAll(
            @RequestParam(defaultValue = "1") @Min(1) int page,
            @RequestParam(defaultValue = "10") @Min(1) @Max(100) int size,
            @RequestParam(required = false) String name) {
        return vehiclesService.findAll(page, size, name);
    }

    @GetMapping("/{id}")
    public VehicleDto findById(@PathVariable int id) {
        return vehiclesService.findById(id);
    }
}
