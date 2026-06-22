1. Double Attack — TF_DOUBLE
Tipo
Pasiva
MaxLv: 10
Arma: Dagger
Propiedad: propiedad del arma
Fórmula Pre-Renewal
DoubleAttackChance% = 5 * SkillLv
DoubleAttackHitBonus = SkillLv
Lv	Chance	HIT bonus
1	5%	+1
2	10%	+2
3	15%	+3
4	20%	+4
5	25%	+5
6	30%	+6
7	35%	+7
8	40%	+8
9	45%	+9
10	50%	+10

RateMyServer Pre-Re define Double Attack como chance de doble swing con dagger igual a (5 * SkillLv)%, con +1 HIT por nivel aplicado solo en Double Attack.

Resolución en combate

Double Attack no debe modelarse como FinalDamage * 2. Debe tratarse como dos golpes físicos normales cuando activa.

if weapon == DAGGER and rand(100) < 5 * SkillLv:
    hits = 2
    effective_hit = HIT + SkillLv
else:
    hits = 1
    effective_hit = HIT

Flujo correcto:

1. Verificar arma: Dagger
2. Roll de Double Attack
3. Si activa: HIT += SkillLv
4. Resolver HIT vs FLEE
5. Si acierta: calcular daño normal
6. Aplicar 2 hits si Double Attack activó

Esto importa porque cada hit puede interactuar separadamente con efectos on-hit, absorciones, reducción por golpe, Kyrie, reflect, autocasts o efectos custom del servidor.

2. Improve Dodge — TF_MISS
Tipo
Pasiva
MaxLv: 10
Fórmula para Thief primera clase
FLEEBonus = 3 * SkillLv
Lv	FLEE
1	+3
2	+6
3	+9
4	+12
5	+15
6	+18
7	+21
8	+24
9	+27
10	+30

Para primera clase, Improve Dodge da +3 FLEE por nivel; iRO Wiki muestra +3 a +30 FLEE para 1st class, y RateMyServer Pre-Re lo formula como +3 * SkillLv.

Integración en substats
FLEE =
BaseLevel
+ AGI
+ ImproveDodgeBonus
+ EquipmentBonus
+ BuffBonus

Para Thief, esto hace que AGI sea especialmente eficiente:

AGI:
  +1 FLEE por punto
  +ASPD

Improve Dodge:
  +FLEE plano

La skill no da DEF, no da Perfect Dodge y no reduce daño recibido. Solo aumenta la probabilidad de evadir ataques que pasan por HIT/FLEE.

3. Steal — TF_STEAL
Tipo
Activa
MaxLv: 10
Target: monster
Range: 1 celda
SP: 10
Requisito para Hiding: Steal Lv 5

Steal intenta robar un ítem de la tabla de drops del monstruo; un Steal exitoso no elimina ni reduce los drops que el monstruo dará al morir, y después de un Steal exitoso no se puede volver a robar del mismo monstruo. No funciona contra bosses, targets congelados ni targets con Stone Curse según RateMyServer Pre-Re.

Chance base Pre-Renewal
Lv	Success chance
1	10%
2	16%
3	22%
4	28%
5	34%
6	40%
7	46%
8	52%
9	58%
10	64%

RateMyServer muestra esa tabla para Pre-Re y lista el coste como 10 SP.

Fórmula pública Pre-Re
AdjustedStealChance =
DropRatio * (DEX - MonsterDEX + 10 + 3 * SkillLv) / 100

Donde:

DropRatio   = chance normal de drop del item
DEX         = DEX total del Thief
MonsterDEX  = DEX del mob
SkillLv     = nivel de Steal

La página pública de RateMyServer tiene un carácter incompleto al final de la fórmula, así que para implementación exacta en servidor conviene revisar el código del emulador. La parte estable es: más DEX del caster aumenta chance, más DEX del mob la reduce, y el nivel de Steal aumenta el factor.

Restricciones
No funciona contra players.
No funciona contra boss monsters.
No funciona contra frozen monsters.
No funciona contra stone cursed monsters.
Solo un Steal exitoso por mob.
Solo roba ítems existentes en la drop table del mob.
No afecta el drop final al morir.
Modelo de resolución
1. Validar que el target sea monster.
2. Rechazar boss/frozen/stone cursed.
3. Rechazar si el mob ya fue robado exitosamente.
4. Leer drop table del mob.
5. Calcular adjusted chance por item.
6. Si éxito: entregar item y marcar mob como stolen.
4. Hiding — TF_HIDING
Tipo
Activa / toggle
MaxLv: 10
Target: self
SP inicial: 10
Prereq: Steal Lv 5

Hiding oculta al personaje, puede activarse/desactivarse usando la skill de nuevo, cuesta 10 SP al activarse y drena SP periódicamente mientras está activo. RateMyServer indica que el personaje hidden no puede moverse, atacar ni usar skills normales, y no regenera HP/SP mientras está oculto.

Fórmulas
DurationSeconds = 30 * SkillLv
SPDrainInterval = 4 + SkillLv
SPDrainAmount = 1
InitialSPCost = 10
Lv	Duración	Drain
1	30s	1 SP / 5s
2	60s	1 SP / 6s
3	90s	1 SP / 7s
4	120s	1 SP / 8s
5	150s	1 SP / 9s
6	180s	1 SP / 10s
7	210s	1 SP / 11s
8	240s	1 SP / 12s
9	270s	1 SP / 13s
10	300s	1 SP / 14s

RateMyServer lista exactamente estas duraciones y drenajes de SP.

Counters / detección

Hiding puede ser revelado por:

Sight
Ruwach
Detecting
Attention Concentrate / Improve Concentration

Además, mobs de tipo Insect, Demon y mobs con Boss protocol pueden detectar personajes en Hiding.

Modelo
if caster.has_status(HIDING):
    remove_status(HIDING)
else:
    consume_sp(10)
    apply_status(
        HIDING,
        duration = 30 * SkillLv,
        drain = 1 SP every (4 + SkillLv) seconds
    )

Mientras está en Hiding:

can_move = false
can_attack = false
can_use_normal_skills = false
can_use_items = false
hp_sp_regen = false
5. Envenom — TF_POISON
Tipo
Ofensiva física
MaxLv: 10
Target: enemy
Range: 2 celdas
Elemento: Poison
SP: 12
Prereq para Detoxify: Envenom Lv 3

RateMyServer Pre-Re define Envenom como ataque físico de propiedad Poison, rango 2, que añade 15 * SkillLv de daño a tu daño normal; ese bonus no es modificado por Armor DEF ni VIT DEF, y se inflige incluso si el ataque normal no conecta.

Fórmula de daño
EnvenomFlatBonus = 15 * SkillLv
Lv	Bonus
1	+15
2	+30
3	+45
4	+60
5	+75
6	+90
7	+105
8	+120
9	+135
10	+150

Modelo:

EnvenomDamage =
NormalPhysicalDamageWithPoisonProperty
+ 15 * SkillLv

No lo trates como ATK%. Es un daño adicional plano especial.

Chance de Poison
PoisonChance% = 10 + 4 * SkillLv
Lv	Poison chance
1	14%
2	18%
3	22%
4	26%
5	30%
6	34%
7	38%
8	42%
9	46%
10	50%

La tabla pública muestra +15 a +150 daño y 14% a 50% de chance de Poison.

Estado Poison

En Pre-Re, el estado Poison aplicado por Envenom:

Physical DEF -25%
Pierde 3% del MaxHP cada 3 segundos
No puede bajar al target por debajo de 25% MaxHP
No afecta Undead property
No afecta Boss monsters
Duración Pre-Re RMS: 60s

RateMyServer Pre-Re documenta DEF -25%, drain de 3% MaxHP cada 3 segundos, límite de no bajar de 25% MaxHP, inmunidad de Undead property y Boss monsters, y duración 60s para la sección Pre-Re.

Resolución
1. Validar target en rango 2.
2. Consumir 12 SP.
3. Resolver daño físico Poison.
4. Añadir +15 * SkillLv.
5. Roll Poison: 10 + 4 * SkillLv %.
6. Si target no es Boss ni Undead property: aplicar Poison.
6. Detoxify — TF_DETOXIFY
Tipo
Support / Buff
MaxLv: 1
Target: friend
Range: 9 celdas
SP: 10
Prereq: Envenom Lv 3
Efecto
remove_status(target, POISON)

Detoxify remueve el estado Poison de un target y cuesta 10 SP. RateMyServer lo lista como skill de soporte de nivel 1, rango 9, requisito Envenom Lv3 y efecto de curar/remover Poison.

Limitación

No remueve:

Blind
Silence
Curse
Stun
Sleep
Stone Curse
Confusion

Solo remueve Poison normal.

7. Resumen limpio — solo Thief primera clase
Skill	Fórmula / efecto
Double Attack	Chance = 5 * Lv %
Double Attack HIT	+Lv HIT, solo cuando activa
Double Attack daño	2 golpes normales con dagger
Improve Dodge	FLEE += 3 * Lv
Steal base Pre-Re	Lv1–10: 10%, 16%, 22%, 28%, 34%, 40%, 46%, 52%, 58%, 64%
Steal fórmula pública	DropRatio * (DEX - MonsterDEX + 10 + 3*Lv) / 100
Steal SP	10
Hiding duration	30 * Lv segundos
Hiding drain	1 SP / (4 + Lv) segundos
Hiding initial SP	10
Envenom damage bonus	+15 * Lv
Envenom poison chance	10 + 4 * Lv %
Envenom SP	12
Envenom range	2 celdas
Poison Pre-Re	DEF -25%, 3% MaxHP cada 3s, no baja de 25% HP
Detoxify	remueve Poison
Detoxify SP	10
8. Pseudocódigo limpio
// Thief Pre-Renewal, primera clase solamente.
// Sin quest skills. Sin Assassin/Rogue/Katar.

int double_attack_chance(int lv) {
    return 5 * lv; // 5..50%
}

int double_attack_hit_bonus(int lv) {
    return lv; // +1..+10 HIT
}

int improve_dodge_flee_bonus(int lv) {
    return 3 * lv; // +3..+30 FLEE for first-class Thief
}

double steal_adjusted_chance(
    double drop_ratio,
    int caster_dex,
    int monster_dex,
    int lv
) {
    return drop_ratio * (caster_dex - monster_dex + 10 + 3 * lv) / 100.0;
}

int hiding_duration_seconds(int lv) {
    return 30 * lv;
}

int hiding_sp_drain_interval_seconds(int lv) {
    return 4 + lv; // Lv1=5s, Lv10=14s
}

int envenom_flat_bonus(int lv) {
    return 15 * lv; // +15..+150
}

int envenom_poison_chance(int lv) {
    return 10 + 4 * lv; // 14..50%
}

bool can_be_poisoned_by_envenom(const Target& target) {
    return !target.is_boss && target.element != UNDEAD;
}

void detoxify(Character& target) {
    target.remove_status(POISON);
}

Puntos a validar si esto va a código de servidor: la fórmula exacta de Steal en el emulador objetivo y el orden preciso entre Double Attack, HIT/FLEE y Critical, porque esos detalles suelen depender de implementación.