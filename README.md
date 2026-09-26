# Star Wars API

API REST en Java 21 + Spring Boot que se integra con [SWAPI](https://www.swapi.tech/documentation) para listar **People**, **Films**, **Starships** y **Vehicles** de forma paginada, con filtrado por ID o por nombre.

> 🚧 Proyecto en desarrollo. Este README se completa a medida que avanzan las funcionalidades.

## Stack

- Java 21
- Spring Boot 4.1 (Spring Web MVC, Validation, Actuator)
- Cliente HTTP declarativo (`@HttpExchange`) sobre `RestClient`
- springdoc-openapi (OpenAPI 3.1 + Swagger UI)
- Maven (vía Maven Wrapper)
- JUnit 5, Mockito, MockMvc y `MockRestServiceServer`

## Requisitos

- JDK 21
- No hace falta instalar Maven: el proyecto incluye el wrapper (`./mvnw`).

## Cómo correrlo

```bash
./mvnw spring-boot:run
```

La aplicación levanta en `http://localhost:8080`.

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

Cualquier propiedad se puede sobrescribir con una variable de entorno, por ejemplo `SWAPI_BASEURL`.

## Cómo correr los tests

```bash
./mvnw test
```

Los tests no dependen de la red: las respuestas de SWAPI se simulan con respuestas reales guardadas en `src/test/resources/swapi/`.

## Uso

### Documentación interactiva (Swagger)

Con la aplicación levantada, la documentación de todos los endpoints está disponible en:

- **Swagger UI:** [`http://localhost:8080/swagger-ui.html`](http://localhost:8080/swagger-ui.html): permite ver cada endpoint con sus parámetros, respuestas y errores, y probarlo desde el navegador con **Try it out**.
- **Especificación OpenAPI (JSON):** [`http://localhost:8080/v3/api-docs`](http://localhost:8080/v3/api-docs): útil para importar la API en Postman u otras herramientas.

Las secciones siguientes resumen el uso de cada recurso.

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
curl "http://localhost:8080/api/v1/people?page=1&size=2"
curl "http://localhost:8080/api/v1/people?name=sky"
curl "http://localhost:8080/api/v1/people/1"
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
      "gender": "male"
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
curl "http://localhost:8080/api/v1/films?size=2"
curl "http://localhost:8080/api/v1/films?title=the"
curl "http://localhost:8080/api/v1/films/1"
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
  "openingCrawl": "It is a period of civil war. ..."
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
curl "http://localhost:8080/api/v1/starships?size=2"
curl "http://localhost:8080/api/v1/starships?name=star"
curl "http://localhost:8080/api/v1/starships/9"
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
  "mglt": "10"
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
curl "http://localhost:8080/api/v1/vehicles?size=2"
curl "http://localhost:8080/api/v1/vehicles?name=speeder"
curl "http://localhost:8080/api/v1/vehicles/4"
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
  "maxAtmospheringSpeed": "30"
}
```

Igual que en Starships, los atributos se exponen como texto, tal como los informa SWAPI.

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
| `400` | Parámetros inválidos (`page` o `size` fuera de rango, `id` no numérico) |
| `404` | El recurso no existe, o la ruta no existe |
| `405` | Método HTTP no soportado (por ejemplo, `POST` en un listado) |
| `500` | Error inesperado de la aplicación |
| `502` | SWAPI respondió con un error |
| `503` | No se pudo conectar con SWAPI |
| `504` | SWAPI no respondió a tiempo (ver timeouts en [Configuración](#configuración)) |

Los errores de validación incluyen la lista de parámetros inválidos en `errors`:

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
| Manejo de errores | ✅ Listo |
| Documentación de la API (Swagger / OpenAPI) | ✅ Listo |
