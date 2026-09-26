# 0006. Estructura de paquetes híbrida

- **Estado:** Aceptada
- **Fecha:** 2026-09-26

## Contexto

El proyecto tiene cuatro entidades con la misma forma (People, Films, Starships, Vehicles), más autenticación y piezas compartidas (cliente de SWAPI, paginación, errores). La organización de paquetes tiene que reflejar la arquitectura en capas y seguir siendo navegable a medida que crecen las features.

## Decisión

Paquete por **feature** en el primer nivel, con las **capas** adentro. Lo compartido vive en `common`.

```
com.conexa.starwars
├── common/
│   ├── dto/           PageResponse
│   ├── exception/     ResourceNotFoundException, GlobalExceptionHandler
│   └── swapi/         cliente, configuración y records espejo de SWAPI
└── people/
    ├── controller/
    ├── service/
    ├── mapper/
    └── dto/
```

Todo lo que habla con SWAPI (interfaces del cliente incluidas) vive en `common/swapi`, porque lo usan las cuatro features.

## Alternativas consideradas

- **Por capa** (`controller/`, `service/`, `dto/` en el primer nivel): es la convención más difundida y refleja literalmente la arquitectura en capas, pero cada carpeta mezcla archivos de todas las features y crece con cada una.
- **Por feature plano** (sin subcarpetas de capa): escala bien, pero las capas dejan de ser visibles en la estructura.

## Consecuencias

- Todo lo de una feature está bajo una misma raíz, y ninguna carpeta crece al agregar otra feature.
- La separación en capas se mantiene visible.
- Hay más carpetas que en las alternativas.
