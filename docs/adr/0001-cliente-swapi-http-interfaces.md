# 0001. Cliente de SWAPI con HTTP Interfaces y proxy explícito

- **Estado:** Aceptada
- **Fecha:** 2026-09-26

## Contexto

La aplicación consume la API externa SWAPI para obtener People, Films, Starships y Vehicles. Todas las llamadas son `GET` simples con query params o path variables, repetidas con la misma forma para las cuatro entidades.

Spring Boot 4 ofrece varias formas de consumir una API HTTP, y el cliente tiene que poder testearse sin depender de la red.

## Decisión

Los endpoints de SWAPI se declaran como interfaces anotadas con `@HttpExchange` (por ejemplo, `SwapiPeopleClient`). Sus implementaciones son proxies creados explícitamente en `SwapiClientConfig` mediante `HttpServiceProxyFactory`, sobre un `RestClient` propio.

- La URL base se externaliza en `swapi.base-url` (`SwapiProperties`), validada al arrancar (*fail fast*).
- Los timeouts se configuran con las propiedades estándar `spring.http.clients.*` (connect 5s, read 10s), para que un SWAPI lento o caído no bloquee indefinidamente los hilos que atienden requests.

## Alternativas consideradas

- **`RestClient` usado directamente:** control total, pero cada llamada se escribe a mano y el código se repite por entidad. Hoy no es el estilo recomendado para APIs de este tipo.
- **HTTP service groups (`@ImportHttpServices`, Boot 4):** mínimo código, configuración 100% por YAML. Se descartó porque oculta cómo se construye el cliente y porque no estaba garantizado que `MockRestServiceServer` se enganchara a esos clientes en los tests.
- **OpenFeign (Spring Cloud):** suma una dependencia de Spring Cloud y está en modo mantenimiento; Spring recomienda migrar a HTTP Interfaces.

## Consecuencias

- Estilo declarativo, alineado con la dirección actual de Spring: la interfaz describe la llamada y el proxy la ejecuta.
- La construcción del cliente es visible y se puede explicar pieza por pieza.
- El cliente se testea con `@RestClientTest` + `MockRestServiceServer` y respuestas reales de SWAPI guardadas como fixtures, sin salir a internet.
- Cada nueva entidad requiere una interfaz y un `@Bean` de una línea que reutiliza la misma `HttpServiceProxyFactory`.
- `SwapiProperties` se registra en la propia configuración (`@EnableConfigurationProperties`), así funciona igual en la aplicación y en los tests slice.
