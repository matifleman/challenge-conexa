# 0019. Deploy en Render con PostgreSQL en Neon

- **Estado:** Aceptada
- **Fecha:** 2026-09-28

## Contexto

La API tiene que quedar publicada en una URL accesible. Necesita un proceso Java 21 que atienda pedidos de forma continua y una base PostgreSQL con la extensión `citext` (ADR 0015). La consigna sugiere Heroku o Vercel, y el deploy no puede requerir una tarjeta de crédito.

## Decisión

- **Aplicación en Render,** plan gratuito, como servicio web construido desde un `Dockerfile`. El servicio se declara en `render.yaml` (Blueprint) y se deploya automáticamente con cada cambio en `main`.
- **Base en Neon,** plan gratuito, PostgreSQL 17 en la misma región que la aplicación. Se usa la conexión directa, no la del pooler: el pooler de Neon trabaja en modo transacción y no es compatible con los *prepared statements* de JDBC ni con Flyway.
- **Credenciales como variables de entorno** en Render (`SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME`, `SPRING_DATASOURCE_PASSWORD`); no se guardan en el repositorio.
- **Imagen multi-stage:** compila con el Maven wrapper y ejecuta sobre una imagen con solo el JRE, con un usuario sin privilegios. La JVM se ajusta a un contenedor chico (512 MB y una fracción de CPU): heap al 65 % de la memoria, recolector serial y compilación C1.
- **La aplicación toma el puerto de `PORT`** y respeta los headers `X-Forwarded-*` del proxy HTTPS de la plataforma, para que las URLs que genera (por ejemplo, el servidor de Swagger UI) usen `https`.

## Alternativas consideradas

- **Heroku:** es la opción sugerida por la consigna y soporta Java de forma nativa, pero no tiene plan gratuito y exige una tarjeta, aun con el crédito para estudiantes.
- **Vercel:** ejecuta frontends y funciones serverless; no tiene runtime de Java ni admite un servidor como Spring Boot.
- **Azure for Students:** crédito sin tarjeta y sin arranques en frío, pero requiere bastante más configuración (servidor PostgreSQL, reglas de red, habilitar `citext`) y la verificación como estudiante.
- **Base gratuita de Render:** evita un segundo proveedor, pero se elimina a los 30 días.

## Consecuencias

- El deploy no tiene costo y no cambia el flujo de trabajo: lo que se mergea a `main` se publica.
- **Arranque en frío:** el servicio gratuito se suspende tras 15 minutos sin tráfico y, con 0,1 CPU, la aplicación tarda alrededor de dos minutos en volver a arrancar. El primer pedido después de un período sin uso espera ese tiempo.
- Cada arranque genera nuevas claves de firma (ADR 0017): los tokens emitidos antes de una suspensión dejan de ser válidos y hay que volver a hacer login.
- Neon también suspende la base sin uso; la primera conexión después de una pausa demora unos cientos de milisegundos más.
- Pasar a un plan pago de Render, o a otra plataforma, solo requiere configurar las mismas variables de entorno; el código no cambia.
