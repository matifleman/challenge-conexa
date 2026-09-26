# Star Wars API

API REST en Java 21 + Spring Boot que se integra con [SWAPI](https://www.swapi.tech/documentation) para listar **People**, **Films**, **Starships** y **Vehicles** de forma paginada, con filtrado por ID o por nombre.

> 🚧 Proyecto en desarrollo. Este README se completa a medida que avanzan las funcionalidades.

## Stack

- Java 21
- Spring Boot 4.1 (Spring Web MVC, Validation, Actuator)
- Cliente HTTP declarativo (`@HttpExchange`) sobre `RestClient`
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
- Un `id` inexistente devuelve `404` con un cuerpo [`ProblemDetail`](https://www.rfc-editor.org/rfc/rfc9457):

```json
{
  "title": "Not Found",
  "status": 404,
  "detail": "Person with id 999 not found",
  "instance": "/api/v1/people/999"
}
```

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

## Decisiones de arquitectura

Las decisiones de diseño, con sus alternativas y motivos, están documentadas como ADRs en [`docs/adr`](docs/adr/README.md).

## Funcionalidades

| Funcionalidad | Estado |
|---|---|
| Listado paginado y filtrado de People | ✅ Listo |
| Listado paginado y filtrado de Films | ✅ Listo |
| Listado paginado y filtrado de Starships | ✅ Listo |
| Listado paginado y filtrado de Vehicles | ⏳ Pendiente |
| Manejo de errores | ⏳ Pendiente |
| Documentación de la API (Swagger / OpenAPI) | ⏳ Pendiente |
