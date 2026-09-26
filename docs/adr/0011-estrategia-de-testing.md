# 0011. Estrategia de testing

- **Estado:** Aceptada
- **Fecha:** 2026-09-26

## Contexto

La consigna exige pruebas unitarias y de integración. La aplicación depende de una API externa (SWAPI) cuya disponibilidad y datos no controlamos, y cuyo JSON tiene particularidades (anidado en `properties`, `snake_case`, un campo en mayúsculas) que producen errores silenciosos si el mapeo es incorrecto: el código compila y los campos quedan en `null`.

## Decisión

Los tests se escriben junto con cada feature y se organizan por capa, cada una probada de forma aislada con la herramienta adecuada:

| Capa | Herramienta | Qué verifica |
|---|---|---|
| Lógica pura (`PageResponse`) | JUnit 5 | Casos borde de la paginación en memoria |
| Service | JUnit 5 + Mockito (cliente mockeado, mapper real) | Estrategia de paginación, filtros, normalizaciones y traducción de errores |
| Controller | `@WebMvcTest` + MockMvc (service mockeado) | Rutas, parámetros por defecto, validación (`400`), forma del JSON y errores `ProblemDetail` |
| Cliente SWAPI | `@RestClientTest` + `MockRestServiceServer` | La URL que arma cada interfaz `@HttpExchange` y el parseo de respuestas **reales** de SWAPI |

Criterios:

- **Ningún test depende de la red.** SWAPI se simula con `MockRestServiceServer`; la integración real se verifica manualmente con la aplicación levantada.
- **Fixtures reales:** las respuestas de SWAPI usadas en los tests del cliente se capturaron de la API real y se guardan en `src/test/resources/swapi/`.
- **Se mockea solo lo que sale del proceso** (el cliente HTTP); clases simples sin dependencias, como los mappers, se usan reales.
- **Mockito se carga como agente de la JVM** (Surefire), porque las versiones nuevas de Java restringen que se adjunte dinámicamente.

## Alternativas consideradas

- **Tests contra SWAPI real:** lentos, dependientes de la disponibilidad de un tercero y de datos que pueden cambiar.
- **JSON de prueba inventado:** puede reproducir el mismo error que el código (por ejemplo, `hairColor` en lugar de `hair_color`) y dejar pasar un mapeo incorrecto.
- **Solo tests `@SpringBootTest` con la aplicación completa:** más lentos y con fallas más difíciles de localizar.
- **WireMock en lugar de `MockRestServiceServer`:** simula un servidor HTTP real, pero suma una dependencia; `MockRestServiceServer` viene con Spring y se integra con el `RestClient` del proyecto (ADR 0001).

## Consecuencias

- Los tests son rápidos y deterministas, y un fallo apunta a la capa exacta que se rompió.
- Si SWAPI cambia su formato, los fixtures quedan desactualizados sin que los tests lo detecten: hay que volver a capturarlos.
- **Pendiente:** tests de integración de punta a punta con `@SpringBootTest` (seguridad, base de datos con Testcontainers y SWAPI simulada) y reporte de cobertura con JaCoCo, en su propio issue.
