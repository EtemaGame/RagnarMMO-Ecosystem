# Cards

Estado al 2026-06-24.

Este documento registra el comportamiento actual de las cards. No es una tabla final de balance RO.

## Sistema actual

- Hay 77 card JSON activos.
- Hay card para todos los mobs vanilla perfilados en `VanillaMobTaxonomyDefaults`.
- `illusioner_card` existe como extra valido aunque `illusioner` no este en la tabla vanilla perfilada.
- Las cards se cargan desde `data/ragnarmmo/cards`.
- Al dropear, la card guarda sus modifiers en NBT `card_modifiers`.
- Al compounding, esos modifiers se agregan al item en `RoCompoundedCardModifiers`.
- Los stats primarios se aplican por `RoItemRuleResolver` como enteros.
- Los modifiers no-stat se leen desde NBT por sistemas de combate.

## Modifiers Soportados Ahora

Stats primarios:

- `ragnarmmo:str`
- `ragnarmmo:agi`
- `ragnarmmo:vit`
- `ragnarmmo:int`
- `ragnarmmo:dex`
- `ragnarmmo:luk`

Combat modifiers activos:

- `ragnarmmo:damage_all`
- `ragnarmmo:damage_race_<race>`
- `ragnarmmo:damage_element_<element>`
- `ragnarmmo:damage_size_<small|medium|large>`
- `ragnarmmo:resist_all`
- `ragnarmmo:resist_race_<race>`
- `ragnarmmo:resist_size_<small|medium|large>`
- `ragnarmmo:resist_element_<element>`
- `ragnarmmo:resist_all_elements`

Pendiente:

- Efectos especiales de card tipo RO.
- Restricciones finales por slot/equipo una vez se cierre equipment custom.
- Balance final de cada card.

## Cards Actuales

La columna `Efecto runtime` muestra el resultado efectivo actual. Los stats primarios se redondean a enteros.

| Card | Mob | Slot | Efecto runtime |
| --- | --- | --- | --- |
| Allay Card | `minecraft:allay` | any | LUK +2 |
| Axolotl Card | `minecraft:axolotl` | any | VIT +1 |
| Bat Card | `minecraft:bat` | any | AGI +1 |
| Bee Card | `minecraft:bee` | any | AGI +1 |
| Blaze Card | `minecraft:blaze` | armor | DEX +2, resist fire +20% |
| Camel Card | `minecraft:camel` | any | VIT +2 |
| Cat Card | `minecraft:cat` | any | LUK +1 |
| Cave Spider Card | `minecraft:cave_spider` | any | AGI +2 |
| Chicken Card | `minecraft:chicken` | any | AGI +1 |
| Cod Card | `minecraft:cod` | any | VIT +1 |
| Cow Card | `minecraft:cow` | any | VIT +1 |
| Creeper Card | `minecraft:creeper` | any | STR +2 |
| Dolphin Card | `minecraft:dolphin` | any | AGI +1 |
| Donkey Card | `minecraft:donkey` | any | STR +1 |
| Drowned Card | `minecraft:drowned` | any | VIT +1 |
| Elder Guardian Card | `minecraft:elder_guardian` | armor | VIT +2, resist water +20% |
| Ender Dragon Card | `minecraft:ender_dragon` | any | STR +5, INT +3 |
| Enderman Card | `minecraft:enderman` | any | INT +2 |
| Endermite Card | `minecraft:endermite` | any | AGI +1 |
| Evoker Card | `minecraft:evoker` | any | INT +3 |
| Fox Card | `minecraft:fox` | any | LUK +1 |
| Frog Card | `minecraft:frog` | any | AGI +1 |
| Ghast Card | `minecraft:ghast` | any | INT +2 |
| Glow Squid Card | `minecraft:glow_squid` | any | LUK +1 |
| Goat Card | `minecraft:goat` | any | STR +1 |
| Guardian Card | `minecraft:guardian` | armor | AGI +2, DEX +1, resist water +20% |
| Hoglin Card | `minecraft:hoglin` | any | STR +2 |
| Horse Card | `minecraft:horse` | any | AGI +1 |
| Husk Card | `minecraft:husk` | any | VIT +2 |
| Illusioner Card | `minecraft:illusioner` | any | INT +3, LUK +2 |
| Iron Golem Card | `minecraft:iron_golem` | any | STR +2, VIT +2 |
| Llama Card | `minecraft:llama` | any | STR +1 |
| Magma Cube Card | `minecraft:magma_cube` | any | VIT +2 |
| Mooshroom Card | `minecraft:mooshroom` | any | VIT +1, LUK +1 |
| Mule Card | `minecraft:mule` | any | STR +1 |
| Ocelot Card | `minecraft:ocelot` | any | AGI +1 |
| Panda Card | `minecraft:panda` | any | VIT +1 |
| Parrot Card | `minecraft:parrot` | any | AGI +1 |
| Phantom Card | `minecraft:phantom` | any | AGI +2 |
| Pig Card | `minecraft:pig` | any | VIT +1 |
| Piglin Brute Card | `minecraft:piglin_brute` | any | VIT +3 |
| Piglin Card | `minecraft:piglin` | any | DEX +1 |
| Pillager Card | `minecraft:pillager` | any | DEX +1 |
| Polar Bear Card | `minecraft:polar_bear` | any | STR +2 |
| Pufferfish Card | `minecraft:pufferfish` | any | VIT +2 |
| Rabbit Card | `minecraft:rabbit` | any | AGI +1 |
| Ravager Card | `minecraft:ravager` | any | STR +3 |
| Salmon Card | `minecraft:salmon` | any | AGI +1 |
| Sheep Card | `minecraft:sheep` | any | LUK +1 |
| Shulker Card | `minecraft:shulker` | any | VIT +2 |
| Silverfish Card | `minecraft:silverfish` | any | AGI +1 |
| Skeleton Card | `minecraft:skeleton` | any | DEX +1 |
| Skeleton Horse Card | `minecraft:skeleton_horse` | any | AGI +1 |
| Slime Card | `minecraft:slime` | any | VIT +1 |
| Sniffer Card | `minecraft:sniffer` | any | LUK +2 |
| Snow Golem Card | `minecraft:snow_golem` | any | DEX +1 |
| Spider Card | `minecraft:spider` | any | AGI +1 |
| Squid Card | `minecraft:squid` | any | AGI +1 |
| Stray Card | `minecraft:stray` | any | DEX +2 |
| Strider Card | `minecraft:strider` | any | AGI +1 |
| Tadpole Card | `minecraft:tadpole` | any | AGI +1 |
| Trader Llama Card | `minecraft:trader_llama` | any | STR +1, LUK +1 |
| Tropical Fish Card | `minecraft:tropical_fish` | any | LUK +1 |
| Turtle Card | `minecraft:turtle` | armor | VIT +1 |
| Vex Card | `minecraft:vex` | any | AGI +2 |
| Villager Card | `minecraft:villager` | any | LUK +1 |
| Vindicator Card | `minecraft:vindicator` | any | STR +2 |
| Wandering Trader Card | `minecraft:wandering_trader` | any | LUK +2, DEX +1 |
| Warden Card | `minecraft:warden` | any | VIT +4, STR +2 |
| Witch Card | `minecraft:witch` | any | INT +2 |
| Wither Card | `minecraft:wither` | any | INT +4, STR +3 |
| Wither Skeleton Card | `minecraft:wither_skeleton` | any | STR +1 |
| Wolf Card | `minecraft:wolf` | any | AGI +1 |
| Zoglin Card | `minecraft:zoglin` | any | STR +2 |
| Zombie Card | `minecraft:zombie` | any | VIT +1 |
| Zombie Villager Card | `minecraft:zombie_villager` | any | INT +1 |
| Zombified Piglin Card | `minecraft:zombified_piglin` | any | STR +1 |
