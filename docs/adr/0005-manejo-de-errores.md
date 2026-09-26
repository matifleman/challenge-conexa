# 0005. Manejo de errores con ProblemDetail

- **Estado:** Aceptada
- **Fecha:** 2026-09-26

## Contexto

La API tiene que comunicar errores de forma uniforme: recursos inexistentes, parámetros inválidos y fallas de SWAPI. Además, las excepciones técnicas del cliente HTTP (`HttpClientErrorException`) no deberían llegar a las capas superiores ni filtrarse en las respuestas.

## Decisión

- **Excepciones de dominio:** el service lanza excepciones propias, *unchecked* (por ejemplo, `ResourceNotFoundException`), sin conocer HTTP.
- **Traducción en el borde:** el service atrapa **solo** `HttpClientErrorException.NotFound` de SWAPI y la traduce a `ResourceNotFoundException`. Otros errores (5xx de SWAPI, timeouts) no se convierten en `404`.
- **Manejador global:** un `@RestControllerAdvice` (`GlobalExceptionHandler`) traduce cada excepción a una respuesta HTTP.
- **Formato estándar:** los errores se responden con `ProblemDetail` (RFC 9457): `title`, `status`, `detail` e `instance`.

## Alternativas consideradas

- **`Optional` en el service y `404` en cada controller:** repite la conversión en cada controller y el `404` sale sin cuerpo.
- **`@ResponseStatus` sobre la excepción:** acopla la excepción de dominio a HTTP y controla poco el cuerpo de la respuesta.
- **`ResponseStatusException` lanzada desde el service:** el service pasa a hablar en HTTP, responsabilidad del controller.
- **`@ExceptionHandler` dentro de cada controller:** se duplica en los cuatro controllers.

## Consecuencias

- El formato de error se define una sola vez y aplica a todos los endpoints.
- Los controllers solo contienen el caso feliz.
- Los errores que no son "no existe" no le mienten al cliente con un `404`.
- **Pendiente:** hoy solo se maneja el `404`. Los errores de validación (`400`) salen con el formato por defecto de Spring Boot, y las fallas de SWAPI todavía no tienen tratamiento (`502`/`503`). Se completa en el issue de manejo de errores, donde se evaluará extender `ResponseEntityExceptionHandler`.
