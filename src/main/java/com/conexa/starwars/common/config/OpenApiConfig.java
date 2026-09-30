package com.conexa.starwars.common.config;

import java.util.Map;

import io.swagger.v3.core.converter.ModelConverters;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.ProblemDetail;

/**
 * OpenAPI metadata served by springdoc at {@code /v3/api-docs} and rendered by Swagger UI.
 */
@Configuration
public class OpenApiConfig {

    private static final String PROBLEM_JSON = "application/problem+json";
    private static final String PROBLEM_DETAIL_SCHEMA = "ProblemDetail";
    private static final String BEARER_AUTH = "bearerAuth";
    private static final String AUTH_PATH = "/api/v1/auth/";

    @Bean
    OpenAPI starWarsOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("Star Wars API")
                        .version("v1")
                        .description("Paginated listing and lookup of Star Wars people, films, starships, "
                                + "vehicles, species and planets, backed by SWAPI. "
                                + "Errors follow RFC 9457 (ProblemDetail). "
                                + "Register, log in and use the access token with the Authorize button."))
                .externalDocs(new ExternalDocumentation()
                        .description("SWAPI documentation")
                        .url("https://www.swapi.tech/documentation"))
                .components(new Components().addSecuritySchemes(BEARER_AUTH, new SecurityScheme()
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")
                        .description("Access token returned by POST /api/v1/auth/login")))
                .addSecurityItem(new SecurityRequirement().addList(BEARER_AUTH));
    }

    /**
     * Documents the errors shared by many endpoints, so they are not repeated on every operation: an unexpected
     * failure (500) everywhere, a missing or invalid token (401) on protected endpoints, and the per-user request
     * limit (429) and failures of the upstream Star Wars API (502, 503, 504) on the endpoints backed by it.
     */
    @Bean
    OpenApiCustomizer commonErrorResponses() {
        return openApi -> {
            Map<String, Schema> problemDetailSchemas = ModelConverters.getInstance().read(ProblemDetail.class);
            problemDetailSchemas.forEach(openApi.getComponents()::addSchemas);

            openApi.getPaths().forEach((path, pathItem) -> pathItem.readOperations().forEach(operation -> {
                ApiResponses responses = operation.getResponses();
                responses.addApiResponse("500", problem("Unexpected error"));
                if (requiresToken(operation)) {
                    responses.addApiResponse("401", problem("Missing, invalid or expired access token"));
                }
                if (!path.startsWith(AUTH_PATH)) {
                    responses.addApiResponse("429",
                            problem("Too many requests from this user; retry after Retry-After seconds"));
                    responses.addApiResponse("502", problem("The Star Wars API responded with an error"));
                    responses.addApiResponse("503",
                            problem("The Star Wars API could not be reached or is limiting requests"));
                    responses.addApiResponse("504", problem("The Star Wars API did not respond in time"));
                }
            }));
        };
    }

    /**
     * Operations inherit the global bearer requirement unless they declare their own; public ones declare an empty one.
     */
    private static boolean requiresToken(Operation operation) {
        return operation.getSecurity() == null || !operation.getSecurity().isEmpty();
    }

    private static ApiResponse problem(String description) {
        Schema<?> schema = new Schema<>().$ref("#/components/schemas/" + PROBLEM_DETAIL_SCHEMA);
        return new ApiResponse()
                .description(description)
                .content(new Content().addMediaType(PROBLEM_JSON, new MediaType().schema(schema)));
    }
}
