# 0007. Versionado de la API por URI

- **Estado:** Aceptada
- **Fecha:** 2026-09-26

## Contexto

El contrato de la API puede cambiar con el tiempo (por ejemplo, el formato de paginación o de los DTOs). Los clientes existentes no deberían romperse cuando eso ocurra.

## Decisión

Los endpoints se exponen bajo el prefijo `/api/v1` (por ejemplo, `/api/v1/people`).

## Alternativas consideradas

- **Sin versión** (`/api/people`): más simple, pero cualquier cambio incompatible rompe a los clientes existentes.
- **Versionado por header o parámetro,** con soporte nativo en Spring Framework 7: más flexible, pero menos visible y excesivo para el alcance del proyecto.

## Consecuencias

- Un cambio incompatible se publica como `/api/v2` sin afectar a los clientes de `v1`.
- La versión es visible en cada URL, en los ejemplos y en la documentación.
