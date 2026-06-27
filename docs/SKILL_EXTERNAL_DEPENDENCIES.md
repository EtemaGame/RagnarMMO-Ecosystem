# Skill External Dependencies

Estado al 2026-06-24. Este documento registra el estado de implementacion de skills por clase y separa pendientes que no dependen de la formula de la skill, sino de otros sistemas aun no cerrados.

Referencia: `docs/GROUND_CELLS.md` define la regla base para ground effects: `1 RO cell = 1 bloque Minecraft`, con volumen vertical de 2 bloques sobre la base.

## Resumen de implementacion por clase

### Swordman

Skills implementadas: Sword Mastery, Two-Handed Sword Mastery, Increase HP Recovery, Bash, Provoke, Magnum Break, Endure. Quest skills excluidas: HP Recovery While Moving, Fatal Blow, Auto Berserk.

- Masteries aplican bono plano `4 * SkillLv` tardio despues de DEF.
- Bash sin stun (Fatal Blow excluido).
- Magnum Break fuerza Fire, tiene componente de dano que ignora DEF y buff posterior de 20% Fire.
- Endure da MDEF +SkillLv y cancelacion por 7 golpes de monstruo.

### Acolyte

Skills implementadas: Divine Protection, Demon Bane, Heal, Increase AGI, Decrease AGI, Angelus, Blessing, Signum Crucis, Cure, Ruwach, Pneuma. Quest skill excluida: Holy Light. Datos corregidos: Teleport, Warp Portal, Aqua Benedicta.

- Holy Light removida del arbol normal.
- Divine Protection: reduccion plana tardia contra Undead/Demon no-player.
- Demon Bane: bono plano tardio contra Undead/Demon no-player.
- Heal: `max(1, floor((BaseLv + INT) / 8)) * (4 + 8 * SkillLv)`, dano ofensivo Holy contra Undead.
- Increase AGI, Decrease AGI, Angelus, Blessing y Signum usan estados RO propios.
- Blessing remueve Curse en uso de soporte.
- Cure limpia Blind/Confusion equivalentes actuales y Silence RO propio.
- Blind y Chaos ya tienen estado RO propio; Confusion vanilla se convierte a Chaos y se remueve. Chaos aplica targeting/movimiento caotico basico.

### Archer

Skills implementadas: Owl's Eye, Vulture's Eye, Improve Concentration, Double Strafe, Arrow Shower. Quest skills excluidas: Arrow Crafting, Charge Arrow.

- Owl's Eye modifica DEX antes de derived stats.
- Vulture's Eye: HIT +SkillLv, rango como dato explicito, afecta autoattack ranged, Double Strafe y Arrow Shower.
- Improve Concentration: estado propio, DEX/AGI por porcentaje, revela Hiding RO propio.
- Double Strafe: 100..190% por hit x2, exige Bow + Arrow.
- Arrow Shower: 80..125%, 3x3, ground target, knockback 2 celdas.

### Thief

Skills implementadas: Double Attack, Improve Dodge, Envenom, Detoxify. Datos corregidos: Steal, Hiding.

- Double Attack ya no da critico global; chance antes del HIT, 2 hits logicos con dagger.
- Envenom: fisico Poison 100% + `15 * SkillLv` plano post-DEF, aplica Poison RO propio.
- Poison RO: DEF -25%, 3% MaxHP cada 3s, piso 25% MaxHP.
- Detoxify limpia Poison RO y remueve vanilla `POISON`/`WITHER` si entraron desde compatibilidad externa.

### Mage

Skills implementadas: Increase SP Recovery, Sight, Napalm Beat, Soul Strike, Safety Wall, Cold/Fire/Lightning Bolt, Frost Diver, Stone Curse, Fire Ball, Fire Wall, Thunder Storm. Quest skill excluida: Energy Coat.

- Dano magico usa roll MinMATK/MaxMATK, sin HIT/FLEE/CRIT.
- Increase SP Recovery: regen formula, no aumenta MaxMana.
- Soul Strike: `ceil(SkillLv/2)` hits, bonus contra Undead.
- Frozen cambia defensa a Water 1.
- Stone Curse cambia defensa a Earth 1, inmoviliza con estado propio y se rompe con dano.
- Fire Wall: linea de 3 celdas con hit budget y knockback.
- Thunder Storm: ground target 5x5, impactos separados por `hit_spacing_ticks`.
- Se quitaron quemaduras vanilla de Fire Bolt, Fire Ball y Fire Wall.

### Merchant

Skills implementadas: Enlarge Weight Limit, Discount, Overcharge, Pushcart, Item Appraisal, Vending, Mammonite. Quest skills excluidas: Cart Revolution, Change Cart, Crazy Uproar, Cart Decoration, Buying Store.

- Enlarge Weight Limit: `+200 * SkillLv`, ya no aumenta MaxSP.
- Discount/Overcharge: tabla Pre-Renewal `7,9,11,13,15,17,19,21,23,24%`.
- Pushcart: velocidad `50 + 5 * SkillLv`, cart 8000 weight y 100 slots.
- Mammonite: melee fisico `100 + 50 * SkillLv`, SP 5, zeny `100 * SkillLv` en data.

---

## Dependencias externas por skill

### Swordman

#### Increase HP Recovery

Pendiente externo:

- Hook de uso/curacion de items consumibles para aplicar bonus.

#### Sword Mastery / Two-Handed Sword Mastery

Pendiente externo:

- Sistema final de armas que clasifique correctamente dagger, 1H sword y 2H sword.

#### Bash

Pendiente externo:

- Validar contra sistema final de armas si habra restricciones adicionales por tipo.

#### Provoke

Pendiente externo:

- PvP: contra players debe afectar soft/VIT DEF, no hard DEF de equipo.
- Sistema PvP/duelos/facciones para uso contra jugadores.
- Perfiles de mobs completos para clasificar Undead, boss-like y tipos especiales.

#### Magnum Break

Pendiente externo:

- Knockback exacto segun reglas de mapas/WoE/GvG.
- Datos finales de elemento/resistencias para validar Fire contra todos los tipos de objetivo.

#### Endure

Pendiente externo:

- Hook propio de anti-flinch/anti-stagger con knockback o interrupcion.
- Regla para distinguir golpes de monstruo, jugadores, skills, trampas u otras fuentes.
- Sistema PvP si Endure debe comportarse distinto contra jugadores.

### Acolyte

#### Pneuma

Pendiente externo:

- Reglas finales de solapamiento con Safety Wall, Land Protector y futuros efectos de mapa.
- Clasificacion fina de mobs ranged por rango natural si el dano no entra por proyectil vanilla ni por pipeline propio.

#### Teleport

Pendiente externo:

- Lv2: sistema real de Save Point.
- Lv1: validador de celdas seguras y reglas de mapas no teleportables.
- Restricciones de WoE, instancias o mapas especiales.

#### Warp Portal

Pendiente externo:

- Items consumibles/catalysts (Blue Gemstone).
- Sistema de memo points.
- Entidad/area persistente de portal.
- Reglas de mapas que permitan o bloqueen memo/warp.

#### Aqua Benedicta

Pendiente externo:

- Items finales: Empty Bottle y Holy Water.
- Inventario/consumo de items final.
- Regla confiable para detectar agua valida.

#### Angelus

Pendiente externo:

- Aplicacion completa a party/friendly targeting.

#### Blessing

Pendiente externo:

- Pipeline de mobs con STR/DEX/INT reales para uso ofensivo.
- Targeteo aliado/enemigo final por sistema de party/friendly targeting.
- Aplicacion completa de Curse contra mobs/players desde fuentes futuras.

#### Cure

Formula/logica incluida:

- Remueve Blind y Confusion vanilla como equivalentes actuales.
- Remueve Blind RO y Chaos RO.
- Remueve Silence RO propio.
- Si el caster esta silenciado, no puede autocastear Cure.

Pendiente externo:

- Aplicadores completos de Silence fuera de efectos futuros.

#### Ruwach

Pendiente externo:

- Hiding RO propio ya existe; Cloaking futuro ya tiene estado runtime base, falta skill/aplicador real.
- Reglas de enemigo/aliado.

#### Divine Protection / Demon Bane / Signum Crucis

Pendiente externo:

- Perfiles de mobs completos.
- Reglas contra jugadores por sistema PvP/equipment final.

### Archer

#### Flechas / ammunition

Pendiente externo:

- Sistema final de flechas RO con ATK, elemento, peso y casos especiales.
- Elemento ofensivo del bow desde flecha equipada.
- Conversion de flechas de otros mods a propiedad RagnarMMO.

#### Vulture's Eye range

Pendiente externo:

- Validar si bonus debe afectar proyectiles vanilla fuera del pipeline propio.

#### Arrow Shower

Pendiente externo:

- Interaccion con traps.
- Knockback exacto por celdas y reglas de mapas/WoE.

#### Improve Concentration eligible stats

Pendiente externo:

- Separar stats elegibles: base, job, armor y Owl's Eye; excluir cartas segun Pre-Renewal.

### Thief

#### Steal

Pendiente externo:

- Integracion con drop tables reales de mobs.
- Seleccion de item robable desde tabla del target.
- Marcado persistente de mob ya robado.
- Restricciones contra frozen/stone cursed.
- Entrega de item sin reducir drops al morir.

#### Hiding

Pendiente externo:

- Bloqueo de items y regen.
- Afinar deteccion por mobs Insect/Demon/Boss protocol cuando se cierren perfiles finales.
- Detecting futuro.

#### Double Attack multi-hit

Pendiente externo:

- Ya existen `hitCount`, popoffs separados y consumo de Safety Wall por golpe.
- Falta persistir/resolver cada golpe como evento independiente si despues se necesitan logs, reflect, on-hit cards o analytics por golpe.

#### Poison RO vs vanilla

Pendiente externo:

- Conversion final de efectos vanilla/otros mods a estados RO.
- `POISON`/`WITHER` vanilla ya se convierten a Poison RO y se limpian para evitar doble dano.
- Damage source propio y reglas PvP/party para Poison.

### Mage

#### Increase SP Recovery item bonus

Pendiente externo:

- Hook de consumibles/pociones SP para aplicar bonus.

#### Sight

Pendiente externo:

- Hiding y Cloaking runtime base ya se revelan; falta skill Cloaking futura.
- Aura persistente que revele al entrar al area.

#### Napalm Beat

Pendiente externo:

- Reparto de dano entre multiples objetivos.

#### Safety Wall

Pendiente externo:

- Reglas de solapamiento con Pneuma, Land Protector y futuros efectos de terreno.

#### Frost Diver / Stone Curse statuses

Pendiente externo:

- Ajustes por MDEF/level/LUK.
- Visual final sin depender de slowness/weakness vanilla.
- Consumo real de Red Gemstone para Stone Curse.

#### Fire Wall / Thunder Storm ground logic

Pendiente externo:

- Fire Wall: reglas exactas de max active, solapamiento y diagonales.
- Thunder Storm ya agenda impactos separados por `hit_spacing_ticks`.
- Faltan reglas finas de retarget por golpe, limites por objetivo e interacciones especiales por mapa.

#### Cast system

Pendiente externo:

- UI final de barra de cast.
- Silence RO ya bloquea casteo; Blind/Chaos/Hiding/Cloaking/Stone Curse/Stun/Sleep/Curse/Bleeding tienen estado propio. Faltan aplicadores completos y afinacion final.
- Modificadores de equipo/cartas anti-interrupcion o cast.

### Merchant

#### Enlarge Weight Limit

Pendiente externo:

- Sistema final de peso/inventario.

#### Discount / Overcharge

Pendiente externo:

- Economy/NPC shop rewrite.
- Hooks separados para compra/venta NPC normal.
- Excluir player shops, Vending, deals especiales y NPC custom.

#### Pushcart

Pendiente externo:

- Renta de cart/Kafra.
- Inventario separado del cart.
- Penalizacion real de movimiento solo con cart equipado.

#### Item Appraisal

Pendiente externo:

- Sistema de items no identificados.
- UI para seleccionar item del inventario.

#### Vending

Pendiente externo:

- Cart real, menu personalizado de venta, player shop service.

#### Mammonite

Pendiente externo:

- Economy/zeny wallet para validar y consumir zeny.

---

## Pendientes transversales

- Consumibles: necesarios para bonuses de curacion de skills pasivas.
- Inventario/equipment nuevo: leer armas, DEF/refine de armaduras RO finales y restricciones finales. La DEF fisica vanilla ya entra como compatibilidad por `Attributes.ARMOR`.
- PvP: cerrar diferencias entre mobs y jugadores.
- Perfiles de mobs: raza, tamano, elemento, boss-like.
- Hooks de interrupcion/knockback: Endure, Magnum Break.
- Estados RO propios: completar aplicadores, curas especificas y afinacion final. Politica en `docs/VANILLA_EFFECTS_POLICY.md`.
- UI/menus: no bloquean formulas; bloquean activacion visual.
- Efectos/sonidos/assets: afinacion final.
