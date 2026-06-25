# Elementos, Razas, Size y Range de Mobs

Estado al 2026-06-24.

Este documento define la taxonomia temporal para mobs vanilla mientras no existan suficientes mobs propios con perfiles completos. No reemplaza perfiles authored: si un mob propio o datapack declara raza, elemento, size o range, ese dato debe tener prioridad.

## Reglas actuales

- Los stats de mobs vanilla escalan temporalmente con el nivel del jugador para mantener dificultad durante la reconstruccion.
- El ataque normal de mobs contra jugadores usa elemento Neutral, aunque el mob tenga elemento defensivo Fire, Water, Shadow, etc.
- El elemento defensivo del mob se usa cuando el mob recibe dano.
- `Shadow` se documenta como elemento RO. En codigo actual se acepta como alias de `DARK`.
- Boss-like temporal: Warden, Ender Dragon y Wither.

## Size temporal

Cuando no hay size resuelto desde un perfil inicializado, se calcula por hitbox:

| Size RO | Regla Minecraft |
| --- | --- |
| Small | mayor eje de hitbox <= 1 bloque |
| Medium | mayor eje de hitbox > 1 y <= 3 bloques |
| Large | mayor eje de hitbox > 3 bloques |

## Attack Range temporal

`attackRange` representa el rango natural del ataque normal del mob, en bloques/cells.

Regla para clasificar:

- `0`: mob sin ataque normal relevante.
- `2`: melee normal.
- `4`: melee largo o contacto especial corto.
- `8+`: ranged o magia/proyectil.

Uso esperado:

- `attackRange <= 3`: ataque fisico melee; Safety Wall puede bloquearlo.
- `attackRange > 3`: ataque fisico ranged o ataque a distancia; Pneuma puede bloquearlo si la fuente es fisica/proyectil.
- Si el mob usa magia real en el futuro, el perfil debe separar `attackRange` de `damageType`.

## Elementos vanilla

| Mob | Elemento RO | Lv | Motivo |
| --- | ---: | -: | --- |
| Allay | Holy | 1 | Espiritu benigno/auxiliar. |
| Axolotl | Water | 1 | Criatura acuatica. |
| Bat | Wind | 1 | Mob volador menor. |
| Bee | Poison | 1 | Picadura venenosa. |
| Blaze | Fire | 3 | Elemental de fuego. |
| Camel | Neutral | 1 | Animal comun. |
| Cat | Neutral | 1 | Animal comun. |
| Cave Spider | Poison | 2 | Arana venenosa. |
| Chicken | Neutral | 1 | Animal comun. |
| Cod | Water | 1 | Pez. |
| Cow | Neutral | 1 | Animal comun. |
| Creeper | Neutral | 3 | Explosivo, pero no elemental. |
| Dolphin | Water | 2 | Acuatico avanzado. |
| Donkey | Neutral | 1 | Animal comun. |
| Drowned | Undead | 2 | Zombie acuatico. |
| Elder Guardian | Water | 4 | Boss-like acuatico. |
| Ender Dragon | Shadow | 4 | Boss del End/void. |
| Enderman | Shadow | 3 | Entidad del End. |
| Endermite | Shadow | 1 | Parasito del End. |
| Evoker | Shadow | 2 | Illager magico oscuro. |
| Fox | Neutral | 1 | Animal comun. |
| Frog | Water | 1 | Anfibio. |
| Ghast | Ghost | 3 | Entidad espectral del Nether. |
| Glow Squid | Water | 2 | Acuatico luminoso. |
| Goat | Earth | 1 | Bestia de montana/impacto fisico. |
| Guardian | Water | 3 | Mob acuatico hostil. |
| Hoglin | Fire | 1 | Bestia del Nether. |
| Horse | Neutral | 1 | Animal comun. |
| Husk | Undead | 2 | Zombie seco/desertico. |
| Iron Golem | Earth | 2 | Constructo fisico/metalico. |
| Llama | Neutral | 1 | Animal comun. |
| Magma Cube | Fire | 3 | Masa viva de magma. |
| Mooshroom | Earth | 1 | Vaca con rasgo fungico/terrestre. |
| Mule | Neutral | 1 | Animal comun. |
| Ocelot | Neutral | 1 | Animal comun. |
| Panda | Neutral | 1 | Animal comun. |
| Parrot | Wind | 1 | Ave voladora. |
| Phantom | Undead | 2 | Mob undead volador. |
| Pig | Neutral | 1 | Animal comun. |
| Piglin | Fire | 1 | Humanoide del Nether. |
| Piglin Brute | Fire | 2 | Variante mas peligrosa del Nether. |
| Pillager | Neutral | 1 | Humanoide hostil fisico/ranged. |
| Polar Bear | Water | 1 | Asociado a hielo/frio. |
| Pufferfish | Poison | 1 | Identidad mecanica venenosa. |
| Rabbit | Neutral | 1 | Animal comun. |
| Ravager | Earth | 2 | Bestia pesada/de impacto. |
| Salmon | Water | 1 | Pez. |
| Sheep | Neutral | 1 | Animal comun. |
| Shulker | Ghost | 2 | Entidad extrana/intangible del End. |
| Silverfish | Earth | 1 | Insecto asociado a piedra. |
| Skeleton | Undead | 2 | No-muerto explicito. |
| Skeleton Horse | Undead | 1 | Montura no-muerta. |
| Slime | Water | 1 | Masa gelatinosa. |
| Sniffer | Earth | 1 | Bestia antigua que excava. |
| Snow Golem | Water | 2 | Nieve/hielo mapeado a Water. |
| Spider | Poison | 1 | Aracnido. |
| Squid | Water | 1 | Acuatico. |
| Stray | Undead | 2 | Skeleton helado no-muerto. |
| Strider | Fire | 2 | Criatura adaptada a lava. |
| Tadpole | Water | 1 | Acuatico. |
| Trader Llama | Neutral | 1 | Animal comun. |
| Tropical Fish | Water | 1 | Pez. |
| Turtle | Water | 2 | Animal acuatico/marino. |
| Vex | Ghost | 3 | Espiritu hostil invocado. |
| Villager | Neutral | 1 | Humanoide civil. |
| Vindicator | Neutral | 1 | Humanoide hostil fisico. |
| Wandering Trader | Neutral | 1 | Humanoide civil. |
| Warden | Shadow | 4 | Sculk/deep dark. |
| Witch | Poison | 2 | Pociones, veneno y dano magico. |
| Wither | Undead | 4 | Boss no-muerto. |
| Wither Skeleton | Undead | 3 | No-muerto del Nether con wither. |
| Wolf | Neutral | 1 | Animal comun. |
| Zoglin | Undead | 2 | Hoglin zombificado. |
| Zombie | Undead | 2 | No-muerto explicito. |
| Zombie Villager | Undead | 2 | Villager zombificado. |
| Zombified Piglin | Undead | 2 | Piglin zombificado. |

## Razas vanilla

| Raza RO | Mobs |
| --- | --- |
| Angel | Allay |
| Brute | Bat, Camel, Cat, Chicken, Cow, Donkey, Fox, Frog, Goat, Hoglin, Horse, Llama, Mooshroom, Mule, Ocelot, Panda, Parrot, Pig, Polar Bear, Rabbit, Ravager, Sheep, Sniffer, Strider, Trader Llama, Wolf |
| Demi-Human | Evoker, Piglin, Piglin Brute, Pillager, Villager, Vindicator, Wandering Trader, Witch |
| Demon | Enderman, Ghast, Vex, Warden |
| Dragon | Ender Dragon |
| Fish | Axolotl, Cod, Dolphin, Elder Guardian, Glow Squid, Guardian, Pufferfish, Salmon, Squid, Tadpole, Tropical Fish, Turtle |
| Formless | Blaze, Creeper, Iron Golem, Magma Cube, Shulker, Slime, Snow Golem |
| Insect | Bee, Cave Spider, Endermite, Silverfish, Spider |
| Plant | Ninguno vanilla por ahora |
| Undead | Drowned, Husk, Phantom, Skeleton, Skeleton Horse, Stray, Wither, Wither Skeleton, Zoglin, Zombie, Zombie Villager, Zombified Piglin |

## Attack Range vanilla

Estos rangos son perfiles de combate temporales del mod basados en el comportamiento vanilla. Se usan para dejar de inferir ranged por distancia arbitraria.

| Range | Mobs |
| ---: | --- |
| 0 | Allay, Axolotl, Bat, Camel, Cat, Chicken, Cod, Cow, Dolphin, Donkey, Fox, Frog, Glow Squid, Horse, Llama, Mooshroom, Mule, Ocelot, Panda, Parrot, Pig, Rabbit, Salmon, Sheep, Skeleton Horse, Sniffer, Squid, Tadpole, Trader Llama, Tropical Fish, Turtle, Villager, Wandering Trader |
| 2 | Bee, Cave Spider, Drowned, Enderman, Endermite, Hoglin, Husk, Magma Cube, Phantom, Piglin Brute, Polar Bear, Ravager, Silverfish, Slime, Spider, Strider, Vex, Vindicator, Wither Skeleton, Wolf, Zoglin, Zombie, Zombie Villager, Zombified Piglin |
| 4 | Creeper, Iron Golem, Pufferfish, Warden |
| 8 | Evoker, Witch |
| 12 | Blaze, Pillager, Shulker, Skeleton, Snow Golem, Stray |
| 15 | Guardian, Elder Guardian |
| 32 | Ghast, Wither |
| 64 | Ender Dragon |

Notas:

- Creeper queda en `4` por explosion de corto alcance, no por melee.
- Warden queda en `4` como corto alcance base; su sonic boom debe tratarse como skill/ranged especial cuando exista ruta propia.
- Ender Dragon queda en `64` por boss arena/comportamiento especial; no debe tratarse como melee comun.
- Ghast y Wither quedan ranged por proyectiles.
- Guardian/Elder Guardian quedan ranged por beam.
- Evoker y Witch quedan como rango magico/ranged temporal; cuando exista `damageType` por mob, deben separarse de ranged fisico.

## Pendiente de implementacion

- `OK`: `attackRange` existe en el perfil runtime de mobs.
- `OK`: `attack_range` se permite en definiciones/templates de datapack.
- `OK`: los defaults vanilla se cargan desde `VanillaMobTaxonomyDefaults`.
- `OK`: `MobPreRenewalDamageEventHandler` usa `attackRange` para clasificar melee/ranged.
- Pendiente futuro: separar `attackRange` de `damageType`, porque no todo ataque a distancia es fisico.
