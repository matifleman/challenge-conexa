# 0012. Manejo de errores completo

- **Estado:** Aceptada
- **Fecha:** 2026-09-26
- **Complementa:** [0005](0005-manejo-de-errores.md)

## Contexto

El ADR 0005 estableció el formato `ProblemDetail` y el manejador global, pero solo para el `404` de recurso inexistente. El resto de los errores salía con el formato por defecto de Spring Boot (`timestamp`, `error`, `path`):

- errores propios de Spring MVC: parámetros inválidos, tipos incorrectos, rutas inexistentes, métodos no soportados;
- fallas de SWAPI (respuestas `5xx`, timeouts, errores de conexión), que terminaban en un `500` genérico, como si el error fuera de esta API.

Se verificó qué excepciones lanza realmente el cliente HTTP de Spring Boot 4 (basado en el cliente HTTP del JDK):

| Falla | Excepción |
|---|---|
| SWAPI responde `5xx` | `HttpServerErrorException` |
| Timeout de lectura o de conexión | `ResourceAccessException` causada por `java.net.http.HttpTimeoutException` |
| Host inexistente o conexión fallida | `ResourceAccessException` causada por `ConnectException` |

## Decisión

- **`GlobalExceptionHandler` extiende `ResponseEntityExceptionHandler`**, la clase base de Spring que traduce sus excepciones internas de MVC a `ProblemDetail` (`400`, `404`, `405`, etc.).
- **Validación:** el `400` por parámetros inválidos agrega una propiedad `errors` con el nombre de cada parámetro y su mensaje.
- **Fallas de SWAPI, traducidas en el manejador global:**
  - `HttpServerErrorException` → `502 Bad Gateway`
  - `ResourceAccessException` con una causa de timeout en la cadena → `504 Gateway Timeout`
  - `ResourceAccessException` por otro motivo → `503 Service Unavailable`
- **Catch-all:** cualquier otra excepción → `500` con mensaje genérico.
- **Sin detalles internos en las respuestas:** los errores `5xx` usan mensajes genéricos. El detalle técnico se registra con SLF4J (`warn` para fallas de SWAPI, `error` con stack trace para el `500`).

## Alternativas consideradas

- **`spring.mvc.problemdetails.enabled=true`:** Boot registra su propio manejador para las excepciones de MVC. Es una línea de configuración, pero deja dos manejadores y personalizar el `400` es más incómodo.
- **Un `@ExceptionHandler` por cada excepción de MVC:** control total, pero reimplementa lo que Spring ya provee y es fácil olvidar alguna.
- **Traducir las fallas de SWAPI en la capa del cliente** (status handler e interceptor del `RestClient` que lancen una excepción propia): aísla por completo las excepciones del `RestClient`, pero agrega más código.
- **Traducirlas en cada service,** como el `404`: multiplica la duplicación ya aceptada en el ADR 0010.
- **Solo `502`/`503`:** más simple, pero no distingue "no respondió a tiempo" de "no se pudo conectar", información útil para decidir si reintentar.

## Consecuencias

- Todos los errores de la API tienen el mismo formato (`application/problem+json`), incluidos los de Spring MVC.
- El cliente distingue un error de esta API (`4xx`, `500`) de una falla del proveedor externo (`502`, `503`, `504`).
- El manejador global conoce excepciones de Spring (`HttpServerErrorException`, `ResourceAccessException`), aunque no de SWAPI. Es aceptable porque es la frontera HTTP de la aplicación.
- La detección del timeout revisa la cadena de causas y contempla tanto el tipo del cliente del JDK como el clásico (`SocketTimeoutException`), para no depender del cliente HTTP en uso.
- El catch-all atraparía también las excepciones de autorización (`AccessDeniedException`) que se lancen dentro de un controller; al implementar la seguridad habrá que manejarlas explícitamente para responder `403`.
