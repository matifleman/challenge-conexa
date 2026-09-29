# 0023. Recurso Species

- **Estado:** Aceptada
- **Fecha:** 2026-09-29
- **Modifica:** [0022](0022-relaciones-como-ids-propios.md)

## Contexto

El ADR 0022 dejó fuera las relaciones hacia `species` porque la API no exponía ese recurso. Al sumarlo, SWAPI presenta varias diferencias respecto de las otras entidades:

- **Paginación y búsqueda:** iguales a People. Pagina con `page`, `limit` y `expanded`; busca por `name` y devuelve las coincidencias sin paginar.
- **Relaciones en un solo sentido:**
  - Una especie informa sus personajes (`people`), pero un personaje no informa su especie.
  - Un film informa sus especies (`species`), pero una especie no informa sus films.
- **`homeworld`:** apunta a planets, que la API no expone. En algunas especies llega como `.../planets/null`.
- **Colores en plural:** `eye_colors`, `hair_colors` y `skin_colors` llegan como texto separado por comas (`"brown, blue, green"`), a veces con valores como `"n/a"` o `"none"`.
- **Datos incompletos:** SWAPI no asigna especie a todos los personajes. Por ejemplo, Human lista solo cuatro personajes y Luke Skywalker no figura en ninguna especie.

## Decisión

- Se exponen `GET /api/v1/species` (listado paginado con filtro opcional por nombre) y `GET /api/v1/species/{id}`, con el mismo diseño que People: paginación delegada en SWAPI, búsqueda paginada en memoria y caché.
- Las relaciones se exponen **tal como las informa SWAPI**, sin invertirlas:

  | Recurso | Relación nueva |
  |---|---|
  | Species | `characterIds` |
  | Films | `speciesIds` |

  People no suma `speciesIds` y Species no suma `filmIds`.
- **Regla de nombres:** una relación hacia `/people` se llama `characterIds`, como en Films, salvo que SWAPI indique un rol específico, como `pilots` → `pilotIds`.
- Los colores se exponen como `String`, sin separarlos, siguiendo el criterio del ADR 0009: el texto libre se mantiene como texto.
- `homeworld` se omite, como en People.
- Los datos incompletos de SWAPI se exponen sin corregir.

## Alternativas consideradas

- **Invertir las relaciones** (calcular `speciesIds` de un personaje o `filmIds` de una especie): requiere consultar todas las especies o todos los films por cada respuesta y acopla los services entre sí. Además, con los datos incompletos de SWAPI, la mayoría de los personajes quedaría con una lista vacía, lo que podría leerse como "no tiene especie".
- **`peopleIds`**, igual que el campo de SWAPI: más fiel al proveedor, pero Films ya expone `characterIds` hacia el mismo recurso. Unificar hacia `peopleIds` implicaría renombrar un campo ya publicado.
- **Colores como lista** (`["brown", "blue"]`): da más estructura, pero obliga a interpretar texto libre y a decidir qué hacer con `"n/a"` o `"none"`.

## Consecuencias

- La navegación entre personajes y especies solo funciona en un sentido: de la especie a sus personajes.
- Films suma un campo (`speciesIds`); el cambio es aditivo.
- El recurso suma dos caches (`species-list` y `species-by-id`) y repite la estructura de las otras entidades, en línea con el ADR 0010.
