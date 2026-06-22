Acolyte Pre-Renewal — skills sin quest skill

Excluyo:

AL_HOLYLIGHT = Holy Light

Porque Holy Light es la quest skill de Acolyte. Incluyo el árbol normal:

AL_DP          Divine Protection
AL_DEMONBANE   Demon Bane
AL_RUWACH      Ruwach
AL_PNEUMA      Pneuma
AL_TELEPORT    Teleport
AL_WARP        Warp Portal
AL_HEAL        Heal
AL_INCAGI      Increase Agility
AL_DECAGI      Decrease Agility
AL_HOLYWATER   Aqua Benedicta
AL_CRUCIS      Signum Crucis
AL_ANGELUS     Angelus
AL_BLESSING    Blessing
AL_CURE        Cure

Fuentes usadas: iRO Wiki Classic cuando existe página clásica, y RateMyServer en modo Pre-Re para tablas/fórmulas. Algunas entradas públicas mezclan secciones Renewal/iRO moderno; cuando hay discrepancia, marco el punto.

1. Divine Protection — AL_DP
Tipo
Pasiva
MaxLv: 10
Aplica contra: Undead property y Demon race/family
No aplica contra jugadores
Fórmula Pre-Renewal
DamageReduction =
3 * SkillLv + floor(0.04 * (BaseLv + 1))

Equivalente práctico:

DamageReduction =
3 * SkillLv + floor((BaseLv + 1) / 25)

Se resta después de las reducciones de DEF, como reducción plana. RateMyServer lo define como reducción contra Undead property y Demon family, no contra jugadores.

Tabla base sin bonus por BaseLv
Lv	Reducción base
1	3
2	6
3	9
4	12
5	15
6	18
7	21
8	24
9	27
10	30
Integración en combate
if target_attack_source_is_undead_property_or_demon_race
   and defender_is_not_player:
    final_damage -= 3 * AL_DP_Lv + floor((BaseLv + 1) / 25)

No lo trates como Hard DEF porcentual. Es una reducción plana tipo “soft defense” especial.

2. Demon Bane — AL_DEMONBANE
Tipo
Pasiva ofensiva
MaxLv: 10
Aplica contra: Undead property y Demon family/race
No aplica contra jugadores
Fórmula Pre-Renewal
DemonBaneBonus =
3 * SkillLv + floor(0.05 * (BaseLv + 1))

Equivalente:

DemonBaneBonus =
3 * SkillLv + floor((BaseLv + 1) / 20)

El bonus se comporta como weapon mastery: aumenta el daño contra Undead/Demon, ignora reducción por Armor DEF, pero no ignora VIT DEF. RateMyServer especifica que no funciona contra jugadores. iRO Wiki Classic también lo describe como daño extra tipo weapon mastery contra Undead y Demon.

Tabla base sin bonus por BaseLv
Lv	ATK base
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
Integración en daño físico
DamageAfterDEF =
PhysicalDamage * (1 - HardDEF / 100) - SoftDEF

if target_is_undead_property_or_demon_race and target_is_not_player:
    DamageAfterDEF += 3 * AL_DEMONBANE_Lv + floor((BaseLv + 1) / 20)

Esto importa para Battle Acolyte / Monk path, pero en Acolyte support puro normalmente es prerequisito más que fuente real de daño.

3. Ruwach — AL_RUWACH
Tipo
Activa mágica / detección
MaxLv: 1
Target: self
Área: 5x5 alrededor del caster
Duración: 10 segundos
SP: 10
Elemento: Holy
Efecto

Revela enemigos en Hiding o Cloaking dentro del área. Si el objetivo revelado es enemigo, recibe daño mágico Holy. RateMyServer lo define como una magia Holy de 145% MATK.

Fórmula
RuwachDamage =
MATK_Damage * 1.45 * HolyElementModifier

Más explícito:

RuwachDamage =
{
  rnd(MinMATK, MaxMATK)
  * 1.45
  * (1 - TargetMDEF / 100)
  - TargetINT
  - TargetVIT / 2
}
* HolyElementModifier
Nota de implementación

No es un autoattack ni un golpe físico. No usa HIT/FLEE/CRIT/DEF física. Es magia Holy con ratio 145%.

if target.hidden && distance(caster, target) <= 2:
    reveal(target)
    if target.is_enemy:
        apply_magic_damage(caster, target, element=HOLY, ratio=1.45)
4. Pneuma — AL_PNEUMA
Tipo
Activa / ground support
MaxLv: 1
Target: ground
Range: 9 celdas
Área: 3x3
Duración: 10 segundos
SP: 10
Cast: ninguno
Delay: ninguno en Classic

iRO Wiki Classic lista Pneuma con 10 SP, 10 segundos, target ground y área 3x3.

Efecto

Bloquea ataques físicos ranged dentro del área. Esto incluye ataques normales de Bow, ataques ranged físicos de jugadores y ataques de mobs cuyo rango sea 4 o más celdas. No bloquea magia. Tampoco bloquea splash damage ni elimina la penalización de FLEE por múltiples mobs. No se puede solapar con otro Pneuma ni con Safety Wall, y sobre Land Protector puede verse la animación pero no tiene efecto.

Regla de combate
if target_cell_has_pneuma
   and incoming_attack.type == PHYSICAL
   and incoming_attack.range_type == RANGED:
       damage = 0
       block_attack()
No bloquea
Magic attacks
Melee physical attacks
Splash damage indirecto
Status effects independientes
FLEE penalty por mobbing

Pneuma es una de las skills defensivas más fuertes de Pre-Renewal porque no “reduce” ranged physical: lo anula.

5. Teleport — AL_TELEPORT
Tipo
Activa / movilidad
MaxLv: 2
Target: self
Prereq: Ruwach Lv 1
Tabla
Lv	Efecto	SP
1	Teleport aleatorio dentro del mapa	10
2	Teleport a Save Point	9

RateMyServer Pre-Re lista Lv1 como random spot y Lv2 como save point. También indica que la skill está deshabilitada dentro de Land Protector.

Pseudocódigo
if AL_TELEPORT_Lv == 1:
    consume_sp(10)
    warp_to_random_cell_same_map()

if AL_TELEPORT_Lv == 2:
    consume_sp(9)
    warp_to_save_point()
Nota

En muchos servidores privados hay restricciones adicionales: mapas no teleportables, campos de WoE, instancias, celdas inválidas, mapas con flag noteleport, etc. No lo asumas universal.

6. Warp Portal — AL_WARP
Tipo
Activa / ground support
MaxLv: 4
Target: ground
Range: 9 celdas
Catalyst: Blue Gemstone x1
Max active portals: 3
Prereq: Teleport Lv 2
Fórmula de SP
SPCost = 38 - 3 * SkillLv
Lv	Destinos disponibles	SP
1	Save Point	35
2	Save Point + 1 memo	32
3	Save Point + 2 memo	29
4	Save Point + 3 memo	26

RateMyServer lista Blue Gemstone x1, máximo 3 portales activos, rango 9, SP 35/32/29/26, y número de memo points igual a SkillLv - 1.

Reglas
No se puede castear bajo un player o mob.
Si alguien entra en la celda mientras eliges destino, falla.
Usa /memo para guardar destinos válidos.
No todos los mapas permiten memo.
Hasta 8 jugadores pueden usar el portal.

RateMyServer indica que hasta 8 jugadores pueden ser transportados sin depender del nivel de skill.

7. Heal — AL_HEAL
Tipo
Activa / recovery / Holy magic contra Undead property
MaxLv: 10
Target: friend
Range: 9 celdas
Elemento: Holy cuando se usa ofensivamente
Cast Delay: 1 segundo
Fórmula de curación Pre-Renewal

iRO Wiki Classic da:

BaseHeal = 4 + 8 * SkillLv
HealAmount = floor((BaseLv + INT) / 8) * BaseHeal

La tabla clásica implica un multiplicador mínimo de 1 para rangos bajos, así que en implementación segura conviene modelarlo como:

HealMultiplier = max(1, floor((BaseLv + INT) / 8))
HealAmount = HealMultiplier * (4 + 8 * SkillLv)

iRO Wiki Classic lista Base Heal de 12 a 84 para Lv1–10 y SP 13 a 40. RateMyServer Pre-Re usa la misma estructura [(BaseLv+INT)/8]*(4+8*SkillLv).

Tabla base
Lv	BaseHeal	SP
1	12	13
2	20	16
3	28	19
4	36	22
5	44	25
6	52	28
7	60	31
8	68	34
9	76	37
10	84	40
Heal ofensivo contra Undead property

Contra targets Undead property, Heal no cura: hace daño Holy igual a la mitad del valor de Heal, modificado por el elemento Undead. Ignora MDEF e INT del objetivo según RateMyServer. iRO Wiki Classic también indica que Heal contra Undead element hace daño Holy igual a la mitad de la curación normal, modificado por el nivel elemental Undead.

OffensiveHealDamage =
floor(HealAmount * HolyVsTargetElementModifier / 2)
Reglas importantes
Heal normal:
  cura HP
  ignora DEF del target

Heal ofensivo:
  solo contra Undead property
  requiere shift-click o /noshift
  elemento Holy
  daño = HealAmount * ElementModifier / 2
  ignora MDEF/INT del target en Pre-Re según RMS
Ejemplo

Caster:

BaseLv = 60
INT = 70
HealLv = 10
BaseLv + INT = 130
HealMultiplier = floor(130 / 8) = 16
BaseHeal = 4 + 8*10 = 84

HealAmount = 16 * 84 = 1344

Contra Undead con modificador Holy 200%:

Damage = floor(1344 * 2.00 / 2)
Damage = 1344
8. Increase Agility — AL_INCAGI
Tipo
Activa / buff
MaxLv: 10
Target: friend
Range: 9 celdas
Prereq: Heal Lv 3
HP Cost: 15
Cast Time: 1 segundo
Cast Delay: 1 segundo
Fórmulas Pre-Renewal
AGIBonus = 2 + SkillLv
MoveSpeedBonus = +25%
DurationSeconds = 40 + 20 * SkillLv
SPCost = 15 + 3 * SkillLv

Tabla:

Lv	AGI	Duración	SP
1	+3	60s	18
2	+4	80s	21
3	+5	100s	24
4	+6	120s	27
5	+7	140s	30
6	+8	160s	33
7	+9	180s	36
8	+10	200s	39
9	+11	220s	42
10	+12	240s	45

RateMyServer Pre-Re define el aumento como 2 + SkillLv, movimiento +25%, HP cost 15, SP 18–45, duración 1–4 minutos, y remueve Decrease Agility al aplicarse.

Efecto en combate

Increase AGI no da daño directo. Afecta:

FLEE += AGIBonus
ASPD sube indirectamente por AGI
MoveSpeed *= 1.25

No reduce cast time. En Pre-Renewal, DEX reduce cast; AGI no.

Interacciones
Dispels Decrease Agility
Es removido por Decrease Agility
No puede aplicarse dentro de Quagmire
Consume HP además de SP
9. Decrease Agility — AL_DECAGI
Tipo
Activa / debuff
MaxLv: 10
Target: enemy
Range: 9 celdas
Prereq: Increase Agility Lv 1
Cast Time: 1 segundo
Cast Delay: 1 segundo
No funciona contra Boss monsters
Fórmulas
AGIReduction = 2 + SkillLv
MoveSpeedReduction = -25%
DurationVsMonster = 30 + 10 * SkillLv
SPCost = 13 + 2 * SkillLv

RateMyServer lista AGI -3 a -12, duración 40–130 segundos, SP 15–33, cast 1s y delay 1s. La diferencia aparente de duración se debe a que su tabla empieza en 40s a Lv1, lo cual equivale a 30 + 10*Lv.

Lv	AGI	Duración monster	SP
1	-3	40s	15
2	-4	50s	17
3	-5	60s	19
4	-6	70s	21
5	-7	80s	23
6	-8	90s	25
7	-9	100s	27
8	-10	110s	29
9	-11	120s	31
10	-12	130s	33
Success rate

La fuente lo marca como “believed”, así que no lo trataría como contrato absoluto si estás implementando servidor:

SuccessRate% =
40 + 2 * SkillLv + floor((BaseLv + INT) / 5) - TargetMDEF

RateMyServer dice explícitamente que la fórmula de éxito “is believed to be” esa fórmula; también indica que la duración se reduce a la mitad contra jugadores y que no funciona contra Boss.

Interacciones

Un cast exitoso remueve:

Increase Agility
Adrenaline Rush
Two-Hand Quicken
Spear Quicken
Cart Boost

También combina con Quagmire bajo la forma indicada por RMS:

AGI_after_Quagmire_and_DecAgi =
AGI / 2 - 2 + SkillLv

La notación de RMS es ambigua para implementación exacta; para un emulador real validaría contra código, no contra esta frase.

10. Aqua Benedicta — AL_HOLYWATER
Tipo
Activa / crafting support
MaxLv: 1
Target: self
SP: 10
Cast Time: 1 segundo en Classic
Cast Delay: 0.5 segundos
Prereq: ninguno como Acolyte
Requisito de terreno: estar sobre una celda de agua válida
Efecto

Crea Holy Water. El caster debe estar sobre agua; mapas con submersión global como Undersea Tunnel/Sunken Ship pueden no contar como agua válida según RMS.

En práctica Pre-Renewal usual, también se consume:

Empty Bottle x1

RMS en la sección moderna lo explicita como “Creates Holy Water with 1 Empty Bottle while standing in water”, pero su bloque Pre-Re citado solo menciona el requisito de agua. En servidor privado conviene verificar si el item consume está en skill_db/script.

Pseudocódigo
if !caster.is_on_valid_water_cell():
    fail()

if !caster.has_item(EMPTY_BOTTLE, 1):
    fail()

consume_sp(10)
consume_item(EMPTY_BOTTLE, 1)
add_item(HOLY_WATER, 1)
11. Signum Crucis — AL_CRUCIS
Tipo
Activa / debuff AoE
MaxLv: 10
Target: self
Área: pantalla / 31x31 en RMS
SP: 35
Cast Time: 0.5s
Cast Delay: 2s
Prereq: Demon Bane Lv 3
Afecta: Undead property y Demon race/family monsters
Fórmula de reducción de DEF
HardDEFReduction% = 10 + 4 * SkillLv
Lv	Hard DEF reduction
1	-14%
2	-18%
3	-22%
4	-26%
5	-30%
6	-34%
7	-38%
8	-42%
9	-46%
10	-50%

RateMyServer especifica que reduce DEF, no VIT DEF, contra Undead property y Demon family monsters, y que stackea con Provoke. iRO Wiki Classic también lo describe como reducción de Hard DEF contra Undead Element y Demon Race en pantalla.

Success rate

La fórmula pública también está marcada como “believed”:

SuccessRate% =
23 + 4 * SkillLv + CasterBaseLv - TargetBaseLv

RateMyServer y otras tablas muestran base success de 27% a 63% antes de diferencia de niveles.

Reglas
Aplica a mobs Undead property o Demon race.
Funciona en boss monsters según iRO Wiki Classic.
No funciona contra jugadores usando Undead armor.
Al acertar, el objetivo muestra /swt.
Stackea con Provoke.

iRO Wiki Classic indica que funciona en boss monsters y no funciona contra jugadores con Evil Druid/undead armor.

Integración con DEF
if target.is_monster()
   && (target.element == UNDEAD || target.race == DEMON)
   && chance(success_rate):
       target.hard_def *= (1.0 - (10 + 4 * skill_lv) / 100.0)

No reduzcas Soft DEF/VIT DEF con Signum. Ese es un error frecuente.

12. Angelus — AL_ANGELUS
Tipo
Activa / party buff
MaxLv: 10
Target: self
Área: screen-wide / party members
Prereq: Divine Protection Lv 3
Cast Time: 0.5s
Cast Delay: 3.5s
Fórmula Pre-Renewal
VITDEFMultiplier =
1 + 0.05 * SkillLv

Equivalente:

VIT_DEF_Final =
VIT_DEF_Base * (100 + 5 * SkillLv) / 100
Lv	VIT DEF
1	105%
2	110%
3	115%
4	120%
5	125%
6	130%
7	135%
8	140%
9	145%
10	150%

RateMyServer Pre-Re lo define como aumento del DEF proveniente de VIT en 5 * SkillLv %, sin aumentar otras cosas relacionadas con VIT.

Duración y SP
DurationSeconds = 30 * SkillLv
SPCost = 20 + 3 * SkillLv
Lv	Duración	SP
1	30s	23
2	60s	26
3	90s	29
4	120s	32
5	150s	35
6	180s	38
7	210s	41
8	240s	44
9	270s	47
10	300s	50

RMS lista esa duración y coste de SP para la sección Pre-Re.

Nota importante

Angelus no aumenta VIT real.

No hace esto:

VIT += X

Hace esto:

SoftDEF_from_VIT *= 1 + 0.05 * SkillLv

Por tanto, no aumenta HP, resistencia a estados, recuperación ni healing items en Pre-Renewal clásico. Las líneas que mencionan MaxHP pertenecen a secciones iRO/Renewal modernas, no al comportamiento Pre-Re que estamos usando.

13. Blessing — AL_BLESSING
Tipo
Activa / buff o debuff ofensivo especial
MaxLv: 10
Target: friend / any entity según implementación
Range: 9 celdas
Prereq: Divine Protection Lv 5
Fórmulas Pre-Renewal sobre aliados
STRBonus = SkillLv
DEXBonus = SkillLv
INTBonus = SkillLv
DurationSeconds = 40 + 20 * SkillLv
SPCost = 24 + 4 * SkillLv
Lv	STR/DEX/INT	Duración	SP
1	+1	60s	28
2	+2	80s	32
3	+3	100s	36
4	+4	120s	40
5	+5	140s	44
6	+6	160s	48
7	+7	180s	52
8	+8	200s	56
9	+9	220s	60
10	+10	240s	64

RateMyServer Pre-Re indica que Blessing aumenta STR, DEX e INT por 1 * SkillLv y remueve Curse; iRO Wiki moderno también lista duración 40 + 20*Lv y SP 24 + 4*Lv, pero agrega HIT explícito en su versión actual.

Efecto indirecto de los stats

Blessing no es solo “+daño”:

STR:
  aumenta ATK melee y peso

DEX:
  aumenta HIT
  reduce cast time
  mejora daño ranged/missile
  reduce variación de WeaponATK melee

INT:
  aumenta MATK
  aumenta MaxSP y SP recovery
  aumenta MDEF blanda

En Pre-Renewal, +10 DEX puede ser más valioso que parece porque afecta HIT y cast time.

Offensive Blessing

Contra monstruos Undead property o Demon race/family, Blessing puede usarse ofensivamente:

TargetSTR = floor(TargetSTR / 2)
TargetDEX = floor(TargetDEX / 2)
TargetINT = floor(TargetINT / 2)

RMS Pre-Re dice que este efecto reduce STR/DEX/INT a la mitad sin depender del nivel de Blessing, baja HIT y MATK del monstruo, pero no afecta ATK; no funciona contra jugadores ni boss monsters.

Curse / Stone Curse

Blessing remueve Curse. Las páginas modernas también indican que puede purgar Stone/Stone Curse en ciertas condiciones; para Pre-Re estricto, trataría Curse como seguro y validaría Stone Curse contra el servidor objetivo si estás implementando.

14. Cure — AL_CURE
Tipo
Activa / status recovery
MaxLv: 1
Target: self, ally, foe o player según fuente
Range: 9 celdas en RMS
SP: 15
Prereq: Heal Lv 2
Efecto Pre-Renewal

Cure remueve:

Blind
Chaos / Confusion
Silence

iRO Wiki Classic lista Silence, Chaos y Blind. RateMyServer Pre-Re lista Blind, Confusion y Silence, con la limitación práctica de que no puedes curarte a ti mismo de Silence porque no puedes castear mientras estás silenciado.

Pseudocódigo
if target.has_status(BLIND):
    remove_status(BLIND)

if target.has_status(CONFUSION) || target.has_status(CHAOS):
    remove_status(CONFUSION)

if target.has_status(SILENCE):
    remove_status(SILENCE)
Limitación práctica
Self Cure vs Silence:
  normalmente imposible,
  porque Silence bloquea casteo.
15. Resumen de fórmulas
Skill	Fórmula principal
Divine Protection	3*Lv + floor((BaseLv+1)/25) reducción plana
Demon Bane	3*Lv + floor((BaseLv+1)/20) daño extra tipo mastery
Ruwach	145% MATK, Holy, revela hidden/cloak
Pneuma	bloquea ranged physical en 3x3 por 10s
Teleport	Lv1 random / Lv2 save point
Warp Portal	SP = 38 - 3*Lv, memo slots Lv - 1
Heal	max(1, floor((BaseLv+INT)/8)) * (4+8*Lv)
Offensive Heal	HealAmount * ElementModifier / 2 contra Undead property
Increase AGI	AGI + (2+Lv), move speed +25%, duración 40+20*Lv
Decrease AGI	AGI - (2+Lv), move speed -25%, duración monster 30+10*Lv
Decrease AGI success	40 + 2*Lv + floor((BaseLv+INT)/5) - TargetMDEF %, fórmula pública no confirmada
Aqua Benedicta	crea Holy Water; requiere agua, usualmente Empty Bottle
Signum Crucis	Hard DEF -(10+4*Lv)%
Signum success	23 + 4*Lv + CasterBaseLv - TargetBaseLv %, fórmula pública no confirmada
Angelus	VIT DEF * (1 + 0.05*Lv)
Blessing	STR/DEX/INT +Lv, duración 40+20*Lv
Offensive Blessing	STR/DEX/INT del mob Demon/Undead a 50%
Cure	remueve Blind, Chaos/Confusion, Silence
16. Pseudocódigo compacto
// Acolyte Pre-Renewal normal skills.
// Excluye quest skill: Holy Light.

int divine_protection_reduction(int skill_lv, int base_lv) {
    return 3 * skill_lv + (base_lv + 1) / 25;
}

int demon_bane_bonus(int skill_lv, int base_lv) {
    return 3 * skill_lv + (base_lv + 1) / 20;
}

int heal_amount(int skill_lv, int base_lv, int total_int) {
    int base_heal = 4 + 8 * skill_lv;
    int multiplier = std::max(1, (base_lv + total_int) / 8);
    return multiplier * base_heal;
}

int heal_sp_cost(int skill_lv) {
    return 10 + 3 * skill_lv; // Lv1=13, Lv10=40
}

int increase_agi_bonus(int skill_lv) {
    return 2 + skill_lv;
}

int increase_agi_duration(int skill_lv) {
    return 40 + 20 * skill_lv;
}

int increase_agi_sp_cost(int skill_lv) {
    return 15 + 3 * skill_lv;
}

int decrease_agi_penalty(int skill_lv) {
    return 2 + skill_lv;
}

int decrease_agi_duration_vs_monster(int skill_lv) {
    return 30 + 10 * skill_lv;
}

int decrease_agi_sp_cost(int skill_lv) {
    return 13 + 2 * skill_lv;
}

double decrease_agi_success_rate(int skill_lv, int caster_base_lv, int caster_int, int target_mdef) {
    return 40 + 2 * skill_lv + (caster_base_lv + caster_int) / 5 - target_mdef;
}

int warp_portal_sp_cost(int skill_lv) {
    return 38 - 3 * skill_lv;
}

int warp_portal_memo_slots(int skill_lv) {
    return skill_lv - 1;
}

double signum_def_reduction(int skill_lv) {
    return (10 + 4 * skill_lv) / 100.0;
}

double signum_success_rate(int skill_lv, int caster_base_lv, int target_base_lv) {
    return 23 + 4 * skill_lv + caster_base_lv - target_base_lv;
}

double angelus_vit_def_multiplier(int skill_lv) {
    return 1.0 + 0.05 * skill_lv;
}

int angelus_duration(int skill_lv) {
    return 30 * skill_lv;
}

int angelus_sp_cost(int skill_lv) {
    return 20 + 3 * skill_lv;
}

int blessing_stat_bonus(int skill_lv) {
    return skill_lv;
}

int blessing_duration(int skill_lv) {
    return 40 + 20 * skill_lv;
}

int blessing_sp_cost(int skill_lv) {
    return 24 + 4 * skill_lv;
}

La validación crítica para un emulador sería revisar en código real: Decrease AGI success, Signum success, consumo exacto de Aqua Benedicta, y si el servidor usa reglas modernas de Blessing/Angelus o reglas Pre-Re puras.