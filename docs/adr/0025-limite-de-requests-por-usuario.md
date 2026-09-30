# 0025. Límite de requests por usuario

- **Estado:** Aceptada
- **Fecha:** 2026-09-29

## Contexto

SWAPI limita los pedidos por IP. Su documentación habla de 10.000 por día, pero los headers `x-ratelimit-*` de sus respuestas indican unos 100 pedidos cada 15 minutos, y a partir del quinto pedido en esa ventana agrega una demora creciente. Todos los usuarios de la API comparten la IP del servidor (ADR 0019), así que la cuota también es compartida: un solo usuario que recorra páginas sin caché puede agotarla y dejar sin servicio a todos los demás.

La caché (ADR 0020) evita repetir llamadas, pero no limita las que todavía no están cacheadas. Además, un `429` de SWAPI terminaba en el manejador genérico y se respondía como `500`.

## Decisión

- **Un límite de requests por usuario autenticado** en los endpoints respaldados por SWAPI (`/api/v1/**`, salvo `/api/v1/auth/**`). Los endpoints de autenticación no llaman a SWAPI y el login ya tiene su propio límite (ADR 0021).
- **Se cuentan todos los requests,** estén o no en caché. Así el límite es predecible para quien usa la API: el mismo request cuenta siempre igual, sin depender del estado de la caché.
- **Ventana fija de 30 requests por minuto** (configurable con `rate-limit.*`). La ventana empieza con el primer request del usuario y vence un minuto después; los requests posteriores no la extienden. Con la caché, un uso normal no llega a ese número.
- **Al superarlo se responde `429 Too Many Requests` con `Retry-After`,** el tiempo que falta para que termine la ventana, redondeado hacia arriba en segundos.
- **Se aplica con un `HandlerInterceptor`,** que corre después de Spring Security (el usuario ya está autenticado) y cuyas excepciones pasan por el manejador global de errores (ADR 0012), por lo que el `429` sale como `ProblemDetail` sin código adicional.
- **Contadores en memoria con Caffeine,** igual que el límite de login (ADR 0021): cada entrada vence al terminar su ventana y un tope de 10.000 usuarios acota la memoria. Ambos límites comparten la excepción `TooManyRequestsException` y su manejador.
- **Los errores de SWAPI se responden como errores de gateway:** su `429` pasa a ser `503` (reenviando `Retry-After` si SWAPI lo informa) y cualquier otro `4xx` pasa a ser `502`. Responder `429` le diría al cliente que él superó un límite, cuando lo superó el servidor; y un `4xx` de SWAPI indica un pedido que armó la API, no un error del cliente.

## Alternativas consideradas

- **Contar solo los requests que llegan a SWAPI** (los que no están en caché): mide exactamente el consumo de la cuota, pero el mismo request contaría a veces sí y a veces no, y el cliente de SWAPI tendría que conocer al usuario, mezclando capas.
- **Token bucket con Bucket4j:** reparte los pedidos de forma más pareja que una ventana fija, que permite ráfagas en el borde entre dos ventanas; a cambio suma una dependencia y un concepto más, sin una ventaja relevante con este volumen.
- **Límite global de salida hacia SWAPI** (por ejemplo, el `RateLimiter` de Resilience4j): protege la cuota total, pero no evita que un solo usuario la acapare.
- **Filtro de servlet en lugar de interceptor:** corre antes y no depende de Spring MVC, pero tendría que armar la respuesta de error por su cuenta, como el entry point de seguridad.

## Consecuencias

- Un usuario no puede agotar por sí solo la cuota de SWAPI; su uso queda acotado a 30 requests por minuto.
- Muchos usuarios a la vez todavía pueden agotarla: el límite es por usuario, no global. En ese caso SWAPI responde `429` y la API devuelve `503` con `Retry-After`.
- Un reinicio borra los contadores, y con varias instancias cada una llevaría el suyo; igual que con el límite de login, convendría moverlos a un almacenamiento compartido como Redis.
- Los requests cacheados también cuentan, aunque no consuman cuota de SWAPI.
