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

## Funcionalidades

| Funcionalidad | Estado |
|---|---|
| Listado paginado y filtrado de People | ✅ Listo |
| Listado paginado y filtrado de Films | ⏳ Pendiente |
| Listado paginado y filtrado de Starships | ⏳ Pendiente |
| Listado paginado y filtrado de Vehicles | ⏳ Pendiente |
| Manejo de errores | ⏳ Pendiente |
| Documentación de la API (Swagger / OpenAPI) | ⏳ Pendiente |
