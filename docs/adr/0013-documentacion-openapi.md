# 0013. Documentación de la API con springdoc-openapi

- **Estado:** Aceptada
- **Fecha:** 2026-09-26

## Contexto

La consigna pide que la aplicación esté bien documentada, y quien la evalúe necesita poder explorar y probar los endpoints sin leer el código. Además de lo que se puede inferir del código (rutas, parámetros, DTOs), la documentación tiene que explicar el comportamiento: qué hace cada endpoint, qué significa cada parámetro y qué errores puede devolver.

Los errores `500`, `502`, `503` y `504` aplican a todos los endpoints (los produce el manejador global, ADR 0012), mientras que el `400` y el `404` dependen de cada endpoint.

## Decisión

- **springdoc-openapi 3.1.1** genera la especificación OpenAPI 3.1 (`/v3/api-docs`) y sirve Swagger UI (`/swagger-ui.html`). Se usa la línea 3.x, compatible con Spring Boot 4; la 2.x es para Boot 3.
- **Información general** (título, versión, descripción y link a SWAPI) en un bean `OpenAPI`.
- **Anotaciones en los controllers:** `@Tag` por recurso; `@Operation`, `@Parameter` (con ejemplos) y `@ApiResponse` para los errores propios de cada endpoint (`400`, `404`). Los límites de los parámetros se toman de las anotaciones de validación (`@Min`, `@Max`), sin repetirlos.
- **Errores comunes documentados una sola vez:** un `OpenApiCustomizer` agrega `500`/`502`/`503`/`504` con el schema `ProblemDetail` a todas las operaciones.
- **Swagger UI habilitado explícitamente en todos los ambientes** (`springdoc.swagger-ui.enabled: true`), porque es la forma prevista de probar la API deployada.
- Las descripciones están en inglés, como el resto del código.

## Alternativas consideradas

- **Solo la documentación inferida,** sin anotaciones: rápido, pero sin descripciones ni errores documentados.
- **Ejemplos JSON por cada respuesta y entidad:** más completo, pero agrega mucho ruido a los controllers para un beneficio menor; los ejemplos de respuesta están en el README.
- **Interfaces de contrato** (`PeopleApi` con las anotaciones, implementada por el controller): separa documentación e implementación, patrón habitual en equipos *contract-first*, pero suma una interfaz por recurso sin necesidad en este alcance.
- **Repetir los errores comunes en cada endpoint:** 32 anotaciones idénticas, fáciles de desincronizar.
- **Deshabilitar Swagger UI en producción:** práctica habitual en APIs internas, pero acá eliminaría la forma principal de probar la API.

## Consecuencias

- La API se puede explorar y probar desde el navegador, y la especificación se puede importar en herramientas como Postman.
- Los controllers crecen en anotaciones, aunque siguen sin lógica.
- La validación y la documentación de los límites no se pueden desincronizar, porque la segunda se deriva de la primera.
- Un test verifica la especificación generada (título, endpoints, descripciones, parámetros y errores), para que cambios en los controllers no rompan la documentación sin aviso.
- Al implementar la seguridad, `/swagger-ui/**` y `/v3/api-docs/**` deberán quedar públicos y Swagger UI deberá permitir enviar el token JWT.
