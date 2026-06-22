# Combat RO Target

Objetivo: que el combate sea lo mas parecido posible a Ragnarok Online pre-renewal, adaptado a Minecraft solo donde el motor lo obligue.

## Fuentes de referencia

- iRO Wiki Stats: HIT/FLEE/CRIT/Perfect Dodge y comportamiento de criticos.
- iRO Wiki Classic Stats: base stats y contexto pre-renewal.
- iRO Wiki Size / RateMyServer size table: penalidades por tipo de arma contra Small/Medium/Large.
- iRO Wiki ATK: separacion conceptual de StatusATK, WeaponATK y modificadores.

## Regla fijada en este corte

### Criticos

- El critico puede conectar aunque el HIT normal fallaria.
- Perfect Dodge se evalua antes del dano y puede evitar el ataque.
- Crit Shield reduce la chance critica efectiva.
- El critico no ignora DEF.
- Multiplicador base de critico: `1.4`.

Codigo relacionado:

- `src/main/java/com/etema/ragnarmmo/combat/engine/RagnarHitCalculator.java`
- `src/main/java/com/etema/ragnarmmo/combat/engine/RagnarCombatEngine.java`
- `src/main/java/com/etema/ragnarmmo/combat/contract/CombatContract.java`

### Nivel del player

- El combate ya no usa `player.experienceLevel`.
- Para players se usa `IPlayerStats.getLevel()`.

Codigo corregido:

- `RagnarCombatEngine`
- `HandAttackProfileResolver`
- `TargetCombatProfileResolver`
- `CombatMath.tryGetTargetLevel`

### Elemento y tamano

- `RagnarDamageCalculator.applyModifiers` aplica elemento siempre.
- Para dano fisico, tambien aplica penalidad de tamano del arma.
- Para dano magico, no aplica penalidad de tamano del arma.

Codigo relacionado:

- `src/main/java/com/etema/ragnarmmo/combat/engine/RagnarDamageCalculator.java`
- `src/main/java/com/etema/ragnarmmo/player/stats/compute/CombatMath.java`
- `src/main/java/com/etema/ragnarmmo/combat/formula/DamageFormulaService.java`

## Queda por revisar en el siguiente corte

1. Separar `StatusATK` y `WeaponATK` de forma mas fiel:
   - Size penalty debe afectar principalmente WeaponATK.
   - StatusATK no debe sufrir la misma penalidad.

2. Convertir weapon type a una clasificacion pura:
   - dagger
   - one handed sword
   - two handed sword
   - spear
   - axe
   - mace
   - staff
   - bow
   - katar
   - unarmed

3. Completar DEF/MDEF:
   - soft DEF por VIT
   - hard DEF por equipo
   - soft MDEF por INT/VIT
   - hard MDEF por equipo
   - decidir minimo de dano cuando elemento da 0 o negativo.

4. Unificar ruta oficial:
   - `RagnarCombatEngine.resolve` y `CombatContract.resolveBasicAttack` todavia son dos rutas.
   - La meta es que `RagnarCombatEngine` sea wrapper de `CombatContract`.

5. Skills:
   - Skills fisicas deben reutilizar la misma ruta de dano fisico.
   - Skills magicas deben reutilizar la misma ruta de dano magico.
   - `SkillCombatSpecResolver` aun debe parsear tipo de dano, elemento y hit policy desde data real.

## Pruebas agregadas

- `src/test/java/com/etema/ragnarmmo/combat/engine/RagnarCombatEngineFormulaTest.java`

Cubre:

- critico conecta aunque HIT normal fallaria
- crit shield reduce critico efectivo
- elemento se aplica en calculador comun
- dano magico usa elemento sin penalidad de tamano de arma
