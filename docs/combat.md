Alcance: Pre-Renewal clásico

Voy a tratar Ragnarok Online Pre-Renewal, no Renewal ni Revo-Classic. En servidores privados puede cambiar por configuración de emulador, pero el modelo base es este: el combate se divide en ataques físicos y ataques mágicos; los físicos pasan por HIT/FLEE/CRIT/DEF/elemento/cards, y los mágicos pasan por cast, MATK/MDEF/elemento, normalmente sin HIT/FLEE. La propia página clásica de iRO Wiki marca la sección de ataques como necesitada de revisión experta, así que conviene validarlo contra el código del servidor si estás implementando esto en un emulador o balanceando un servidor.

1. Entidades de combate: jugador vs mob
Jugador

Un jugador tiene:

Categoría	Ejemplos
Stats base	STR, AGI, VIT, INT, DEX, LUK
Bonos de stats	job bonus, equipo, cartas, buffs
Substats visibles	ATK, MATK, DEF, MDEF, HIT, CRIT, FLEE, ASPD
Equipo	arma, armadura, shield, garment, shoes, accesorios
Propiedad defensiva	normalmente Neutral 1, salvo armor elemental o efectos
Propiedad ofensiva	arma, flecha, endow, skill
Skills	pasivas, activas, ofensivas, buffs, debuffs
Estado actual	HP, SP, peso, estados alterados, buffs, debuffs

Los stats base del jugador se suben con puntos. En Pre-Renewal el límite normal de stat base es 99, aunque los bonos pueden llevar el total efectivo por encima de 100. El coste de subir stats aumenta por rangos; por ejemplo, subir un stat de x a x+1 cuesta floor((x-1)/10) + 2 puntos.

Mob / monstruo

Un mob no es “un jugador sin equipo”. Tiene datos definidos por base de datos:

Campo de mob	Uso en combate
Level	afecta HIT/FLEE derivados, EXP, escalas específicas
HP	vida
MinATK / MaxATK	daño físico base del mob
DEF / MDEF	mitigación física/mágica
STR/AGI/VIT/INT/DEX/LUK	usados para substats, resistencias, soft DEF, HIT, crit shield, etc.
Race	Brute, Demi-Human, Demon, Undead, etc.
Size	Small, Medium, Large
Element + level	ejemplo: Fire 2, Undead 3
Attack range	melee/ranged
Skill list	skills de mob: magias, estados, Critical Slash, etc.

La diferencia importante: los mobs tienen BaseATK = 0 en el flujo físico clásico; su WeaponATK se toma como un número aleatorio entre MinATK y MaxATK. En crítico por skill, se usa MaxATK.

2. Stats principales y cómo afectan
STR

Jugador melee:

Aumenta ATK físico de armas cuerpo a cuerpo.
Aporta límite de peso.
Tiene breakpoint fuerte cada 10 STR por la fórmula cuadrática.
BaseATK melee =
STR + floor(STR / 10)^2 + floor(DEX / 5) + floor(LUK / 5)
+ UpgradeBonus + Impositio Manus + ATK cards

En Pre-Renewal, STR no es lineal: 90 STR no es solo 10 más que 80; también sube el bono floor(STR/10)^2.

Mob:

No usa STR para calcular su daño físico normal de la misma forma que un jugador.
El daño físico normal del mob viene de rnd(MinATK, MaxATK).
STR puede existir en la base de datos del mob, pero no reemplaza el MinATK/MaxATK del mob en el algoritmo físico clásico.
AGI

Jugador:

Da FLEE.
Aumenta ASPD.
Ayuda a builds de autoattack.
No reduce cast time en Pre-Renewal.
FLEE base ≈ BaseLevel + AGI + bonos

FLEE se degrada cuando varios mobs te atacan: a partir de más de dos atacantes, se reduce 10% por cada mob adicional. La fórmula clásica de Actual Flee usa (Level + AGI + Item Bonus) afectado por la penalización de cantidad de mobs, más skill bonus.

Mob:

AGI del mob contribuye a su evasión.
Para pegarle a un mob, tu HIT debe superar su FLEE.
Las bases de datos suelen mostrar “100% HIT” requerido, que deriva de esta relación HIT/FLEE.
VIT

Jugador:

Aumenta Max HP.
Mejora recuperación de HP.
Aporta Soft DEF.
Ayuda contra estados como Stun, Poison, Silence, etc., según estado.
Reduce daño físico después de Hard DEF.

Soft DEF de jugador en el algoritmo clásico:

SoftDEF jugador =
floor(VIT * 0.5)
+ rnd(floor(VIT * 0.3), max(floor(VIT * 0.3), floor(VIT^2 / 150) - 1))

El punto crítico: VIT DEF no es porcentaje; es reducción plana aplicada después de Hard DEF. En crítico, Hard DEF y VIT DEF se tratan como 0.

Mob:

Soft DEF de mob:

SoftDEF mob =
VIT + rnd(0, floor(VIT / 20)^2 - 1)

Por eso mobs con VIT alta no solo tienen más vida o resistencia: también reducen daño físico plano.

INT

Jugador:

Aumenta MATK.
Aumenta Max SP.
Mejora recuperación de SP.
Aporta defensa mágica blanda.
Ayuda a resistir algunos estados.
Afecta Heal, daño mágico y sustain.

MATK Pre-Renewal tiene rango:

Min MATK = INT + floor(INT / 7)^2
Max MATK = INT + floor(INT / 5)^2

Esto genera breakpoints: INT en múltiplos relevantes de 5 y 7 produce saltos de MATK mínimo/máximo.

Mob:

INT del mob se usa como reducción mágica plana cuando recibe magia.
También puede afectar daño de skills mágicas del mob si el script/skill lo usa, aunque el algoritmo genérico de magia usa rnd(minMATK, maxMATK) para ataques mágicos.
DEX

Jugador:

Da HIT.
Reduce cast time.
Aumenta daño de armas missile: bow, gun, whip, instrument.
Reduce variación del WeaponATK melee.
Aporta algo de ASPD.
HIT = BaseLevel + DEX + bonos

Para ataques físicos, la probabilidad de acierto normal es:

Hit chance (%) = 80 + AttackerHIT - DefenderFLEE

Algunos skills tienen autohit o bonus de HIT, así que no todo skill físico usa esta fórmula pura.

Para cast mágico o skill con cast variable:

ActualCastTime =
BaseCastTime * EquipmentMods * max(1 - DEX / 150, 0)

Con 150 DEX efectiva, el cast variable clásico llega a 0, salvo reglas especiales del skill/servidor.

Mob:

DEX del mob cuenta para su HIT.
iRO Wiki Classic especifica que el HIT del atacante también aplica a monstruos como BaseLevel + DEX.
LUK

Jugador:

Aumenta CRIT.
Aumenta Perfect Dodge.
Aporta algo de ATK.
Ayuda en brewing/forging y ciertos estados.
Reduce el crítico recibido indirectamente como “crit shield”.

Critical real del atacante:

Crit chance real =
(1 + LUK * 0.3 + EquipmentBonuses) * CritModifier
- TargetLUK / 5

La ventana de estado no muestra exactamente el crítico real aplicado contra cada objetivo. Los Katar duplican el crítico. Los críticos ignoran FLEE normal y DEF, pero no Perfect Dodge.

Perfect Dodge de jugador:

PerfectDodge =
1 + floor(LUK / 10) + EquipmentBonuses

Los mobs no hacen Perfect Dodge como target; iRO Wiki Classic indica que los monstruos objetivo no pueden perfect dodge.

Mob:

LUK del mob reduce tu CRIT real.
Los mobs no crittean por LUK normal; solo hacen crítico si usan skills como Critical Slash o Counter Attack, que producen crítico automático.
3. Substats y definición en combate
ATK

En la ventana aparece como A + B.

A: daño principal derivado de arma + stats.
B: daño de refine/upgrade.
En Pre-Renewal, incluso si el daño principal es negado, el refine puede seguir aportando daño salvo interacciones elementales.
BaseATK jugador melee
STR + floor(STR / 10)^2
+ floor(DEX / 5)
+ floor(LUK / 5)
+ UpgradeBonus
+ Impositio Manus
+ ATKCards
BaseATK jugador ranged / missile
DEX + floor(DEX / 10)^2
+ floor(STR / 5)
+ floor(LUK / 5)
+ UpgradeBonus
+ Impositio Manus
+ ATKCards
BaseATK mob
0

El mob usa su MinATK/MaxATK como WeaponATK.

WeaponATK
Melee jugador
WeaponATK melee =
rnd(min(DEX * (0.8 + 0.2 * WeaponLevel), WeaponATK), WeaponATK)

En crítico:

WeaponATK = WeaponATK máximo del arma

Esto significa que DEX no solo da HIT: también reduce la variación del daño de armas melee.

Bow jugador
WeaponATK bow =
rnd(
  WeaponATK/100 * min(WeaponATK, DEX * (0.8 + 0.2 * WeaponLevel)),
  max(WeaponATK, WeaponATK/100 * min(WeaponATK, DEX * (0.8 + 0.2 * WeaponLevel)))
)
+ rnd(0, ArrowATK - 1)

En crítico:

WeaponATK bow crit = WeaponATK + ArrowATK

El detalle importante: el ATK de la flecha entra como una parte adicional aleatoria en ataques normales, y completa en crítico.

Mob
WeaponATK mob = rnd(MinATK, MaxATK)

En crítico por skill:

WeaponATK mob crit = MaxATK

MATK

MATK es rango, no valor fijo:

MinMATK = INT + floor(INT / 7)^2
MaxMATK = INT + floor(INT / 5)^2

El daño mágico toma:

rnd(MinMATK, MaxMATK)

Luego aplica modificador de ítem, modificador de skill, MDEF, reducción plana por INT/VIT del objetivo y elemento.

DEF física

DEF tiene dos partes:

Tipo	Fuente	Efecto
Hard DEF	equipo/refine	reducción porcentual
Soft DEF	VIT	reducción plana

Flujo:

Daño después de Hard DEF = Daño * (1 - DEF / 100)
Daño después de Soft DEF = resultado - SoftDEF

Un crítico cuenta Hard DEF como 0 y Soft/VIT DEF como 0.

MDEF

MDEF funciona de manera análoga para magia:

Daño mágico parcial = Total * (1 - MDEF / 100)
Daño mágico parcial = resultado - INT - VIT/2

Fórmula completa por hit mágico:

{ rnd(MinMATK, MaxMATK)
  * ItemModifier
  * SkillModifier
  * (1 - MDEF / 100)
  - INT
  - VIT / 2
}
* ElementalModifier

La parte entre llaves se redondea y se normaliza según reglas del algoritmo.

HIT y FLEE
HIT jugador = BaseLevel + DEX + bonos
FLEE jugador = BaseLevel + AGI + bonos

Check básico:

Hit chance (%) = 80 + AttackerHIT - DefenderFLEE
Dodge chance (%) = 100 - Hit chance

La evasión normal tiene cap de 95% fuera de ciertas condiciones; iRO Wiki Classic indica que la Dodge Rate no puede bajar de 0% ni subir de 95%, excepto en WoE.

ASPD

ASPD depende de clase, arma, AGI, DEX, potions, skills, equipo y penalizaciones. Fórmula clásica documentada:

ASPD = 200 - (WD - (floor(WD * AGI / 25) + floor(WD * DEX / 100)) / 10) * (1 - SM)
WD = 50 * BTBA

Donde BTBA es Base Time Between Attacks y SM son Speed Modifiers. Las potions de ASPD no se apilan entre sí: se usa la de mayor efecto.

Conversión práctica:

Hits per second = 50 / (200 - ASPD)
Delay entre ataques = (200 - ASPD) / 50

A 190 ASPD:

50 / (200 - 190) = 5 ataques por segundo

4. Resolución de un autoattack físico

Un autoattack físico no es solo “ATK contra DEF”. El servidor resuelve una cadena.

Flujo conceptual
1. ¿Es skill attack?
2. ¿Safety Wall bloquea melee?
3. ¿Pneuma bloquea ranged?
4. ¿Perfect Dodge?
5. ¿Critical?
6. ¿HIT vs FLEE?
7. Calcular BaseATK
8. Calcular WeaponATK
9. Aplicar size modifier al WeaponATK
10. Aplicar skill modifiers
11. Aplicar Hard DEF
12. Restar Soft DEF
13. Aplicar protecciones
14. Añadir Bane/mastery/refine/envenom/etc.
15. Aplicar elemento
16. Aplicar bonuses de daño
17. Aplicar reducciones de daño
18. Multi-hit / Double Attack / Katar / dual wield
19. Kyrie Eleison / herb plant / mínimos
20. Mostrar hit, miss, crit, lucky

iRO Wiki Classic describe este proceso paso a paso y explícitamente separa ataques físicos de ataques mágicos.

5. Melee, ranged, Safety Wall y Pneuma
Clasificación
Ataque	Cómo se trata
Normal melee jugador	Cualquier arma que no sea bow
Normal bow jugador	Ranged
Skill físico ranged	Ranged si el skill está marcado como ranged
Mob melee	Ataque normal con rango menor a 4 celdas
Mob ranged	Ataque normal con rango igual o mayor a 4 celdas

Safety Wall bloquea ataques melee; Pneuma bloquea ataques ranged. Para jugadores, los ataques normales con bow y skills físicos marcados como ranged son bloqueados por Pneuma; ataques normales melee son bloqueados por Safety Wall.

6. Tamaño: Small, Medium, Large

Cada mob tiene size: Small, Medium o Large. Los jugadores adultos cuentan como Medium en PvP; personajes adoptados/baby cuentan como Small.

El modificador de tamaño se aplica al WeaponATK, no necesariamente a todo el daño final.

Arma	Large	Medium	Small
Fist	100%	100%	100%
Dagger	50%	75%	100%
1H Sword	75%	100%	75%
2H Sword	100%	75%	75%
Spear	100%	75%	75%
Spear + Peco	100%	100%	75%
Axe	100%	75%	50%
Mace	100%	100%	75%
Rod	100%	100%	100%
Bow	75%	100%	100%
Katar	75%	100%	75%
Book	50%	100%	100%
Claw	50%	75%	100%
Instrument	75%	100%	75%
Whip	50%	100%	75%
Gun	100%	100%	100%
Huuma Shuriken	100%	100%	100%

Implicación práctica: contra Large, dagger es malo; contra Medium, sword/mace/bow/katar son buenos; contra Small, dagger/bow/claw/book funcionan bien.

7. Elementos y propiedad

Cada jugador y mob tiene una propiedad elemental defensiva. Hay 10 elementos:

Neutral, Water, Earth, Fire, Wind,
Poison, Holy, Shadow, Ghost, Undead

La propiedad del defensor tiene nivel, usualmente 1 a 4. El atacante no tiene “nivel de elemento”; el nivel está en la propiedad defensiva.

Propiedad de ataques normales
Caso	Elemento ofensivo
Mob normal attack	Neutral
Mob skill attack	Depende del skill
Jugador melee normal	Elemento del arma
Jugador bow normal	Elemento de la flecha
Jugador skill físico	Elemento del skill o del arma, según skill
Magia	Elemento fijo del spell

Los ataques normales de mobs son Neutral; skills de mobs pueden tener otras propiedades. Ataques normales melee del jugador usan la propiedad del arma, aunque efectos como Enchant Poison o Aspersio pueden sobrescribirla. Ataques normales con bow usan la propiedad de la flecha, salvo casos de arma elemental o sobrescritura por endow.

Ejemplos importantes de tabla elemental

Contra Ghost 1, Neutral hace 25%. Contra Ghost 2/3/4, Neutral hace 0%. Contra Undead, Holy suele ser muy fuerte; Holy contra Undead 3 o 4 hace 200%. Contra Undead, Poison y Shadow pueden incluso curar o hacer daño negativo según nivel.

8. Arcos y flechas
Qué cambia con bow

Para bow:

El stat principal de daño es DEX, no STR.
El ataque normal es ranged.
El bow usa size modifier de bow: Small 100%, Medium 100%, Large 75%.
El elemento normalmente viene de la flecha.
La flecha aporta ArrowATK al cálculo del WeaponATK.
Pneuma bloquea ataques normales de bow.

La fórmula de BaseATK missile es:

BaseATK missile =
DEX + floor(DEX / 10)^2
+ floor(STR / 5)
+ floor(LUK / 5)
+ UpgradeBonus
+ Impositio Manus
+ ATKCards

Flechas

Las flechas son munición usada por bows y también por skills como Melody Strike, Slinging Arrow y Arrow Vulcan. Se equipan manualmente como munición.

Ejemplos:

Flecha	ATK	Elemento
Arrow	25	Neutral
Iron Arrow	30	Neutral
Steel Arrow	40	Neutral
Oridecon Arrow	50	Neutral
Silver Arrow	30	Holy
Fire Arrow	30	Fire
Crystal Arrow	30	Water
Wind Arrow	30	Wind
Stone Arrow	30	Earth
Immaterial Arrow	30	Ghost
Rusty Arrow	30	Poison
Shadow Arrow	30	Shadow

Orden real para bow
1. Determinar si el ataque es ranged.
2. Verificar Pneuma.
3. Verificar Perfect Dodge del target si es jugador.
4. Verificar crítico.
5. Si no crítico: HIT vs FLEE.
6. Calcular BaseATK missile con DEX.
7. Calcular WeaponATK bow + ArrowATK.
8. Aplicar size modifier de bow.
9. Aplicar skill modifier si es skill.
10. Aplicar DEF/VIT DEF.
11. Aplicar elemento de flecha/skill/endow.
12. Aplicar cards y reducciones.
9. Skills físicos

No todos los skills físicos se comportan igual. Hay que mirar cada skill por separado. Pero el modelo común es:

Daño base físico =
BaseATK + WeaponATK ajustado por size

Luego:
* SkillModifier
* DEF
* SoftDEF
* Element
* Damage bonus cards
* Damage reductions
* Multi-hit

El algoritmo clásico indica que los skill modifiers se aplican al total de BaseATK + WeaponATK, y que modificadores como Power Thrust, Provoke y el modificador propio del skill se suman dentro del multiplicador de skill:

SkillModifiers =
(100 + PowerThrust% + Provoke% + AttackSkill%) / 100

Casos especiales
Tipo de skill	Comportamiento
Autohit	Ignora HIT/FLEE normal
Bonus HIT	Usa HIT aumentado
Forced element	Ignora elemento del arma/flecha y fuerza uno propio
Weapon element	Usa elemento del arma o flecha
Ranged physical	Interactúa con Pneuma
Melee physical	Interactúa con Safety Wall
Multi-hit	Divide o muestra golpes múltiples según lógica del skill
Ignore DEF	Algunos skills ignoran DEF total o parcial

Ejemplo documentado: Magnum Break fuerza Fire; Cart Revolution es raro porque aplica modificador elemental dos veces, una como Neutral y otra como elemento del arma.

10. Skills mágicos

La magia no usa HIT/FLEE normal. Si el spell se castea exitosamente, no se evade con FLEE. Lo que importa es:

Cast time
Interrupción
Rango / línea de visión
Targets válidos
MATK
Item modifier
Skill modifier
MDEF
INT/VIT del defensor
Elemento

iRO Wiki Classic indica que ataques mágicos exitosamente casteados no requieren check de hit, aunque sí tienen reglas de casteo e interrupción.

Cast
ActualCastTime =
BaseCastTime * EquipmentMods * max(1 - DEX / 150, 0)

Si recibes al menos 1 punto de daño durante el cast, el spell normalmente se interrumpe. Poison y bleeding no cuentan como ese daño de interrupción. Phen Card y Bloody Butterfly vuelven el cast no interrumpible, salvo Spell Breaker.

Daño mágico

Por hit mágico:

MagicDamage =
{
  rnd(MinMATK, MaxMATK)
  * ItemModifier
  * SkillModifier
  * (1 - MDEF / 100)
  - TargetINT
  - TargetVIT / 2
}
* ElementalModifier

Después se aplican reglas de mínimo, miss visual si queda en 0 o menos, multi-hit y casos especiales como herb plants.

Qué significa esto
DEX no aumenta daño mágico; reduce cast.
INT aumenta MATK y reduce daño mágico recibido.
MDEF reduce porcentaje.
INT + VIT/2 del objetivo reducen plano.
El elemento del spell es fundamental: usar Fire contra Fire 2 puede hacer 0%, mientras Fire contra Earth 3 puede hacer 200%.
11. Damage bonus: cards, race, size, element

En Pre-Renewal, los bonus de daño no se apilan todos igual.

Categorías comunes:

Size bonus
Race bonus
Element/property bonus
Special bonus

Los bonos del mismo tipo se suman; bonos de tipos distintos se multiplican:

DamageBonusMultiplier =
(1 + SizeBonusTotal / 100)
* (1 + RaceBonusTotal / 100)
* (1 + ElementBonusTotal / 100)
* (1 + SpecialBonusTotal / 100)

Ejemplo:

Opción A:
+20% Race +20% Race +20% Race
= 1.60x

Opción B:
+20% Race +20% Size +20% Element
= 1.20 * 1.20 * 1.20
= 1.728x

Por eso en Pre-Renewal las armas “bien cardeadas” mezclan categorías cuando el objetivo está bien definido.

12. Damage reductions

La lógica defensiva es parecida:

DamageReductionMultiplier =
(1 - SizeReductionTotal / 100)
* (1 - RaceReductionTotal / 100)
* (1 - ElementReductionTotal / 100)
* SpecialReduction

La fuente clásica indica que reducciones por Size, Race, Element y Special se suman dentro del mismo tipo y se multiplican entre tipos. Energy Coat aparece como reducción especial.

Ejemplo conceptual:

Daño recibido = 10000

Thara Frog 30% vs Demi-Human:
10000 * 0.70 = 7000

Raydric 20% Neutral:
7000 * 0.80 = 5600

Esto no equivale a “50% reducción total”; es multiplicativo por categoría.

13. PvM, PvP y WoE
PvM
Los mobs tienen race/size/element fijos.
FLEE es muy potente contra pocos mobs, pero se degrada con mobbing.
DEF/VIT es más estable contra muchos hits.
Elemento correcto puede duplicar daño.
Bow y magia brillan cuando explotan propiedad elemental o rango.
Pneuma/Safety Wall pueden anular paquetes enteros de daño dependiendo de tipo de ataque.
PvP
Jugadores adultos son Medium.
La mayoría de jugadores son Demi-Human.
Elemento defensivo depende de armor.
FLEE puede ser contrarrestado por CRIT, autohit o skills.
Perfect Dodge sí funciona contra críticos.
La LUK del objetivo reduce tu CRIT real.
WoE
Hay reglas especiales del mapa.
FLEE puede comportarse distinto por la excepción mencionada en iRO Wiki Classic.
Reducciones por raza/elemento y equipo defensivo son decisivas.
Cast, after-cast delay, Pneuma, Safety Wall, Land Protector, estados y resistencias importan más que DPS de autoattack puro.
14. Resumen operativo
Ataque físico de jugador contra mob
Input:
Jugador:
  STR/DEX/LUK, arma, refine, cartas, buffs, elemento, skill
Mob:
  FLEE, DEF, VIT, race, size, element, reductions

Proceso:
  1. Safety Wall / Pneuma si aplica
  2. Crit o HIT vs FLEE
  3. BaseATK según melee/ranged
  4. WeaponATK según arma
  5. Size modifier
  6. Skill modifier
  7. DEF porcentual
  8. SoftDEF por VIT
  9. Refine/mastery/bane/etc.
  10. Elemento
  11. Cards bonus
  12. Reducciones
  13. Multi-hit / mínimos
Ataque físico de mob contra jugador
Input:
Mob:
  MinATK/MaxATK, DEX, level, range, skill, element
Jugador:
  FLEE, DEF, VIT, reductions, armor element

Proceso:
  1. Safety Wall / Pneuma si aplica
  2. Perfect Dodge del jugador
  3. Critical solo si mob usa skill crítica
  4. HIT mob = BaseLevel + DEX
  5. WeaponATK = rnd(MinATK, MaxATK)
  6. DEF / VIT DEF del jugador
  7. Elemento: normal mob = Neutral; skill mob puede variar
  8. Reducciones defensivas
Ataque mágico
Input:
Caster:
  INT, MATK, item modifier, skill modifier, DEX para cast
Target:
  MDEF, INT, VIT, element

Proceso:
  1. Calcular cast
  2. Verificar interrupción
  3. Verificar rango/target válido
  4. rnd(MinMATK, MaxMATK)
  5. Item modifier
  6. Skill modifier
  7. MDEF porcentual
  8. INT + VIT/2 del objetivo
  9. Elemental modifier
15. Reglas que no debes asumir mal
STR no es el stat principal de bow. Para bow, el daño principal viene de DEX.
Mobs no usan la misma fórmula de BaseATK que jugadores. Su daño físico normal viene de MinATK/MaxATK.
FLEE no sirve contra magia casteada exitosamente. Sirve contra ataques normales y muchos skills físicos, no contra la mayoría de ataques mágicos.
CRIT no es el número exacto de la ventana. El LUK del objetivo reduce tu crítico real.
El size modifier afecta WeaponATK, no todo el daño de forma ingenua.
Elemento se aplica tarde en el flujo físico, después de DEF/VIT DEF y otros agregados específicos.
Cards del mismo tipo se suman; categorías distintas se multiplican.
Pneuma y Safety Wall son checks estructurales, no simples reducciones de daño.
Cada skill puede romper reglas generales. Algunos fuerzan elemento, otros autohit, otros ignoran DEF, otros son ranged aunque parezcan melee.
Pre-Renewal está lleno de redondeos. floor, rnd, mínimos de 1 daño y multi-hit pueden cambiar resultados visibles.
