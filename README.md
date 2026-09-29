# Star Wars API

[![CI](https://github.com/matifleman/challenge-conexa/actions/workflows/ci.yml/badge.svg)](https://github.com/matifleman/challenge-conexa/actions/workflows/ci.yml)

API REST en Java 21 + Spring Boot que se integra con [SWAPI](https://www.swapi.tech/documentation) para listar **People**, **Films**, **Starships**, **Vehicles** y **Species** de forma paginada, con filtrado por ID o por nombre. El acceso a los listados requiere autenticación con JWT.

> 🚧 Proyecto en desarrollo. Este README se completa a medida que avanzan las funcionalidades.

## Stack

- Java 21
- Spring Boot 4.1 (Spring Web MVC, Validation, Actuator)
- Spring Security con OAuth2 Resource Server (JWT firmados con RS256)
- PostgreSQL 17, JDBC y Flyway (migraciones)
- Cliente HTTP declarativo (`@HttpExchange`) sobre `RestClient`
- Caffeine (caché en memoria de las respuestas de SWAPI)
- springdoc-openapi (OpenAPI 3.1 + Swagger UI)
- Maven (vía Maven Wrapper)
- JUnit 5, Mockito, MockMvc, `MockRestServiceServer` y Testcontainers

## Requisitos

- JDK 21
- Docker (con Docker Compose), para la base de datos y para los tests
- No hace falta instalar Maven: el proyecto incluye el wrapper (`./mvnw`).

## Cómo correrlo

```bash
docker compose up -d      # PostgreSQL en localhost:5432
./mvnw spring-boot:run
```

La aplicación levanta en `http://localhost:8080`. Al arrancar, Flyway crea las tablas de la base si todavía no existen.

Para detener la base: `docker compose down` (los datos se conservan en un volumen; `docker compose down -v` los borra).

Verificar que esté funcionando:

```bash
curl http://localhost:8080/actuator/health
# {"status":"UP"}
```

### Configuración

| Propiedad | Descripción | Valor por defecto |
|---|---|---|
| `swapi.base-url` | URL base de SWAPI | `https://www.swapi.tech/api` |
| `spring.http.clients.connect-timeout` | Tiempo máximo para conectar con SWAPI | `5s` |
| `spring.http.clients.read-timeout` | Tiempo máximo de espera de la respuesta de SWAPI | `10s` |
| `spring.cache.caffeine.spec` | Tamaño máximo y vencimiento de la caché de respuestas de SWAPI | `maximumSize=500,expireAfterWrite=24h` |
| `spring.datasource.url` | URL de conexión a PostgreSQL | `jdbc:postgresql://localhost:5432/starwars` |
| `spring.datasource.username` | Usuario de la base | `starwars` |
| `spring.datasource.password` | Contraseña de la base | `starwars` |
| `jwt.issuer` | Emisor (`iss`) de los tokens | `starwars-api` |
| `jwt.expiration` | Duración de los tokens | `1h` |
| `auth.login-attempts.max-failures` | Logins fallidos seguidos que bloquean un username | `5` |
| `auth.login-attempts.block-duration` | Duración del bloqueo, desde el último fallo | `15m` |
| `server.port` (variable `PORT`) | Puerto HTTP | `8080` |

Cualquier propiedad se puede sobrescribir con una variable de entorno, por ejemplo `SWAPI_BASEURL` o `SPRING_DATASOURCE_URL`. Los valores por defecto de la base coinciden con `compose.yaml` y son solo para desarrollo local.

### Con Docker

La imagen es la misma que se usa en producción:

```bash
docker build -t starwars-api .
docker run -p 8080:8080 \
  -e SPRING_DATASOURCE_URL=jdbc:postgresql://host.docker.internal:5432/starwars \
  --add-host=host.docker.internal:host-gateway \
  starwars-api
```

## Deploy

La API está publicada en Render, con la base de datos en Neon (ver [ADR 0019](docs/adr/0019-deploy-en-render-y-neon.md)):

- **API:** [`https://starwars-api-zdeq.onrender.com`](https://starwars-api-zdeq.onrender.com)
- **Swagger UI:** [`https://starwars-api-zdeq.onrender.com/swagger-ui.html`](https://starwars-api-zdeq.onrender.com/swagger-ui.html)

> El plan gratuito suspende el servicio tras 15 minutos sin uso: el primer pedido después de una pausa puede tardar alrededor de dos minutos, y los tokens emitidos antes de la pausa dejan de ser válidos (hay que volver a hacer login).

Cada merge a `main` se deploya automáticamente, una vez que el pipeline de CI (GitHub Actions: `./mvnw verify`) termina en verde. El servicio está definido en [`render.yaml`](render.yaml); las credenciales de la base se configuran como variables de entorno en Render (`SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME` y `SPRING_DATASOURCE_PASSWORD`).

## Cómo correr los tests

```bash
./mvnw test      # tests + reporte de cobertura
./mvnw verify    # además, falla si la cobertura baja del mínimo
```

- **Tipos de tests:** unitarios de cada capa (services, controllers, cliente de SWAPI, manejo de errores), de integración con la base de datos y la seguridad, y end-to-end sobre la aplicación completa (`ApiEndToEndTest`): registro, login y consumo de los endpoints con un token real.
- **Sin red:** las respuestas de SWAPI se simulan con respuestas reales guardadas en `src/test/resources/swapi/`.
- **Docker:** los tests que usan la base levantan un PostgreSQL descartable con Testcontainers, por lo que Docker tiene que estar corriendo (no hace falta `docker compose up`).
- **Cobertura:** el reporte de JaCoCo queda en `target/site/jacoco/index.html`. `./mvnw verify` exige un mínimo de 90 % de líneas y 85 % de ramas.

## Uso

### Documentación interactiva (Swagger)

Con la aplicación levantada, la documentación de todos los endpoints está disponible en:

- **Swagger UI:** [`http://localhost:8080/swagger-ui.html`](http://localhost:8080/swagger-ui.html): permite ver cada endpoint con sus parámetros, respuestas y errores, y probarlo desde el navegador con **Try it out**.
- **Especificación OpenAPI (JSON):** [`http://localhost:8080/v3/api-docs`](http://localhost:8080/v3/api-docs): útil para importar la API en Postman u otras herramientas.

Para probar los endpoints protegidos desde Swagger UI: registrar un usuario, hacer login, copiar el `accessToken` y pegarlo en **Authorize**.

Las secciones siguientes resumen el uso de cada recurso.

### Autenticación

| Método | Endpoint | Descripción |
|---|---|---|
| `POST` | `/api/v1/auth/register` | Registra un usuario (`201`) |
| `POST` | `/api/v1/auth/login` | Devuelve un access token |

El registro y el login son públicos; el resto de los endpoints requiere el token en el header `Authorization: Bearer <token>`. La documentación (`/swagger-ui.html`, `/v3/api-docs`) y `/actuator/health` también son públicos.

```bash
curl -X POST http://localhost:8080/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username": "luke", "password": "password123"}'

curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username": "luke", "password": "password123"}'
```

Respuesta del login:

```json
{
  "accessToken": "eyJraWQiOi...",
  "tokenType": "Bearer",
  "expiresIn": 3600
}
```

Los ejemplos de las secciones siguientes usan el token guardado en una variable:

```bash
TOKEN=$(curl -s -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username": "luke", "password": "password123"}' | sed -E 's/.*"accessToken":"([^"]+)".*/\1/')
```

**Reglas**

- `username`: de 3 a 50 caracteres entre letras, números, `.`, `_` y `-`. No distingue mayúsculas: `Luke` y `luke` son el mismo usuario.
- `password`: de 8 a 72 caracteres. Se guarda hasheada con BCrypt.
- Tras 5 logins fallidos seguidos, el username queda bloqueado 15 minutos (configurable): el login responde `429` con el header `Retry-After`, aunque la contraseña sea correcta.
- El token dura 1 hora (configurable) y no hay refresh token: al vencer, se vuelve a hacer login.
- Las claves de firma se generan al arrancar la aplicación, por lo que un reinicio invalida los tokens emitidos.

### People

| Método | Endpoint | Descripción |
|---|---|---|
| `GET` | `/api/v1/people` | Listado paginado, con filtro opcional por nombre |
| `GET` | `/api/v1/people/{id}` | Detalle de un personaje |

**Parámetros del listado**

| Parámetro | Descripción | Por defecto | Restricciones |
|---|---|---|---|
| `page` | Número de página (empieza en 1) | `1` | Mínimo 1 |
| `size` | Elementos por página | `10` | Entre 1 y 100 |
| `name` | Filtro por nombre (parcial, sin distinguir mayúsculas) | — | Opcional |

**Ejemplos**

```bash
curl -H "Authorization: Bearer $TOKEN" "http://localhost:8080/api/v1/people?page=1&size=2"
curl -H "Authorization: Bearer $TOKEN" "http://localhost:8080/api/v1/people?name=sky"
curl -H "Authorization: Bearer $TOKEN" "http://localhost:8080/api/v1/people/1"
```

Respuesta de un listado:

```json
{
  "content": [
    {
      "id": "1",
      "name": "Luke Skywalker",
      "height": "172",
      "mass": "77",
      "hairColor": "blond",
      "skinColor": "fair",
      "eyeColor": "blue",
      "birthYear": "19BBY",
      "gender": "male",
      "filmIds": ["1", "2", "3", "6"],
      "starshipIds": ["12", "22"],
      "vehicleIds": ["14", "30"]
    }
  ],
  "page": 1,
  "size": 2,
  "totalElements": 82,
  "totalPages": 41
}
```

**Comportamiento**

- Una página fuera de rango devuelve `content` vacío (no la última página).
- Un filtro por nombre sin coincidencias devuelve `200` con `content` vacío.
- `page` o `size` fuera de rango, o un `id` no numérico, devuelven `400`.
- Un `id` inexistente devuelve `404`.

El formato de los errores se describe en [Errores](#errores).

### Films

| Método | Endpoint | Descripción |
|---|---|---|
| `GET` | `/api/v1/films` | Listado paginado, con filtro opcional por título |
| `GET` | `/api/v1/films/{id}` | Detalle de un film |

**Parámetros del listado**

| Parámetro | Descripción | Por defecto | Restricciones |
|---|---|---|---|
| `page` | Número de página (empieza en 1) | `1` | Mínimo 1 |
| `size` | Elementos por página | `10` | Entre 1 y 100 |
| `title` | Filtro por título (parcial, sin distinguir mayúsculas) | — | Opcional |

**Ejemplos**

```bash
curl -H "Authorization: Bearer $TOKEN" "http://localhost:8080/api/v1/films?size=2"
curl -H "Authorization: Bearer $TOKEN" "http://localhost:8080/api/v1/films?title=the"
curl -H "Authorization: Bearer $TOKEN" "http://localhost:8080/api/v1/films/1"
```

Respuesta de un film:

```json
{
  "id": "1",
  "title": "A New Hope",
  "episodeId": 4,
  "director": "George Lucas",
  "producer": "Gary Kurtz, Rick McCallum",
  "releaseDate": "1977-05-25",
  "openingCrawl": "It is a period of civil war. ...",
  "characterIds": ["1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "12", "13", "14", "15", "16", "18", "19", "81"],
  "starshipIds": ["2", "3", "5", "9", "10", "11", "12", "13"],
  "vehicleIds": ["4", "6", "7", "8"],
  "speciesIds": ["1", "2", "3", "4", "5"]
}
```

SWAPI no pagina los films, por lo que la paginación siempre se resuelve en la API. El resto del comportamiento (páginas fuera de rango, filtros sin coincidencias, errores `400` y `404`) es igual al de People.

### Starships

| Método | Endpoint | Descripción |
|---|---|---|
| `GET` | `/api/v1/starships` | Listado paginado, con filtro opcional por nombre |
| `GET` | `/api/v1/starships/{id}` | Detalle de una nave |

Acepta los mismos parámetros que People (`page`, `size` y `name`) y tiene el mismo comportamiento.

**Ejemplos**

```bash
curl -H "Authorization: Bearer $TOKEN" "http://localhost:8080/api/v1/starships?size=2"
curl -H "Authorization: Bearer $TOKEN" "http://localhost:8080/api/v1/starships?name=star"
curl -H "Authorization: Bearer $TOKEN" "http://localhost:8080/api/v1/starships/9"
```

Respuesta de una nave:

```json
{
  "id": "9",
  "name": "Death Star",
  "model": "DS-1 Orbital Battle Station",
  "manufacturer": "Imperial Department of Military Research, Sienar Fleet Systems",
  "starshipClass": "Deep Space Mobile Battlestation",
  "costInCredits": "1000000000000",
  "length": "120000",
  "crew": "342,953",
  "passengers": "843,342",
  "cargoCapacity": "1000000000000",
  "consumables": "3 years",
  "maxAtmospheringSpeed": "n/a",
  "hyperdriveRating": "4.0",
  "mglt": "10",
  "pilotIds": [],
  "filmIds": ["1"]
}
```

Los atributos se exponen como texto, tal como los informa SWAPI: pueden incluir separadores de miles (`"342,953"`), rangos (`"30-165"`) o valores como `"n/a"` y `"unknown"`.

### Vehicles

| Método | Endpoint | Descripción |
|---|---|---|
| `GET` | `/api/v1/vehicles` | Listado paginado, con filtro opcional por nombre |
| `GET` | `/api/v1/vehicles/{id}` | Detalle de un vehículo |

Acepta los mismos parámetros que People (`page`, `size` y `name`) y tiene el mismo comportamiento.

**Ejemplos**

```bash
curl -H "Authorization: Bearer $TOKEN" "http://localhost:8080/api/v1/vehicles?size=2"
curl -H "Authorization: Bearer $TOKEN" "http://localhost:8080/api/v1/vehicles?name=speeder"
curl -H "Authorization: Bearer $TOKEN" "http://localhost:8080/api/v1/vehicles/4"
```

Respuesta de un vehículo:

```json
{
  "id": "4",
  "name": "Sand Crawler",
  "model": "Digger Crawler",
  "manufacturer": "Corellia Mining Corporation",
  "vehicleClass": "wheeled",
  "costInCredits": "150000",
  "length": "36.8 ",
  "crew": "46",
  "passengers": "30",
  "cargoCapacity": "50000",
  "consumables": "2 months",
  "maxAtmospheringSpeed": "30",
  "pilotIds": [],
  "filmIds": ["1", "5"]
}
```

Igual que en Starships, los atributos se exponen como texto, tal como los informa SWAPI.

### Species

| Método | Endpoint | Descripción |
|---|---|---|
| `GET` | `/api/v1/species` | Listado paginado, con filtro opcional por nombre |
| `GET` | `/api/v1/species/{id}` | Detalle de una especie |

Acepta los mismos parámetros que People (`page`, `size` y `name`) y tiene el mismo comportamiento.

**Ejemplos**

```bash
curl -H "Authorization: Bearer $TOKEN" "http://localhost:8080/api/v1/species?size=2"
curl -H "Authorization: Bearer $TOKEN" "http://localhost:8080/api/v1/species?name=wook"
curl -H "Authorization: Bearer $TOKEN" "http://localhost:8080/api/v1/species/3"
```

Respuesta de una especie:

```json
{
  "id": "3",
  "name": "Wookie",
  "classification": "mammal",
  "designation": "sentient",
  "averageHeight": "210",
  "skinColors": "gray",
  "hairColors": "black, brown",
  "eyeColors": "blue, green, yellow, brown, golden, red",
  "averageLifespan": "400",
  "language": "Shyriiwook",
  "characterIds": ["13", "80"]
}
```

- Los atributos se exponen como texto, tal como los informa SWAPI. Los colores son listas separadas por comas dentro de un mismo texto, y hay valores como `"n/a"` o `"indefinite"`.
- `characterIds` refleja los datos de SWAPI, que no asigna especie a todos los personajes. Por ejemplo, Human lista solo cuatro personajes y Luke Skywalker no figura en ninguna especie.

### Relaciones entre recursos

Cada recurso incluye sus relaciones como listas de ids de esta API. Cada id se consulta en el endpoint del recurso relacionado:

| Recurso | Campo | Endpoint del id |
|---|---|---|
| People | `filmIds` | `/api/v1/films/{id}` |
| People | `starshipIds` | `/api/v1/starships/{id}` |
| People | `vehicleIds` | `/api/v1/vehicles/{id}` |
| Films | `characterIds` | `/api/v1/people/{id}` |
| Films | `starshipIds` | `/api/v1/starships/{id}` |
| Films | `vehicleIds` | `/api/v1/vehicles/{id}` |
| Films | `speciesIds` | `/api/v1/species/{id}` |
| Starships, Vehicles | `pilotIds` | `/api/v1/people/{id}` |
| Starships, Vehicles | `filmIds` | `/api/v1/films/{id}` |
| Species | `characterIds` | `/api/v1/people/{id}` |

Por ejemplo, para ver los personajes de un film se consulta `/api/v1/films/1` y luego `/api/v1/people/{id}` por cada id de `characterIds`.

- Una relación sin elementos se devuelve como lista vacía (`[]`), nunca como `null`.
- Las relaciones se exponen en el sentido en que las informa SWAPI: una especie lista sus personajes, pero un personaje no informa su especie; un film lista sus especies, pero una especie no informa sus films ([ADR 0023](docs/adr/0023-recurso-species.md)).
- No se incluyen planetas (`homeworld`, `planets`), porque la API no expone ese recurso.
- Las relaciones no se resuelven en la misma respuesta: cada una requeriría una llamada extra a SWAPI. El motivo está en el [ADR 0022](docs/adr/0022-relaciones-como-ids-propios.md).

## Errores

Todos los errores se responden con el formato estándar [`ProblemDetail`](https://www.rfc-editor.org/rfc/rfc9457) (`Content-Type: application/problem+json`):

```json
{
  "title": "Not Found",
  "status": 404,
  "detail": "Person with id 999 not found",
  "instance": "/api/v1/people/999"
}
```

| Código | Cuándo |
|---|---|
| `400` | Parámetros inválidos (`page` o `size` fuera de rango, `id` no numérico) o body inválido (registro y login) |
| `401` | Falta el token, es inválido o venció; o credenciales incorrectas en el login |
| `404` | El recurso no existe, o la ruta no existe |
| `405` | Método HTTP no soportado (por ejemplo, `POST` en un listado) |
| `409` | El username ya está registrado |
| `429` | Demasiados logins fallidos para el username; reintentar después de `Retry-After` segundos |
| `500` | Error inesperado de la aplicación |
| `502` | SWAPI respondió con un error |
| `503` | No se pudo conectar con SWAPI |
| `504` | SWAPI no respondió a tiempo (ver timeouts en [Configuración](#configuración)) |

Los errores de validación incluyen la lista de parámetros (o campos del body) inválidos en `errors`:

```json
{
  "title": "Bad Request",
  "status": 400,
  "detail": "Invalid request parameters",
  "instance": "/api/v1/people",
  "errors": [
    { "parameter": "page", "message": "must be greater than or equal to 1" },
    { "parameter": "size", "message": "must be less than or equal to 100" }
  ]
}
```

Las respuestas de error nunca incluyen detalles internos: los errores `5xx` responden con un mensaje genérico y el detalle técnico se registra en el log de la aplicación.

## Decisiones de arquitectura

Las decisiones de diseño, con sus alternativas y motivos, están documentadas como ADRs en [`docs/adr`](docs/adr/README.md).

## Funcionalidades

| Funcionalidad | Estado |
|---|---|
| Listado paginado y filtrado de People | ✅ Listo |
| Listado paginado y filtrado de Films | ✅ Listo |
| Listado paginado y filtrado de Starships | ✅ Listo |
| Listado paginado y filtrado de Vehicles | ✅ Listo |
| Listado paginado y filtrado de Species | ✅ Listo |
| Manejo de errores | ✅ Listo |
| Documentación de la API (Swagger / OpenAPI) | ✅ Listo |
| Usuarios en PostgreSQL y autenticación con JWT | ✅ Listo |
| Relaciones entre recursos como ids | ✅ Listo |
