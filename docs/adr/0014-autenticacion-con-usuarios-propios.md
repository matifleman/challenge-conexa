# 0014. Autenticación con usuarios propios, sin roles

- **Estado:** Aceptada
- **Fecha:** 2026-09-27

## Contexto

La consigna pide implementar un sistema de login seguro y un manejo adecuado de autenticación y autorización para acceder a las listas. No menciona roles, tipos de usuario ni un mecanismo concreto.

Cualquier persona que use la API tiene que poder obtener credenciales y probar los endpoints protegidos con la menor configuración posible.

## Decisión

- **Usuarios propios:** la API guarda los usuarios y emite sus propios tokens JWT, que se validan con el soporte de *resource server* de Spring Security. Los detalles del token se definen en un ADR aparte.
- **Registro público:** cualquiera puede crear un usuario (`POST /api/v1/auth/register`) y obtener un token (`POST /api/v1/auth/login`).
- **Autorización = usuario autenticado:** todos los listados requieren un token válido. No hay roles: la consigna no distingue permisos entre usuarios, y ningún endpoint los necesitaría.

## Alternativas consideradas

- **Proveedor de identidad externo** (Keycloak, Auth0): es lo habitual en producción, pero delega el login que pide implementar la consigna y obliga a levantar y configurar otro servicio para probar la API.
- **Roles USER/ADMIN:** muestran autorización por rol, pero sin un endpoint exclusivo de administrador son código sin uso, y además obligan a resolver cómo se crea el primer administrador (credenciales sembradas o configuración extra).
- **Usuario demo precargado** en lugar del registro: evita un endpoint, pero deja una contraseña en el repositorio o en la configuración.

## Consecuencias

- Cualquiera puede registrarse y probar todo desde Swagger UI, sin configuración previa.
- Migrar a un proveedor de identidad externo cambiaría quién emite el token, no cómo lo valida la API.
- **Mejora futura:** roles o permisos, si aparecen operaciones que no deban estar disponibles para todos los usuarios.
