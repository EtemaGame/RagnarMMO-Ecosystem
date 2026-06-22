# Acolyte Skill Review

Estado al 2026-06-22. Revision contra `docs/acolyte.md`, sin asumir sistemas futuros.

## Resultado de implementacion

Despues de esta revision se aplico la primera correccion de Acolyte:

- Holy Light fue removida por ser quest skill.
- Divine Protection paso de reduccion porcentual global a reduccion plana tardia contra Undead/Demon no-player.
- Demon Bane paso de ATK global a bono plano tardio contra Undead/Demon no-player.
- Heal usa `max(1, floor((BaseLv + INT) / 8)) * (4 + 8 * SkillLv)` y dano ofensivo Holy contra Undead.
- Increase AGI, Decrease AGI, Angelus, Blessing y Signum usan estados RO propios en vez de efectos vanilla como logica principal.
- Cure fue limitado a Blind/Confusion equivalentes actuales.
- Pneuma, Warp Portal, Teleport Lv2 y Aqua Benedicta quedaron con datos/logica base, pero dependen de ground effects, save/memo o items finales.

Las secciones siguientes conservan el analisis original de brechas para trazabilidad; el estado actualizado de dependencias externas esta en `docs/SKILL_EXTERNAL_DEPENDENCIES.md`.

## Resumen

La clase Acolyte existe en arbol, JSON y registry, pero varias skills estan implementadas como efectos vanilla o placeholders. La base de datos de costos/duraciones esta parcialmente correcta, pero las formulas Pre-Renewal aun no estan unificadas en servicios propios.

Holy Light aparece en el arbol activo, aunque el documento de Acolyte lo excluye por ser quest skill.

## Estado por skill

### Divine Protection

Documento:

- Pasiva contra Undead property y Demon race/family.
- No aplica contra jugadores.
- Reduccion plana: `3 * SkillLv + floor((BaseLv + 1) / 25)`.
- Se resta despues de DEF, no es Hard DEF porcentual.

Estado actual:

- JSON usa `damage_reduction_percent`, lo que contradice la formula plana.
- `JobPassiveStatsContributor` suma `divine * 0.3` a `physicalDamageReduction`, global y porcentual.
- No filtra Undead/Demon.
- No incluye bonus por BaseLv.
- No excluye jugadores.

Decision:

- Mover a formula plana aplicada al recibir dano, solo si atacante es Undead property o Demon race/family y defensor no es player.

### Demon Bane

Documento:

- Pasiva ofensiva contra Undead property y Demon race/family.
- No aplica contra jugadores.
- Bono tipo mastery: `3 * SkillLv + floor((BaseLv + 1) / 20)`.
- Ignora Armor DEF, pero no ignora VIT/soft DEF.

Estado actual:

- JSON usa `damage_bonus_percent` 4..40.
- `JobPassiveStatsContributor` suma `demon * 3` al `physicalAttack` global.
- No filtra Undead/Demon.
- No incluye bonus por BaseLv.
- Se aplica demasiado temprano, antes de DEF.

Decision:

- Implementar como bono plano tardio en dano fisico contra Undead/Demon, despues de Hard DEF y antes de elemento/modificadores, sin afectar players.

### Ruwach

Documento:

- Self, area 5x5, 10s, SP 10.
- Revela Hiding/Cloaking.
- Si revela enemigo, aplica magia Holy 145% MATK.

Estado actual:

- Aplica `GLOWING` al caster y a entidades cercanas.
- No comprueba si el objetivo estaba oculto.
- No aplica dano Holy 145% MATK.
- Usa radio 6.0 en codigo, mientras el JSON declara `reveal_radius`.

Decision:

- Mantener revelado visual como aproximacion temporal, pero separar la logica real: detectar estados ocultos y aplicar dano magico Holy solo a enemigos revelados.

### Pneuma

Documento:

- Ground support, range 9, area 3x3, 10s.
- Bloquea dano fisico ranged dentro del area.
- No bloquea melee, magia ni splash indirecto.
- No se solapa con Safety Wall/Pneuma.

Estado actual:

- Aplica `DAMAGE_RESISTANCE` al caster.
- No crea area en ground.
- No distingue ranged physical.
- No bloquea dano en el pipeline de combate.

Decision:

- Dejar como dependencia externa de ground effects/area controller, pero el hook de combate debe poder preguntar si la celda del target tiene Pneuma.

### Teleport

Documento:

- Lv1: teleport aleatorio dentro del mapa, SP 10.
- Lv2: teleport a Save Point, SP 9.
- Restricciones por mapas deben venir de reglas externas.

Estado actual:

- Ambos niveles hacen teleport aleatorio por offset cerca del jugador.
- Lv2 no va al save point.
- No valida celda segura, mapa ni flags.

Decision:

- Lv1 puede quedar temporal como random seguro local.
- Lv2 depende de sistema de save point/spawn RO.
- Restricciones de mapa van a dependencias externas.

### Warp Portal

Documento:

- Ground support, range 9.
- Consume Blue Gemstone x1.
- Max 3 portales activos.
- Destinos: save point + memos segun nivel.
- Hasta 8 usuarios.

Estado actual:

- Muestra mensaje y ejecuta Teleportation fallback.
- No crea portal.
- No consume catalyst.
- No usa memo/save point.

Decision:

- Registrar como pendiente externo hasta tener items consumibles, destinos/memo y entidad/area de portal.

### Heal

Documento:

- Formula: `max(1, floor((BaseLv + INT) / 8)) * (4 + 8 * SkillLv)`.
- Range 9.
- Heal ofensivo contra Undead property: `floor(HealAmount * HolyElementModifier / 2)`.
- Heal ofensivo ignora MDEF/INT del objetivo.

Estado actual:

- Cura solo `heal_base` 12..84.
- No usa BaseLv ni INT.
- Range JSON es 5, no 9.
- Contra `MobType.UNDEAD` hace dano magic igual a `heal_base`, no mitad modificada por elemento Holy.
- Usa `damageSources().magic()`, no ruta elemental Holy propia.

Decision:

- Crear formula de Heal Acolyte.
- Usar elemento defensivo `UNDEAD` del sistema RO, no solo `MobType.UNDEAD`.
- Aplicar dano ofensivo especial sin MDEF.

### Increase Agility

Documento:

- AGI + `2 + SkillLv`.
- MoveSpeed +25%.
- Duracion `40 + 20 * SkillLv`.
- SP `15 + 3 * SkillLv`.
- HP cost 15.
- Remueve Decrease Agility.

Estado actual:

- JSON tiene duracion y HP cost correctos, pero `cast_time_ticks` esta en 0 y doc pide 20 ticks.
- Codigo aplica `MOVEMENT_SPEED` vanilla al caster solamente.
- No aplica AGI real.
- No consume HP en efecto.
- No remueve Decrease Agility.
- No targetea aliado.

Decision:

- Implementar como status RO que modifique AGI derivada y movimiento.
- Conectar HP cost donde se consumen recursos.

### Decrease Agility

Documento:

- AGI - `2 + SkillLv`.
- MoveSpeed -25%.
- Duracion monster `30 + 10 * SkillLv`.
- SP `13 + 2 * SkillLv`.
- Success publico no confirmado: `40 + 2*Lv + floor((BaseLv + INT)/5) - TargetMDEF`.
- No funciona contra boss.

Estado actual:

- JSON usa SP 27..45, pero deberia 15..33.
- Duracion JSON 600..2400 ticks, pero deberia 800..2600 ticks.
- Range 8, documento indica 9.
- Codigo usa `MOVEMENT_SLOWDOWN` vanilla.
- No aplica `RoCombatStatusService.applyDecreaseAgi`, aunque el servicio existe.
- No usa BaseLv, INT ni TargetMDEF en success.
- No bloquea bosses.
- No remueve Increase Agility ni quicken/adrenaline/cart boost.

Decision:

- Corregir JSON.
- Usar status RO propio para AGI penalty y velocidad.
- Implementar boss guard y success formula como aproximacion marcada.

### Aqua Benedicta

Documento:

- Requiere agua valida.
- Usualmente consume Empty Bottle x1.
- Crea Holy Water.

Estado actual:

- Solo mensaje, sonido y particulas.
- No verifica agua.
- No consume Empty Bottle.
- No crea Holy Water.

Decision:

- Marcar como pendiente externo por item/inventario/consumible.

### Signum Crucis

Documento:

- AoE pantalla / 31x31.
- Afecta mobs Undead property o Demon race/family.
- Reduce Hard DEF: `10 + 4 * SkillLv`.
- No reduce soft/VIT DEF.
- Stackea con Provoke.
- Funciona en boss monsters.
- No funciona contra jugadores con Undead armor.

Estado actual:

- Aplica `WEAKNESS` vanilla a todas las entidades cercanas.
- No filtra Undead/Demon.
- No reduce Hard DEF.
- No usa success por nivel/base level.
- Radio codigo 8.0, JSON 6.

Decision:

- Implementar status propio de reduccion Hard DEF porcentual, separado de Provoke para que stackee.

### Angelus

Documento:

- Party buff.
- Multiplica soft/VIT DEF: `1 + 0.05 * SkillLv`.
- No aumenta VIT real.
- No aumenta HP, estados, recuperacion ni healing items.

Estado actual:

- Usa `DAMAGE_RESISTANCE` vanilla.
- No modifica soft/VIT DEF RO.
- No aplica a party.

Decision:

- Implementar status propio que multiplique soft DEF calculada.
- Aplicacion a party depende del sistema party/friendly target final.

### Blessing

Documento:

- Aliados: STR/DEX/INT + SkillLv.
- Duracion `40 + 20 * SkillLv`.
- SP `24 + 4 * SkillLv`.
- Remueve Curse.
- Ofensivo contra Undead/Demon mobs: STR/DEX/INT del target a 50%, no depende del nivel, no boss, no players.

Estado actual:

- Usa `DAMAGE_BOOST` y `DIG_SPEED` vanilla.
- No modifica STR/DEX/INT RO.
- No remueve Curse.
- No implementa uso ofensivo.
- No targetea aliado real.

Decision:

- Implementar status propio para bonuses de STR/DEX/INT.
- Conectar derived stats para leer esos bonuses.
- Ofensivo requiere soporte para debuffs de stats en mobs.

### Cure

Documento:

- Remueve Blind, Chaos/Confusion, Silence.
- Range 9.
- SP 15.

Estado actual:

- Remueve Poison, Wither, Weakness, Slowness, Blindness, Confusion.
- No existe Silence propio.
- Range JSON es 5, no 9.
- Limpia mas estados de los que indica Pre-Renewal.

Decision:

- Limitar Cure a estados RO equivalentes: Blind, Confusion/Chaos, Silence cuando exista.
- Poison/Wither deben quedar para Detoxify u otra skill.

## Pendientes externos

- Ground effects: Pneuma, Safety Wall, Warp Portal.
- Items/consumibles: Aqua Benedicta, Warp Portal Blue Gemstone, Holy Water, Empty Bottle.
- Save point y memo: Teleport Lv2 y Warp Portal.
- Party/friendly targeting: Angelus, Blessing e Increase Agility sobre aliados.
- Estados RO propios: Curse, Silence, Chaos, Hiding/Cloaking, stat buffs/debuffs.
- Mob profiles completos: Undead property, Demon race/family, boss-like, MDEF, base level.
- PvP: reglas de no aplicar Divine Protection/Demon Bane/Blessing ofensivo/Signum contra jugadores.

## Orden sugerido de implementacion

1. Quitar Holy Light del arbol normal de Acolyte.
2. Crear `AcolyteSkillFormulaService` para Divine Protection, Demon Bane, Heal, AGI buffs, Signum, Angelus y Blessing.
3. Corregir JSON de rangos, SP, duraciones y cast time.
4. Implementar pasivas de combate: Divine Protection y Demon Bane.
5. Implementar Heal correctamente, incluido Undead offensive Heal.
6. Implementar statuses propios: Increase AGI, Decrease AGI, Angelus, Blessing, Signum.
7. Dejar Pneuma/Warp/Aqua/Teleport Lv2 documentados como dependencias externas hasta tener ground effects, items y save/memo.
