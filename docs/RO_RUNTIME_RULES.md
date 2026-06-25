# RO Runtime Rules

Estado al 2026-06-25.

Este documento registra reglas activas del runtime, no formulas maestras. Las formulas maestras siguen en `combat.md` y en los docs por clase.

## ASPD

La formula activa es Pre-Renewal compatible:

- `WD = 200 - baseWeaponASPD`
- `ASPD = 200 - (WD - (floor(WD * AGI / 25) + floor(WD * DEX / 100)) / 10) * (1 - SM) + flatBonus`
- escudo aplica penalizacion plana de `-5 ASPD`
- ataques por segundo: `50 / (200 - ASPD)`

La fuente de `baseWeaponASPD` es `WeaponAspdTableService`:

1. Si el item declara `combatProfile.aspd` en `ro_item_rules`, ese valor gana.
2. Si no hay override, se clasifica la familia de arma por tags/clase vanilla.
3. Se usa una tabla provisional por primera clase y familia de arma.

La tabla actual es una tabla interna de compatibilidad para evitar constantes dispersas. No se declara como tabla oficial definitiva de RO. Cuando exista una tabla validada por clase/arma, debe reemplazar este servicio sin cambiar las rutas consumidoras.

Familias activas:

- `UNARMED`
- `DAGGER`
- `ONE_HANDED_SWORD`
- `TWO_HANDED_SWORD`
- `SPEAR`
- `AXE`
- `MACE`
- `BOW`
- `ROD`
- `KATAR`

## Dual Wield

Estado actual:

- El input puede alternar main hand/offhand si ambas manos tienen armas validas.
- La ruta server-side valida que el offhand no sea escudo ni projectile weapon.
- Offhand aplica penalizacion temporal `-8 ASPD`.

Pendiente:

- Penalizaciones oficiales por mano.
- Masteries de mano derecha/izquierda.
- Restricciones por clase/arma.
- On-hit/reflejos/cards por golpe independiente.

## Estados RO Propios

Estados con logica propia actual:

- `Poison`: DEF fisica `*0.75`, drena `3% MaxHP` cada 3s, no baja de `25% MaxHP`.
- `Silence`: bloquea casteo de job hotbar y packet combat.
- `Blind`: HIT `*0.75` y FLEE `*0.75`.
- `Chaos`: estado propio y puente desde Confusion/Nausea; mobs pierden target y se mueven erraticamente, players reciben empujes erraticos leves.
- `Frozen`: cambia defensa elemental a Water 1 y se rompe con dano.
- `Stone Curse`: cambia defensa elemental a Earth 1, inmoviliza con estado propio y se rompe con dano.
- `Hiding`: estado propio; no usa `INVISIBILITY` como fuente de verdad, bloquea target normal, restringe movimiento y bloquea ataques/skills normales.

## Conversion de Efectos Vanilla

Regla general:

- Los efectos vanilla no son fuente final de formulas RO.
- Si un efecto vanilla afecta mecanicas centrales, se convierte a estado RO propio.
- Despues de convertir, el efecto vanilla se remueve.
- Si no hay equivalencia RO util, el efecto vanilla se remueve.

Conversion activa:

| Vanilla | RO runtime |
| --- | --- |
| `POISON` | se convierte a Poison RO y se remueve para evitar doble dano |
| `WITHER` | se convierte a Poison RO y se remueve como puente temporal |
| `BLINDNESS` | aplica/renueva Blind RO y se remueve |
| `CONFUSION` | aplica/renueva Chaos RO y se remueve |
| `INVISIBILITY` | aplica/renueva Hiding RO y se remueve |
| `GLOWING` | se remueve; reveal usa estado RO |
| `MOVEMENT_SPEED` | aplica/renueva Increase AGI RO de compatibilidad y se remueve |
| `MOVEMENT_SLOWDOWN` | aplica/renueva Decrease AGI RO de compatibilidad y se remueve |

Efectos peligrosos:

- `DAMAGE_RESISTANCE`
- `DAMAGE_BOOST`
- `WEAKNESS`
- `DIG_SPEED`
- `REGENERATION`
- `ABSORPTION`
- `FIRE_RESISTANCE`

Estos y cualquier otro efecto `minecraft:*` no convertido no deben usarse como formula principal ni feedback de una skill RO. Si aparecen, se remueven hasta que exista una equivalencia RO propia.

## Atributos Vanilla y RagnarMMO

Regla:

- Minecraft `MAX_HEALTH` se usa como puente runtime para que el HP maximo RO sea real en el motor vanilla.
- Los stats primarios RO viven en atributos RagnarMMO: STR, AGI, VIT, INT, DEX, LUK.
- `MAX_SP`, `MAX_MANA`, `CRIT_CHANCE`, `CRIT_DAMAGE`, `LIFE_STEAL`, `ARMOR_PIERCE`, `ARMOR_SHRED`, `OVERHEAL` y `MAGIC_DEFENSE` viven como atributos RagnarMMO.
- Vanilla `ATTACK_DAMAGE`, `ARMOR` y similares solo se leen como fallback o compatibilidad cuando aun no hay item/equipment RO final. `MOVEMENT_SPEED` como efecto se convierte/remueve; el atributo de movimiento queda pendiente de politica final.

No hacer:

- No usar `DAMAGE_BOOST` como STR/ATK.
- No usar `DAMAGE_RESISTANCE` como DEF/MDEF.
- No usar `REGENERATION` como Increase HP Recovery.
- No usar `ABSORPTION` como MaxHP.

## Cure y Detoxify

- Cure remueve Blind RO, Chaos RO, `BLINDNESS`, `CONFUSION` y Silence RO.
- Un caster silenciado no puede autocastear Cure.
- Detoxify remueve Poison RO y limpia `POISON`/`WITHER` vanilla.

## Comandos Admin/Dev

Raiz: `/roadmin`, permiso requerido `2`.

Comandos activos:

- `/roadmin stats reset [target]`: reinicia stats primarios y conserva job/progresion actual.
- `/roadmin skills reset [target]`: limpia skills, cooldowns y hotbar de skills.
- `/roadmin stat set <str|agi|vit|int|dex|luk> <value> [target]`.
- `/roadmin stat add <str|agi|vit|int|dex|luk> <value> [target]`.
- `/roadmin level base set <value> [target]`.
- `/roadmin level job set <value> [target]`.
- `/roadmin exp base set|add <value> [target]`.
- `/roadmin exp job set|add <value> [target]`.
- `/roadmin job set <novice|swordsman|mage|archer|thief|merchant|acolyte> [target]`.
