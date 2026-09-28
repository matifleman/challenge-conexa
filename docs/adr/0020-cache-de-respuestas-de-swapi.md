# 0020. Caché de respuestas de SWAPI con Caffeine

- **Estado:** Aceptada
- **Fecha:** 2026-09-28

## Contexto

Cada pedido a la API genera al menos un llamado a SWAPI, que suele tardar entre uno y varios segundos y a veces responde con errores `5xx`. Los datos de Star Wars prácticamente no cambian, así que volver a pedirlos en cada consulta agrega latencia y hace que la API dependa de la disponibilidad de SWAPI incluso para datos que ya obtuvo.

## Decisión

- **Caché en los services:** `findAll` y `findById` de los cuatro services se anotan con `@Cacheable`. Se guarda el resultado ya convertido al contrato propio (`PageResponse` o el DTO del recurso), con las correcciones del ADR 0003 aplicadas. La clave son los parámetros del método (`page`, `size` y filtro, o `id`).
- **Caffeine como proveedor:** caché en memoria con tamaño máximo y vencimiento, configurada con `spring.cache.caffeine.spec`.
- **Dos cachés por recurso** (listado y detalle), con una lista fija de nombres en `spring.cache.cache-names`: un nombre mal escrito en `@Cacheable` produce un error en lugar de crear una caché sin configurar.
- **Vencimiento de 24 horas y hasta 500 entradas por caché.** El vencimiento permite reflejar correcciones en SWAPI; el tope acota la memoria ante muchas combinaciones de página, tamaño y filtro.
- **Los errores no se cachean:** si SWAPI responde `404`, falla o no responde a tiempo, el pedido siguiente vuelve a consultarla.
- **`sync = true`:** si llegan varios pedidos iguales con la caché vacía, solo uno consulta a SWAPI y el resto espera su resultado.

## Alternativas consideradas

- **Cachear en los clientes de SWAPI:** guardaría las respuestas crudas, pero `@Cacheable` sobre los proxies de `@HttpExchange` es poco habitual, y cada pedido repetiría la conversión al DTO.
- **Precargar cada recurso completo y paginar en memoria:** eliminaría casi todos los llamados, pero reemplaza la estrategia de paginación del ADR 0003 y hace más lento el arranque.
- **`ConcurrentMapCache` (el proveedor por defecto de Spring):** no necesita dependencias, pero no tiene vencimiento ni tamaño máximo.
- **Redis:** comparte la caché entre instancias y sobrevive a los reinicios, pero agrega un servicio externo que una sola instancia no justifica.

## Consecuencias

- Los pedidos repetidos se responden en milisegundos y siguen funcionando aunque SWAPI esté caída, mientras el dato siga en caché.
- Los datos pueden tener hasta 24 horas de atraso respecto de SWAPI.
- La caché es local a cada instancia y se pierde al reiniciar, incluida cada suspensión del plan gratuito de Render (ADR 0019). Con varias instancias, cada una tendría la suya; en ese caso conviene una caché compartida como Redis.
- Los objetos cacheados se comparten entre pedidos, así que tienen que ser inmutables: los DTOs son records y las listas de `PageResponse` son copias inmutables.
- Los tests end-to-end vacían las cachés antes de cada caso para que el resultado no dependa del orden de ejecución, y verifican que las respuestas exitosas se cachean y los errores no.
