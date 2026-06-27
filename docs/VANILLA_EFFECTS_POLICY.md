# Vanilla Effects Policy

Estado al 2026-06-25. Este documento define como tratar efectos vanilla de Minecraft dentro de RagnarMMO para evitar incongruencias con stats, combate y skills estilo Ragnarok Online. Las reglas activas de runtime se resumen en `RO_RUNTIME_RULES.md`.

## Decision

Los efectos vanilla no quedan activos como fuente mecanica ni como feedback de skills RO.

Si Minecraft u otro mod aplica un efecto vanilla, RagnarMMO lo procesa como entrada de compatibilidad:

1. Si existe equivalencia util, se convierte a estado RO propio.
2. Luego se remueve el efecto vanilla para evitar doble formula, doble dano o feedback contradictorio.
3. Si no existe equivalencia util, se remueve.

Regla:

- Efecto vanilla con impacto mecanico: debe traducirse a una semantica RagnarMMO.
- Efecto vanilla sin equivalencia RO: debe eliminarse del runtime.
- Skill RO nueva: no debe aplicar ni depender de `MobEffects` vanilla.

## Categorias

### Conversion activa

Estos efectos pueden entrar desde Minecraft u otros mods, pero no deben quedar activos:

| Vanilla | Tratamiento RagnarMMO |
| --- | --- |
| `POISON` | Poison RO y remover vanilla |
| `WITHER` | Poison RO y remover vanilla |
| `BLINDNESS` | Blind RO y remover vanilla |
| `CONFUSION` | Chaos RO y remover vanilla |
| `INVISIBILITY` | Hiding RO y remover vanilla |
| `MOVEMENT_SPEED` | Increase AGI RO de compatibilidad y remover vanilla |
| `MOVEMENT_SLOWDOWN` | Decrease AGI RO de compatibilidad y remover vanilla |
| `GLOWING` | remover; reveal se maneja por estado RO |

### Logica RO propia

Estos casos deben manejarse con tags/servicios propios, como `RoCombatStatusService`:

- STR/AGI/VIT/INT/DEX/LUK temporales.
- Hard DEF y soft DEF temporales.
- HIT/FLEE/ASPD/cast derivados.
- Provoke.
- Endure.
- Blessing.
- Angelus.
- Signum Crucis.
- Increase/Decrease AGI.
- Magnum Break fire bonus.
- Silence.
- Poison.

### Efectos vanilla eliminados

Estos efectos no tienen equivalencia activa cerrada y se remueven si aparecen:

- `DAMAGE_RESISTANCE`
- `DAMAGE_BOOST`
- `WEAKNESS`
- `DIG_SPEED`
- `REGENERATION`
- `ABSORPTION`
- `FIRE_RESISTANCE`

Cualquier otro efecto con namespace `minecraft:*` tambien se remueve si no fue convertido antes. Si despues se decide que alguno representa una mecanica RO real, debe agregarse como estado/atributo RagnarMMO propio antes de permitirlo.

## Cure / Detoxify

Pre-Renewal estricto:

- Cure remueve Blind, Chaos/Confusion y Silence.
- Detoxify remueve Poison.

Estado actual:

- Cure limpia Blind RO, Chaos RO y Silence RO propio. Tambien remueve `BLINDNESS`/`CONFUSION` si un efecto externo acaba de entrar antes del puente.
- Detoxify limpia Poison RO y, como puente, `POISON`/`WITHER` vanilla.
- Chaos RO propio existe y aplica movimiento/targeting caotico basico.
- Un caster silenciado no puede autocastear Cure.

## Ruwach / Hiding

Estado actual:

- Hiding tiene estado RO propio.
- Hiding ya no depende de `INVISIBILITY`.
- `INVISIBILITY` externo se convierte a Hiding RO y luego se remueve.
- Ruwach y Sight revelan Hiding RO. Ruwach dispara dano Holy cuando revela.

Pendiente:

- Cloaking futuro ya tiene estado runtime propio; falta skill/aplicador real y no debe depender de `INVISIBILITY`.

## Velocidad

Estado actual:

- Increase AGI y Decrease AGI ya tienen estado RO propio para AGI.
- `MOVEMENT_SPEED` externo se convierte a Increase AGI RO de compatibilidad y se remueve.
- `MOVEMENT_SLOWDOWN` externo se convierte a Decrease AGI RO de compatibilidad y se remueve.

Pendiente:

- Reemplazar los cambios de movimiento por un modificador controlado por RagnarMMO si se necesita precision completa.

## Dano degenerativo

Problema:

- `POISON` y `WITHER` hacen dano vanilla fuera del pipeline de combate RO.

Decision:

- Se convierten a Poison RO y luego se remueven para evitar doble dano.
- Poison RO usa tick damage propio: 3% MaxHP cada 3s, sin bajar de 25% MaxHP.

## Trabajo pendiente

- Crear `RoStatusType` o equivalente para estados RO canonicos si el set de estados sigue creciendo.
- Completar aplicadores y equivalencias futuras para estados RO ya creados: Curse, Sleep, Stun, Bleeding y variantes de slow/curse.
- Revisar efectos de otros mods cuando aparezcan y mapearlos a estados RO o removerlos.
