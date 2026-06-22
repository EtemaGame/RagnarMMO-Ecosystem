# RagnarMMO Mod Audit

Estado despues de consolidar todo en un solo mod Forge (`modId: ragnarmmo`).

## Estructura actual

- Codigo activo: `src/main/java`.
- Recursos activos: `src/main/resources`.
- Tests activos: `src/test`.
- Build principal: `./gradlew.bat build --no-daemon`.
- Alias legacy de build: `./gradlew.bat modularBuild --no-daemon`.

Conteo actual:

- 374 archivos Java.
- 468 recursos.
- 14 tests Java.

## Funcional

- Mod unico `ragnarmmo`, con entrypoint central y subsistemas internos.
- Core: stats base, progresion, atributos compartidos, configs, red, capacidades y perfil universal de mobs.
- Combat: formulas RO, hit/flee/crit, defensa, elementos, tamano, status, aggro, combat mode y handlers de dano.
- Jobs: clases Novice/first class, skill trees por datapack, puntos, hotbar, packets y cambio de clase inicial.
- Items: reglas de item/equipo, cards, modifiers, loot modifiers y base de capability de equipo. El catalogo visual actual no es definitivo.
- Lifeskills: capability, progreso, eventos, sync, perks base y datos de fuentes XP.
- Social: party, party XP, party loot, achievements, titles, bestiary, HUD y pantallas cliente.
- Client: overlays, pantallas, tooltips y keybinds asociados al mod unico.

## En reconstruccion

- Stats y combate: prioridad actual. Deben quedar cerradas las formulas, subida de stats, derived stats y efectos reales sobre el jugador.
- Reset de skills/stats: por ahora se manejara por comandos, no por pociones ni menus.
- Skills: mantener logica y funcionamiento; efectos visuales, sonidos y polish quedan fuera de alcance por ahora.
- Inventario/equipment: el sistema actual con capability es transitorio. Se reemplazara por menus personalizados que intercepten/reemplacen el inventario vanilla.
- Items visuales: el catalogo actual puede eliminarse o reescribirse cuando se defina que items reales quedan.
- Config cliente: quedara en edicion constante y se reescribira al final.

## Diferido intencionalmente

- Mobs propios: pendiente por creacion de modelos 3D; `mobs` no registra entidades/spawns propios todavia.
- Economy: eliminada del codigo activo. Se reescribira completa mas adelante.
- `buying_store` y `vending`: quedan conceptuales mientras se disenan menus personalizados.
- Assets de sonido, efectos, renderers y polish visual: no son prioridad hasta cerrar la logica interna.
- Modelos JSON faltantes de items: warnings conocidos de `runClient`; no bloquean el trabajo actual porque los items visuales seran redefinidos.

## Conceptual o reescribible

- `original-source` fue eliminado. Lo que falte debe reescribirse sobre la arquitectura actual, no copiarse como dependencia.
- Los antiguos submods fisicos fueron eliminados. Las fronteras ahora son paquetes internos.
- Parches externos no aplicados por Gradle no forman parte del mod activo.

## Carpetas conservadas

- `src`: codigo, recursos y tests del mod.
- `docs`: documentacion viva.
- `gradle`: wrapper.
- `run`: runtime local de desarrollo.
- `.vscode`: configuracion local del IDE.

## Proximo orden recomendado

1. Cerrar formulas de stats, derived stats y efectos reales sobre el player.
2. Validar combate: dano, hit/flee/crit, defensa, elementos, ASPD y aggro.
3. Implementar comandos temporales para reset de stats/skills.
4. Revisar que skills mantengan logica funcional sin depender de efectos visuales/sonidos/economy.
5. Replantear inventario/equipment cuando el menu personalizado este disenado.
