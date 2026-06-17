# RagnarMMO Port Status

Estado actual del port desde `original-source` hacia los 9 modulos.

## Resumen

- Build modular: `./gradlew.bat modularBuild --quiet` pasa.
- Java original: 735 archivos en `original-source`.
- Java modular: 223 archivos en los modulos nuevos.
- Recursos originales: 688 archivos.
- Recursos modulares: 683 archivos portados o staged.
- Recursos no copiados tal cual: `META-INF/mods.toml`, `pack.mcmeta`, `pack.png`, `logo.png`, `ragnarmmo.mixins.json`.

Los recursos gameplay ya estan casi todos ubicados. La deuda grande pendiente es portar logica Java funcional.

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
- Arquitectura definida para perfiles RO universales de mobs en core: deteccion, nivel, raza, elemento, tamano, boss/rank, stats derivados y API consumible por combat/client/social/mobs.

### Combat

- Contratos y formulas puras iniciales.
- Element types, hit/reject reasons, profiles simples, defensa/damage/resistencias.
- Runtime basico de ataque normal server-authoritative: request packet, target resolver, cadence/cooldown, hit/crit/damage resolution y result packet.
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
- Utility items legacy: `blue_gemstone`, `oridecon`, `elunium`.
- Recipes de `elunium` y `oridecon`.
- Loot modifier de cards con aliases `ragnarmmo:skill_loot` y `ragnarmmo:ragnar_loot`.
- Tags de items.

### Economy

- Zeny fisico legacy y recipes de conversion como compatibilidad inicial.
- API abstracta vive en `core`; wallet real aun falta.

### Mobs

- Entidades placeholder con IDs legacy para mobs base.
- Mob definitions, templates, entity tags, worldgen tags y biome modifiers staged.

### Life Skills

- Capability, persistencia NBT y sync server/client.
- Manager de progreso separado de Jobs.
- Packets de sync, update, puntos, level up y perk choice registrados en la red comun de core.
- Eventos originales de Life Skills portados: mining/woodcutting/farming/excavation/fishing/exploration y tracker de bloques colocados.
- Perk registry base.
- Datos/resources staged: skill data, `life_1`, life point sources y XP sources.

### Social

- Party runtime portado: SavedData persistente, service, invites, settings, member sync, client cache y comandos `/party`.
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
  - Cambio de clase server-side para la fase actual: `/job current` y `/job change <class>`.
  - `PacketChangeJob` legacy queda cubierto por `JobChangeService`: Novice solo puede cambiar a primera clase, exige Novice Job Lv cap, `basic_skill` Lv 9 y cero puntos de skill sin gastar.
  - Novice cubierto funcionalmente como clase/base inicial propia.
  - Primeras clases cubiertas funcionalmente: Swordman, Archer, Acolyte, Thief, Merchant y Mage.
  - Todas las skills activas `NOVICE`/`FIRST` tienen handler modular.
  - Pasivas `NOVICE`/`FIRST` cubiertas mediante `JobPassiveStatsContributor`: masteries, recovery, dodge, owl/vulture eye, divine/demon, merchant economy passives y survival.
  - Los handlers activos consumen SP desde `core`, aplican delay/cooldown desde JSON y usan targeting/dano vanilla mientras las integraciones opcionales de combat se completan.
  - Merchant shop/economy actives (`buying_store`, `vending`) quedan como placeholder funcional con mensaje hasta completar wallet/shop UI en `economy`.
  - Comandos de smoke test: `/job current`, `/job change <class>`, `/jobskills list`, `/jobskills use <skill>`, `/jobskills hotbar <slot> <skill>`.

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
- Implementar `core` universal mob profile/scaling: clasificacion hostil/neutral/pasivo/boss, configs por entidad/tag/bioma/dimension/estructura, aplicacion de atributos runtime y sync minimo para target HUD.
- Completar combat runtime avanzado: tuning de auto-attack client-side, cast, skill packets, status, aggro, kill credit y contratos para skills/mobs.
- Port funcional completo de mobs: IA, profiles, stats, drops, boss/world state.
- Wallet/currency real, transactions, money bag actions y comercio.
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
- `./gradlew.bat :ragnarmmo-client:runClient`

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
