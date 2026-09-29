# 0022. Relaciones como ids propios

- **Estado:** Aceptada
- **Fecha:** 2026-09-29
- **Modifica:** [0002](0002-anti-corruption-layer-swapi.md)
- **Concreta:** la mejora futura de [0009](0009-films-filtro-por-titulo-y-atributos-tipados.md)

## Contexto

SWAPI envía las relaciones entre recursos como URLs a sus propios endpoints:

```json
"films": ["https://www.swapi.tech/api/films/1", "https://www.swapi.tech/api/films/2"]
```

El ADR 0002 las dejó fuera del contrato público para no exponer URLs de un tercero. Como consecuencia, quien usa la API no puede saber, por ejemplo, qué personajes aparecen en un film.

## Decisión

Las relaciones se exponen como **ids de esta API**, extrayendo el último segmento de cada URL de SWAPI:

```json
"filmIds": ["1", "2"]
```

- Cada id se consulta en el endpoint del recurso correspondiente (por ejemplo, `filmIds` → `/api/v1/films/{id}`).
- Los campos llevan el sufijo `Ids`, para distinguirlos de objetos embebidos.
- Solo se exponen las relaciones **navegables**, es decir, hacia recursos que esta API expone:

  | Recurso | Relaciones |
  |---|---|
  | People | `filmIds`, `starshipIds`, `vehicleIds` |
  | Films | `characterIds`, `starshipIds`, `vehicleIds` |
  | Starships | `pilotIds`, `filmIds` |
  | Vehicles | `pilotIds`, `filmIds` |

  Se omiten `homeworld`, `planets` y `species`, porque apuntan a recursos sin endpoint en esta API.
- Los records espejo conservan las URLs tal como llegan. La conversión ocurre en el mapper, con un helper compartido (`SwapiUrls`).
- Si SWAPI no envía una relación, se expone como lista vacía.

## Alternativas consideradas

- **Exponer las URLs de SWAPI:** acopla el contrato al proveedor y lleva a los clientes a llamar a SWAPI directamente, sin pasar por la autenticación ni la caché de esta API.
- **Links propios** (`/api/v1/films/1`): mismo costo que los ids, pero el mapper pasaría a depender de las rutas y del host de la API. Además, la navegación con un click pierde valor porque todos los endpoints requieren un token.
- **Resolver las relaciones** (embeber los objetos): implica una llamada extra a SWAPI por cada relación. Un film tiene decenas, así que un listado sumaría cientos de llamadas por request contra una API externa con límite de uso.
- **Resolución opt-in** (`?include=characters`, solo en el detalle): se deja como mejora posible. Acota el costo a quien lo pide, pero suma complejidad que hoy no se necesita.
- **Exponer también planets y species:** serían ids sin un endpoint donde consultarlos.
- **Convertir las URLs con un deserializer de Jackson:** simplifica el mapper, pero los records espejo dejarían de reflejar el JSON de SWAPI y la conversión quedaría oculta en la configuración de Jackson.

## Consecuencias

- El cambio es aditivo: se suman campos y no se modifica ninguno existente, así que los clientes actuales no se rompen.
- No agrega llamadas a SWAPI: las relaciones ya llegan en las respuestas que se piden hoy.
- Si SWAPI cambiara el formato de sus URLs, solo se ajusta `SwapiUrls`.
- Si la API sumara planets o species, sus relaciones se agregan con el mismo criterio.
