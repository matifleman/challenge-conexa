# 0017. Tokens JWT firmados con RS256

- **Estado:** Aceptada
- **Fecha:** 2026-09-27

## Contexto

La API emite y valida sus propios tokens (ADR 0014). Hay que definir cómo se firman, cuánto duran, qué contienen, cómo se obtienen y cómo responde la API cuando falta o falla la autenticación. La aplicación corre como una única instancia.

## Decisión

- **Firma RS256** con un par de claves RSA generado al arrancar la aplicación. El token se emite con `NimbusJwtEncoder` y se valida con el soporte de *resource server* de Spring Security (`oauth2-resource-server`), igual que en los ejemplos oficiales de Spring.
- **Access token de 1 hora** (configurable), **sin refresh token**.
- **Claims estándar:** `iss` (validado al recibir el token), `sub` (username), `iat` y `exp`. No se incluye `scope` porque no hay roles (ADR 0014).
- **Login con body JSON:** `POST /api/v1/auth/login` con `{username, password}` responde `{accessToken, tokenType, expiresIn}`, con los campos de la respuesta de OAuth2 (RFC 6749) en camelCase, como el resto de la API. La verificación de credenciales usa el `AuthenticationManager` de Spring Security.
- **Registro:** `POST /api/v1/auth/register` responde `201`, o `409` si el username ya existe.
- **Errores en formato `ProblemDetail`:**
  - Sin token o con un token inválido: `401`, conservando el header `WWW-Authenticate` (RFC 6750).
  - Credenciales incorrectas en el login: `401` con un mensaje genérico, que no revela si el usuario existe. La excepción se lanza dentro del controller, por lo que el manejador global la trata explícitamente para que no la capture el caso genérico (`500`).
- **Rutas públicas:** `/api/v1/auth/**`, Swagger UI, `/v3/api-docs/**` y `/actuator/health`. El resto requiere token.
- **Sin sesión ni CSRF:** la API es *stateless* y la autenticación viaja en el header `Authorization`, no en cookies, que es el caso contra el que protege CSRF. Se deshabilitan el formulario de login y HTTP Basic.
- **Swagger UI** declara el esquema *bearer* para poder enviar el token desde la documentación.

## Alternativas consideradas

- **HS256 con un secreto compartido:** más simple, pero quien valida también puede emitir tokens, y un secreto por defecto olvidado en producción permitiría fabricarlos.
- **Claves RSA provistas por configuración:** los tokens sobreviven a los reinicios y funciona con varias instancias, a cambio de gestionar la clave privada en cada ambiente.
- **Refresh tokens:** permiten sesiones largas con access tokens cortos, pero requieren guardarlos, rotarlos y revocarlos.
- **Login con HTTP Basic** devolviendo el token como texto: menos código, pero poco habitual en APIs REST frente a un body JSON.

## Consecuencias

- No hay claves ni secretos en el repositorio ni en la configuración.
- Al reiniciar la aplicación, los tokens emitidos dejan de ser válidos y hay que volver a hacer login.
- Un token no se puede revocar antes de que expire; la duración corta limita el impacto de un token filtrado.
- No se manejan respuestas `403`: sin roles ni seguridad a nivel de método, un usuario autenticado accede a todos los recursos protegidos.
- **Mejoras futuras:** claves RSA por configuración (necesarias para escalar a varias instancias) y refresh tokens.
