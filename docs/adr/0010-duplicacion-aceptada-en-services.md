# 0010. Duplicación aceptada entre los services de entidades

- **Estado:** Aceptada
- **Fecha:** 2026-09-26

## Contexto

Con la tercera entidad (Starships) se aplicó la "regla de tres": no abstraer con dos casos, evaluar al tener el tercero. `StarshipsService` resultó casi idéntico a `PeopleService`: misma paginación delegada en SWAPI, misma normalización de páginas fuera de rango (ADR 0003), misma búsqueda por nombre paginada en memoria y misma traducción del `404` (ADR 0005). Vehicles repetirá el patrón. Films comparte solo la traducción del `404`, porque SWAPI no lo pagina.

El proyecto tiene un plazo de entrega corto.

## Decisión

Cada entidad mantiene **su propio service** con la lógica completa, sin abstracción compartida. La duplicación se acepta de forma consciente.

## Alternativas consideradas

- **Helpers compartidos (composición):** extraer a `common` solo la lógica repetida, como funciones reutilizables: el armado de la página desde la respuesta paginada de SWAPI (con la normalización de páginas fuera de rango) y la traducción del `404` a `ResourceNotFoundException`. Los services seguirían siendo uno por entidad, pero más cortos.
- **Service base genérico (herencia):** una clase abstracta `AbstractSwapiService<S, D>` de la que hereden los services. Menos código, pero más indirección y rigidez: Films no encaja (no pagina en SWAPI) y la herencia acopla las entidades entre sí.

## Consecuencias

- Cada service se lee de forma independiente, sin saltar a clases base.
- La normalización de páginas fuera de rango y la traducción del `404` están repetidas: un cambio en esas reglas hay que aplicarlo en cada service. Los tests de cada service, que cubren los mismos casos, reducen el riesgo de que alguno quede desactualizado.

## Mejora futura: helpers compartidos

La mejora recomendada es la alternativa de **helpers compartidos** (composición, no herencia): mueve a un único lugar la lógica que hoy se repite, sin acoplar las entidades entre sí. Los tests existentes de cada service sirven como red de seguridad para el refactor.
