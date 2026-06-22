Swordman Pre-Renewal — skills sin quest skills

Excluyo explícitamente:

SM_MOVINGRECOVERY  = HP Recovery While Moving
SM_FATALBLOW       = Fatal Blow
SM_AUTOBERSERK     = Auto Berserk / Berserk

Incluyo solo el árbol normal de Swordman:

SM_SWORD     Sword Mastery
SM_TWOHAND   Two-Handed Sword Mastery
SM_RECOVERY  Increase HP Recovery
SM_BASH      Bash
SM_PROVOKE   Provoke
SM_MAGNUM    Magnum Break
SM_ENDURE    Endure
1. Base común: cómo entra una skill física

En Pre-Renewal, una skill física no reemplaza todo el cálculo de daño. Entra como SkillModifier sobre el paquete físico calculado desde BaseATK + WeaponATK. iRO Wiki Classic describe que los modificadores de skill se aplican al total de BaseATK + WeaponATK, y que se calculan como:

SkillModifier = (100 + PowerThrust% + Provoke% + AttackSkill%) / 100

Luego vienen DEF, VIT DEF, masteries, elemento, bonus de cartas y reducciones.

Modelo simplificado para skills Swordman:

RawPhysical =
BaseATK + WeaponATK * SizeModifier

AfterSkill =
RawPhysical * SkillModifier

AfterDEF =
AfterSkill * (1 - HardDEF / 100) - SoftDEF

AfterMastery =
AfterDEF + WeaponMasteryBonus

Final =
AfterMastery
* ElementModifier
* DamageBonusModifiers
* DamageReductionModifiers

En esta etapa, Sword Mastery y Two-Handed Sword Mastery son especiales: se agregan tarde, después de DEF/VIT DEF, por eso “bypassean” defensa, pero todavía pueden ser afectados por elemento, cartas y reducciones.

2. Sword Mastery — SM_SWORD
Tipo
Pasiva
MaxLv: 10
Armas válidas: Dagger, One-Handed Sword
Fórmula
MasteryBonus = 4 * SkillLv
Lv	Bonus
1	+4
2	+8
3	+12
4	+16
5	+20
6	+24
7	+28
8	+32
9	+36
10	+40

RateMyServer lo define como 4 * SkillLv con daggers y one-handed swords; el bonus ignora Armor DEF y VIT DEF, pero no elemento ni modificadores de cartas, y aplica a todos los hits de ataques multi-hit.

Integración en combate

No lo metas dentro de BaseATK.

Correcto:

DamageAfterDEF = ...
DamageWithMastery = DamageAfterDEF + SwordMasteryBonus

Incorrecto:

BaseATK += SwordMasteryBonus

La diferencia importa porque si lo sumas antes de DEF, el bonus sería reducido por DEF, lo cual no corresponde al comportamiento Pre-Renewal documentado.

3. Two-Handed Sword Mastery — SM_TWOHAND
Tipo
Pasiva
MaxLv: 10
Arma válida: Two-Handed Sword
Prereq: Sword Mastery Lv 1
Fórmula
MasteryBonus = 4 * SkillLv
Lv	Bonus
1	+4
2	+8
3	+12
4	+16
5	+20
6	+24
7	+28
8	+32
9	+36
10	+40

Funciona igual que Sword Mastery, pero solo con Two-Handed Sword. El bonus ignora Armor DEF y VIT DEF, pero sigue siendo afectado por elemento y cartas.

Nota de implementación
if weapon_type == TWO_HANDED_SWORD:
    mastery_bonus = 4 * SM_TWOHAND_Lv
else:
    mastery_bonus = 0

No debería aplicar a spear, dagger, one-handed sword, mace, axe ni bow.

4. Increase HP Recovery — SM_RECOVERY
Tipo
Pasiva
MaxLv: 10
Regeneración cada 10 segundos
HPRecovered =
(5 * SkillLv) + (MaxHP * 0.002 * SkillLv)

Equivalente:

HPRecovered =
5 * SkillLv + MaxHP * (0.2% * SkillLv)
Lv	Fórmula
1	5 + 0.2% MaxHP
2	10 + 0.4% MaxHP
3	15 + 0.6% MaxHP
4	20 + 0.8% MaxHP
5	25 + 1.0% MaxHP
6	30 + 1.2% MaxHP
7	35 + 1.4% MaxHP
8	40 + 1.6% MaxHP
9	45 + 1.8% MaxHP
10	50 + 2.0% MaxHP

RateMyServer y iRO Wiki Classic coinciden en que la recuperación ocurre cada 10 segundos mientras el personaje permanece en una celda sin moverse manualmente. También aumenta la eficiencia de healing items en 10% * SkillLv, acumulándose con el bono de VIT.

Bonus a ítems de curación
RecoverySkillItemBonus = 10% * SkillLv

La parte de VIT, según iRO Wiki Classic, es:

VITItemBonus = 2% * VIT

Por tanto, una forma práctica de modelarlo:

FinalHealing =
BaseItemHealing * (1 + 0.02 * VIT + 0.10 * SkillLv)

iRO Wiki Classic indica que cada VIT da +2% a healing items, y que el bonus de Increase HP Recovery es acumulativo con VIT.

Condiciones

No recupera HP si la regeneración normal no está permitida, por ejemplo si el personaje está overweight.

5. Bash — SM_BASH
Tipo
Ofensiva física
MaxLv: 10
Target: enemigo
Rango: melee
Elemento: propiedad del arma
No funciona con bow
SP: 8 en Lv 1–5, 15 en Lv 6–10
Fórmula de daño

La fórmula común Pre-Renewal / RMS es:

BashSkillRatio = 100 + 30 * SkillLv
BashDamage =
PhysicalDamageUsingWeaponProperty * BashSkillRatio / 100
Lv	ATK
1	130%
2	160%
3	190%
4	220%
5	250%
6	280%
7	310%
8	340%
9	370%
10	400%

RateMyServer Pre-Re documenta Bash como (100 + 30 * SkillLv)% y lo lista con propiedad del arma, rango 1 y restricción de no funcionar con bow.

Bonus de HIT

Modelo común:

HitBonus = 5 * SkillLv
Lv	HIT bonus
1	+5
2	+10
3	+15
4	+20
5	+25
6	+30
7	+35
8	+40
9	+45
10	+50

RateMyServer lo describe como HIT bonus of 5 * SkillLv. iRO Wiki Classic lo describe de forma algo distinta: como porcentaje añadido a tu hit rate actual; por ejemplo, si tienes 67% de chance de golpear, Bash Lv10 lo llevaría a 67% * 1.5 ≈ 100%. Esta es una de las partes que conviene validar contra el emulador concreto.

Fatal Blow excluido

Como pediste no considerar quest skills, no aplico stun de Fatal Blow. Sin Fatal Blow:

BashStunChance = 0

Aunque muchas tablas mencionan stun en Bash Lv6+, eso depende de la quest skill Fatal Blow, que aquí queda fuera.

Nota de consistencia

iRO Wiki Classic tiene una tabla de Bash que muestra valores intermedios inconsistentes con la fórmula común 100 + 30 * SkillLv para algunos niveles, mientras que RateMyServer Pre-Re muestra la fórmula explícita y la progresión 130/160/190/220/250/280/310/340/370/400. Para implementación real, yo usaría la fórmula común salvo que el servidor objetivo demuestre otra tabla.

6. Provoke — SM_PROVOKE
Tipo
Activa / debuff
MaxLv: 10
Target: enemigo
Range: 9 celdas
Duration: 30 segundos
SP Cost: 3 + SkillLv
No afecta Undead property ni Boss monsters
Fórmulas
ATK aumentado del objetivo
TargetATKBonus% = 2 + 3 * SkillLv
Lv	Target ATK
1	+5%
2	+8%
3	+11%
4	+14%
5	+17%
6	+20%
7	+23%
8	+26%
9	+29%
10	+32%
DEF reducida del objetivo
TargetDEFReduction% = 5 + 5 * SkillLv
Lv	DEF
1	-10%
2	-15%
3	-20%
4	-25%
5	-30%
6	-35%
7	-40%
8	-45%
9	-50%
10	-55%

RateMyServer formula Provoke como reducción de DEF/VIT DEF en (5 + 5 * SkillLv)% y aumento de ATK en (2 + 3 * SkillLv)%; iRO Wiki Classic muestra la misma tabla de ATK/DEF por nivel.

Success chance
SuccessChance% = 50 + 3 * SkillLv
Lv	Success
1	53%
2	56%
3	59%
4	62%
5	65%
6	68%
7	71%
8	74%
9	77%
10	80%

iRO Wiki Classic muestra 53% a 80% de chance de éxito por nivel.

Diferencia jugador vs mob

Contra mobs:

Reduce Hard DEF / physical DEF del mob
Aumenta ATK del mob

Contra jugadores:

Solo reduce VIT DEF / soft DEF
Aumenta ATK del jugador provocado

iRO Wiki Classic especifica que contra jugadores solo se reduce su VIT defense, mientras que Undead y Boss monsters no pueden ser provocados.

Integración con daño físico

Si el atacante está bajo Provoke, su bono de ATK funciona como modificador dentro del bloque de SkillModifier:

SkillModifier =
(100 + ProvokeATKBonus% + AttackSkill%) / 100

Por ejemplo, un Bash Lv10 contra un atacante provocado con Provoke Lv10, si el usuario es el provocado:

SkillModifier =
(100 + 32 + 300) / 100
= 4.32x

Esto ilustra por qué Provoke es peligroso en PvP: reduces algo de defensa, pero puedes regalar un multiplicador ofensivo alto.

7. Magnum Break — SM_MAGNUM
Tipo
Ofensiva física + self-buff
MaxLv: 10
Target: self
AoE: 5x5 alrededor del caster
Elemento: Fire forzado
Knockback: 2 celdas
SP Cost: 30
Cast Time: ninguno
Cast Delay: 2 segundos en Classic
Prereq: Bash Lv 5

iRO Wiki Classic lo define como AoE Fire 5x5, knockback de 2 celdas, SP fijo 30, sin cast time y con cast delay de 2 segundos.

Fórmula de daño
MagnumBreakSkillRatio = 100 + 20 * SkillLv
MagnumBreakDamage =
PhysicalDamageWithForcedFire * MagnumBreakSkillRatio / 100
Lv	ATK	HIT bonus	HP cost
1	120%	+10	20
2	140%	+20	20
3	160%	+30	19
4	180%	+40	19
5	200%	+50	18
6	220%	+60	18
7	240%	+70	17
8	260%	+80	17
9	280%	+90	16
10	300%	+100	16

RateMyServer Pre-Re da la fórmula 100 + 20 * SkillLv, HIT +10 * SkillLv, AoE 5x5, Fire property y knockback de 2 celdas. iRO Wiki Classic coincide en la tabla de ATK, accuracy, HP cost, área y propiedad Fire.

Bonus de HIT
HitBonus = 10 * SkillLv

A Lv10:

HitBonus = +100
Buff posterior de Magnum Break

Después de usar Magnum Break:

Duration = 10 segundos
ExtraDamage = 20% Fire-property damage

Punto importante: no convierte tu arma a Fire. Agrega un componente adicional de daño Fire de 20% encima de los ataques normales. iRO Wiki Classic advierte explícitamente que es un error común creer que Magnum Break endowa el arma; en realidad añade daño extra Fire por 10 segundos.

Modelo conceptual:

NormalAttackDamage =
DamageUsingWeaponElement

MagnumBreakBonus =
NormalAttackBaseRelevantPart * 20% * FireElementModifier

FinalAttackDamage =
NormalAttackDamage + MagnumBreakBonus
Defensa y elemento del bonus Fire

iRO Wiki Classic indica que el daño Fire añadido por Magnum Break ignora defensa, y que también se calcula dentro de cada hit del propio Magnum Break. A Lv10, el resultado efectivo se explica como 250% + (20% * 250%) = 300%, donde 1/6 del daño es Fire y bypassea defensa.

Esto implica que, para una implementación fiel, Magnum Break no debería tratarse simplemente como “300% Fire normal”. Tiene un componente especial de 20% Fire que se comporta distinto frente a DEF.

WoE

En War of Emperium no hay push-back de Magnum Break según iRO Wiki Classic.

8. Endure — SM_ENDURE
Tipo
Activa / self-buff
MaxLv: 10
Target: self
SP Cost: 10
Prereq: Provoke Lv 5
Cooldown: 10 segundos
Fórmula de duración
DurationSeconds = 7 + 3 * SkillLv
Lv	Duration	MDEF
1	10s	+1
2	13s	+2
3	16s	+3
4	19s	+4
5	22s	+5
6	25s	+6
7	28s	+7
8	31s	+8
9	34s	+9
10	37s	+10

iRO Wiki Classic formula la duración como (7 + Skill Level * 3) segundos y lista MDEF +SkillLv. RateMyServer también documenta el límite de 7 hits de monstruos, cooldown de 10 segundos y que en WoE el efecto de endure no funciona salvo el bonus de MDEF.

Efecto

Endure elimina el “flinch” o hit-stun al recibir daño físico, permitiendo seguir moviéndose o atacando sin ser detenido por golpes normales. No reduce el daño recibido.

Modelo:

OnReceivePhysicalHit:
    take_full_damage()
    if EndureActive:
        ignore_flinch_delay()
Límite de hits
MonsterHitLimit = 7

Después de 7 golpes de monstruo, el estado se cancela. RateMyServer especifica que no hay máximo para golpes de jugadores, pero sí 7 hits de monstruos.

WoE
InWoE:
    anti_flinch_effect = disabled
    MDEF bonus = still active

RateMyServer indica que Endure no funciona en War of Emperium, salvo por el bonus de MDEF.

9. Resumen de fórmulas
Skill	Fórmula principal
Sword Mastery	+4 * Lv daño con dagger/1H sword
Two-Handed Sword Mastery	+4 * Lv daño con 2H sword
Increase HP Recovery	5*Lv + MaxHP*0.002*Lv HP cada 10s
Healing item bonus por SM_RECOVERY	+10% * Lv
Bash	(100 + 30*Lv)% ATK
Bash HIT	+5*Lv o modelo porcentual según servidor
Provoke ATK bonus	2 + 3*Lv %
Provoke DEF reduction	5 + 5*Lv %
Provoke success	50 + 3*Lv %
Magnum Break	(100 + 20*Lv)% ATK, Fire forced
Magnum Break HIT	+10*Lv
Magnum Break buff	+20% Fire-property damage, 10s
Endure duration	7 + 3*Lv seconds
Endure MDEF	+Lv