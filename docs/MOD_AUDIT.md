# RagnarMMO Mod Audit

Estado al 2026-06-24 despues de consolidar todo en un solo mod Forge (`modId: ragnarmmo`).

## Estructura actual

- Codigo activo: `src/main/java`.
- Recursos activos: `src/main/resources`.
- Tests activos: `src/test`.
- Build principal: `./gradlew.bat build --no-daemon`.
- Alias legacy de build: `./gradlew.bat modularBuild --no-daemon`.

Conteo actual:

- 375 archivos Java.
- 353 recursos.
- 15 tests Java.

## Funcional

- Mod unico `ragnarmmo`, con entrypoint central y subsistemas internos.
- Core: stats base, progresion, atributos compartidos, configs, red, capacidades y perfil universal de mobs.
- Combat: formulas RO, hit/flee/crit, defensa, elementos, tamano, status, aggro, combat mode y handlers de dano.
- Jobs: clases Novice/first class, skill trees por datapack, puntos, hotbar, packets y cambio de clase inicial.
- Items: reglas de item/equipo, cards, modifiers, loot modifiers y base de capability de equipo. El catalogo visual actual no es definitivo.
- Admin/dev commands: `/roadmin` permite reset de stats/skills, set/add de exp, set de niveles, set/add de stats primarios y cambio de job.
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

## Hallazgos activos de logica interna

1. Combate ya comparte snapshot fisico para ataque basico.
   - Ataque basico directo: `RagnarCombatEngine.resolve`.
   - Contrato: `CombatContract.resolveBasicAttack`.
   - Ambos usan `PhysicalAttackProfile` y `BasicPhysicalAttackFormulaService` para StatusATK, WeaponATK, ArrowATK, WeaponLevel, tamano y crit.
   - Pendiente: cerrar reglas finas de skills fisicas, refine, upgrade bonus y dual wield avanzado.

2. Comandos servidor temporales ya existen bajo `/roadmin`.
   - Cubren reset de stats, reset de skills/hotbar, set/add base exp, set/add job exp, set base/job level, set/add stat primario y set job.

3. Bestiary no muestra stats reales todavia.
   - `BestiaryDetailsResolver.resolveStats` devuelve `null`.

4. Armadura fisica de items entra al derived stat actual como compatibilidad.
   - `ItemDerivedStatsContributor.computeArmorHardDefense` suma `Attributes.ARMOR` de casco, pechera, pantalones y botas equipadas.
   - Si el offhand es escudo, tambien puede aportar `Attributes.ARMOR` si el item lo declara.
   - Pendiente: DEF/refine/armaduras RO finales por data propia.

5. Persisten superficies historicas de formula entre `CoreDerivedStatsCalculator`, `CombatMath` y servicios en `combat/formula`.
   - Se deben seguir migrando a servicios puros comunes cuando se toque cada sistema.

## Diferido intencionalmente

- Mobs propios: pendiente por creacion de modelos 3D; `mobs` no registra entidades/spawns propios todavia.
- Economy: eliminada del codigo activo. Se reescribira completa mas adelante.
- `vending`: queda conceptual mientras se disenan menus personalizados.
- Quest/future skills como `buying_store`: retiradas del data activo hasta que entren al alcance.
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

1. Cerrar reglas fisicas finas implementables sin UI: refine/upgrade bonus temporal si hay data y dual wield avanzado.
2. Reemplazar la tabla ASPD provisional por tabla oficial validada si se busca paridad exacta.
3. Completar aplicadores y afinaciones de estados RO: skill Cloaking futura, fuentes reales de Stun/Sleep/Curse/Bleeding, consumibles reales y curas especificas.
4. Completar gaps minimos de items que afectan formulas: DEF/refine RO final, efectos especiales de cards y resolver si equipment extra queda apagado hasta menu nuevo.
5. Replantear inventario/equipment cuando el menu personalizado este disenado.

## Pruebas

```powershell
.\gradlew.bat test --no-daemon --stacktrace
```

Pruebas actuales cubren contratos de stats, formulas base, progresion, cards, item combat profile, packets de stats y API de skills. Falta cobertura de integracion para ruta completa player -> combat -> mob profile -> XP.
