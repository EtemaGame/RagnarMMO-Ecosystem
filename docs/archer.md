Archer Pre-Renewal — skills sin quest skills

Excluyo las quest skills de Archer:

AC_MAKINGARROW = Arrow Crafting / Making Arrow
AC_CHARGEARROW = Charge Arrow / Arrow Repel

Incluyo solo el set normal:

AC_OWL            Owl's Eye
AC_VULTURE        Vulture's Eye
AC_CONCENTRATION  Improve Concentration / Attention Concentrate
AC_DOUBLE         Double Strafe / Double Strafing
AC_SHOWER         Arrow Shower

Punto crítico: Archer es una clase de daño físico ranged. Sus skills ofensivas normales usan Bow + Arrow, escalan principalmente con DEX, consumen flechas y son bloqueadas por Pneuma si el ataque es ranged physical.

1. Base común: daño físico con bow

Para ataques de arco en Pre-Renewal, el stat principal de daño es DEX, no STR.

BaseATK missile
BaseATK_missile =
DEX + floor(DEX / 10)^2
+ floor(STR / 5)
+ floor(LUK / 5)
+ UpgradeBonus
+ ImpositioManus
+ ATKCards

iRO Wiki Classic define esta fórmula como BaseATK para missile weapons. También especifica que el WeaponATK de bow añade una parte aleatoria de ArrowATK, y que en crítico usa BowATK + ArrowATK.

WeaponATK bow
WeaponATK_bow =
rnd(
  BowATK / 100 * min(BowATK, DEX * (0.8 + 0.2 * WeaponLevel)),
  max(BowATK, BowATK / 100 * min(BowATK, DEX * (0.8 + 0.2 * WeaponLevel)))
)
+ rnd(0, ArrowATK - 1)

En crítico:

WeaponATK_bow_crit = BowATK + ArrowATK

Para skills físicas de Archer, el paquete general entra así:

RawDamage =
BaseATK_missile + WeaponATK_bow * SizeModifier

AfterSkill =
RawDamage * SkillRatio

AfterDEF =
AfterSkill * (1 - HardDEF / 100) - SoftDEF

Final =
AfterDEF
* ElementModifier
* DamageBonusModifiers
* DamageReductionModifiers

El sistema de ataques clásico aplica el modificador de skill al total de BaseATK + WeaponATK, luego Hard DEF, Soft DEF, elemento, bonus y reducciones.

2. Arcos, flechas y elemento
Bow size modifier

Los bows son armas de dos manos; no puedes usar shield con bow. Su modificador por tamaño es:

Target size	Bow damage
Small	100%
Medium	100%
Large	75%

iRO Wiki Classic lista bows como armas two-handed y muestra ese modificador de tamaño.

Flechas

Las flechas son munición usada por bows. Se equipan manualmente y aportan ATK y elemento al ataque. Ejemplos clásicos:

Flecha	ATK	Elemento
Arrow	25	Neutral
Iron Arrow	30	Neutral
Steel Arrow	40	Neutral
Oridecon Arrow	50	Neutral
Fire Arrow	30	Fire
Crystal Arrow	30	Water
Arrow of Wind	30	Wind
Stone Arrow	30	Earth
Silver Arrow	30	Holy
Immaterial Arrow	30	Ghost
Arrow of Shadow	30	Shadow
Rusty Arrow	30	Poison
Sharp Arrow	10	Neutral, +20 CRIT

La lista clásica de flechas muestra ATK, peso y elemento; también indica que Arrow Crafting es un método de obtención, pero esa skill es quest skill y aquí queda excluida.

Elemento ofensivo

Para ataques normales de bow, el elemento ofensivo viene normalmente de la flecha, no del arco. iRO Wiki Classic especifica que los ataques normales con bow usan la propiedad de la flecha, salvo overrides como arco elemental o efectos externos.

Para skills de Archer:

Double Strafe = Weapon Property
Arrow Shower  = Weapon Property

En la práctica, con bow eso significa: propiedad de la flecha equipada, salvo servidor/equipo/efecto que sobrescriba propiedad.

3. Pneuma, Safety Wall y rango

Los ataques de Archer son physical ranged.

if target_cell_has_pneuma
   and incoming_attack.type == PHYSICAL
   and incoming_attack.range_type == RANGED:
       damage = 0

iRO Wiki Classic define como ranged los ataques normales con bow y los skills físicos marcados como ranged; Pneuma bloquea esos ataques. Safety Wall bloquea melee, no bow.

Consecuencia:

Ataque Archer	¿Pneuma lo bloquea?
Autoattack con bow	Sí
Double Strafe	Sí
Arrow Shower	Sí, como ranged physical
Improve Concentration	No hace daño
Owl's Eye / Vulture's Eye	Pasivas
4. Owl's Eye — AC_OWL
Tipo
Pasiva
MaxLv: 10
Prereq: ninguno
Fórmula
DEXBonus = SkillLv
Lv	DEX
1	+1
2	+2
3	+3
4	+4
5	+5
6	+6
7	+7
8	+8
9	+9
10	+10

iRO Wiki Classic y RateMyServer Pre-Re coinciden: Owl's Eye aumenta DEX pasivamente hasta +10.

Impacto real en combate

Owl's Eye no es solo “+10 daño”. Por subir DEX afecta:

HIT += SkillLv

BaseATK_missile aumenta:
DEX + floor(DEX / 10)^2

Cast time de skills con cast variable baja:
ActualCastTime = BaseCastTime * max(1 - DEX / 150, 0)

WeaponATK bow se estabiliza por mayor DEX

Para Archer, esto es un multiplicador indirecto enorme porque DEX participa en daño, HIT y consistencia del bow.

5. Vulture's Eye — AC_VULTURE
Tipo
Pasiva
MaxLv: 10
Afecta: Bow
Prereq: Owl's Eye Lv 3
Fórmula
RangeBonus = SkillLv
HITBonus   = SkillLv
Lv	Range	HIT
1	+1	+1
2	+2	+2
3	+3	+3
4	+4	+4
5	+5	+5
6	+6	+6
7	+7	+7
8	+8	+8
9	+9	+9
10	+10	+10

iRO Wiki Classic lo describe como aumento de attack range y HIT con bow; RateMyServer Pre-Re especifica +1 range y +1 HIT por nivel.

Impacto real
BowAttackRange += AC_VULTURE_Lv
HIT += AC_VULTURE_Lv

Para skills:

Double Strafe range affected by Vulture's Eye
Arrow Shower range affected by Vulture's Eye

RateMyServer Pre-Re marca explícitamente que Double Strafe y Arrow Shower tienen rango afectado por Vulture's Eye.

6. Improve Concentration — AC_CONCENTRATION

También aparece como Attention Concentrate.

Tipo
Activa / self-buff
MaxLv: 10
Target: self
Área de detección: alrededor del caster
Prereq: Vulture's Eye Lv 1
Fórmula Pre-Renewal
StatBonusPercent = 2 + SkillLv

Aplica a DEX y AGI:

DEXBonus = floor(EligibleDEX * (2 + SkillLv) / 100)
AGIBonus = floor(EligibleAGI * (2 + SkillLv) / 100)

RateMyServer Pre-Re especifica que aumenta DEX y AGI por (2 + SkillLv)%, y que solo afecta DEX/AGI provenientes de base stat, job bonus, armor y Owl's Eye; no incluye cartas.

Tabla
Lv	DEX/AGI	Duración	SP
1	+3%	60s	25
2	+4%	80s	30
3	+5%	100s	35
4	+6%	120s	40
5	+7%	140s	45
6	+8%	160s	50
7	+9%	180s	55
8	+10%	200s	60
9	+11%	220s	65
10	+12%	240s	70

RateMyServer lista esos porcentajes, duraciones y SP en la sección Pre-Re/iRO de Improve Concentration.

Detección de hidden/cloak

Al activarse, Improve Concentration revela enemigos hidden/cloaked alrededor del usuario. RateMyServer Pre-Re dice “within a 3 cells range”; su bloque iRO moderno lo expresa como 3x3 alrededor del usuario.

Modelo seguro:

void cast_improve_concentration(Character& c, int lv) {
    int percent = 2 + lv;

    int eligible_dex = c.base_dex + c.job_dex + c.armor_dex + c.owl_eye_bonus;
    int eligible_agi = c.base_agi + c.job_agi + c.armor_agi;

    c.dex_bonus += floor(eligible_dex * percent / 100.0);
    c.agi_bonus += floor(eligible_agi * percent / 100.0);

    c.add_status(CONCENTRATE, duration_seconds = 40 + 20 * lv);

    reveal_hidden_near(c);
}
Impacto en combate

Improve Concentration mejora:

DEX:
  + daño missile
  + HIT
  + menor cast time
  + mejor estabilidad de WeaponATK bow

AGI:
  + FLEE
  + ASPD

No es un multiplicador directo de daño como Double Strafe; es un buff de stats que puede empujar breakpoints de DEX, especialmente floor(DEX / 10)^2.

7. Double Strafe — AC_DOUBLE

También aparece como Double Strafing.

Tipo
Ofensiva física ranged
MaxLv: 10
Target: enemy
Weapon: Bow
Ammunition: Arrow x1
Property: Weapon Property
SP: 12
Prereq: ninguno para Archer

RateMyServer Pre-Re lista Double Strafe como ofensiva física, bow requerido, una flecha consumida, propiedad del arma y rango afectado por Vulture's Eye.

Fórmula Pre-Renewal

Hay dos formas equivalentes de leerlo.

Forma total
DoubleStrafeTotalRatio% = 180 + 20 * SkillLv
Lv	Daño total
1	200%
2	220%
3	240%
4	260%
5	280%
6	300%
7	320%
8	340%
9	360%
10	380%

RateMyServer Pre-Re lo formula como (180 + 20 * SkillLv)%, con una sola flecha consumida.

Forma por hit
DoubleStrafeHitRatio% = 90 + 10 * SkillLv
NumberOfHits = 2

Total = 2 * (90 + 10 * SkillLv)

Pero muchas tablas iRO/RMS modernas lo expresan como:

Lv1  = 100% x2
Lv10 = 190% x2

Eso da el mismo total visible: 200% a 380%. RateMyServer muestra ambas representaciones en la misma entrada, lo que confirma que el resultado total esperado es 200–380%.

Para implementación Pre-Re clásica, yo modelaría:

int double_strafe_total_ratio(int lv) {
    return 180 + 20 * lv; // 200..380
}

O, si tu motor requiere hits separados:

int double_strafe_per_hit_ratio(int lv) {
    return (180 + 20 * lv) / 2; // 100..190 si se divide como tabla moderna
}

int hits = 2;
Rango

iRO Wiki Classic dice que Double Strafe tiene rango máximo siempre 3 celdas mayor que un ataque normal. RateMyServer Pre-Re indica base range 9 y que el rango es afectado por Vulture's Eye.

Modelo práctico:

DoubleStrafeRange = BowBaseRange + VultureEyeLv

Si el servidor usa la convención de skill range:

DoubleStrafeBaseRange = 9
DoubleStrafeEffectiveRange = 9 + VultureEyeLv

Validaría esto contra emulador porque iRO Classic y RMS lo expresan distinto.

Interacciones
Requiere Bow
Consume 1 Arrow
Usa elemento de la flecha / weapon property
Es ranged physical
Pneuma lo bloquea
Safety Wall no lo bloquea
Usa HIT/FLEE salvo configuración/skill flag especial
No es crítico normal

Si usas Double Strafe con elemento incorrecto que da 0%, iRO Wiki Classic indica que puede consumir SP y una flecha sin mostrar animación.

8. Arrow Shower — AC_SHOWER
Tipo
Ofensiva física ranged AoE
MaxLv: 10
Target: ground / target cell
Weapon: Bow
Ammunition: Arrow x1
Property: Weapon Property
Knockback: 2 cells
SP: 15
Prereq: Double Strafe Lv 5
Fórmula Pre-Renewal clásica
ArrowShowerRatio% = 75 + 5 * SkillLv
Lv	Damage
1	80%
2	85%
3	90%
4	95%
5	100%
6	105%
7	110%
8	115%
9	120%
10	125%

iRO Wiki Classic lista Arrow Shower con 80% a 125% de daño, y RateMyServer Pre-Re da la fórmula (75 + 5 * SkillLv)%.

Área

Para Pre-Renewal clásico, uso:

AoE = 3x3

La entrada Pre-Re de RMS dice “3x3 cells, ranged splash attack”. En cambio, secciones iRO/Renewal modernas muestran 160–250% y área 3x3/5x5 según nivel. No mezcles esas tablas si el objetivo es Pre-Renewal.

Knockback
Knockback = 2 cells

RateMyServer Pre-Re e iRO moderno coinciden en knockback de 2 celdas; en WoE normalmente los efectos de knockback pueden estar deshabilitados por reglas de mapa, así que eso depende del servidor.

Interacciones especiales

RateMyServer indica:

Skill ignores Land Protector
Skill range affected by Vulture's Eye
Skill can affect or target traps

Modelo de combate
for (Target* t : targets_in_3x3(target_cell)) {
    if (!can_be_hit_by_ranged_physical(t))
        continue;

    if (t->cell.has_pneuma())
        continue;

    int ratio = 75 + 5 * skill_lv; // 80..125
    int dmg = physical_bow_skill_damage(caster, t, ratio, arrow_element);

    apply_damage(t, dmg);
    knockback(t, from = target_cell, cells = 2);
}

consume_arrow(1);
consume_sp(15);
9. Comparación rápida: Double Strafe vs Arrow Shower
Skill	Uso	Ratio Lv10	Target	AoE	Consumo
Double Strafe	Burst single-target	380% total	Enemy	No	12 SP + 1 arrow
Arrow Shower	AoE / knockback / trap utility	125% Pre-Re	Ground/cell	3x3	15 SP + 1 arrow

Double Strafe es el botón de daño single-target del Archer. Arrow Shower en Pre-Renewal no es una gran herramienta de DPS puro; su valor está en área, knockback, revelar/pegar objetivos agrupados y manipulación de traps.

10. Orden de resolución para una skill Archer ofensiva

Para Double Strafe o Arrow Shower:

1. Validar Bow equipado
2. Validar Arrow equipada
3. Consumir SP
4. Consumir 1 Arrow
5. Validar rango: base + Vulture's Eye
6. Verificar Pneuma si el target/celda está protegido
7. HIT vs FLEE, salvo flags específicos del servidor
8. Calcular BaseATK missile con DEX
9. Calcular WeaponATK bow + ArrowATK
10. Aplicar size modifier de Bow
11. Aplicar SkillRatio
12. Aplicar Hard DEF
13. Restar Soft DEF
14. Aplicar elemento de flecha / weapon property
15. Aplicar cards de daño
16. Aplicar reducciones
17. Aplicar multi-hit si corresponde
18. Mostrar daño

El algoritmo físico clásico define los checks de Pneuma, Perfect Dodge, CRIT/HIT, BaseATK, WeaponATK, size, skill modifiers, DEF, elemento, bonus y reducciones en ese orden general.

11. Tablas compactas de fórmulas
Skill	Fórmula / efecto
Owl's Eye	DEX += SkillLv
Vulture's Eye	BowRange += SkillLv, HIT += SkillLv
Improve Concentration	DEX/AGI += floor(EligibleStat * (2+Lv) / 100)
Improve Concentration duration	40 + 20*Lv segundos
Improve Concentration SP	20 + 5*Lv
Double Strafe	(180 + 20*Lv)% total, equivalente a 200–380%
Double Strafe SP	12
Arrow Shower Pre-Re	(75 + 5*Lv)%, 80–125%
Arrow Shower SP	15
Arrow Shower AoE Pre-Re	3x3
Arrow Shower knockback	2 celdas
12. Pseudocódigo compacto
// Archer Pre-Renewal normal skills.
// Excluye quest skills: Arrow Crafting / Making Arrow, Charge Arrow / Arrow Repel.

int owl_eye_dex_bonus(int lv) {
    return lv; // +1..+10 DEX
}

int vulture_eye_range_bonus(int lv) {
    return lv; // +1..+10 range with bow
}

int vulture_eye_hit_bonus(int lv) {
    return lv; // +1..+10 HIT
}

int improve_concentration_percent(int lv) {
    return 2 + lv; // 3..12%
}

int improve_concentration_duration(int lv) {
    return 40 + 20 * lv; // 60..240 sec
}

int improve_concentration_sp_cost(int lv) {
    return 20 + 5 * lv; // 25..70 SP
}

int improve_concentration_bonus(int eligible_stat, int lv) {
    return floor(eligible_stat * improve_concentration_percent(lv) / 100.0);
}

int double_strafe_total_ratio(int lv) {
    return 180 + 20 * lv; // 200..380%
}

int double_strafe_sp_cost() {
    return 12;
}

int arrow_shower_ratio_pre_re(int lv) {
    return 75 + 5 * lv; // 80..125%
}

int arrow_shower_sp_cost() {
    return 15;
}

int arrow_shower_area_pre_re() {
    return 3; // 3x3
}

int arrow_shower_knockback_cells() {
    return 2;
}
13. Validaciones que haría en servidor/emulador
Arrow Shower table
Pre-Re clásico usa 80–125% y 3x3. Tablas modernas muestran 160–250% y área 3x3/5x5. No mezclar.
Double Strafe hit model
Puedes implementarlo como total 200–380%, o como 2 hits de 100–190%. Visualmente y en interacción con Kyrie/multi-hit puede importar.
Vulture's Eye HIT
Fuentes clásicas lo expresan como +HIT, mientras algunas tablas modernas lo muestran como “HIT +x%”. Para Pre-Re clásico, modelaría +SkillLv HIT.
Improve Concentration eligible stats
RMS Pre-Re dice que no incluye cartas. Si el emulador incluye cartas o buffs externos, eso cambia breakpoints de DEX/AGI.
Pneuma
Double Strafe y Arrow Shower deben tratarse como ranged physical. Si atraviesan Pneuma, algo está customizado o mal flaggeado.