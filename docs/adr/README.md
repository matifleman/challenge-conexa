# Architecture Decision Records

Registro de las decisiones de arquitectura del proyecto. Cada ADR describe el contexto, la decisión tomada, las alternativas descartadas y sus consecuencias.

Se sigue el formato de [Michael Nygard](https://cognitect.com/blog/2011/11/15/documenting-architecture-decisions). Un ADR aceptado no se edita: si la decisión cambia, se escribe uno nuevo que lo reemplaza.

| # | Decisión | Estado |
|---|---|---|
| [0001](0001-cliente-swapi-http-interfaces.md) | Cliente de SWAPI con HTTP Interfaces y proxy explícito | Aceptada |
| [0002](0002-anti-corruption-layer-swapi.md) | Anti-corruption layer entre SWAPI y el contrato público | Aceptada |
| [0003](0003-estrategia-de-paginacion.md) | Estrategia de paginación | Aceptada |
| [0004](0004-filtrado-por-id-y-nombre.md) | Filtrado por ID y por nombre | Aceptada |
| [0005](0005-manejo-de-errores.md) | Manejo de errores con ProblemDetail | Aceptada |
| [0006](0006-estructura-de-paquetes.md) | Estructura de paquetes híbrida | Aceptada |
| [0007](0007-versionado-de-la-api.md) | Versionado de la API por URI | Aceptada |
| [0008](0008-flujo-de-trabajo-git.md) | GitHub Flow con squash merge | Aceptada |
| [0009](0009-films-filtro-por-titulo-y-atributos-tipados.md) | Films: filtro por título y atributos tipados | Aceptada |
| [0010](0010-duplicacion-aceptada-en-services.md) | Duplicación aceptada entre los services de entidades | Aceptada |
| [0011](0011-estrategia-de-testing.md) | Estrategia de testing | Aceptada |
| [0012](0012-manejo-de-errores-completo.md) | Manejo de errores completo | Aceptada |
| [0013](0013-documentacion-openapi.md) | Documentación de la API con springdoc-openapi | Aceptada |
| [0014](0014-autenticacion-con-usuarios-propios.md) | Autenticación con usuarios propios, sin roles | Aceptada |
| [0015](0015-gestion-de-usuarios-con-jdbcuserdetailsmanager.md) | Gestión de usuarios con JdbcUserDetailsManager | Aceptada |
| [0016](0016-postgresql-con-migraciones-flyway.md) | PostgreSQL con migraciones Flyway | Aceptada |
| [0017](0017-tokens-jwt.md) | Tokens JWT firmados con RS256 | Aceptada |
| [0018](0018-tests-end-to-end-y-cobertura.md) | Tests end-to-end y cobertura con JaCoCo | Aceptada |

## Mejoras futuras

Mejoras identificadas durante el desarrollo y documentadas en los ADR correspondientes:

- **Relaciones como ids propios** (ADR 0009): exponer las relaciones entre recursos como ids navegables dentro de la API, sin llamadas extra a SWAPI.
- **Helpers compartidos entre services** (ADR 0010): extraer la paginación desde SWAPI y la traducción del `404` a funciones reutilizables.
- **Roles o permisos** (ADR 0014): si aparecen operaciones que no deban estar disponibles para todos los usuarios.
- **Claves RSA por configuración** (ADR 0017): para que los tokens sobrevivan a los reinicios y funcionen con varias instancias.
- **Refresh tokens** (ADR 0017): sesiones largas manteniendo access tokens de corta duración.
