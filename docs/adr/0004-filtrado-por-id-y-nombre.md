# 0004. Filtrado por ID y por nombre

- **Estado:** Aceptada
- **Fecha:** 2026-09-26

## Contexto

La consigna pide "permitir el filtrado de resultados por ID y/o nombre". SWAPI expone el detalle por id (`/{resource}/{id}`, `404` si no existe) y la búsqueda por nombre (`?name=`, o `?title=` en films), parcial y sin distinguir mayúsculas.

## Decisión

- **Por ID:** `GET /api/v1/{resource}/{id}`, el recurso individual según la convención REST. Devuelve el elemento o `404`.
- **Por nombre:** `GET /api/v1/{resource}?name=`, filtro opcional sobre la colección paginada. Devuelve `200`, con una página vacía si no hay coincidencias.
- "ID y/o nombre" se interpreta como **"por ID o por nombre"**: como el ID es único, combinarlo con el nombre no restringe el resultado, solo lo validaría.
- Un nombre vacío o con solo espacios se trata como "sin filtro", y se le aplica `trim` antes de enviarlo a SWAPI.
- El `id` se recibe como `int`, así un valor no numérico responde `400` automáticamente.

## Alternativas consideradas

- **Solo query params** (`?id=&name=` en el listado): cumple una lectura literal del "y/o", pero el filtro por id es redundante con la convención REST y combinarlo con el nombre no tiene un caso de uso real.
- **Ambos** (`/{id}` y `?id=` combinable con `name`): suma un filtro que se superpone con el detalle y que un reviewer podría cuestionar.

## Consecuencias

- La API sigue la convención estándar de recurso y colección.
- Se distingue claramente "el recurso no existe" (`404`) de "no hay coincidencias" (`200` con página vacía).
- La interpretación de la consigna queda documentada en este ADR y en el README.
