# Skill External Dependencies

Estado al 2026-06-22. Este documento separa pendientes de skills que no dependen de la formula de la skill en si, sino de otros sistemas aun no cerrados.

## Objetivo

- No marcar una skill como incompleta si su formula base ya existe.
- Registrar que parte depende de inventario, items, mobs, PvP, UI, efectos, hooks de combate o datos externos.
- Mantener este archivo como lista corta de bloqueos reales para revisar clase por clase.

## Swordman

### Increase HP Recovery

Formula interna implementada:

- Regeneracion pasiva por MaxHP y nivel de skill.
- Multiplicador de curacion por items calculable por VIT y nivel de skill.

Pendiente externo:

- Falta conectar el bonus de curacion por items a un hook real de uso/curacion de items.
- Depende del futuro sistema de items consumibles y/o inventario nuevo.

### Sword Mastery / Two-Handed Sword Mastery

Formula interna implementada:

- Bono plano `4 * SkillLv`.
- Aplicacion tardia despues de DEF/soft DEF y antes de elemento/modificadores.

Pendiente externo:

- Depende de que el sistema final de armas clasifique correctamente dagger, one-handed sword y two-handed sword.
- Si los items finales no usan tags o clases claras de arma, habra que ajustar el resolver de tipo de arma.

### Bash

Formula interna implementada:

- Skill fisica melee.
- Modificador de dano por nivel.
- Bono de HIT por nivel.
- Sin stun, porque Fatal Blow esta excluido.

Pendiente externo:

- Falta validar contra el sistema final de armas si habra restricciones adicionales por tipo de arma.
- No depende de efectos visuales, sonidos ni menus para funcionar.

### Provoke

Formula interna implementada:

- Chance de exito por nivel.
- Duracion.
- Aumento de ATK del objetivo.
- Reduccion de DEF fisica del objetivo.
- Restriccion contra Undead y boss-like mobs.

Pendiente externo:

- El comportamiento PvP aun no esta cerrado. En Ragnarok, contra players debe afectar soft/VIT DEF y no la hard DEF de equipo.
- Depende del sistema final de PvP/duelos/facciones si se quiere permitir uso contra jugadores.
- Depende de perfiles de mobs completos para clasificar con precision propiedad Undead, boss-like y futuros tipos especiales.

### Magnum Break

Formula interna implementada:

- Dano fisico con elemento Fire forzado.
- Bono de HIT por nivel.
- Costo de HP por nivel.
- Componente de dano que ignora DEF.
- Buff posterior de dano Fire por tiempo limitado.

Pendiente externo:

- El knockback exacto depende de reglas futuras de mapas, WoE/GvG o zonas donde el empuje debe desactivarse.
- Los efectos visuales y sonidos siguen siendo afinacion externa; no son parte de la formula.
- Depende de datos finales de elemento/resistencias para validar el dano Fire contra todos los tipos de objetivo.

### Endure

Formula interna implementada:

- Duracion por nivel.
- MDEF bonus por nivel.
- Cooldown.
- Cancelacion por cantidad maxima de golpes de monstruo.
- Sin reduccion de dano.

Pendiente externo:

- Falta decidir si se modelara anti-flinch/anti-stagger con un hook propio de knockback o interrupcion.
- Depende de una regla final para distinguir golpes de monstruo, jugadores, skills, trampas u otras fuentes.
- Depende del futuro sistema PvP si Endure debe comportarse distinto contra jugadores.

## Pendientes transversales

- Consumibles: necesarios para aplicar bonuses de curacion de skills pasivas.
- Inventario/equipment nuevo: necesario para leer armas, armaduras y restricciones finales sin rutas temporales.
- PvP: necesario para cerrar diferencias entre mobs y jugadores.
- Perfiles de mobs: necesarios para raza, tamano, elemento, boss-like y propiedades especiales.
- Hooks de interrupcion/knockback: necesarios para Endure y reglas exactas de Magnum Break.
- Estados RO propios: necesarios para reemplazar efectos vanilla como fuente de verdad. Politica inicial en `docs/VANILLA_EFFECTS_POLICY.md`.
- UI/menus: no bloquean formulas; solo bloquearan activacion visual o configuracion cuando se reemplacen menus.
- Efectos/sonidos/assets: no bloquean formulas y deben tratarse como afinacion final.

## Acolyte

Estado despues de la primera implementacion de formulas:

- Holy Light fue removida del arbol normal y del JSON activo por ser quest skill.
- Divine Protection, Demon Bane, Heal, Increase AGI, Decrease AGI, Angelus, Blessing y Signum Crucis ya tienen logica/formulas internas conectadas.
- Las dependencias siguientes son cosas que no pertenecen solo a la skill, sino a sistemas externos todavia no cerrados.

### Pneuma

Formula/logica incluida:

- Datos correctos: SP, duracion, area 3x3, range 9.
- La skill ya no simula resistencia vanilla.

Pendiente externo:

- Depende de un sistema de ground effects/celdas activas.
- Depende de que el pipeline de combate distinga `PHYSICAL_RANGED` de melee, magia y splash indirecto.
- Depende de reglas de solapamiento con Safety Wall, otro Pneuma y futuros efectos de mapa.

### Teleport

Formula/logica incluida:

- Lv1 mantiene teleport aleatorio temporal.
- Lv2 ya no se simula como teleport aleatorio.
- Datos correctos para Lv1 random y Lv2 save point.

Pendiente externo:

- Lv2 depende de un sistema real de Save Point.
- Lv1 requiere validador de celdas seguras y reglas de mapas no teleportables.
- Las restricciones de WoE, instancias o mapas especiales deben venir de flags externos.

### Warp Portal

Formula/logica incluida:

- Datos correctos: SP, Blue Gemstone, memo slots, max 3 portales y 8 usos.
- Valida que exista Blue Gemstone antes de intentar.
- Ya no usa Teleport como fallback.

Pendiente externo:

- Depende de items consumibles/catalysts.
- Depende de sistema de memo points.
- Depende de entidad/area persistente de portal.
- Depende de reglas de mapas que permitan o bloqueen memo/warp.

### Aqua Benedicta

Formula/logica incluida:

- Valida agua.
- Valida Empty Bottle usando `Items.GLASS_BOTTLE` como equivalente temporal.
- No consume ni crea item todavia para evitar perder materiales sin Holy Water registrado.

Pendiente externo:

- Depende de items finales: Empty Bottle y Holy Water.
- Depende del inventario/consumo de items final.
- Depende de una regla confiable para detectar agua valida.

### Angelus

Formula/logica incluida:

- Multiplica soft/VIT DEF con status propio.
- No aumenta VIT real ni usa resistencia vanilla como logica.

Pendiente externo:

- Aplicacion completa depende de party/friendly targeting.
- Si se quiere afectar aliados cercanos, falta definir reglas de alianza, party y rango.

### Blessing

Formula/logica incluida:

- Aliados/self: STR/DEX/INT + SkillLv mediante status propio.
- Ofensivo contra mobs Undead/Demon no boss: aplica estado ofensivo que reduce el HIT derivado del mob como primer efecto real.

Pendiente externo:

- Uso ofensivo completo depende de que el pipeline de mobs use STR/DEX/INT reales para HIT/MATK y otros derivados.
- Targeteo aliado/enemigo final depende del sistema de party/friendly targeting.
- Remocion de Curse depende de implementar Curse como estado RO.

### Cure

Formula/logica incluida:

- Remueve Blind y Confusion vanilla como equivalentes actuales.
- Ya no limpia Poison/Wither/Weakness/Slowness, porque eso no corresponde a Cure Pre-Renewal.

Pendiente externo:

- Depende de estados RO propios para Silence y Chaos si no se usan equivalentes vanilla.
- La limitacion de no autocastear durante Silence depende del sistema final de bloqueo de casteo por estados.

### Ruwach

Formula/logica incluida:

- Remueve Invisibility vanilla como equivalente temporal de oculto.
- Aplica dano magico Holy 145% MATK por la ruta de skill combat cuando revela.

Pendiente externo:

- Depende de estados reales de Hiding/Cloaking.
- Depende de reglas de enemigo/aliado para no danar objetivos incorrectos.

### Divine Protection / Demon Bane / Signum Crucis

Formula/logica incluida:

- Divine Protection ya aplica reduccion plana tardia contra ataques de Undead/Demon no-player.
- Demon Bane ya aplica bono plano tardio contra targets Undead/Demon no-player.
- Signum ya aplica reduccion de Hard DEF contra Undead/Demon por status propio y stackea por multiplicador separado.

Pendiente externo:

- Dependen de perfiles de mobs completos.
- Las reglas contra jugadores dependen del sistema PvP/equipment final.

## Archer

Estado despues de la primera implementacion de formulas:

- Owl's Eye, Vulture's Eye, Improve Concentration, Double Strafe y Arrow Shower tienen formulas/datos base corregidos.
- Las quest skills Arrow Crafting y Charge Arrow quedan excluidas.
- Las dependencias siguientes son sistemas externos a la skill o todavia no cerrados.

### Flechas / ammunition

Formula/logica incluida:

- Double Strafe y Arrow Shower requieren Bow y consumen 1 Arrow vanilla como puente.
- El calculo ranged suma Arrow ATK neutral basico `25` mientras no exista municion final.

Pendiente externo:

- Crear sistema final de flechas RO con ATK, elemento, peso y casos especiales como Sharp Arrow.
- Hacer que el elemento ofensivo del bow venga de la flecha equipada.
- Soportar flechas de otros mods mediante conversion a propiedad RagnarMMO.

### Vulture's Eye range

Formula/logica incluida:

- `RangeBonus = SkillLv` queda registrado en data.
- `HITBonus = SkillLv` ya afecta derived stats.

Pendiente externo:

- El sistema de targeting/range debe consumir el bonus para ataques bow, Double Strafe y Arrow Shower.

### Pneuma / ranged physical

Formula/logica incluida:

- Double Strafe y Arrow Shower ya estan tratadas como skills fisicas de bow.

Pendiente externo:

- Falta flag central `PHYSICAL_RANGED`.
- Falta que Pneuma bloquee esas rutas cuando exista ground-effect/cell system.

### Arrow Shower ground target

Formula/logica incluida:

- Dano 80..125%, 3x3 y knockback estan en data.
- La implementacion actual usa entidad objetivo como centro temporal.

Pendiente externo:

- Target ground/cell real.
- Interaccion con traps.
- Knockback exacto por celdas y reglas de mapas/WoE.

### Improve Concentration eligible stats

Formula/logica incluida:

- DEX/AGI suben por `2 + SkillLv`%.

Pendiente externo:

- Cuando equipo/cartas esten cerrados, separar stats elegibles: base, job, armor y Owl's Eye; excluir cartas segun Pre-Renewal.

## Thief

Estado despues de la primera implementacion de formulas:

- Double Attack, Improve Dodge, Envenom, Poison y Detoxify tienen formulas/logica base corregidas.
- Steal y Hiding tienen datos corregidos, pero dependen de sistemas externos para quedar completos.

### Steal

Formula/logica incluida:

- Tabla Pre-Renewal de chance base.
- SP 10.
- Range 1.

Pendiente externo:

- Integracion con drop tables reales de mobs.
- Seleccion de item robable desde la tabla del target.
- Marcar mob como robado una sola vez.
- Restricciones contra frozen/stone cursed cuando esos estados existan.
- Entregar item sin reducir drops al morir.

### Hiding

Formula/logica incluida:

- Duracion `30 * SkillLv`.
- Drain interval `4 + SkillLv`.
- Costo inicial 10 SP.

Pendiente externo:

- Estado RO propio de Hiding.
- Toggle real de activar/desactivar.
- Drenaje periodico de SP.
- Bloqueo de movimiento, ataques, skills normales, items y regen.
- Deteccion por mobs Insect/Demon/Boss protocol.
- Interaccion final con Sight, Ruwach, Detecting e Improve Concentration.

### Double Attack feedback

Formula/logica incluida:

- Chance, HIT bonus y doble hit logico estan conectados.

Pendiente externo:

- El packet/feedback de combate todavia agrega el resultado en una resolucion de dano.
- Si se necesita mostrar dos numeros o interactuar por golpe con barreras/reflejos, falta resolucion multi-hit real.

### Poison RO vs vanilla

Formula/logica incluida:

- Envenom aplica Poison RO propio.
- Poison RO baja DEF fisica 25%, drena 3% MaxHP cada 3s y no baja de 25% MaxHP.
- Detoxify limpia Poison RO y efectos vanilla `POISON`/`WITHER` como puente.

Pendiente externo:

- Definir la conversion final de efectos vanilla/otros mods a estados RO.
- Definir damage source propio y reglas PvP/party para Poison.
