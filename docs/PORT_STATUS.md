# RagnarMMO Port Status

Estado actual del port desde `original-source` hacia los 8 modulos.

## Resumen

- Build modular: `./gradlew.bat modularBuild --quiet` pasa.
- Java original restante: 503 archivos en `original-source`.
- Java modular: 360 archivos en los modulos nuevos.
- Recursos originales restantes: 332 archivos en `original-source`.
- Recursos modulares: 314 archivos portados o staged.
- Duplicados exactos ya eliminados de `original-source`: 232 Java y 356 recursos.
- Recursos restantes no duplicados exactos: assets visuales/sonoros/effects, metadata legacy, mixins y datos que aun requieren port o descarte explicito.

Los recursos gameplay de los modulos activos estan ubicados en sus dueños. `ragnarmmo-mobs` queda intencionalmente sin contenido staged hasta crear mobs/dungeons reales. La deuda grande pendiente es portar logica Java funcional.

## Portado

### Core

- Stats base, capability y persistencia NBT.
- Base Lv, Job Lv, exp base/job, stat points y skill points.
- `IPlayerStats`, `JobType`, `LifeSkillType`, `Stats6`, `RoBaseStats`, `StatKeys`.
- Atributos Forge compartidos.
- Config server-side base.
- Networking comun y packets base de stats/progression/resources.
- Sync server-side.
- Derived stats base y pipeline extensible por contributors.
- API minima de economia: account/service/transaction result.
- Arquitectura definida para perfiles RO universales de mobs en core: deteccion, nivel, raza, elemento, nivel elemental, tamano, boss/rank, stats derivados y API consumible por combat/social/mobs.
- API declarativa de mobs portada a `core`: `MobRank`, scaling mode, definitions/templates, bloques de stats, AI/movement/loot/metamorphosis/spawn, resolver de definiciones, registry, loader datapack y read-view extensible.
- `MobRank` queda como unica categoria funcional de mobs: `NORMAL`, `ELITE` y `BOSS`; `MobTier`, `MINI_BOSS` y `WEAK` quedan fuera del contrato modular.
- Runtime universal de perfiles de mobs portado a `core`: capability persistente, `MobProfile`, factory de stats/EXP, bootstrap en `EntityJoinLevelEvent`, clasificacion de entidades elegibles, aplicacion de HP/attack/armor/move speed y read-view para consumidores.
- Penalizacion por muerte de jugador restaurada en `core` para Base EXP y Job EXP.
- Level-up base/job ahora recalcula derivados inmediatamente para que HP/SP y stats resueltos no queden atrasados tras ganar nivel.
- Contrato de parser de mob definitions cubierto en tests de `core`.

### Combat

- Contratos y formulas puras iniciales.
- Element types, hit/reject reasons, profiles simples, defensa/damage/resistencias.
- Runtime basico de ataque normal server-authoritative: request packet, target resolver, cadence/cooldown, hit/crit/damage resolution y result packet.
- Integracion con perfil runtime de mobs: `combat` consume flee/def/level/aspd/crit/hit resueltos desde `core` cuando existen, con fallback vanilla.
- Daño entrante mob -> jugador usa formula fisica pre-renewal: HIT vs FLEE/perfect dodge, roll de ATK min/max del `MobProfile`, crit opcional, reduccion por hard DEF, resta de soft DEF y minimo 1.
- Daño saliente jugador -> mob ya aplica propiedades RO pre-renewal: penalizacion de arma por size, tabla elemental con nivel defensivo 1-4 y modifiers por raza/elemento/tamano desde NBT de equipo/cartas.
- Hook inicial de `AttackEntityEvent` para cancelar vanilla cuando el ataque RO se procesa correctamente.
- Ataque basico ignora la invulnerabilidad vanilla entre golpes y usa cadencia propia por ASPD.
- Request de ataque preparado para main hand/off-hand; off-hand valida arma secundaria y aplica penalidad de cadencia.

### Items

- Card contracts, card registry y loader.
- 68 JSON de cards.
- `ro_item_rules`, loader y resolver.
- Tipo de equipo RO, NBT helpers, refine base.
- Weapon stat helpers.
- Contribucion server-side de equipo/refine/cards a derived stats.
- Item fisico generico de card con ID legacy.
- Al slotear una card, sus modifiers se copian al equipo en `RoCompoundedCardModifiers` para que `combat` pueda consumir bonuses/resistencias sin depender del modulo `items`.
- Utility items legacy: `blue_gemstone`, `oridecon`, `elunium`.
- Recipes de `elunium` y `oridecon`.
- Loot modifier de cards con aliases `ragnarmmo:skill_loot` y `ragnarmmo:ragnar_loot`.
- Tags de items.
- Hooks portados para restricciones/equipamiento/combate/hotbar, revalidacion de stats y mensajes de requisito.
- Comandos de debug de items/refine portados.
- Tooltips cliente para items y cards portados en el nuevo layout modular.
- Snapshot de proyectiles y helper de skills de proyectil portados en versiones modulares.

### Economy

- Zeny fisico legacy y recipes de conversion como compatibilidad inicial.
- Wallet real, capability persistente y sync cliente/servidor.
- Money bag UI/acciones para depositar y retirar zeny.
- API abstracta vive en `core`; comercio y transacciones avanzadas aun pueden crecer encima de esta base.

### Mobs

- Modulo base minimo activo con `modId` `ragnarmmo_mobs`.
- No registra entidades, spawns, mob definitions, templates ni biome modifiers por ahora.
- Queda reservado para cuando se creen mobs, dungeons, bosses, IA, drops y world state reales.

### Life Skills

- Capability, persistencia NBT y sync server/client.
- Manager de progreso separado de Jobs.
- Packets de sync, update, puntos, level up y perk choice registrados en la red comun de core.
- Eventos originales de Life Skills portados: mining/woodcutting/farming/excavation/fishing/exploration y tracker de bloques colocados.
- Perk registry base.
- Datos/resources staged: skill data, `life_1`, life point sources y XP sources.

### Social

- Party runtime portado: SavedData persistente, service, invites, settings, member sync, client cache y comandos `/party`.
- EXP por kill restaurada sin antifarm: al morir un mob elegible, `social` toma el perfil runtime, aplica penalizacion por diferencia de nivel, distribuye Base/Job EXP via party share y sincroniza stats/HUD.
- Party loot inicial portado: modos `free`, `priority`, `roundrobin`, `off`, prioridad temporal y validacion server-side de pickup para drops de mobs.
- Achievements runtime portado: capability, registry JSON, triggers, claim/title packets y sync de definiciones/progreso.
- Bestiary runtime base portado: API DTOs, loader, registry, index/details packets, metadata JSON y loot natural.
- Social compila solo contra `core`; items/mobs/jobs/economy quedan como integraciones opcionales, no imports directos.

### Jobs

- Resources staged: skills, skill trees y skill families.
- Runtime inicial de job skills:
  - Registry de skill definitions y skill trees desde JSON.
  - Capability server-side de niveles de skills con persistencia NBT usando ID legacy `ragnarmmo:player_skills`.
  - Migracion de NBT legacy: lee niveles por skill top-level y hotbar antigua.
  - API puente en `core` para consultar niveles sin acoplar `client` a internals de `jobs`.
  - Packets de sync, upgrade, set hotbar y use skill registrados en la red comun.
  - Sync server/client de niveles y hotbar.
  - Upgrade server-authoritative: valida skill existente, job permitido, prerequisitos, max level y consume `SkillPoints` de core.
  - Runtime basico de ejecucion de skills activas con targeting vanilla y cooldown/delay.
  - Cooldown y cast delay se resuelven por nivel desde `level_data` cuando el JSON lo define.
  - Cambio de clase server-side para la fase actual mediante `JobChangeService` y UI/packets.
  - `PacketChangeJob` legacy queda cubierto por `JobChangeService`: Novice solo puede cambiar a primera clase, exige Novice Job Lv cap, `basic_skill` Lv 9 y cero puntos de skill sin gastar.
  - Novice cubierto funcionalmente como clase/base inicial propia.
  - Primeras clases cubiertas funcionalmente: Swordman, Archer, Acolyte, Thief, Merchant y Mage.
  - Todas las skills activas `NOVICE`/`FIRST` tienen handler modular.
  - Pasivas `NOVICE`/`FIRST` cubiertas mediante `JobPassiveStatsContributor`: masteries, recovery, dodge, owl/vulture eye, divine/demon, merchant economy passives y survival.
  - Los handlers activos consumen SP desde `core`, aplican delay/cooldown desde JSON y usan targeting/dano vanilla mientras las integraciones opcionales de combat se completan.
  - Merchant shop/economy actives (`buying_store`, `vending`) quedan como placeholder funcional con mensaje hasta completar wallet/shop UI en `economy`.
  - Los comandos de smoke test `/job` y `/jobskills` fueron removidos; el flujo de jugador queda en UI/hotbar.

### Client

- Datos/resources staged: assets, lang, sounds, effects, models, textures.
- Client envia packets de ataque basico mientras el click izquierdo se mantiene sobre una entidad; si hay arma secundaria valida alterna main/off-hand.
- Skills UI modular inicial: pantalla estilo RO con grillas por `skill_trees`, seccion Novice, seccion de primera clase, seccion segunda clase, soporte futuro para clases nuevas, iconos, tooltips y lectura de niveles sincronizados por `jobs`.
- Change Class UI inicial: boton activo desde Stats y Skills, pantalla de seleccion de primera clase, requisitos visibles y confirmacion via `RagnarJobsAPI` cuando `ragnarmmo-jobs` esta presente.
- Bestiary UI portado desde legacy: pantalla, busqueda, tabs, detalles, preview de entidad, boton de menu y keybind `key.ragnarmmo.open_bestiary`.
- Achievements UI modular: pantalla funcional, tabs por categoria, busqueda, progreso, claim rewards, equip/unequip title y entrada desde menu/keybind.
- Party HUD modular: frame compacto client-side usando `PartyClientData` sincronizado por social.
- Skill hotbar client: overlay visual de slots 1-6, cast de hotbar en combat mode y asignacion desde SkillsScreen con teclas 1-6.
- Tooltips client de items RO: bonuses de stats, requisitos, slots, refine y cards mediante integracion opcional con `ragnarmmo-items`.
- Card tooltip component portado para render visual de cards cuando un item expone `CardTooltipData`.

## Pendiente

- Jobs: dejar segundas clases y superiores para una fase posterior/rediseño.
- Jobs: mejorar fidelidad de primera clase con integraciones opcionales de `combat`, `items` y `economy` donde aplique, sin convertirlas en dependencias obligatorias.
- Jobs/client: Apply/Reset con cambios pendientes en UI, cast bar visual, cooldowns visibles en hotbar y pulido visual del change class.
- Completar reglas avanzadas de `core` para mob profile/scaling: configs por entidad/tag/bioma/dimension/estructura, sync minimo para target HUD y deteccion fina de dungeon/estructura.
- Completar combat runtime avanzado: tuning de auto-attack client-side, cast, skill packets, status, aggro avanzado y contratos para skills/mobs.
- Port funcional completo de mobs de contenido: IA propia, drops, boss/world state y spawns reales cuando existan mobs/dungeons/bosses propios.
- Comercio y extensiones economicas avanzadas sobre la wallet base.
- Life Skills: migrar/aislar efectos de las antiguas `skills/job/life` si siguen siendo necesarios como acciones activables.
- Social: enriquecer bestiary con integraciones opcionales de mobs/items/economy para stats, cards y recompensas dinamicas.
- Client: portar renderers/effects runtime completos, entity renderers/modelos GeckoLib, pantallas de economy/items y configuracion avanzada de HUD.
- Comandos legacy.
- Mixins: reasignar cada mixin a su modulo propietario antes de activar `ragnarmmo.mixins.json`.
- Smoke test full pack y tests de carga con modulos opcionales ausentes.

## Comandos utiles

- `./gradlew.bat ecosystemStatus`
- `./gradlew.bat modularBuild --quiet`
- `./gradlew.bat portAudit --quiet`
- `./gradlew.bat verifyPortComplete`
- `./gradlew.bat runClient`

Nota: el `runClient` de smoke test carga todos los modulos funcionales salvo `ragnarmmo-mobs` por ahora, porque sus sprites/renderers aun no estan portados. Jade tambien queda fuera del runtime dev hasta revisar compatibilidad de mixins en userdev.

`verifyPortComplete` debe fallar mientras `original-source` conserve codigo o recursos necesarios.

## Criterio para borrar original-source

Solo borrar `original-source` cuando:

- Ningun sistema funcional dependa de leer codigo del monolito.
- Los comandos legacy esten portados o documentados con migracion.
- Los mixins esten reasignados o descartados con razon.
- `modularBuild` pase.
- `verifyPortComplete` pase o sea actualizado para ignorar solo archivos archivados intencionalmente.
- Exista smoke test de los modulos principales y de combinaciones sin mods opcionales.
