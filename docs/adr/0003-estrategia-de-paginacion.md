# 0003. Estrategia de paginación

- **Estado:** Aceptada
- **Fecha:** 2026-09-26

## Contexto

La consigna exige que los listados sean paginados. SWAPI no se comporta de forma uniforme:

- Los listados sin filtro se paginan con `page` y `limit` (páginas desde 1) y, por defecto, devuelven solo `uid`, `name` y `url` de cada elemento; con `expanded=true` devuelven los datos completos.
- La búsqueda por nombre ignora la paginación y devuelve todas las coincidencias con los datos completos.
- Films nunca se pagina.
- Una página fuera de rango (por ejemplo, `page=100`) responde `200` con los datos de la **última** página, en lugar de una lista vacía.
- Acepta cualquier valor de `limit`.

## Decisión

- **Formato propio:** todos los listados responden con `PageResponse<T>` (`content`, `page`, `size`, `totalElements`, `totalPages`), con páginas numeradas desde 1. El contenido se copia en una lista inmutable.
- **Datos completos:** los listados piden `expanded=true`, así listado, búsqueda y detalle comparten un único DTO.
- **Paginación delegada o en memoria:** sin filtro se delega en SWAPI; cuando SWAPI no pagina (búsqueda por nombre, films), la lista completa se pagina en memoria (`PageResponse.fromList`).
- **Páginas fuera de rango:** devuelven contenido vacío en ambos casos, corrigiendo el comportamiento de SWAPI.
- **Parámetros:** `page` por defecto 1 (mínimo 1) y `size` por defecto 10 (entre 1 y 100). Los valores fuera de rango responden `400` en lugar de corregirse en silencio.

## Alternativas consideradas

- **Devolver el `Page` de Spring Data:** serializa campos internos (`pageable`, `sort`) cuyo JSON no es estable entre versiones.
- **Copiar el formato de SWAPI** (`total_records`, `next`, `previous`): acopla el contrato público al del proveedor.
- **Páginas desde 0,** como Spring Data: obliga a convertir el índice en cada llamada a SWAPI y es menos natural para quien consume la API.
- **Resumen (`id` y `name`) en los listados:** inconsistente con la búsqueda, que ya devuelve datos completos.
- **Devolver sin paginar cuando SWAPI no pagina:** incumple el requisito y hace que el mismo endpoint responda con estructuras distintas.
- **Dejar pasar las páginas fuera de rango de SWAPI:** la respuesta diría `"page": 100` con los datos de la página 9; un cliente con scroll infinito nunca terminaría.

## Consecuencias

- Todos los listados tienen el mismo contrato, independiente de cómo pagine SWAPI cada recurso.
- La paginación en memoria es aceptable porque los volúmenes son chicos (82 personajes como máximo).
- El tope de `size` protege a la API y a SWAPI de pedidos masivos.
- Criterio general: la API es un contrato propio, no un proxy. Los datos de SWAPI pasan tal cual (por ejemplo, `"unknown"`), pero se normalizan los comportamientos que harían el contrato inconsistente o incorrecto.
