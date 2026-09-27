# 0015. Gestión de usuarios con JdbcUserDetailsManager

- **Estado:** Aceptada
- **Fecha:** 2026-09-27

## Contexto

La autenticación propia (ADR 0014) necesita guardar usuarios con su contraseña, crearlos al registrarse y buscarlos al hacer login. El usuario no tiene más datos que sus credenciales.

## Decisión

- **`JdbcUserDetailsManager` de Spring Security** para crear, buscar y verificar usuarios, sin entidad ni repositorio propios.
- **Esquema estándar de Spring adaptado a PostgreSQL:** tabla `users` (`username` como clave primaria, `password`, `enabled`) y tabla `authorities` (`username`, `authority`). El DDL que provee Spring está escrito para HSQLDB y no corre en PostgreSQL.
- **Username sin distinción de mayúsculas** con el tipo `citext` de PostgreSQL: `Luke` y `luke` son el mismo usuario. Es el equivalente al `varchar_ignorecase` del DDL original. Formato: de 3 a 50 caracteres entre letras, números, `.`, `_` y `-`.
- **Una única authority, `ROLE_USER`,** por usuario: el manager trata como inexistente a un usuario sin authorities. No es un sistema de roles (ADR 0014).
- **Contraseñas** hasheadas con el `DelegatingPasswordEncoder` de Spring (BCrypt, guardado como `{bcrypt}...`), que permite cambiar de algoritmo sin invalidar los hashes existentes. El manager no hashea: se codifica antes de crear el usuario. Largo de 8 a 72 caracteres: el mínimo sigue la guía NIST SP 800-63B, sin reglas de composición; el máximo es el límite de BCrypt. La columna admite 500 caracteres para algoritmos con hashes más largos.

## Alternativas consideradas

- **Entidad JPA propia con un `UserDetailsService`:** es el patrón más común cuando el usuario tiene más datos (perfil, email), y deja el esquema bajo control propio. Acá suma Hibernate, una entidad, un repositorio y un adaptador para una tabla que solo guarda credenciales.
- **Username que distingue mayúsculas** (comportamiento por defecto de PostgreSQL): permite usuarios distintos que se ven iguales.
- **Normalizar a minúsculas en el código:** funciona, pero cualquier camino que olvide normalizar rompe la regla; con `citext` la garantiza la base.

## Consecuencias

- El registro y el login usan componentes de Spring Security, con muy poco código propio.
- No hay repositorio propio para testear: se verifica que el esquema funcione con el manager contra PostgreSQL real.
- `citext` requiere la extensión del mismo nombre, incluida en PostgreSQL; algunos proveedores gestionados exigen habilitarla.
- Si el usuario necesitara más datos, se migra a una entidad propia sin cambiar las tablas existentes, mediante una nueva migración (ADR 0016).
