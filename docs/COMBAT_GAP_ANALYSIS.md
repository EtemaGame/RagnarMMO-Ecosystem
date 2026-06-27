# Combat Gap Analysis

Fuente principal: `docs/combat.md`.

Estado: verificado contra el codigo actual al 2026-06-24. Este archivo registra brechas reales y reglas fijadas. Si el codigo ya fue corregido, no se mantiene como conflicto.

Leyenda:

- `OK`: coincide de forma util con el documento.
- `PARCIAL`: existe base funcional, pero falta precision o integracion.
- `NO`: no existe como sistema real.
- `BLOQUEADO`: depende de items, equipment, UI, economy u otro sistema externo.

## Reglas fijadas

### Criticos

- El critico puede conectar aunque el HIT normal fallaria.
- Perfect Dodge se evalua antes del dano y puede evitar el ataque.
- Crit Shield reduce la chance critica efectiva.
- El critico ignora Hard DEF y Soft DEF.
- Multiplicador base de critico: `1.4`.

### Nivel del player

- El combate ya no usa `player.experienceLevel`.
- Para players se usa `IPlayerStats.getLevel()`.

### Elemento y tamano

- `RagnarDamageCalculator.applyModifiers` aplica elemento siempre.
- Para dano fisico, tambien aplica penalidad de tamano del arma.
- Para dano magico, no aplica penalidad de tamano del arma.

### Orden de dano fisico

1. Resolver hit: Perfect Dodge, Critical, HIT/FLEE si no fue critico.
2. Construir ataque por golpe: StatusATK, WeaponATK, Size modifier solo sobre WeaponATK, modificadores activos como Over Thrust o Provoke ofensivo.
3. Aplicar defensa por golpe: Hard DEF porcentual, Soft DEF. Critical ignora Hard DEF y Soft DEF. Componentes que ignoran DEF se suman despues.
4. Sumar bonuses planos tardios: Weapon Mastery, Demon Bane, flat damage bonus de skill.
5. Aplicar propiedad y modificadores: Elemento ofensivo vs defensivo, bonuses por race/element/size/special, reducciones entrantes.
6. Aplicar multi-hit: Double Attack, `hit_count` de skill fisica.

Estado actual del orden:

- Autoattack directo aplica size solo a WeaponATK y deja Double Attack al final.
- Autoattack directo y contrato de ataque basico usan `PhysicalAttackProfile` con componentes separados y `BasicPhysicalAttackFormulaService`.
- Skills fisicas por contrato aplican `hit_count` al final del bloque fisico.
- `CombatResolution.hitCount` permite representar golpes separados y consumo por golpe de Safety Wall.
- DEF fisica comun usa `DefenseFormulaService`.
- `SkillCombatSpec` tiene flags centrales: `range_type`, `element_policy`, `defense_policy` y `multi_hit_policy`.
- El contrato de combate resuelve mobs desde `MobProfile`, por lo que level, stats base, HIT, FLEE, ATK, MATK, DEF, MDEF, race, elemento y tamano son comunes entre autoattack/skills.
- Queda pendiente completar data por skill donde los defaults no basten.

## Rutas reales de combate

Estado: `PARCIAL`.

Rutas activas:

1. Autoattack jugador -> target: `RagnarCombatEngine.processBasicAttackRequest` -> `resolve`.
2. Skill packet combat: `ServerboundRagnarSkillUsePacket` -> `RagnarSkillResolver` + `CombatContract.resolveSkill`.
3. Skills de job hotbar: `ServerboundUseJobSkillPacket` -> `JobSkillExecutor` -> `JobSkillEffectRegistry`. Las skills de dano migradas usan la ruta de combate comun.
4. Mob -> jugador: `MobPreRenewalDamageEventHandler.onLivingHurt`.

Brecha real:

- `OK`: autoattack directo y contrato de ataque basico comparten snapshot fisico para StatusATK, WeaponATK, ArrowATK, WeaponLevel, rango y arma.
- `PARCIAL`: skills fisicas ya consumen el mismo `PhysicalAttackProfile`, pero su ruta porcentual de skill conserva reglas propias por `SkillCombatSpec`.

## Autoattack fisico jugador

Estado: `PARCIAL`.

Existe:

- Cooldown de ataque basico, validacion server-side de target, melee/ranged basico segun arma.
- HIT = level + DEX, FLEE = level + AGI, Perfect Dodge antes de HIT/FLEE, critico antes de HIT/FLEE.
- Critico ignora DEF, BaseATK melee/ranged por STR/DEX/LUK.
- Size modifier aplicado sobre WeaponATK, elemento ofensivo desde NBT del arma, elemento defensivo desde perfil de mob.
- DEF antes de elemento/modificadores, Safety Wall y Pneuma bloquean rutas fisicas propias.
- FLEE penalty por mobbing: mas de 2 monstruos atacando reducen FLEE 10% por monstruo adicional.

Falta:

- `PARCIAL`: WeaponATK viene de atributos vanilla/NBT, no de una estructura RO final con WeaponATK, WeaponLevel, refine y upgrade bonus.
- `PARCIAL`: dual wield alterna manos y valida offhand, pero faltan penalizaciones oficiales, masteries y restricciones finales.
- `PARCIAL`: Double Attack ya genera `hitCount`, popoffs separados y consume Safety Wall por golpe; falta resolucion/log persistido por golpe individual.

## Ranged y bow

Estado: `PARCIAL`.

Existe:

- Deteccion basica de arma ranged, BaseATK ranged usa DEX.
- `RangedWeaponStatsHelper` crea snapshots para proyectiles.
- Arrow ATK temporal neutral `25` en rutas soportadas.
- Pneuma bloquea ataques fisicos ranged del pipeline propio y proyectiles vanilla.
- Vulture's Eye suma `range_bonus` al rango efectivo de autoattack ranged, Double Strafe y Arrow Shower.

Falta:

- `BLOQUEADO`: flecha equipada real, ArrowATK, elemento y propiedades de flecha RO.
- `PARCIAL`: validar si el rango aumentado debe aplicar a proyectiles vanilla fuera del pipeline propio.

## Magic

Estado: `PARCIAL`.

Existe:

- MATK min/max por INT, rama magica en `CombatContract`, MDEF porcentual + soft MDEF.
- Cast real para job hotbar y packet combat, cast variable reducido por DEX.
- Finalizacion de cast en tick server-side, interrupcion por dano si no hay resistencia total.
- Magia por contrato usa `ALWAYS_HIT` por defecto.
- Silence RO propio bloquea casteo en job hotbar y packet combat.
- Cure limpia Silence si el caster puede castear.
- Blind RO reduce HIT/FLEE; Chaos RO existe como puente desde Confusion/Nausea y aplica targeting/movimiento caotico basico.
- Hiding RO propio bloquea targeting normal, restringe movimiento y ya no depende de `INVISIBILITY`.
- Stone Curse cambia defensa a Earth 1, inmoviliza con estado propio y se rompe con dano.
- Stun, Sleep, Curse, Bleeding y Cloaking tienen runtime base propio.
- Los efectos vanilla compatibles se convierten a estados RO y luego se remueven; los no compatibles se remueven.

Falta:

- `PARCIAL`: no todas las reglas especiales de magia estan completas.
- `PARCIAL`: estados RO canonicos base existen para Poison, Silence, Blind, Chaos, Frozen, Stone Curse, Hiding, Cloaking, Stun, Sleep, Curse y Bleeding; falta afinacion fina por servidor objetivo y aplicadores completos.
- `BLOQUEADO`: UI final de barra de cast, modificadores de equipo/cartas para cast e interrupcion.

## Mob -> jugador

Estado: `PARCIAL`.

Existe:

- Usa `MobProfile`, HIT de mob contra FLEE del jugador, Perfect Dodge del jugador.
- Dano normal usa `atkMin..atkMax`, ataque normal de mob usa elemento Neutral, no crit normal.
- DEF fisica y reducciones se aplican, Safety Wall/Pneuma bloquean melee/proyectiles vanilla.
- Ranged de mob se clasifica por proyectil o por `profile.attackRange() > 3`.

Falta:

- `NO`: MaxATK garantizado para critico de mob por skill futura.
- `PARCIAL`: si se agrega skill critica de mob, debe saltar DEF.

## HIT, FLEE, CRIT y Perfect Dodge

Estado: `PARCIAL`.

Existe:

- HIT: `level + DEX`, FLEE: `level + AGI`.
- Hit chance: `80 + HIT - FLEE`, clamp 5%-95%.
- Crit chance: `1 + LUK * 0.3`, Crit shield: `floor(LUK / 5)`.
- Katar duplica la chance critica antes de aplicar Crit Shield.
- Perfect Dodge: `1 + floor(LUK / 10)`.
- Perfect Dodge antes de crit/hit, crit antes de HIT/FLEE, FLEE penalty por mobbing.

Falta:

- `PARCIAL`: bonuses externos de CRIT/PDODGE dependen de equipment/cards finales.

## DEF y MDEF

Estado: `PARCIAL`.

Existe:

- Hard DEF como reduccion porcentual, Soft DEF despues de hard DEF, critico salta DEF.
- MDEF porcentual + soft MDEF, `DefenseFormulaService` centraliza formulas.
- La ruta comun ya usa roll Pre-Renewal de SoftDEF de jugador o mob segun tipo.
- La SoftDEF de mob usa VIT del `MobProfile` en autoattack directo y en contrato de skills.
- La DEF fisica de armaduras vanilla equipadas entra como Hard DEF desde `Attributes.ARMOR` en casco, pechera, pantalones y botas. Escudo offhand tambien puede aportar si declara ese atributo.

Falta:

- `BLOQUEADO`: hard DEF/MDEF de equipo/refine RO final por data propia.

## Size modifier

Estado: `PARCIAL`.

Existe:

- Dagger, Sword 1H, Sword 2H por tag `two_handed`, Spear sin Peco, Axe, Mace, Bow, Katar y Rod/Staff/Wand.
- Rod/Staff/Wand usa 100/100/100 contra small/medium/large.
- Si no hay size resuelto desde un perfil inicializado, se usa hitbox: small <= 1 bloque, medium <= 3, large > 3.

Falta:

- `NO`: Fist, Spear+Peco, Book, Claw, Instrument, Whip, Gun, Huuma Shuriken.
- `BLOQUEADO`: tags/tipos de armas RO finales.

## Elementos

Estado: `PARCIAL`.

Existe:

- Tabla elemental Pre-Renewal con defensa nivel 1-4, elemento defensivo de mob desde perfil.
- Frozen cambia defensa a Water 1, default Neutral 1.
- Elemento ofensivo de arma desde NBT, `shadow` alias de `DARK`, ataque normal de mob usa Neutral.

Falta:

- `BLOQUEADO`: elemento de flecha equipada.
- `PARCIAL`: endows y forced element por skill no cerrados para todas las rutas.
- `PARCIAL`: decidir si dominio interno queda como `DARK` o se renombra a `SHADOW`.
- `PARCIAL`: multiplicadores elementales 0/negativos existen en tabla, pero varias rutas clipean dano final.

## Cards, bonuses y reducciones

Estado: `PARCIAL`.

Existe:

- Lectura de modificadores por NBT, claves ofensivas por all/race/element/size y reducciones entrantes por all/race/element/size.
- Las categorias ofensivas se suman dentro de su categoria y se multiplican entre categorias.
- Las categorias defensivas se suman dentro de su categoria y se multiplican entre categorias.
- Hay card JSON para todos los mobs vanilla perfilados en `VanillaMobTaxonomyDefaults`; `illusioner` queda como extra valido.

Falta:

- `PARCIAL`: cards actuales tienen bonuses basicos; faltan efectos especiales tipo RO y balance final.
- `BLOQUEADO`: equipment final.

## ASPD y autoattack timing

Estado: `PARCIAL`.

Existe:

- Cooldown server/client mediante `AttackCadenceCalculator`.
- ASPD usa modelo documentado `WD = 200 - baseWeaponASPD`, reduccion por AGI/DEX y conversion `50 / (200 - ASPD)`.
- `WeaponAspdTableService` centraliza base ASPD por job/familia de arma y respeta overrides `combatProfile.aspd`.

Falta:

- `PARCIAL`: la tabla clase/arma actual es provisional de compatibilidad, no tabla oficial final de RO.
- `PARCIAL`: speed modifiers existen como parametro de formula, pero faltan fuentes reales de buffs/pociones/equipo RO.
- `BLOQUEADO`: pociones ASPD.

## Skills fisicas y magicas

Estado: `PARCIAL`.

Existe:

- `SkillCombatSpecResolver` crea specs, `CombatContract.resolveSkill` tiene rutas fisica y magica.
- Muchas skills de dano del job hotbar pasan por la ruta de combate.
- Ground target existe para Pneuma, Safety Wall, Fire Wall, Thunder Storm y Arrow Shower.
- `SkillCombatSpec` tiene flags centrales: `range_type`, `element_policy`, `defense_policy`, `multi_hit_policy`.
- Skills de dano revisadas ya declaran bloque `combat` en data.
- Resoluciones de combate tienen `hitCount` para golpes multiples separados.
- Safety Wall consume golpes segun `hitCount`.
- Thunder Storm agenda impactos separados usando `hit_spacing_ticks`.
- `SkillCombatSpecResolver` parsea `damage_type`, `element`, `hit_policy`, `range_type`, `element_policy`, `defense_policy` y `multi_hit_policy` desde data.

Falta:

- `PARCIAL`: multi-hit emite popoffs separados; falta resoluciones persistidas por golpe para logs/analytics.
- `PARCIAL`: algunas skills conservan efectos legacy auxiliares despues del contrato.
- `OK`: Blessing ya no limpia `WITHER`; Poison/Wither quedan bajo Detoxify y conversion Poison RO.

## HP, SP y recursos

Estado: `PARCIAL`.

Existe:

- HP/SP max y regen derivados desde stats, skills consumen SP, sync de recursos.

Falta:

- `PARCIAL`: HP/SP usan formulas simplificadas, no tablas job/class completas.
- `BLOQUEADO`: peso, consumibles, potions, inventory/equipment final.

## Pendiente implementable ahora

1. Completar reglas fisicas finas que aun dependen de data final: refine, upgrade bonus y dual wield avanzado.
2. Reemplazar tabla ASPD provisional por tabla oficial validada si se decide buscar paridad exacta de RO.
3. Completar aplicadores y afinacion fina de estados RO: Cloaking skill futura, fuentes reales de Stun/Sleep/Curse/Bleeding y consumibles reales.

## Pendiente bloqueado por otros sistemas

- Flechas RO reales.
- Armas RO reales: WeaponATK, WeaponLevel, refine, tipo, slots/cards.
- Equipment/inventario final.
- Economy/zeny.
- Consumibles/catalysts.
- Party/friendly targeting.
- UI final de cast/menus.
- Aplicadores completos para estados RO y politica final para efectos de otros mods.

## Pruebas de combate

- `RagnarCombatEngineFormulaTest.java` cubre: critico conecta aunque HIT normal fallaria, crit shield, elemento en calculador comun, dano magico sin penalidad de tamano.
