# Thief Skill Review

Estado al 2026-06-22. Revision contra `docs/thief.md`.

## Resultado de implementacion

- El arbol Thief contiene solo skills normales de primera clase: Double Attack, Improve Dodge, Steal, Hiding, Envenom y Detoxify.
- Double Attack ya no da critico global.
- Double Attack se resuelve con chance antes del HIT, suma HIT solo cuando activa y aplica 2 hits logicos sobre dagger.
- Improve Dodge conserva `FLEE += 3 * SkillLv`.
- Steal tiene tabla Pre-Renewal corregida y rango 1.
- Hiding tiene duracion y drain interval correctos en data.
- Envenom fue corregido a dano fisico Poison 100% + `15 * SkillLv` plano post-DEF.
- Envenom aplica Poison RO propio, no solo poison vanilla.
- Poison RO reduce DEF fisica 25%, hace tick de 3% MaxHP cada 3s y no baja al objetivo de 25% MaxHP.
- Detoxify limpia Poison RO y tambien limpia `POISON`/`WITHER` vanilla como compatibilidad temporal.

## Estado por skill

### Double Attack

Implementado:

- Requiere dagger por tag `ragnarmmo:daggers`.
- Chance `5 * SkillLv`.
- HIT bonus `+SkillLv` solo si el proc activa.
- Si activa, aplica `hitCount = 2`.

Limitacion consciente:

- El resultado de combate sigue empaquetado como una resolucion de dano agregada. Falta feedback visual/packet separado por hit si queremos mostrar dos numeros o interactuar con barreras por golpe.

### Improve Dodge

Implementado:

- `FLEE += 3 * SkillLv`.
- No da DEF, Perfect Dodge ni reduccion de dano.

### Steal

Implementado:

- Datos Pre-Renewal: SP 10, range 1, chance base 10/16/22/28/34/40/46/52/58/64%.

Pendiente externo:

- Loot table real por mob.
- Marcado persistente de mob ya robado.
- Restricciones completas contra frozen/stone cursed.
- Entrega de item robado sin afectar drops al morir.

### Hiding

Implementado:

- Datos correctos: duracion `30 * SkillLv`, drain `1 SP / (4 + SkillLv)s`, costo inicial 10 SP.
- La skill actual conserva invisibilidad/slowness vanilla como puente visual/mecanico.

Pendiente externo:

- Estado RO propio de Hiding.
- Toggle real.
- Drenaje periodico de SP.
- Bloqueo de movimiento, ataques, skills normales, items y regen.
- Deteccion por Insect/Demon/Boss protocol y skills reveal.

### Envenom

Implementado:

- Dano fisico Poison.
- Base 100% fisico + flat `15 * SkillLv`.
- Flat bonus se aplica despues de DEF.
- Poison chance `10 + 4 * SkillLv`.
- No aplica Poison a Undead property ni boss-like.

### Poison

Implementado:

- Estado propio en `RoCombatStatusService`.
- Hard DEF fisica efectiva x0.75.
- Tick cada 60 ticks.
- Dano 3% MaxHP.
- Piso de 25% MaxHP.

Pendiente externo:

- Definir si Poison debe usar damage source propio y si debe pasar por reglas PvP/party.
- Reemplazar/convertir totalmente `MobEffects.POISON`/`WITHER` cuando el sistema de estados RO quede cerrado.

### Detoxify

Implementado:

- Range 9.
- SP 10.
- Limpia Poison RO.
- Limpia `POISON` y `WITHER` vanilla como puente de compatibilidad.

## Pendientes transversales Thief

- Loot/drop service para Steal.
- Estado RO canonico para Hiding y reveal.
- Separacion visual de multiples hits para Double Attack.
- Reglas finales de Poison vanilla/otros mods contra Poison RO.
- Estados Frozen y Stone Curse propios para restricciones de Steal.
