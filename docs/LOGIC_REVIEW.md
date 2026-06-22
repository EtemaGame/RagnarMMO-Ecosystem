# RagnarMMO Logic Review

Estado al 2026-06-22. Objetivo: revisar logicas internas antes de UI final, assets, mobs propios, economia nueva o menus avanzados.

## Resumen ejecutivo

El mod activo ya esta en una sola base (`src/`) y compila. La base funcional real esta en stats, derived stats, progresion, perfiles de mobs, combate basico, skills por job, items/rules/cards, lifeskills y social/party.

La prioridad tecnica no es agregar mas contenido: es unificar rutas de formulas. Hoy existen caminos paralelos para combate y skills que pueden producir resultados distintos aunque usen datos parecidos.

## Hallazgos principales

1. Combate tiene dos rutas de calculo para ataques.
   - Ataque basico directo: `src/main/java/com/etema/ragnarmmo/combat/engine/RagnarCombatEngine.java:124`
   - Contrato nuevo: `src/main/java/com/etema/ragnarmmo/combat/contract/CombatContract.java:19`
   - Riesgo: crit, defensa, modificadores y elementos no se aplican igual.

2. El contrato de combate ignora modificadores importantes.
   - `src/main/java/com/etema/ragnarmmo/combat/engine/RagnarDamageCalculator.java:15`
   - `applyModifiers` devuelve el dano casi intacto; no aplica elemento, raza, tamano, reducciones, tipo magico/fisico ni equipo.

3. Criticos no son consistentes.
   - `RagnarCombatEngine` aplica defensa fisica tambien en criticos: `src/main/java/com/etema/ragnarmmo/combat/engine/RagnarCombatEngine.java:160`
   - `CombatContract` salta defensa si es critico: `src/main/java/com/etema/ragnarmmo/combat/contract/CombatContract.java:71`
   - Hay que decidir una regla unica.

4. Varias formulas usan el nivel vanilla de Minecraft.
   - `src/main/java/com/etema/ragnarmmo/combat/engine/RagnarCombatEngine.java:224`
   - `src/main/java/com/etema/ragnarmmo/combat/profile/HandAttackProfileResolver.java:31`
   - `src/main/java/com/etema/ragnarmmo/combat/resolver/TargetCombatProfileResolver.java:15`
   - `src/main/java/com/etema/ragnarmmo/player/stats/compute/CombatMath.java:313`
   - Debe usarse el base level de RagnarMMO cuando el objetivo/actor sea player.

5. Skills data-driven cargan datos, pero parte del contrato no los interpreta.
   - `src/main/java/com/etema/ragnarmmo/combat/contract/SkillCombatSpecResolver.java:29`
   - `parseDamageType`, `parseElement` y `parseHitPolicy` reciben `Double` y devuelven fallback: `SkillCombatSpecResolver.java:75`
   - Resultado: si el JSON declarara esos campos, no tendrian efecto.

6. Reset temporal por comandos aun no existe.
   - Hay paquetes de allocate/deallocate stats: `src/main/java/com/etema/ragnarmmo/player/stats/network/StatsNetwork.java:31`
   - No hay comando servidor para resetear stats/skills completo.
   - Esto bloquea el flujo que queremos usar mientras no existan pociones/menus.

7. Bestiary no muestra stats reales todavia.
   - `src/main/java/com/etema/ragnarmmo/bestiary/data/BestiaryDetailsResolver.java:157`
   - `resolveStats` devuelve `null`; drops/spawn funcionan mejor que la previsualizacion de stats.

8. Armadura fisica de items no entra al derived stat actual.
   - `src/main/java/com/etema/ragnarmmo/items/runtime/ItemDerivedStatsContributor.java:99`
   - `computeArmorHardDefense` devuelve `0.0D`.
   - MDEF si toma atributo, DEF fisica no.

## 1. Bootstrap general

Funciona.

- Entrada unica: `src/main/java/com/etema/ragnarmmo/RagnarMMO.java`
- Inicializa core, items, jobs, combat, lifeskills, mobs y social.
- Network comun registra paquetes por modulos.
- Economy ya no existe en codigo activo.

Pendiente:

- `RagnarMMOMobs.init()` existe, pero no registra mobs propios. Correcto por ahora, porque los modelos 3D estan en proceso.

## 2. Stats del player

Funciona como base.

- Capability: `player/stats/capability/PlayerStats.java`
- Stats primarios: STR, AGI, VIT, INT, DEX, LUK.
- Al cambiar un stat, se recalculan atributos y derived stats.
- Persistencia NBT existe.
- Sync cliente existe.
- Allocate/deallocate por packet existe.

Como funciona:

- `PlayerStats.set(...)` clamp a rango valido y llama `StatResolutionService.resolve(...)`.
- `StatResolutionService` aplica bonus de job, calcula derived stats y sincroniza.
- `CoreDerivedStatsCalculator` calcula ataque, MATK, hit, flee, crit, HP, SP, regen, ASPD y cooldown base.

Pendiente/riesgo:

- Falta comando servidor para reset completo.
- Revisar compatibilidad NBT vieja: algunos defaults dependen de que existan secciones NBT.
- Hay formulas duplicadas entre `CoreDerivedStatsCalculator`, `CombatMath` y servicios en `combat/formula`.

## 3. Progresion base/job

Funciona.

- Servicio: `src/main/java/com/etema/ragnarmmo/player/progression/PlayerProgressionService.java`
- Reglas: `ProgressionRules`
- Level up base da stat points.
- Level up job da skill points.
- Penalizacion por muerte existe en `PlayerProgressionEvents`.

Como funciona:

- El XP de mobs llega principalmente por `PartyMobXpEventHandler`.
- Se toma perfil del mob, se aplica penalizacion por diferencia de nivel y luego rate de job/base.
- Si hay party, `PartyXpService` reparte XP a miembros elegibles.

Pendiente/riesgo:

- Confirmar que no haya rutas futuras que dupliquen XP fuera de `PartyMobXpEventHandler`.
- Las curvas son configurables/adaptadas, no tablas RO exactas.

## 4. Jobs y skills

Parcialmente funcional.

- Skill definitions se cargan desde `data/ragnarmmo/skills`.
- Skill trees se cargan desde `data/ragnarmmo/skill_trees`.
- Capability de skills guarda niveles, hotbar y cooldowns.
- Upgrade de skill consume skill points.
- Job change a first class existe y valida Basic Skill Lv 9 + job level + puntos gastados.

Como funciona:

- `ServerboundUseJobSkillPacket` llama `JobSkillExecutor.use(...)`.
- `JobSkillExecutor` valida job, nivel aprendido, cooldown y SP.
- `JobSkillEffectRegistry` ejecuta efectos portados.

Estado real:

- Varias skills tienen efecto jugable simple.
- Varias skills son aproximaciones internas, no fidelidad final.
- `buying_store` y `vending` son placeholder intencional: `JobSkillEffectRegistry.java:444`.
- Si una skill no tiene efecto registrado, el executor muestra mensaje y la cuenta como ejecutada. Eso puede consumir SP/cooldown aunque no haga nada.

Pendiente/riesgo:

- Separar skills internas funcionales de skills solo declaradas.
- Unificar si las skills de dano deben pasar por `CombatContract` o por `JobSkillEffectRegistry`, no ambas a medias.
- Agregar comando reset skill points/skills.

## 5. Combate

Funciona, pero necesita unificacion.

Piezas activas:

- Input/packets de ataque basico.
- `RagnarCombatEngine` resuelve ataques basicos.
- `CombatContract` intenta ser ruta formal para ataques/skills.
- Perfiles de player/mob existen.
- Mobs atacando player pasan por `MobPreRenewalDamageEventHandler`.
- Kill credit existe.

Como funciona ahora:

- Ataque basico usa stats del player, HIT/FLEE, crit, ATK, variacion, elemento y defensa.
- Skills por contrato resuelven specs desde data y producen `CombatResolution`.
- Mob vs player usa perfil de mob y derived stats del player.

Pendiente/riesgo:

- Unificar formulas en una sola fuente.
- Reemplazar usos de `player.experienceLevel`.
- Implementar `RagnarDamageCalculator.applyModifiers`.
- Decidir reglas de crit vs defensa.
- Definir si multiplicadores elementales negativos curan, absorben, hacen 0 o minimo 1.

## 6. Mob profiles

Funciona como datos/perfiles, no como mobs custom.

- Loader de `mob_definitions` y `mob_templates` existe.
- Capability se adjunta a living entities no-player.
- Se inicializa en entity join.
- Combat y XP consumen estos perfiles.

Estado real:

- Los mobs propios estan diferidos.
- El sistema actual sirve para dar perfiles RO/adaptados a entidades existentes.

Pendiente:

- Conectar bestiary stats reales.
- Revisar formulas de fallback para mobs sin perfil.

## 7. Items, equipment rules y cards

Parcialmente funcional.

- Items utilitarios y armas registran.
- `ro_item_rules` aplica requisitos, slots, bonuses y combat profile.
- Cards cargan desde data y pueden aportar modifiers de stats.
- Tooltips existen.
- Restricciones de equip/uso existen.

Como funciona:

- `RoItemRuleResolver` resuelve regla base + cards slotted.
- `RoAttributeApplier` aplica bonuses STR/AGI/VIT/INT/DEX/LUK como modifiers transitorios.
- `ItemDerivedStatsContributor` agrega ataque, MATK, MDEF y ASPD desde equipo.

Pendiente/riesgo:

- Equipment extra sera redisenado, asi que no conviene expandirlo ahora.
- Items visuales existentes pueden eliminarse/reemplazarse mas adelante.
- DEF fisica de armadura esta vacia.
- Cards no-stat quedan anotadas como consumo futuro por otros sistemas.

## 8. Lifeskills

Funciona como sistema independiente.

- Capability y persistencia existen.
- Block break da puntos segun data.
- Tracker evita farm de bloques puestos por jugador.
- Mining/fishing/farming/etc tienen progresion por puntos.
- Perks existen como definiciones internas.

Pendiente/riesgo:

- No es prioridad de formulas de combate.
- Conviene mantener, pero no ampliar hasta cerrar stats/combate.

## 9. Social, party, achievements, bestiary

Funciona parcialmente.

- Party tiene comandos, saved data, sync y XP share.
- Achievements tienen definiciones/capability/network/UI.
- Bestiary lista entidades, drops naturales y overrides.

Pendiente/riesgo:

- Bestiary stats preview no implementado.
- Party XP debe quedar como ruta oficial para evitar duplicar premios.

## 10. Conceptual o diferido intencionalmente

- Economy: eliminada; se reescribira.
- Buying Store/Vending: placeholders hasta menus personalizados.
- Inventory/equipment menu final: diferido hasta interceptar/reemplazar InventoryMenu.
- Mobs propios: diferido hasta modelos 3D.
- Sonidos, efectos visuales, assets finales: afinacion posterior.
- Client config final: reescritura al final.

## Orden recomendado de limpieza logica

1. Crear comandos temporales de admin/dev:
   - reset stats
   - reset skills
   - set/add base exp
   - set/add job exp
   - set job

2. Unificar nivel del player:
   - reemplazar `player.experienceLevel` por `stats.getLevel()` en combate.

3. Elegir una ruta oficial de combate:
   - o `CombatContract` absorbe todo,
   - o `RagnarCombatEngine` se convierte en wrapper del contrato.

4. Mover formulas a una fuente unica:
   - HIT/FLEE/crit
   - ATK/MATK
   - ASPD/cooldown
   - DEF/MDEF
   - elemento/tamano/raza/modificadores

5. Clasificar skills:
   - funcional interna
   - placeholder intencional
   - solo data
   - eliminar/dejar oculta por ahora

6. Completar gaps minimos de items que afectan formulas:
   - DEF fisica
   - modifiers no-stat de cards
   - resolver si equipment extra queda apagado hasta menu nuevo

## Pruebas ejecutadas

```powershell
.\gradlew.bat test --no-daemon --stacktrace
```

Resultado: pasan las pruebas actuales.

Pruebas actuales cubren contratos de stats, formulas base, progresion, cards, item combat profile, packets de stats y API de skills. Falta cobertura de integracion para ruta completa player -> combat -> mob profile -> XP.
