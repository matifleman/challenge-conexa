# 0021. Límite de intentos de login

- **Estado:** Aceptada
- **Fecha:** 2026-09-28

## Contexto

El endpoint de login aceptaba intentos ilimitados, así que cualquiera podía probar contraseñas por fuerza bruta contra un usuario. La aplicación corre en una sola instancia detrás del proxy HTTPS de Render (ADR 0019), que informa la IP del cliente en el header `X-Forwarded-For`.

## Decisión

- **Contador por username:** se cuentan los logins fallidos de cada username, sin distinguir mayúsculas (igual que `citext`, ADR 0015). También se cuentan los usernames inexistentes, para que la respuesta no revele cuáles existen.
- **5 fallos bloquean el username durante 15 minutos,** medidos desde el último fallo. Ambos valores se configuran con `auth.login-attempts.*`.
- **Durante el bloqueo no se validan las credenciales:** el login responde `429 Too Many Requests` aunque la contraseña sea correcta. Si se validaran, un `401` o un `429` indicaría si la contraseña probada es la buena. Los intentos durante el bloqueo no lo extienden.
- **El `429` incluye `Retry-After`** con la duración del bloqueo en segundos: un máximo, pasado el cual el login vuelve a aceptarse.
- **Un login exitoso reinicia el contador.**
- **Contadores en memoria con Caffeine,** que ya es dependencia (ADR 0020): cada entrada vence sola y un tope de 10.000 usernames acota la memoria ante intentos con nombres al azar.

## Alternativas consideradas

- **Contar por IP o por IP y username:** evita que se pueda bloquear a otro usuario, pero detrás del proxy la IP sale de `X-Forwarded-For`, que el cliente puede falsificar para esquivar el límite.
- **Rate limiting general por IP (Bucket4j):** limita el volumen de pedidos, no los intentos contra una cuenta; comparte el problema de la IP y agrega una dependencia.
- **Guardar los intentos en PostgreSQL:** sobreviven a los reinicios y sirven con varias instancias, pero suman una migración, una escritura en la base por cada fallo y una limpieza periódica.
- **Bloqueo permanente hasta que un administrador lo levante:** frena mejor un ataque sostenido, pero la API no tiene administradores (ADR 0014) y convertiría cualquier ataque en un bloqueo definitivo de la cuenta.
- **CAPTCHA después de varios fallos:** es la opción habitual en aplicaciones con interfaz, pero no aplica a una API consumida por otros programas.

## Consecuencias

- Probar contraseñas contra un usuario queda limitado a 5 intentos cada 15 minutos.
- Cualquiera que conozca un username puede bloquearlo a propósito durante 15 minutos; es el costo de no depender de la IP.
- Un reinicio borra los contadores. En el plan gratuito de Render la aplicación se suspende recién tras 15 minutos sin tráfico, cuando el bloqueo ya venció, y un ataque en curso la mantiene activa.
- Con varias instancias, cada una llevaría su propio contador; en ese caso conviene moverlo a un almacenamiento compartido como Redis.
