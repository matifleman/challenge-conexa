# 0002. Anti-corruption layer entre SWAPI y el contrato público

- **Estado:** Aceptada
- **Fecha:** 2026-09-26

## Contexto

El JSON de SWAPI tiene una forma propia que no conviene exponer:

- Los atributos vienen anidados en `properties` y el identificador aparte, como `uid`.
- Usa `snake_case` (`birth_year`, `hair_color`).
- Incluye metadatos internos (`_id`, `__v`, `created`, `edited`) y relaciones como URLs a otros recursos de SWAPI.
- La forma del sobre cambia según el endpoint: `results` en listados paginados, `result` (lista) en búsquedas y `result` (objeto) en el detalle.

## Decisión

Se separan dos modelos:

- **Records espejo de SWAPI** (`common/swapi/dto`): reflejan el JSON tal como llega (`SwapiPageResponse`, `SwapiListResponse`, `SwapiItemResponse`, `SwapiResource<T>` y un record de propiedades por entidad, como `SwapiPerson`). Se usa `@JsonNaming(SnakeCaseStrategy)` a nivel de record.
- **DTOs públicos** (por ejemplo, `PersonDto`): planos, en `camelCase` y con solo los datos que expone la API.

Un **mapper por entidad** (por ejemplo, `PersonMapper`, un `@Component`) es el único lugar donde conviven ambos modelos.

Criterios del mapeo:

- Los atributos que parecen numéricos se mantienen como `String`, porque SWAPI los envía como texto y puede usar `"unknown"`.
- No se exponen las relaciones (URLs a SWAPI) ni los metadatos internos.
- El `id` se expone como `String` (identificador opaco, tal como lo envía SWAPI).

## Alternativas consideradas

- **Exponer los records de SWAPI directamente:** menos código, pero acopla el contrato público al de un tercero.
- **Método estático en el DTO** (`PersonDto.from(...)`): el DTO público pasaría a depender de las clases de SWAPI.
- **MapStruct:** estándar en la industria, pero suma una dependencia y un procesador de anotaciones para mapeos triviales, y el anidado `properties` requiere configuración extra.

## Consecuencias

- Un cambio en el formato de SWAPI, o un cambio de proveedor, solo afecta a los records espejo y a los mappers; el contrato público y los controllers no cambian.
- Se controla qué campos se exponen y con qué nombres.
- Cada entidad suma un record espejo, un DTO y un mapper.
- Los errores de mapeo compilan y no fallan en tiempo de ejecución (por ejemplo, un campo en `null`); por eso el cliente se testea contra respuestas reales de SWAPI.
