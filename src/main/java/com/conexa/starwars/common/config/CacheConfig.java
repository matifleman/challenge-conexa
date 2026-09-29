package com.conexa.starwars.common.config;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Configuration;

/**
 * Enables caching of SWAPI results. The caches, their size and their expiration are defined in
 * {@code spring.cache.*}; the names here must match {@code spring.cache.cache-names}.
 */
@Configuration
@EnableCaching
public class CacheConfig {

    public static final String PEOPLE_LIST = "people-list";
    public static final String PEOPLE_BY_ID = "people-by-id";
    public static final String FILMS_LIST = "films-list";
    public static final String FILMS_BY_ID = "films-by-id";
    public static final String STARSHIPS_LIST = "starships-list";
    public static final String STARSHIPS_BY_ID = "starships-by-id";
    public static final String VEHICLES_LIST = "vehicles-list";
    public static final String VEHICLES_BY_ID = "vehicles-by-id";
    public static final String SPECIES_LIST = "species-list";
    public static final String SPECIES_BY_ID = "species-by-id";
    public static final String PLANETS_LIST = "planets-list";
    public static final String PLANETS_BY_ID = "planets-by-id";
}
