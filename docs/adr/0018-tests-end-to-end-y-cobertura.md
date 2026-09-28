# 0018. Tests end-to-end y cobertura con JaCoCo

- **Estado:** Aceptada
- **Fecha:** 2026-09-28

## Contexto

La estrategia de testing (ADR 0011) prueba cada capa de forma aislada y dejó pendientes dos cosas: tests que recorran la aplicación completa y un reporte de cobertura.

Los tests existentes nunca ejecutan juntas todas las piezas de un pedido real: seguridad con un token emitido por la propia API, base de datos, controller, service, cliente `@HttpExchange` y parseo de la respuesta de SWAPI. Tampoco se mide qué parte del código ejecutan los tests.

## Decisión

- **Tests end-to-end con `@SpringBootTest`** sobre la aplicación completa. Cada test se registra y hace login por HTTP para obtener un token real, y después consume los endpoints protegidos.
- **Base de datos real** con Testcontainers, como el resto de los tests que la usan (ADR 0016).
- **SWAPI simulada en el borde** con `MockRestServiceServer` (`@AutoConfigureMockRestServiceServer`), respondiendo con los mismos fixtures capturados de la API real que usan los tests del cliente. Es lo único que no se ejecuta de verdad: la llamada de red.
- **Se ejecutan con el resto de los tests** (`./mvnw test`, Surefire), sin separarlos en otra fase.
- **Cobertura con JaCoCo:** el reporte se genera al correr los tests (`target/site/jacoco/index.html`) y `./mvnw verify` falla si la cobertura baja del **90 % de líneas** o del **85 % de ramas**.

## Alternativas consideradas

- **Tests contra SWAPI real:** prueban también que SWAPI no haya cambiado, pero dependen de la disponibilidad y de los datos de un tercero, son lentos y no permiten provocar errores (`5xx`, timeouts). Se descartan para el build; un *smoke test* etiquetado y excluido del build normal sería la forma de cubrir ese riesgo.
- **WireMock:** simula SWAPI como un servidor HTTP real, incluidos los timeouts, pero suma una dependencia; los timeouts ya se prueban en el manejador de errores.
- **Mockear los clientes de SWAPI (`@MockitoBean`):** más simple, pero deja afuera el cliente HTTP y el parseo del JSON, que es donde aparecen los errores de mapeo.
- **Failsafe con tests `*IT` en la fase `verify`:** es la convención de Maven para separar tests unitarios de integración, pero suma un plugin y obliga a unir la cobertura de dos ejecuciones; los tests con Testcontainers ya corren junto con los unitarios.
- **Solo reporte de cobertura, sin mínimo:** más simple, pero una caída de cobertura pasaría sin aviso.

## Consecuencias

- Un pedido real queda probado de punta a punta, incluido que sin token no llega a SWAPI.
- Los tests que usan la aplicación completa o la base requieren Docker y son más lentos que los unitarios.
- Los mínimos de cobertura dejan margen sobre la cobertura actual (cerca del 99 % de líneas y 95 % de ramas) para código que no vale la pena testear, como el `main` o excepciones que no pueden ocurrir.
- Un cambio en el formato de SWAPI sigue sin detectarse automáticamente (ADR 0011).
- Si se agrega otro `RestClient` construido desde el builder de Spring, el `MockRestServiceServer` auto-configurado deja de funcionar y habría que vincularlo explícitamente al cliente de SWAPI.
