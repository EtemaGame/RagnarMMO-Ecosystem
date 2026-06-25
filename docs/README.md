# RagnarMMO Docs

Documentacion viva del mod unico RagnarMMO.

## Documentos activos

### Core y Estado
- `ARCHITECTURE.md`: reglas actuales de arquitectura, ownership por paquete interno y dependencias opcionales.
- `MOD_AUDIT.md`: estado real del mod: funcional, parcial, conceptual y proximo orden recomendado.
- `COMBAT_GAP_ANALYSIS.md`: estado detallado del codigo de combate frente a las formulas RO objetivo.
- `SKILL_EXTERNAL_DEPENDENCIES.md`: estado de skills por clase y sus dependencias externas.

### Sistemas y Reglas
- `VANILLA_EFFECTS_POLICY.md`: politica sobre el uso o reemplazo de efectos de pociones/mods vanilla.
- `RO_RUNTIME_RULES.md`: reglas activas de runtime para ASPD, estados RO, efectos vanilla y atributos vanilla portados.
- `GROUND_CELLS.md`: regla de dimensionamiento espacial para celdas/areas (1 RO cell = 1 bloque Minecraft).
- `CARDS.md`: estado actual de cards, modifiers soportados y efecto runtime de cada card.
- `Inventory.md`: direccion e ideas para la reimplementacion del sistema de inventario y equipo.
- `Elementosyrazas.md`: taxonomias usadas en los perfiles de los mobs.

### Mantenimiento y Estabilizacion
- `LEGACY_COMPATIBILITY.md`: aliases e IDs legacy que no deben eliminarse sin migracion.
- `STABILIZATION.md`: guardrails de estabilizacion y smoke checklist.

### Referencias de Formulas Pre-Renewal
Documentos que contienen informacion extraida de iRO Wiki y RateMyServer como referencia base:
- `combat.md`: base comun de dano, defensa, hits y resolucion.
- `acolyte.md`: skills Acolyte.
- `archer.md`: skills Archer.
- `mage.md`: skills Mage.
- `merchant.md`: skills Merchant.
- `swordman.md`: skills Swordman.
- `thief.md`: skills Thief.

## Regla de Mantenimiento de Documentos

Si un documento empieza a repetir estado operativo, mover esa informacion a `MOD_AUDIT.md` (o `COMBAT_GAP_ANALYSIS.md` / `SKILL_EXTERNAL_DEPENDENCIES.md` segun corresponda) o borrarla. Las formulas maestras residen en los docs de referencia y no deben repetirse en los archivos de estado.
