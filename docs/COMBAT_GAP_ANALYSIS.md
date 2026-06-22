# Analisis de brechas del combate

Fuente de verdad actual: `docs/combat.md`.

Estado del analisis: verificado contra el codigo actual. No se asume comportamiento que no exista en el codigo.

Leyenda:

- `OK`: coincide de forma util con el documento.
- `PARCIAL`: existe una base, pero falta parte de la regla.
- `CONFLICTO`: el codigo hace algo distinto a lo que indica el documento.
- `NO`: no esta implementado como sistema real.

## Rutas reales de combate

Actualmente no hay una unica ruta de combate.

1. Autoattack jugador -> target:
   - Entrada: `BasicAttackEventHandler` / `ServerboundRagnarBasicAttackPacket`.
   - Motor: `RagnarCombatEngine.processBasicAttackRequest`.
   - Formula directa: `RagnarCombatEngine.resolve`.
   - Aplica dano real con `target.hurt`.

2. Skill packet combat:
   - Entrada: `ServerboundRagnarSkillUsePacket`.
   - Motor: `RagnarCombatEngine.handleSkillUseRequest`.
   - Resolver: `RagnarSkillResolver`.
   - Formula: `CombatContract.resolveSkill`.

3. Skills de job hotbar:
   - Entrada: `ServerboundUseJobSkillPacket`.
   - Motor: `JobSkillExecutor`.
   - Efectos: `JobSkillEffectRegistry`.
   - Muchas skills llaman directamente a `target.hurt`, usando dano vanilla/escalado, sin pasar por la formula RO completa.

4. Mob -> jugador:
   - Entrada: `MobPreRenewalDamageEventHandler.onLivingHurt`.
   - Reescribe el dano de mobs contra `ServerPlayer`.
   - Usa perfil de mob si existe.

Conclusion: antes de buscar precision tipo Ragnarok, hay que unificar estas rutas o al menos hacer que todas llamen a los mismos servicios puros de formula.

## Autoattack fisico jugador

Estado: `PARCIAL`.

Lo que existe:

- Cooldown de ataque basico.
- Validacion de target.
- Separacion basica melee/ranged usando `ProjectileWeaponItem`.
- HIT = level + DEX.
- FLEE = level + AGI para jugadores, o FLEE resuelto de mob si existe.
- Perfect Dodge solo en jugadores target.
- Critico antes de HIT/FLEE.
- BaseATK melee/ranged por STR/DEX/LUK.
- Size modifier aplicado al WeaponATK en la ruta directa.
- Elemento ofensivo desde NBT del arma.
- Elemento defensivo desde perfil del mob.
- Hard/Soft DEF aplicados al final.

Brechas:

- `CONFLICTO`: el critico ignora FLEE, pero no ignora DEF. `docs/combat.md` dice que critico trata Hard DEF y Soft/VIT DEF como 0.
- `CONFLICTO`: el orden actual aplica elemento y bonus antes de DEF en `RagnarCombatEngine.resolve`; el documento pone DEF/SoftDEF antes de elemento y bonuses.
- `PARCIAL`: el WeaponATK viene de atributos vanilla o modificadores del item, no de una estructura RO completa con WeaponATK, WeaponLevel, refine y upgrade bonus.
- `PARCIAL`: la variacion se aplica al total `statusAtk + sizedWeaponAtk`; el documento separa BaseATK estable y WeaponATK variable por DEX/WeaponLevel.
- `NO`: no existen checks estructurales reales de Safety Wall y Pneuma dentro del flujo.
- `NO`: no existe degradacion de FLEE por mobbing.
- `NO`: no existe Double Attack/Katar/dual wield como parte formal del flujo de autoattack.
- `NO`: no existe minimo/refine/mastery/bane/envenom en el orden documentado.

## Ranged y bow

Estado: `PARCIAL`.

Lo que existe:

- Deteccion basica de arma ranged.
- BaseATK ranged usa DEX como stat principal.
- Snapshot de flechas en `RangedWeaponStatsHelper`.
- Bow/crossbow tienen ATK fallback y draw ticks.

Brechas:

- `NO`: no existe formula documentada de WeaponATK bow con `ArrowATK`.
- `NO`: no se resuelve municion equipada como flecha RO.
- `NO`: el elemento de bow normal queda neutral en snapshot; el documento dice que normalmente viene de la flecha.
- `NO`: Pneuma no bloquea ataques ranged.
- `PARCIAL`: hay projectile snapshots, pero el autoattack packet directo no usa un modelo RO completo de bow/flecha.

## Magic

Estado: `CONFLICTO`.

Lo que existe:

- MATK min/max por INT existe en `DamageFormulaService`.
- Cast variable por DEX existe en `RoPreRenewalFormulaService.variableCastSeconds`.
- `CombatContract.resolveSkill` tiene rama magica.
- Algunas skills magicas existen en `JobSkillEffectRegistry`.

Brechas:

- `CONFLICTO`: muchas skills magicas ejecutan `target.hurt(...magic(), scaledMagicDamage(...))`, sin MATK/MDEF/elemento/cast real.
- `CONFLICTO`: `RagnarDamageCalculator.applyMagicDefense` termina usando `CombatMath.applyMagicDefense`, que resta `softMDEF + hardMDEF` plano. El documento dice `raw * (1 - MDEF/100) - INT - VIT/2`.
- `PARCIAL`: el cast existe como formula, pero no esta integrado como sistema comun con interrupcion, rango, linea de vision y after-cast para todas las skills.
- `NO`: no hay interrupcion de cast por recibir dano.
- `NO`: no hay regla central de "magia casteada no usa HIT/FLEE".

## Mob -> jugador

Estado: `PARCIAL`.

Lo que existe:

- Usa `MobProfile`.
- Usa `profile.hit()` contra FLEE del jugador.
- Usa Perfect Dodge del jugador.
- Daño normal del mob usa `atkMin..atkMax`, no STR del mob. Esto coincide con el documento.
- Aplica DEF fisica y reducciones.

Brechas:

- `CONFLICTO`: el elemento del ataque normal del mob se toma desde el elemento defensivo del mob. El documento dice que ataque normal de mob es Neutral; solo skills de mob cambian elemento.
- `CONFLICTO`: el codigo permite critico normal por `profile.crit()`. El documento dice que mobs no crittean por LUK normal; critico solo por skills como Critical Slash/Counter Attack.
- `NO`: no se distingue ataque melee/ranged del mob por attack range para Safety Wall/Pneuma.
- `NO`: no existe MaxATK garantizado para critico de mob por skill.
- `PARCIAL`: soft/hard DEF del jugador se aplican, pero si algun ataque critico existe no se salta DEF como indica el documento.

## HIT, FLEE, CRIT y Perfect Dodge

Estado: `PARCIAL`.

Coincidencias:

- HIT: `level + DEX`.
- FLEE: `level + AGI`.
- Hit chance: `80 + HIT - FLEE`, clamp 5%-95% en codigo.
- Critico se tira antes de HIT/FLEE.
- Perfect Dodge se tira antes del critico/hit.
- Mobs target no hacen Perfect Dodge en la ruta directa.

Brechas:

- `CONFLICTO`: crit real en codigo no incluye el `1 + LUK * 0.3` del documento; usa solo `LUK * 0.3`.
- `CONFLICTO`: crit shield de jugador suma `floor(level/15) + floor(LUK/5)`. El documento actual solo indica `TargetLUK/5`.
- `CONFLICTO`: Perfect Dodge de jugador en codigo es `LUK * 0.1%`; el documento dice `1 + floor(LUK/10) + bonos`.
- `NO`: Katar no duplica crit.
- `NO`: no hay FLEE penalty por multiples atacantes.

## DEF y MDEF

Estado: `CONFLICTO`.

Lo que existe:

- `DefenseFormulaService.applyPhysicalDefense` aplica Hard DEF porcentual y luego Soft DEF.
- `CombatMath.applyPhysicalDefense` existe tambien, pero usa otro orden: resta Soft DEF y luego aplica reduccion.
- `DefenseFormulaService.applyMagicDefense` aplica MDEF porcentual y soft MDEF.

Brechas:

- `CONFLICTO`: hay dos implementaciones fisicas con orden distinto.
- `CONFLICTO`: la ruta `CombatContract` usa `RagnarDamageCalculator`, que usa `CombatMath`, no `DefenseFormulaService`.
- `CONFLICTO`: la ruta magica de `RagnarDamageCalculator` no usa la formula porcentual correcta de MDEF.
- `PARCIAL`: SoftDEF jugador es determinista; el documento dice que tiene componente random.
- `PARCIAL`: SoftDEF mob se toma como stat resuelto, no se calcula como `VIT + rnd(0, floor(VIT/20)^2 - 1)`.
- `PARCIAL`: Hard DEF de equipo jugador parece existir como campo derivado, pero no hay una integracion completa de armaduras/refine RO.

## Size modifier

Estado: `PARCIAL`.

Coincide para:

- Dagger.
- Sword 1H.
- Sword 2H si tag `two_handed`.
- Spear sin Peco.
- Axe.
- Mace.
- Bow.
- Katar.

Brechas:

- `CONFLICTO`: staff/wand se tratan como mace; el documento dice que Rod es 100/100/100.
- `NO`: Fist, Spear+Peco, Book, Claw, Instrument, Whip, Gun y Huuma Shuriken no estan modelados.
- `PARCIAL`: depende de clases vanilla y tags; si los items RO no tienen tags correctos, caen a 100/100/100.
- `CONFLICTO`: en `RagnarDamageCalculator.applyModifiers`, el size modifier se aplica a todo el dano fisico. El documento dice que afecta WeaponATK, no todo el dano.

## Elementos

Estado: `PARCIAL`.

Lo que existe:

- Tabla elemental Pre-Renewal con nivel defensivo 1-4.
- Elemento defensivo de mob desde perfil.
- Elemento defensivo default Neutral 1.
- Elemento ofensivo de arma desde NBT.

Brechas:

- `CONFLICTO`: ataque normal de mob usa elemento defensivo del mob; debe ser Neutral.
- `NO`: elemento de flecha no esta integrado.
- `NO`: endows y forced element por skill no estan completos.
- `PARCIAL`: Shadow se mapea como `DARK`; funcionalmente puede servir, pero conviene normalizar nombre de dominio a `SHADOW`.
- `PARCIAL`: multiplicadores negativos/0 existen en tabla, pero varias rutas hacen clamp a minimo 0 o 1, asi que no esta claro si se preserva heal/negative/0 visual como RO.

## Cards, bonuses y reducciones

Estado: `CONFLICTO`.

Lo que existe:

- Lee modificadores NBT de items/cartas.
- Tiene keys para `damage_all`, `damage_race`, `damage_element`, `damage_size`.
- Tiene reduccion elemental entrante.

Brechas:

- `CONFLICTO`: todos los bonuses ofensivos se suman en una sola categoria. El documento dice: mismo tipo suma, categorias distintas multiplican.
- `NO`: reducciones por size/race/special no existen como categorias completas.
- `PARCIAL`: no hay aplicacion clara de mastery/bane/refine/envenom en el orden documentado.

## ASPD y autoattack timing

Estado: `PARCIAL`.

Lo que existe:

- Cooldown server y client usan `AttackCadenceCalculator`.
- ASPD se convierte a ataques por segundo con `50 / (200 - ASPD)`.
- Hay base ASPD melee/ranged simplificada.

Brechas:

- `CONFLICTO`: la formula de ASPD actual es aproximada: `baseWeaponAspd + AGI*0.25 + DEX*0.1 - shieldPenalty`.
- `NO`: no existe modelo documentado `WD = 50 * BTBA` ni speed modifiers.
- `NO`: no hay tabla clase/arma.
- `NO`: potions ASPD no estan modeladas con regla de no apilar.

## Skills fisicas

Estado: `CONFLICTO`.

Lo que existe:

- `SkillCombatSpecResolver` crea specs para varias skills.
- `CombatContract.resolveSkill` tiene ruta fisica.
- `JobSkillEffectRegistry` tiene muchos efectos activos.

Brechas:

- `CONFLICTO`: muchas skills fisicas del job hotbar aplican `target.hurt` directo con `scaledAttackDamage`, sin HIT/FLEE/DEF/elemento/cards/size RO.
- `CONFLICTO`: `SkillCombatSpecResolver` no parsea realmente `damage_type`, `element` ni `hit_policy`; devuelve fallbacks.
- `PARCIAL`: hay defaults por skill, pero no reglas especiales completas por skill.
- `NO`: no hay flags centrales para autohit, bonus HIT, ignore DEF, forced element, weapon element, ranged/melee, multi-hit real.

## HP, SP y recursos

Estado: `PARCIAL`.

Lo que existe:

- HP/SP max y regen se derivan desde stats.
- Skills consumen SP en `JobSkillExecutor`.
- Hay sync de recursos.

Brechas:

- `PARCIAL`: HP/SP usan formulas simplificadas, no tablas job/class Pre-Renewal.
- `NO`: peso, estados y muchas resistencias todavia no participan en combate como indica el documento.

## Decision tecnica recomendada

Para implementar correctamente, no conviene seguir parchando cada ruta. La limpieza correcta es:

1. Declarar `docs/combat.md` como fuente de verdad y marcar cualquier doc antiguo conflictivo como obsoleto.
2. Crear un nucleo puro de formulas RO:
   - hit/flee/crit/perfect dodge.
   - physical BaseATK.
   - WeaponATK melee.
   - WeaponATK bow + ArrowATK.
   - DEF fisica.
   - MATK/MDEF.
   - size table.
   - element table.
   - bonus/reduction categories.
3. Crear snapshots unificados:
   - `AttackContext`: basic/skill, melee/ranged/magic, hit policy, element policy, weapon, arrow, skill modifier.
   - `AttackerSnapshot`: stats, level, weapon stats, cards, buffs.
   - `DefenderSnapshot`: FLEE, Perfect Dodge, crit shield, DEF/MDEF, race, size, element, reductions.
4. Hacer que autoattack, skill packet, job hotbar y mob damage llamen al mismo nucleo.
5. Reescribir las skills para que sus efectos visuales/estados sean secundarios; el dano debe salir del nucleo.
6. Agregar tests por regla del documento antes de extender contenido.

## Orden de implementacion propuesto

1. Unificar formula fisica basica jugador -> mob:
   - crit ignora DEF.
   - orden correcto: hit/crit, BaseATK, WeaponATK, size, DEF, softDEF, elemento, bonuses, reductions.
   - WeaponATK separado de BaseATK.

2. Corregir HIT/CRIT/Perfect Dodge:
   - crit base `1 + LUK*0.3`.
   - crit shield segun documento actual: `TargetLUK/5`.
   - Perfect Dodge `1 + floor(LUK/10) + bonuses`.
   - Katar x2 crit cuando exista categoria katar.

3. Corregir DEF/MDEF:
   - eliminar doble implementacion contradictoria.
   - SoftDEF jugador con random.
   - SoftDEF mob desde VIT si existe.
   - MDEF porcentual + `INT + VIT/2`.

4. Corregir weapon/size:
   - tabla completa por tipo de arma.
   - tags/tipos RO, no clases vanilla como fuente principal.
   - size solo sobre WeaponATK.

5. Implementar bow real:
   - flecha equipada.
   - ArrowATK.
   - elemento de flecha.
   - Pneuma hook preparado.

6. Corregir mob normal attack:
   - elemento Neutral.
   - sin crit normal por LUK/profile.
   - crit solo por skill futura.
   - attack range para melee/ranged.

7. Migrar skills:
   - `JobSkillEffectRegistry` deja de aplicar dano directo.
   - `CombatContract` o su reemplazo calcula dano.
   - cada skill declara hit policy, element policy, damage type, hit count, ranged/melee, ignore DEF si aplica.

8. ASPD:
   - reemplazar formula aproximada por BTBA/WD/SM.
   - tabla clase/arma.
   - potions no apilables.

## Bloqueadores reales

- Falta definir de donde saldran, en datos, estos campos de arma: WeaponATK, WeaponLevel, refine, tipo RO, element, slots/cards y ranged profile.
- Falta definir de donde saldra la flecha equipada y su ATK/elemento.
- Falta definir representacion real de Safety Wall/Pneuma en mundo: zona, duracion, hits restantes y tipo de bloqueo.
- Falta decidir si `ElementType.DARK` se renombra a `SHADOW` o si se mantiene alias interno.

Mientras esos campos no existan, se puede implementar el nucleo con defaults, pero no se puede afirmar fidelidad completa al documento.
