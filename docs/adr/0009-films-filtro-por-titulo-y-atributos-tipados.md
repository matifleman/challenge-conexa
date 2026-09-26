# 0009. Films: filtro por título y atributos tipados

- **Estado:** Aceptada
- **Fecha:** 2026-09-26
- **Complementa:** [0002](0002-anti-corruption-layer-swapi.md), [0004](0004-filtrado-por-id-y-nombre.md)

## Contexto

Los ADR 0002 y 0004 se escribieron a partir de People, y Films se aparta de ese modelo en dos puntos:

- **Filtro:** SWAPI busca films por `title`; si se envía `?name=`, lo ignora y devuelve todos los films.
- **Tipos:** SWAPI envía `episode_id` como número y `release_date` como fecha ISO (`"1977-05-25"`), con formato consistente en todos los films. En People y Starships, en cambio, los atributos llegan como texto libre (`"unknown"`, `"342,953"`, `"30-165"`).

## Decisión

- **Filtro:** el listado de films se filtra con **`?title=`** en lugar de `?name=`, fiel al dominio (los films tienen título) y a SWAPI.
- **Tipos:** `episodeId` se expone como `int` y `releaseDate` como `LocalDate`, serializada en formato ISO.

El criterio de tipos del ADR 0002 se precisa así: **un atributo se tipa cuando SWAPI lo envía con tipo y formato consistentes; si puede llegar como texto libre, se mantiene como `String`.**

## Alternativas consideradas

- **`?name=` en todos los endpoints:** un único nombre de parámetro para toda la API, pero semánticamente incorrecto para films y obliga a traducirlo a `title` al llamar a SWAPI.
- **Todo `String`, como en People:** nunca falla por formato, pero descarta información de tipo que SWAPI sí provee.

## Consecuencias

- El parámetro de filtro cambia según el recurso (`title` en films, `name` en el resto); se documenta en el README y en Swagger.
- Si SWAPI enviara una fecha con formato inválido, el parseo fallaría. Se acepta porque el formato es consistente en todos los films y queda cubierto por el test del cliente con respuestas reales.

## Mejora futura: relaciones como ids propios

Hoy no se exponen las relaciones (por ejemplo, `characters`, `planets` o `starships` de un film), porque SWAPI las envía como URLs a sus propios recursos (ADR 0002). Una mejora posible es exponerlas como **ids navegables dentro de esta API**, extrayendo el id final de cada URL:

```json
"characterIds": ["1", "2", "3"]
```

Cada id se consulta en el endpoint correspondiente (`/api/v1/people/{id}`). No requiere llamadas extra a SWAPI. Se descarta, en cambio, **resolver** las relaciones completas: implicaría decenas de llamadas extra a SWAPI por cada elemento.
