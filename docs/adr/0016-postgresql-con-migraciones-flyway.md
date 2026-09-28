# 0016. PostgreSQL con migraciones Flyway

- **Estado:** Aceptada
- **Fecha:** 2026-09-27

## Contexto

Los usuarios (ADR 0015) se guardan en PostgreSQL. La base arranca vacía, y `JdbcUserDetailsManager` asume que las tablas ya existen: alguien tiene que crearlas, y mantenerlas cuando el esquema cambie, tanto en desarrollo como en la base desplegada, que conserva sus datos.

La aplicación tiene que poder levantarse en local con la menor configuración posible.

## Decisión

- **Flyway** crea y actualiza el esquema con migraciones SQL versionadas (`db/migration/V1__...sql`). Al arrancar, ejecuta solo las que todavía no se aplicaron y lo registra en su propia tabla.
- **PostgreSQL 17** en local con un `compose.yaml` en la raíz del proyecto (`docker compose up -d`).
- **Conexión configurada con variables de entorno** que tienen valores por defecto coincidentes con el `compose.yaml`: en local no hace falta configurar nada; en producción se sobrescriben.
- **Tests contra PostgreSQL real con Testcontainers** y `@ServiceConnection`: una configuración compartida levanta la base para los tests que la necesitan, y Flyway aplica las mismas migraciones que en producción.

## Alternativas consideradas

- **`schema.sql` de Spring Boot:** no requiere dependencias, pero se ejecuta en cada arranque (obliga a escribir SQL que se pueda repetir), no guarda versiones y en PostgreSQL hay que activarlo explícitamente. Un cambio de esquema no llegaría a una base ya creada.
- **Liquibase:** igual de difundido, con más funciones (rollback, formatos XML/YAML), pero más pesado para dos tablas.
- **Integración de Spring Boot con Docker Compose** (levanta el contenedor al arrancar la app): más cómodo, pero oculta la configuración real que se usa al desplegar.
- **H2 en memoria para los tests:** más rápido, pero es otro motor; no soporta `citext` y podría pasar tests que fallarían en PostgreSQL.

## Consecuencias

- Todas las bases (local, tests y producción) tienen el mismo esquema, aplicado de la misma forma.
- Un cambio de esquema es una nueva migración; las migraciones aplicadas no se editan.
- Correr la aplicación y los tests que usan la base requiere Docker.
- Las credenciales por defecto son solo para desarrollo y quedan en el repositorio; en producción se definen por variables de entorno.
