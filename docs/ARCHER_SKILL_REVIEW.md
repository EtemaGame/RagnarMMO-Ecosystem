# Archer Skill Review

Estado al 2026-06-22. Revision contra `docs/archer.md`.

## Resultado de implementacion

- Quest skills excluidas: Arrow Crafting / Making Arrow y Charge Arrow / Arrow Repel no estan en el arbol normal.
- Owl's Eye ahora modifica DEX antes de calcular dano, HIT, cast y estabilidad de bow.
- Vulture's Eye conserva HIT +SkillLv y deja el rango como dato explicito, sin simularlo con velocidad/gravedad de proyectil.
- Improve Concentration usa estado propio y agrega DEX/AGI por porcentaje sobre stats elegibles basicos actuales.
- Improve Concentration revela invisibilidad vanilla como puente temporal de Hiding/Cloaking.
- Double Strafe exige Bow + Arrow y consume 1 Arrow.
- Double Strafe fue corregido a 100..190% por hit, 2 hits, total 200..380%.
- Arrow Shower exige Bow + Arrow y consume 1 Arrow.
- Arrow Shower fue corregido a 80..125%, 3x3, knockback 2 celdas como dato.
- El perfil ranged minimo suma Bow ATK + Arrow ATK neutral basico mientras no exista municion RO final.

## Estado por skill

### Owl's Eye

Implementado:

- `DEXBonus = SkillLv`.
- El bonus entra antes de derived stats y combate.

Efectos cubiertos:

- HIT.
- Dano missile por DEX.
- Cast variable.
- ASPD indirecta donde DEX participa.
- Variacion de WeaponATK bow.

### Vulture's Eye

Implementado:

- `HITBonus = SkillLv`.
- `RangeBonus = SkillLv` queda en JSON.

Pendiente externo:

- El sistema de target/range todavia no consume dinamicamente `range_bonus` por skill.

### Improve Concentration

Implementado:

- Estado propio en `RoCombatStatusService`.
- `StatBonusPercent = 2 + SkillLv`.
- Aplica a DEX y AGI.
- Duracion y SP segun tabla.
- Reveal temporal usando `INVISIBILITY` como puente.

Limitacion consciente:

- El documento indica que los stats elegibles excluyen cartas. Actualmente se usa el total de atributos disponible como aproximacion hasta cerrar equipment/cards finales.

### Double Strafe

Implementado:

- Requiere Bow.
- Requiere Arrow vanilla como puente.
- Consume 1 Arrow.
- Dano total 200..380%, modelado como 2 hits de 100..190%.
- Usa ruta de dano fisico ranged por contrato.

Pendiente externo:

- Flechas RO finales: ATK, elemento y tipos especiales.
- Bloqueo por Pneuma cuando existan ground effects.
- Rango efectivo con Vulture's Eye.

### Arrow Shower

Implementado:

- Requiere Bow.
- Requiere Arrow vanilla como puente.
- Consume 1 Arrow.
- Dano 80..125%.
- Area 3x3.
- Knockback 2 celdas registrado como dato y aproximado por fuerza en Minecraft.

Pendiente externo:

- Target ground real.
- Interaccion con traps.
- Bloqueo por Pneuma.
- Reglas de knockback por mapas/WoE.
- Rango efectivo con Vulture's Eye.

## Pendientes transversales Archer

- Sistema final de flechas/ammunition: ATK, elemento, crit especial y consumo por tipo.
- Resolver de elemento de flecha para que bow use propiedad de municion.
- Targeting/range dinamico afectado por Vulture's Eye.
- Flag de ataque `PHYSICAL_RANGED` para Pneuma y otras defensas.
- Ground targeting para Arrow Shower.
- Knockback por celdas, no solo fuerza fisica de Minecraft.
