# 0024. Recurso Planets

- **Estado:** Aceptada
- **Fecha:** 2026-09-29
- **Modifica:** [0022](0022-relaciones-como-ids-propios.md), [0023](0023-recurso-species.md)

## Contexto

Los ADR 0022 y 0023 omitieron las relaciones hacia planets (`homeworld` y `planets`) porque la API no exponía ese recurso. Al sumarlo, la API real de SWAPI difiere de su documentación y de las otras entidades:

- **Paginación y búsqueda:** iguales a People. Pagina con `page`, `limit` y `expanded`, y busca por `name`.
- **Sin relaciones propias:** la documentación indica que un planeta incluye `residents` y `films`, pero la API no los envía ni en el listado, ni en la búsqueda, ni en el detalle. Planets solo aparece como destino de otras relaciones:
  - `homeworld` en People y Species, como una única URL.
  - `planets` en Films, como una lista de URLs.
- **Primera relación de un solo valor:** hasta ahora todas las relaciones eran listas, y una relación vacía se representa como `[]`. `homeworld` es un valor único y a veces llega roto: en la especie Droid es `.../planets/null`.
- **Planeta placeholder:** el planeta con id `28` se llama `"unknown"`.
- **Texto libre:** `gravity` llega como `"1 standard"`, `"N/A"` o `"1.5 (surface), 1 standard (Cloud City)"`; los valores numéricos pueden ser `"unknown"`; `climate` y `terrain` son listas separadas por comas.

## Decisión

- Se exponen `GET /api/v1/planets` (listado paginado con filtro opcional por nombre) y `GET /api/v1/planets/{id}`, con el mismo diseño que People y Species.
- Planet no tiene campos de relación. No se infieren `residents` ni `films` a partir de otros recursos, con el mismo criterio que el ADR 0023.
- Las relaciones hacia planets se agregan donde SWAPI las informa:

  | Recurso | Relación nueva |
  |---|---|
  | People | `homeworldId` |
  | Species | `homeworldId` |
  | Films | `planetIds` |

- **Una relación de un solo valor sin dato se expone como `null`**, que es la contraparte de `[]` en las listas. El campo siempre está presente en la respuesta. Una URL con id `null` (`.../planets/null`) se trata igual que un valor ausente.
- Todos los atributos se exponen como `String`, siguiendo el criterio del ADR 0009.
- El planeta `"unknown"` se expone tal cual, porque es un recurso válido de SWAPI.

## Alternativas consideradas

- **Omitir el campo cuando no hay dato** (`@JsonInclude(NON_NULL)`): el JSON queda más limpio, pero el campo sería opcional en el contrato y el cliente no podría distinguir "sin planeta" de un campo que no existe.
- **Exponer el id `"null"` tal como llega:** apuntaría a `/api/v1/planets/null`, que responde `400`.
- **Inferir `residentIds` y `filmIds` de un planeta** a partir de People y Films: implicaría consultar todos los personajes o todos los films por cada respuesta, contra una API con límite de uso.
- **Filtrar el planeta `"unknown"`:** ocultaría un recurso al que apuntan otras relaciones y rompería la navegación hacia él.

## Consecuencias

- Las relaciones hacia planets solo se navegan hacia el planeta: desde un planeta no se puede llegar a sus residentes ni a sus films.
- People, Species y Films suman un campo cada uno. Los cambios son aditivos.
- `SwapiUrls` suma la conversión de una URL única, además de la de listas.
- El recurso suma dos caches (`planets-list` y `planets-by-id`).
- Con este recurso, todas las relaciones que informa SWAPI entre los recursos expuestos quedan disponibles como ids.
