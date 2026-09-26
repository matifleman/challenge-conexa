# 0008. GitHub Flow con squash merge

- **Estado:** Aceptada
- **Fecha:** 2026-09-26

## Contexto

El proyecto lo desarrolla una sola persona, sin equipo de QA ni releases planificados, y está pensado para desplegarse automáticamente desde una rama. El historial de `main` tiene que ser fácil de leer y cada feature, fácil de revertir.

## Decisión

- **GitHub Flow:** cada feature completa (por ejemplo, una entidad con su paginación, filtros y tests) se desarrolla en su propia rama (`feat/...`) y entra a `main` por pull request. `main` está protegida (PR obligatorio, sin force push) y se mantiene siempre desplegable.
- **Squash and merge como única estrategia de merge:** cada PR entra a `main` como un único commit.
- **Commits:** Conventional Commits (`feat:`, `fix:`, `docs:`, `test:`, etc.), y un issue de GitHub por feature, que se cierra desde el PR con `Closes #N`.

## Alternativas consideradas

- **GitFlow (con `develop`):** útil con releases planificados, varios ambientes y QA que valida `develop` antes de pasar a `main`. Acá sumaría un merge extra por feature sin un control real en el medio.
- **Merge commit:** conserva todos los commits intermedios y agrega un commit de merge; `main` queda ramificada y con ruido.
- **Rebase and merge:** historial lineal, pero con todos los commits intermedios en `main`.

## Consecuencias

- `main` tiene un commit por feature, lineal y fácil de revertir; el detalle de cada paso queda en el PR.
- Una rama ya mergeada no se reutiliza: como el squash crea un commit nuevo, git no reconoce sus commits como parte de `main`. Cualquier cambio posterior va en una rama nueva.
- En un equipo con releases planificados y QA, se reevaluaría GitFlow.
