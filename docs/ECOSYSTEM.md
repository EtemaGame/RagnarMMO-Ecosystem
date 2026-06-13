# RagnarMMO Ecosistema

Este workspace es la base paralela para convertir RagnarMMO en una familia de mods separados sin romper el repositorio monolitico funcional.

## Baseline

- El repositorio original queda en `C:\Users\Etema\Desktop\RagnarMMO`.
- Este entorno vive en `C:\Users\Etema\Desktop\RagnarMMO-Ecosistema`.
- La primera fase mantiene el codigo como baseline funcional antes de extraer modulos.
- Los cambios de arquitectura deben ser verificables por tests antes de separar el siguiente modulo.

## Modulos objetivo

- `ragnarmmo-core`
- `ragnarmmo-combat`
- `ragnarmmo-jobs`
- `ragnarmmo-items`
- `ragnarmmo-economy`
- `ragnarmmo-lifeskills`
- `ragnarmmo-mobs`
- `ragnarmmo-social`
- `ragnarmmo-client`

## Orden de extraccion

1. Mantener el baseline monolitico compilando en este workspace.
2. Convertir Gradle a multi-project con un modulo temporal de compatibilidad.
3. Extraer `ragnarmmo-core`.
4. Extraer `ragnarmmo-combat`.
5. Extraer `ragnarmmo-jobs`.
6. Extraer `ragnarmmo-items`.
7. Extraer `ragnarmmo-economy`.
8. Extraer `ragnarmmo-mobs`.
9. Extraer `ragnarmmo-lifeskills`.
10. Extraer `ragnarmmo-social`.
11. Extraer `ragnarmmo-client`.

## Reglas de compatibilidad

- Preservar IDs, comandos y datos criticos durante la primera migracion.
- Separar Life Skills de Jobs: Life Skills mantiene su progresion propia; Jobs usa Job Lv y puntos de job.
- Mantener Economy aislado para poder reescribir zeny, wallet y transacciones sin acoplar Items.
- No eliminar aliases legacy sin actualizar `docs/LEGACY_COMPATIBILITY.md`.
