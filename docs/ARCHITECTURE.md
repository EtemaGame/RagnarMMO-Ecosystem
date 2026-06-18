# RagnarMMO Architecture

Este workspace convierte RagnarMMO en una familia de 8 mods Forge. `original-source` es solo referencia de lectura para terminar el port: no es un modulo, no se distribuye y no debe ser dependencia.

## Regla central

- `ragnarmmo-core` es la autoridad server-side comun.
- Los demas mods se adhieren a `core`.
- Cada modulo debe funcionar con Minecraft vanilla/base mas `core`.
- Las integraciones entre mods RagnarMMO son opcionales salvo que se documente lo contrario.
- El codigo client-side de cada modulo solo muestra datos, caches visuales e input; no decide stats, damage, drops, wallet ni progresion.
- Todo gameplay debe poder validarse en servidor dedicado sin depender de una carpeta o modulo client separado.

## Modulos

| Modulo | Rol | Dependencia obligatoria |
| --- | --- | --- |
| `ragnarmmo-core` | API publica, stats, base/job level, atributos, configs, networking comun, eventos compartidos y resolucion universal de perfiles RO para entidades. | Ninguna |
| `ragnarmmo-combat` | Combate RO opcional: formulas, targeting, cast/cooldown, status, aggro, damage, hit/flee/crit/ASPD. | `core` |
| `ragnarmmo-jobs` | Jobs, job trees, job skills, hotbar, requisitos por Job Lv y gasto de puntos. | `core` |
| `ragnarmmo-items` | Items fisicos, equipo, cards, refinement, recipes y drops no economicos. | `core` |
| `ragnarmmo-economy` | Wallet/currency, zeny compat, money bag, transacciones, comercio y shops futuros. | `core` |
| `ragnarmmo-lifeskills` | Mining, woodcutting, excavation, farming, fishing, exploration y progresion propia. | `core` |
| `ragnarmmo-mobs` | Mobs RO, definitions, spawns, boss/world state, mob profiles y drops. | `core` |
| `ragnarmmo-social` | Party, achievements, titles, bestiary y progreso visible. | `core` |

## Integraciones opcionales

- `jobs` usa `combat` si esta presente para damage/cast/status, y `items` si esta presente para requisitos de arma/equipo. Sin ellos debe usar fallback vanilla.
- `mobs` usa `combat` para aggro/damage/status, `items` para drops fisicos y `economy` para recompensas wallet. Sin ellos debe spawnear y comportarse con base vanilla.
- `social` muestra datos de `items`, `mobs`, `jobs` o `economy` solo si existen. Party/titles/achievements deben funcionar con solo `core`.
- Las pantallas, renderers, tooltips, keybinds y caches client-side viven en el modulo que expone esa funcionalidad, activadas por presencia de modulo.
- GeckoLib y Jade son compat/render opcionales, no requisitos del ecosistema base.

## Frontera Core/Combat

- `core` es la fuente de verdad de datos base: stats primarios, progresion, recursos, atributos compartidos, configs y derived stats extensibles.
- `combat` no posee esos datos; los consulta desde `core` y decide como se aplican durante una accion de combate.
- La cadencia de golpes, ASPD runtime, reemplazo del cooldown vanilla, anulacion de i-frames entre golpes, hit/flee/crit, defensa, reduccion, dual wield y reglas melee/ranged pertenecen a `combat`.
- Equipos, cards, enchants, buffs y otros modulos deben aportar modificadores mediante atributos o contributors compartidos; `combat` consume el estado final resuelto sin depender de implementaciones internas de esos modulos.

## Universal Mob Scaling

El sistema de niveles y perfiles RO para mobs no vive en `ragnarmmo-mobs`. Ese modulo queda reservado para entidades RagnarMMO propias, spawns authored, bosses propios, IA especifica, drops y contenido que complete el mundo. El escalado universal debe vivir en `ragnarmmo-core` porque afecta a mobs vanilla, mobs de futuros mods y mobs de otros mods aunque `ragnarmmo-mobs` no este instalado.

Responsabilidades de `core`:

- Detectar cualquier `LivingEntity` no jugador y clasificarla como hostil, neutral, pasiva o boss.
- Excluir por defecto animales de granja y mobs puramente pasivos que no atacan al jugador.
- Resolver un `MobProfile` server-side con nivel, raza, elemento, nivel elemental, tamano, rank/boss, stats primarios, stats derivados, recompensas base y flags de comportamiento.
- Aplicar atributos runtime derivados: HP, attack, defense, magic defense, hit, flee, crit, ASPD/move speed y otros atributos compartidos cuando existan.
- Leer configs/datapacks universales para reglas por entidad, tag, biome, dimension, estructura/dungeon y boss.
- Exponer API publica para que `combat`, `social`, `mobs` u otros modulos consulten el perfil sin depender entre si.

Responsabilidades de `combat`:

- Consumir el perfil RO resuelto por `core` durante hit/flee, damage, reduccion, crit, ASPD, status y aggro.
- Resolver el daño fisico mob -> jugador con formula pre-renewal: el mob usa su ATK min/max y HIT; el jugador defiende con FLEE/perfect dodge, hard DEF porcentual, soft DEF plana y reducciones adicionales.
- Aplicar propiedades RO al daño: elemento atacante vs elemento defensor nivel 1-4, penalizacion de arma por tamano del objetivo y bonuses por raza/elemento/tamano cuando existan en NBT de equipo/cartas.
- No decidir el nivel del mob ni su raza/elemento/nivel elemental/tamano; solo aplicar esas propiedades a formulas de combate.
- Usar fallback vanilla cuando `core` no entregue perfil para un objetivo.

Contrato de raza, elemento y tamano en perfiles de mobs:

- Las definiciones datapack usan campos separados: `race`, `element`, `element_level` y `size`.
- `element_level` representa el nivel defensivo RO del objetivo y acepta 1-4. Si falta, se usa 1.
- Elementos aceptados por combate: `neutral`, `water`, `earth`, `fire`, `wind`, `poison`, `holy`, `dark`/`shadow`, `ghost`, `undead`.
- Razas recomendadas RO: `formless`, `undead`, `brute`, `plant`, `insect`, `fish`, `demon`, `demihuman`, `angel`, `dragon`. Por ahora se guardan como string normalizado para permitir compat con mods externos.
- Tamanos aceptados: `small`, `medium`, `large`.
- Fallback procedural actual para mobs sin definicion: `race=brute`, `element=neutral`, `element_level=1`, `size=medium`.
- La tabla elemental base de `combat` es pre-renewal: el ataque no tiene nivel elemental; solo el defensor tiene nivel 1-4. La tabla puede devolver multiplicadores negativos, igual que RO.

Contrato inicial de modifiers consumidos por `combat`:

- `ragnarmmo:damage_all`
- `ragnarmmo:damage_race_<race>`; ejemplo `ragnarmmo:damage_race_undead`
- `ragnarmmo:damage_element_<element>`; ejemplo `ragnarmmo:damage_element_fire`
- `ragnarmmo:damage_size_<small|medium|large>`
- `ragnarmmo:resist_element_<element>`; ejemplo `ragnarmmo:resist_element_fire`
- `ragnarmmo:resist_all_elements`

Estos valores son porcentajes expresados como decimal (`0.20` = 20%). Pueden vivir como claves directas del item o dentro de `card_modifiers`/`RoCompoundedCardModifiers`.

Responsabilidades client-side y de `social`:

- Mostrar el perfil resuelto, por ejemplo mini HUD/target frame con nombre, nivel, HP y clasificacion.
- No calcular nivel, stats ni recompensas.
- `social` puede enriquecer bestiary/achievements con datos de perfil cuando existan.

Responsabilidades de `ragnarmmo-mobs`:

- Registrar entidades propias y sus definiciones authored.
- Aportar overrides de perfil a `core` para mobs RagnarMMO propios.
- No ser requisito para que mobs vanilla o de otros mods reciban niveles.

Resolucion recomendada por prioridad:

1. Override especifico por entidad o NBT/spawn marker.
2. Regla de estructura/dungeon, cuando exista contexto confiable.
3. Regla de dimension.
4. Regla de biome/tag de biome.
5. Regla por tag de entidad: undead, insect, brute, boss, ranged, caster, etc.
6. Fallback automatico por categoria vanilla/modded: hostil, neutral o pasivo.

Las estructuras/dungeons deben poder definir nivel minimo, maximo o fijo de manera separada del mundo abierto. Para el primer port basta una API/config preparada; la deteccion fina por estructura puede entrar despues con adapters por estructura, spawn reason o marcadores de integracion.

La configuracion avanzada por mob queda planificada como sistema datapack/config server-side: cada entidad o tag podra definir HP, ATK, MATK, DEF, MDEF, HIT, FLEE, ASPD, raza, elemento, nivel elemental, tamano, nivel fijo/rango y multiplicadores. Esta capa no es prioritaria para el smoke inicial, pero la arquitectura debe dejarle espacio.

Separacion recomendada entre config y datapacks:

- Datapack: definiciones exactas por `entity_type`, plantillas reutilizables, overrides de mobs de mods, loot tables, skill trees, recipes, tags y contenido que debe viajar con un pack o mundo.
- Config server-side: tasas globales, multiplicadores por rank, defaults procedurales, toggles de formulas, tabla elemental opcional/custom, rangos por dimension/bioma/tag y presets simples pensados para administradores.
- Regla practica: si el dato apunta a un recurso de Minecraft (`minecraft:zombie`, tag, loot table, recipe, skill tree), va mejor en datapack; si es una perilla de balance global o un preset que el admin cambia a mano, va mejor en `config`.

## Skills UI Modular

La pantalla de skills debe conservar la forma mental de Ragnarok Online: arbol en grilla con iconos, nombre arriba de cada skill, nivel actual/maximo abajo, puntos disponibles, botones Apply/Reset y separacion visual por clase. No debe ser una lista generica.

La vista principal debe componerse a partir de datos, no hardcodear una clase:

- Seccion superior: arbol basico del personaje. Siempre incluye `novice_1` y la primera clase actual o seleccionada: `swordsman_1`, `archer_1`, `acolyte_1`, `thief_1`, `mage_1`, `merchant_1`, etc.
- Seccion inferior: segunda clase disponible o actual, por ejemplo `blacksmith_2` para Merchant. En RO el nombre correcto para esta rama es Blacksmith; Whitesmith queda para una evolucion futura/transcendent si se agrega.
- Cada seccion usa el `grid.width`, `grid.height` y posiciones `x/y` de `data/ragnarmmo/skill_trees`.
- Si un arbol declara `inherit`, la UI puede mostrar la primera clase como contexto, pero no debe duplicar puntos ni perder la separacion visual superior/inferior.
- El selector de clase debe permitir ver promociones disponibles aunque aun no se cumplan requisitos, marcandolas bloqueadas.
- El change class debe consultar reglas server-side de `jobs`: Job Lv minimo, clase actual, prerequisitos y promociones.
- La UI debe soportar futuras clases sin cambio de codigo: si aparece un nuevo `skill_tree` por datapack/mod y cumple el contrato, debe poder mostrarse.

Fuentes de datos:

- `ragnarmmo-jobs` registra job skill definitions, skill trees, skill levels, puntos y packets de aplicar/resetear.
- `ragnarmmo-lifeskills` registra life skills en una pestana separada o una seccion lateral; no se mezclan con Job Skills.
- El codigo client-side del modulo dueño de cada UI renderiza iconos, grillas, tooltips, estado visual y caches. No decide requisitos, puntos ni aprendizaje.
- `ragnarmmo-core` define contratos/API comunes para que otros mods puedan aportar skills visibles sin depender de internals de `jobs`.

Iconos y sprites:

- La UI busca primero icono definido por skill.
- Si no existe, usa icono placeholder estable por categoria: passive, active, buff, crafting, trap, life.
- Los recursos del cliente pueden incluir sprites RO-style, pero la ausencia de un icono nunca debe romper la pantalla.

Estados visuales esperados:

- Aprendida, disponible para subir, bloqueada por prerequisito, bloqueada por job/class, maxeada, pendiente de aplicar y pendiente de reset.
- Tooltips deben mostrar descripcion, tipo de skill, nivel actual/maximo, coste SP/recurso, prerequisitos y efecto por nivel cuando exista.
- El contador inferior debe mostrar puntos usados/limite por seccion, por ejemplo `49 / 49` para primera clase o `61 / 69` para segunda.

## IDs Forge

Las carpetas Gradle usan guion y los `modId` usan guion bajo:

| Carpeta | modId |
| --- | --- |
| `ragnarmmo-core` | `ragnarmmo_core` |
| `ragnarmmo-combat` | `ragnarmmo_combat` |
| `ragnarmmo-jobs` | `ragnarmmo_jobs` |
| `ragnarmmo-items` | `ragnarmmo_items` |
| `ragnarmmo-economy` | `ragnarmmo_economy` |
| `ragnarmmo-lifeskills` | `ragnarmmo_lifeskills` |
| `ragnarmmo-mobs` | `ragnarmmo_mobs` |
| `ragnarmmo-social` | `ragnarmmo_social` |

Algunos registros conservan namespace legacy `ragnarmmo` para compatibilidad de datos viejos. No eliminar esos aliases sin revisar `LEGACY_COMPATIBILITY.md`.

## Reglas de port

- Preservar IDs, comandos y datos criticos durante la primera migracion.
- No borrar `original-source` hasta que el codigo Java necesario este portado.
- No agregar dependencias obligatorias entre modulos de gameplay si se puede resolver con API, evento, `ModList`, IMC o adapter opcional.
- Economy debe seguir reemplazable: otros mods consumen API de `core`, no detalles internos de wallet/zeny.
- Jobs y Life Skills no se mezclan: Jobs usa Job Lv/puntos; Life Skills usa progresion propia.
