# Star Wars API

API REST en Java 21 + Spring Boot que se integra con [SWAPI](https://www.swapi.tech/documentation) para listar **People**, **Films**, **Starships** y **Vehicles** de forma paginada, con filtrado por ID y/o nombre.

> 🚧 Proyecto en desarrollo. Este README se completa a medida que avanzan las funcionalidades.

## Stack

- Java 21
- Spring Boot 4.1 (Spring Web, Validation, Actuator)
- Maven (vía Maven Wrapper)
- JUnit 5 + Mockito

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

## Cómo correr los tests

```bash
./mvnw test
```

## Funcionalidades

| Funcionalidad | Estado |
|---|---|
| Listado paginado de People | ⏳ Pendiente |
| Filtrado de People por ID y/o nombre | ⏳ Pendiente |
| Listado y filtrado de Films, Starships y Vehicles | ⏳ Pendiente |
| Manejo de errores | ⏳ Pendiente |
| Documentación de la API (Swagger / OpenAPI) | ⏳ Pendiente |
