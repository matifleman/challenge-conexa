# 0026. Keep-alive con GitHub Actions

- **Estado:** Aceptada
- **Fecha:** 2026-09-30

## Contexto

El plan gratuito de Render suspende el servicio tras 15 minutos sin tráfico (ADR 0019). El primer pedido posterior espera alrededor de dos minutos, y cada arranque invalida los tokens emitidos (ADR 0017) y vacía la caché de SWAPI (ADR 0020). Para cualquier persona que use la API de forma esporádica, casi todos los primeros pedidos caen en ese arranque en frío.

## Decisión

- **Un workflow de GitHub Actions programado** (`.github/workflows/keep-alive.yml`) hace un pedido periódico a la API publicada para que no llegue a suspenderse.
- **El pedido va a `/actuator/health`:** es público (ADR 0017), no llama a SWAPI y no pasa por el límite de requests por usuario (ADR 0025), así que no consume ninguna cuota.
- **Cada 10 minutos, de lunes a viernes de 8 a 21 h de Argentina.** El intervalo deja margen frente a los 15 minutos de Render; el horario cubre el uso esperable sin gastar las horas gratuitas de Render y de Neon cuando nadie usa la API.
- **Con reintentos:** si el servicio ya estaba suspendido, el pedido insiste hasta que termina de arrancar, de modo que el mismo workflow también lo despierta.
- **Separado del pipeline de CI** y sin permisos sobre el repositorio: solo hace un pedido HTTP. También se puede ejecutar a mano (`workflow_dispatch`).

## Alternativas consideradas

- **Monitor externo** (UptimeRobot, cron-job.org): es puntual y avisa de caídas, pero queda configurado fuera del repositorio y requiere una cuenta en otro servicio.
- **Tarea programada dentro de la aplicación** (`@Scheduled` contra su propia URL): no necesita nada externo, pero mezcla una cuestión de infraestructura con el código de la aplicación y no puede despertarla una vez suspendida.
- **Cron en una máquina local:** solo funciona mientras esa máquina está encendida.
- **Plan pago de Render:** elimina la suspensión, pero tiene costo.

## Consecuencias

- Dentro del horario, la API responde sin arranque en frío y se conservan los tokens y la caché entre pedidos.
- Los workflows programados de GitHub no son puntuales: con demoras largas el servicio puede suspenderse igual. En ese caso el siguiente ping lo despierta, pero un pedido que llegue antes espera el arranque. Si pasa seguido, se puede sumar un monitor externo sin cambiar el repositorio.
- Fuera del horario se mantiene el comportamiento del ADR 0019.
- El chequeo de salud consulta la base, así que en ese horario Neon tampoco se suspende y consume horas de cómputo de su plan gratuito.
- GitHub desactiva los workflows programados tras 60 días sin actividad en el repositorio; hay que reactivarlo a mano.
- Un ping que falla después de los reintentos deja la ejecución en rojo, lo que sirve como aviso básico de que la API no responde.
