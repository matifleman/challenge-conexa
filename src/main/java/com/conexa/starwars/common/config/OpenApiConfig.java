package com.conexa.starwars.common.config;

import java.util.Map;

import io.swagger.v3.core.converter.ModelConverters;
import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
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

    @Bean
    OpenAPI starWarsOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("Star Wars API")
                        .version("v1")
                        .description("Paginated listing and lookup of Star Wars people, films, starships and vehicles, "
                                + "backed by SWAPI. Errors follow RFC 9457 (ProblemDetail)."))
                .externalDocs(new ExternalDocumentation()
                        .description("SWAPI documentation")
                        .url("https://www.swapi.tech/documentation"));
    }

    /**
     * Documents the errors that any endpoint can return, so they are not repeated on every operation:
     * an unexpected failure (500) and failures of the upstream Star Wars API (502, 503, 504).
     */
    @Bean
    OpenApiCustomizer commonErrorResponses() {
        return openApi -> {
            Map<String, Schema> problemDetailSchemas = ModelConverters.getInstance().read(ProblemDetail.class);
            problemDetailSchemas.forEach(openApi.getComponents()::addSchemas);

            openApi.getPaths().values().forEach(pathItem -> pathItem.readOperations().forEach(operation -> {
                ApiResponses responses = operation.getResponses();
                responses.addApiResponse("500", problem("Unexpected error"));
                responses.addApiResponse("502", problem("The Star Wars API responded with an error"));
                responses.addApiResponse("503", problem("The Star Wars API could not be reached"));
                responses.addApiResponse("504", problem("The Star Wars API did not respond in time"));
            }));
        };
    }

    private static ApiResponse problem(String description) {
        Schema<?> schema = new Schema<>().$ref("#/components/schemas/" + PROBLEM_DETAIL_SCHEMA);
        return new ApiResponse()
                .description(description)
                .content(new Content().addMediaType(PROBLEM_JSON, new MediaType().schema(schema)));
    }
}
