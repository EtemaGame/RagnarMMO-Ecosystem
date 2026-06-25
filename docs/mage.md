Mage Pre-Renewal — solo primera clase, sin Skill Quest

Excluyo explícitamente la quest skill:

MG_ENERGYCOAT = Energy Coat

Energy Coat se obtiene por quest de Mage, no por árbol normal de skills; por eso queda fuera. iRO Wiki lista Energy Coat como quest skill de Mage.

Incluyo únicamente skills normales de Mage primera clase:

MG_SRECOVERY     Increase SP Recovery
MG_SIGHT         Sight
MG_NAPALMBEAT    Napalm Beat
MG_SOULSTRIKE    Soul Strike
MG_SAFETYWALL    Safety Wall
MG_COLDBOLT      Cold Bolt
MG_FROSTDIVER    Frost Diver
MG_STONECURSE    Stone Curse
MG_FIREBOLT      Fire Bolt
MG_FIREBALL      Fire Ball
MG_FIREWALL      Fire Wall
MG_LIGHTNINGBOLT Lightning Bolt
MG_THUNDERSTORM  Thunder Storm

iRO Wiki Classic lista estas skills como el set base de Mage: bolts de Fire/Cold/Lightning, Fire Ball, Fire Wall, Frost Diver, Increase SP Recovery, Napalm Beat, Safety Wall, Sight, Soul Strike, Stone Curse y Thunder Storm.

1. Base común: cómo funciona la magia Pre-Renewal

En Pre-Renewal, las skills mágicas:

no usan HIT
no usan FLEE
no pueden crittear
no usan ATK físico
no usan DEF física
usan MATK, MDEF, INT/VIT del objetivo y elemento

Si el spell termina de castear correctamente, el target no “esquiva” con FLEE. El algoritmo clásico de iRO Wiki dice explícitamente que los Magic Attack Skills casteados exitosamente no requieren check de hit.

MATK Pre-Renewal
MinMATK = INT + floor(INT / 7)^2
MaxMATK = INT + floor(INT / 5)^2

iRO Wiki Classic documenta ese rango de MATK derivado de INT.

Daño mágico por hit

Modelo clásico:

MagicDamagePerHit =
{
  rnd(MinMATK, MaxMATK)
  * ItemModifier
  * SkillModifier
  * (1 - TargetMDEF / 100)
  - TargetINT
  - TargetVIT / 2
}
* ElementModifier

iRO Wiki Classic describe el flujo como: calcular rnd(minMATK,maxMATK), aplicar modificador de item, modificador de skill, MDEF, restar INT y VIT/2 del objetivo, y luego aplicar modificador elemental.

Cast time Pre-Renewal
ActualCastTime =
BaseCastTime * EquipmentMods * max(1 - DEX / 150, 0)

Con 150 DEX efectiva, el cast variable clásico llega a 0. Phen Card, Bloody Butterfly y efectos similares modifican interrupción o cast según el caso. iRO Wiki Classic da esa fórmula para magic attacks.

2. Increase SP Recovery — MG_SRECOVERY
Tipo
Pasiva
MaxLv: 10
Fórmula Pre-Renewal
SPRecoveredEvery10Sec =
3 * SkillLv + MaxSP * (0.002 * SkillLv)

Equivalente:

SPRecoveredEvery10Sec =
3 * SkillLv + MaxSP * (0.2% * SkillLv)
Lv	Recuperación
1	+3 + 0.2% MaxSP cada 10s
2	+6 + 0.4% MaxSP cada 10s
3	+9 + 0.6% MaxSP cada 10s
4	+12 + 0.8% MaxSP cada 10s
5	+15 + 1.0% MaxSP cada 10s
6	+18 + 1.2% MaxSP cada 10s
7	+21 + 1.4% MaxSP cada 10s
8	+24 + 1.6% MaxSP cada 10s
9	+27 + 1.8% MaxSP cada 10s
10	+30 + 2.0% MaxSP cada 10s

RateMyServer lista la progresión Pre-Re como +3SP/10sec hasta +30SP/10sec, más 0.2% a 2.0% MaxSP; también indica que mejora la eficiencia de ítems de SP de +2% a +20%.

Bonus a ítems de SP
SPItemBonus = 2% * SkillLv

Modelo:

FinalSPRecoveryItem =
BaseSPRecoveryItem * (1 + 0.02 * SkillLv + INTItemBonus)

Para Mage, esta skill es relevante porque el cuello de botella temprano no suele ser daño, sino SP sustain.

3. Sight — MG_SIGHT
Tipo
Activa / detección
MaxLv: 1
Target: self
Área: 7x7 alrededor del caster
Duración: 10s
SP: 10
Elemento visual/asociado: Fire
Efecto
Revela enemigos ocultos en 7x7 alrededor del caster.

Detecta estados como:

Hiding
Cloaking
otros estados hidden según servidor

RateMyServer lista Sight como skill activa de Mage, nivel máximo 1, área 7x7, duración 10 segundos y coste 10 SP.

Fórmula

No tiene fórmula de daño. Es una skill de detección.

void cast_sight(Character& caster) {
    consume_sp(caster, 10);
    caster.add_status(SIGHT, duration = 10);

    for (Target& t : targets_in_area(caster.cell, area = 7x7)) {
        if (t.is_hidden()) {
            reveal(t);
        }
    }
}
Requisito importante

Sight es prerequisito de Fire Wall Lv1. RateMyServer lista Fire Wall con requisito Fire Ball Lv5 y Sight Lv1.

4. Napalm Beat — MG_NAPALMBEAT
Tipo
Ofensiva mágica
MaxLv: 10
Target: enemy
Range: 9 celdas
Área: 3x3
Elemento: Ghost
Fórmula Pre-Renewal
NapalmBeatRatio% = 70 + 10 * SkillLv
Lv	MATK
1	80%
2	90%
3	100%
4	110%
5	120%
6	130%
7	140%
8	150%
9	160%
10	170%

RateMyServer lista Napalm Beat como magia Ghost de área 3x3, rango 9, con daño de 80% a 170% MATK.

Daño
NapalmBeatDamage =
MagicDamagePerHit(
  SkillModifier = (70 + 10 * SkillLv) / 100,
  Element = Ghost
)
Particularidad: daño repartido

Napalm Beat golpea en área 3x3, pero el daño se dispersa si hay varios objetivos. RateMyServer indica que “the more targets, the more scattered the damage”.

Modelo conceptual:

TotalNapalmDamagePool = MATKDamage * SkillRatio

if targets_hit > 1:
    DamagePerTarget ≈ TotalNapalmDamagePool / targets_hit
else:
    DamagePerTarget = TotalNapalmDamagePool

No todos los emuladores implementan esta dispersión exactamente igual; para código real, validaría en skill.c.

Requisitos
Soul Strike requiere Napalm Beat Lv4
Safety Wall requiere Napalm Beat Lv7

RateMyServer lista Soul Strike con requisito Napalm Beat Lv4 y Safety Wall con requisito Napalm Beat Lv7.

5. Soul Strike — MG_SOULSTRIKE
Tipo
Ofensiva mágica
MaxLv: 10
Target: enemy
Range: 9 celdas
Elemento: Ghost
Prereq: Napalm Beat Lv4
Hits

La fórmula práctica:

SoulStrikeHits = ceil(SkillLv / 2)
Lv	Hits	Nota
1	1	normal
2	1	fast cast
3	2	normal
4	2	fast cast
5	3	normal
6	3	fast cast
7	4	normal
8	4	fast cast
9	5	normal
10	5	fast cast

RateMyServer Pre-Re describe Soul Strike como (1 + SkillLv/2) bolts de 100% MATK Ghost, y su tabla muestra 1, 1, 2, 2, 3, 3, 4, 4, 5, 5 spirits.

Daño

Cada hit:

SoulStrikeHitDamage =
MagicDamagePerHit(
  SkillModifier = 1.00,
  Element = Ghost
)

Total:

SoulStrikeTotalDamage =
SoulStrikeHitDamage * ceil(SkillLv / 2)
Bonus contra Undead property
UndeadBonus% = 5 * SkillLv
Lv	Bonus vs Undead
1	+5%
2	+10%
3	+15%
4	+20%
5	+25%
6	+30%
7	+35%
8	+40%
9	+45%
10	+50%

RateMyServer lista explícitamente el bonus adicional contra Undead property de +5% a +50%.

SP
Lv	SP
1	18
2	14
3	24
4	20
5	30
6	26
7	36
8	32
9	42
10	38

Los niveles pares tienen “fast cast” y menor coste de SP que el impar anterior.

6. Safety Wall — MG_SAFETYWALL
Tipo
Activa / ground support
MaxLv: 10
Target: ground
Range: 9 celdas
Catalyst: Blue Gemstone x1
Prereq: Napalm Beat Lv7 + Soul Strike Lv5

RateMyServer lista Safety Wall como skill activa de terreno, rango 9, requiere Blue Gemstone, Napalm Beat Lv7 y Soul Strike Lv5.

Efecto
Bloquea ataques físicos melee contra quien esté sobre la celda protegida.

No bloquea:

magia
ataques físicos ranged
skills que ignoren Safety Wall
daño indirecto especial según servidor
Hits protegidos
ProtectedHits = SkillLv + 1
Lv	Hits protegidos
1	2
2	3
3	4
4	5
5	6
6	7
7	8
8	9
9	10
10	11

RateMyServer Pre-Re lista de 2 a 11 protected hits.

Duración
DurationSeconds = 5 * SkillLv
Lv	Duración
1	5s
2	10s
3	15s
4	20s
5	25s
6	30s
7	35s
8	40s
9	45s
10	50s

RateMyServer lista la duración Pre-Re de 5 a 50 segundos.

Cast y SP
Lv	Cast	SP
1	4.0s	30
2	3.5s	30
3	3.5s	30
4	2.5s	35
5	2.0s	35
6	1.5s	35
7	1.0s	40
8	1.0s	40
9	1.0s	40
10	1.0s	40
Modelo de combate
if (target.cell.has_safety_wall()
    && incoming_attack.type == PHYSICAL
    && incoming_attack.range_type == MELEE) {
    safety_wall.remaining_hits -= 1;
    block_damage();
}

Safety Wall es un bloqueo, no una reducción porcentual. En Pre-Renewal es una de las mejores defensas de Mage contra mobs melee.

7. Cold Bolt — MG_COLDBOLT
Tipo
Ofensiva mágica
MaxLv: 10
Target: enemy
Range: 9 celdas
Elemento: Water
Fórmula
ColdBoltHits = SkillLv
SkillModifierPerHit = 100% MATK
Element = Water

Total bruto antes de MDEF/elemento por hit:

ColdBoltTotalRaw =
SkillLv * 100% MATK

RateMyServer Pre-Re indica que Cold Bolt golpea con 1 Water bolt por nivel, cada uno con 1*MATK, y lista SP 12–30.

Tabla
Lv	Hits	SP	Cast	After-cast delay
1	1	12	0.7s	1.0s
2	2	14	1.4s	1.2s
3	3	16	2.1s	1.4s
4	4	18	2.8s	1.6s
5	5	20	3.5s	1.8s
6	6	22	4.2s	2.0s
7	7	24	4.9s	2.2s
8	8	26	5.6s	2.4s
9	9	28	6.3s	2.6s
10	10	30	7.0s	2.8s

La tabla Pre-Re de RateMyServer muestra cast time de 0.7 a 7.0 segundos, delay de 1.0 a 2.8 segundos y SP de 12 a 30.

8. Frost Diver — MG_FROSTDIVER
Tipo
Ofensiva mágica + status
MaxLv: 10
Target: enemy
Range: 9 celdas
Elemento: Water
Status: Frozen
Prereq: Cold Bolt Lv5

RateMyServer lista Frost Diver como magia Water, rango 9, requisito Cold Bolt Lv5.

Daño
FrostDiverRatio% = 100 + 10 * SkillLv
Lv	MATK
1	110%
2	120%
3	130%
4	140%
5	150%
6	160%
7	170%
8	180%
9	190%
10	200%
Chance de Freeze
FreezeChance% = 35 + 3 * SkillLv
Lv	Freeze chance	SP
1	38%	25
2	41%	24
3	44%	23
4	47%	22
5	50%	21
6	53%	20
7	56%	19
8	59%	18
9	62%	17
10	65%	16

RateMyServer Pre-Re da exactamente (100 + 10*SkillLv)% MATK y (35 + 3*SkillLv)% de chance de Frozen; también indica que Undead property y Boss monsters no pueden ser congelados.

Frozen status

Mientras está Frozen:

target cannot move
target cannot attack
target cannot use skills
target element becomes Water 1
status ends on damage or duration expiry

RateMyServer indica que el target congelado queda inmóvil, no puede atacar ni usar skills, cuenta como Water 1 y el estado termina al recibir daño o al expirar.

Duración
DurationSeconds = 3 * SkillLv
Lv	Duración
1	3s
2	6s
3	9s
4	12s
5	15s
6	18s
7	21s
8	24s
9	27s
10	30s

MDEF del target afecta la chance y la duración del status.

9. Stone Curse — MG_STONECURSE
Tipo
Activa mágica / debuff
MaxLv: 10
Target: enemy
Range: 2 celdas
Elemento asociado: Earth
Catalyst: Red Gemstone x1

RateMyServer lista Stone Curse como skill mágica/debuff, rango 2, requiere Red Gemstone y aplica Stone Curse.

Chance de Petrify
StoneCurseChance% = 20 + 4 * SkillLv
Lv	Chance	SP
1	24%	25
2	28%	24
3	32%	23
4	36%	22
5	40%	21
6	44%	20
7	48%	19
8	52%	18
9	56%	17
10	60%	16

RateMyServer Pre-Re muestra la progresión de 24% a 60% y coste de SP de 25 a 16.

Consumo de Red Gemstone
Lv1–5: consume Red Gemstone on cast/use
Lv6–10: consume Red Gemstone only on successful cast

RateMyServer indica que cada cast consume una Red Gemstone y que, desde Lv6, solo se consume si la skill tiene éxito.

Cast y duración
CastTime = 1s Pre-Re
StayDuration = 5s
EffectDuration = 20s

La tabla Pre-Re lista 1s de cast, 5s de stay duration y 20s de effect duration; la sección iRO moderna de la misma página muestra otros valores, por lo que para Pre-Re uso la tabla Pre-Re.

Fórmula real de éxito

La chance base no es el resultado final. La fuente indica que chance y duración son afectadas por:

TargetLevel
TargetMDEF
TargetLUK

No hay una fórmula pública confiable completa en esa entrada; si esto va a emulador, hay que revisar código.

10. Fire Bolt — MG_FIREBOLT
Tipo
Ofensiva mágica
MaxLv: 10
Target: enemy
Range: 9 celdas
Elemento: Fire
Fórmula
FireBoltHits = SkillLv
SkillModifierPerHit = 100% MATK
Element = Fire

RateMyServer Pre-Re dice que Fire Bolt golpea con 1 Fire bolt por nivel, cada uno con 1*MATK.

Tabla
Lv	Hits	SP	Cast	After-cast delay
1	1	12	0.7s	1.0s
2	2	14	1.4s	1.2s
3	3	16	2.1s	1.4s
4	4	18	2.8s	1.6s
5	5	20	3.5s	1.8s
6	6	22	4.2s	2.0s
7	7	24	4.9s	2.2s
8	8	26	5.6s	2.4s
9	9	28	6.3s	2.6s
10	10	30	7.0s	2.8s
Requisito
Fire Ball requiere Fire Bolt Lv4.

RateMyServer lista Fire Ball con requisito Fire Bolt Lv4.

11. Fire Ball — MG_FIREBALL
Tipo
Ofensiva mágica AoE
MaxLv: 10
Target: enemy
Range: 9 celdas
Área: 5x5 alrededor del target
Elemento: Fire
Prereq: Fire Bolt Lv4
Fórmula Pre-Renewal
FireBallRatio% = 70 + 10 * SkillLv
Lv	MATK
1	80%
2	90%
3	100%
4	110%
5	120%
6	130%
7	140%
8	150%
9	160%
10	170%

RateMyServer Pre-Re especifica Fire Ball como daño Fire en área 5x5 con (70 + 10*SkillLv)% MATK.

SP y cast
SPCost = 25
Lv	Cast	Delay
1	1.5s	1.5s
2	1.5s	1.5s
3	1.5s	1.5s
4	1.5s	1.5s
5	1.5s	1.5s
6	1.0s	1.0s
7	1.0s	1.0s
8	1.0s	1.0s
9	1.0s	1.0s
10	1.0s	1.0s

Después de Lv6, la entrada Pre-Re indica reducción de cast y after-cooldown/delay.

Nota importante

La misma página de RateMyServer contiene también una sección iRO/Renewal con valores modernos como 160%/120% center/edge. Para Pre-Renewal, uso la sección Pre-Re: 80%–170% MATK uniforme en 5x5.

12. Fire Wall — MG_FIREWALL
Tipo
Ofensiva mágica / ground control
MaxLv: 10
Target: ground
Range: 9 celdas
Elemento: Fire
Max active: 3
Knockback: 2 celdas
Prereq: Fire Ball Lv5 + Sight Lv1

RateMyServer Pre-Re lista Fire Wall como skill de terreno Fire, máximo 3 activas, knockback 2 celdas, requisito Fire Ball Lv5 y Sight Lv1.

Fórmula Pre-Renewal

Cada celda de Fire Wall golpea:

FireWallHitDamage =
MagicDamagePerHit(
  SkillModifier = 0.50,
  Element = Fire
)

Es decir:

EachHit = 50% MATK Fire

La entrada Pre-Re dice que cada celda entrega hits Fire de MATK * 0.5.

Hits por celda
HitsPerCell = 4 + SkillLv
Lv	Hits por celda	Duración
1	5	5s
2	6	6s
3	7	7s
4	8	8s
5	9	9s
6	10	10s
7	11	11s
8	12	12s
9	13	13s
10	14	14s
Cast
Lv	Cast
1	2.00s
2	1.85s
3	1.70s
4	1.55s
5	1.40s
6	1.25s
7	1.10s
8	0.95s
9	0.80s
10	0.65s
Posicionamiento

Fire Wall crea 3 celdas de wall en línea perpendicular entre caster y celda objetivo. Si se castea diagonal, puede aparecer como dos filas, una de 3 celdas y otra de 2. Solo se colocan celdas con línea de tiro válida.

Modelo de combate
for (Cell& c : firewall_cells) {
    c.status = FIRE_WALL;
    c.remaining_hits = 4 + skill_lv;
    c.duration = 4 + skill_lv; // visualmente 5..14s según nivel
}

on_enter_firewall(Target& t, Cell& c) {
    if (c.remaining_hits <= 0) return;

    apply_magic_damage(
        caster,
        t,
        element = FIRE,
        skill_modifier = 0.50
    );

    knockback(t, 2);
    c.remaining_hits--;
}
13. Lightning Bolt — MG_LIGHTNINGBOLT
Tipo
Ofensiva mágica
MaxLv: 10
Target: enemy
Range: 9 celdas
Elemento: Wind
Fórmula
LightningBoltHits = SkillLv
SkillModifierPerHit = 100% MATK
Element = Wind

RateMyServer Pre-Re indica que Lightning Bolt golpea con 1 Wind bolt por nivel, cada uno con 1*MATK.

Tabla
Lv	Hits	SP	Cast	After-cast delay
1	1	12	0.7s	1.0s
2	2	14	1.4s	1.2s
3	3	16	2.1s	1.4s
4	4	18	2.8s	1.6s
5	5	20	3.5s	1.8s
6	6	22	4.2s	2.0s
7	7	24	4.9s	2.2s
8	8	26	5.6s	2.4s
9	9	28	6.3s	2.6s
10	10	30	7.0s	2.8s

RateMyServer Pre-Re lista esos cast times, delays y costes de SP para Lightning Bolt.

Requisito
Thunder Storm requiere Lightning Bolt Lv4.

RateMyServer lista Thunder Storm con requisito Lightning Bolt Lv4.

14. Thunder Storm — MG_THUNDERSTORM
Tipo
Ofensiva mágica AoE
MaxLv: 10
Target: ground
Range: 9 celdas
Área: 5x5 alrededor de la celda objetivo
Elemento: Wind
Prereq: Lightning Bolt Lv4
Fórmula Pre-Renewal
ThunderStormHits = SkillLv
ThunderStormSkillModifierPerHit = 80% MATK
ThunderStormTotalRaw =
SkillLv * 80% MATK

RateMyServer Pre-Re dice que Thunder Storm golpea enemigos en 5x5 con 1 Wind bolt por nivel, a razón de un bolt cada 0.2s, y cada bolt hace 0.8 * MATK Wind damage.

Tabla
Lv	Hits	SP	Cast
1	1	29	1s
2	2	34	2s
3	3	39	3s
4	4	44	4s
5	5	49	5s
6	6	54	6s
7	7	59	7s
8	8	64	8s
9	9	69	9s
10	10	74	10s
CastDelay = 2s
StayDuration = 0.1s

RateMyServer lista cast delay de 2s, stay duration de 0.1s, cast de 1 a 10s y SP de 29 a 74.

Nota Pre-Re vs iRO/Renewal

La misma fuente contiene una descripción iRO moderna donde cada hit aparece como 100% MATK. Para Pre-Re, uso la sección Pre-Re: 0.8 * MATK por bolt.

15. Orden de resolución de una skill mágica Mage

Para bolts, Soul Strike, Napalm Beat, Fire Ball, Frost Diver y Thunder Storm:

1. Validar SP.
2. Validar target o celda.
3. Validar rango y línea de visión.
4. Calcular cast time:
   BaseCastTime * EquipmentMods * max(1 - DEX/150, 0)
5. Si recibe daño durante cast y no tiene protección anti-interrupt:
   cancelar skill.
6. Al terminar cast:
   calcular rnd(MinMATK, MaxMATK).
7. Aplicar bonus de rod/item.
8. Aplicar SkillModifier.
9. Aplicar MDEF porcentual del target.
10. Restar TargetINT y TargetVIT/2.
11. Aplicar elemento.
12. Aplicar status si corresponde.
13. Mostrar hits.

iRO Wiki Classic describe ese flujo para ataques mágicos, incluyendo cast, interrupción, rango, línea de visión, MDEF, INT/VIT y elemento.

16. Tabla compacta de fórmulas
Skill	Fórmula / efecto Pre-Renewal
Increase SP Recovery	3*Lv + MaxSP*(0.002*Lv) cada 10s
SP item bonus	+2% * Lv
Sight	revela hidden en 7x7 por 10s
Napalm Beat	(70 + 10*Lv)% MATK, Ghost, 3x3
Soul Strike hits	ceil(Lv/2) hits
Soul Strike damage	100% MATK Ghost por hit
Soul Strike vs Undead	+5% * Lv
Safety Wall hits	Lv + 1 hits protegidos
Safety Wall duration	5 * Lv segundos
Cold Bolt	Lv hits de 100% MATK Water
Frost Diver	(100 + 10*Lv)% MATK Water
Frost Diver freeze	35 + 3*Lv %
Stone Curse	20 + 4*Lv % base petrify chance
Fire Bolt	Lv hits de 100% MATK Fire
Fire Ball	(70 + 10*Lv)% MATK Fire, 5x5
Fire Wall	50% MATK Fire por hit/celda
Fire Wall hits/cell	4 + Lv
Lightning Bolt	Lv hits de 100% MATK Wind
Thunder Storm	Lv hits de 80% MATK Wind, 5x5
17. Pseudocódigo limpio
// Mage Pre-Renewal, primera clase solamente.
// Sin quest skill: Energy Coat.
// Sin Wizard/Sage.

int sp_recovery_tick(int lv, int max_sp) {
    return floor(3 * lv + max_sp * 0.002 * lv);
}

double sp_item_multiplier_from_sp_recovery(int lv) {
    return 1.0 + 0.02 * lv;
}

double napalm_beat_ratio(int lv) {
    return (70 + 10 * lv) / 100.0; // 80%..170%
}

int soul_strike_hits(int lv) {
    return (lv + 1) / 2; // ceil(lv/2)
}

double soul_strike_undead_bonus(int lv) {
    return 0.05 * lv; // +5%..+50%
}

int safety_wall_hits(int lv) {
    return lv + 1; // 2..11
}

int safety_wall_duration(int lv) {
    return 5 * lv; // 5..50 sec
}

int bolt_hits(int lv) {
    return lv; // 1..10
}

double bolt_ratio_per_hit() {
    return 1.00; // 100% MATK
}

double frost_diver_ratio(int lv) {
    return (100 + 10 * lv) / 100.0; // 110%..200%
}

int frost_diver_freeze_chance(int lv) {
    return 35 + 3 * lv; // 38..65%
}

int frost_diver_duration(int lv) {
    return 3 * lv; // 3..30 sec, modified by target MDEF/etc.
}

int stone_curse_base_chance(int lv) {
    return 20 + 4 * lv; // 24..60%
}

bool stone_curse_consumes_gem_on_cast(int lv) {
    return lv <= 5;
}

bool stone_curse_consumes_gem_on_success_only(int lv) {
    return lv >= 6;
}

double fire_ball_ratio(int lv) {
    return (70 + 10 * lv) / 100.0; // 80%..170%
}

double fire_wall_ratio_per_hit() {
    return 0.50; // 50% MATK
}

int fire_wall_hits_per_cell(int lv) {
    return 4 + lv; // 5..14
}

int fire_wall_duration(int lv) {
    return 4 + lv; // represented as 5..14 sec in table
}

double thunder_storm_ratio_per_hit_pre_re() {
    return 0.80; // 80% MATK
}

int thunder_storm_hits(int lv) {
    return lv; // 1..10
}

int thunder_storm_sp_cost(int lv) {
    return 24 + 5 * lv; // Lv1=29, Lv10=74
}
18. Validaciones críticas si esto va a emulador
RateMyServer mezcla Pre-Re, Renewal e iRO en la misma página. Para este bloque usé las secciones Pre-Re. No mezcles Fire Ball 80–170% con la tabla moderna center/edge 160–340%.
Thunder Storm Pre-Re: usa 80% MATK por bolt en la sección Pre-Re; otra sección moderna lo muestra como 100% MATK.
Stone Curse: la chance base es 24–60%, pero el resultado real depende de level, MDEF y LUK del objetivo. Hay que revisar código del emulador si necesitas exactitud.
Napalm Beat: el reparto de daño entre múltiples targets debe validarse en implementación real.
Cast time: en Pre-Renewal, DEX reduce cast con max(1 - DEX/150, 0). Renewal usa otro sistema; no lo mezcles