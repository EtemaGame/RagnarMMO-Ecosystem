Merchant Pre-Renewal — solo primera clase, sin Skill Quest

Excluyo las skills de quest / no árbol normal:

MC_CARTREVOLUTION = Cart Revolution
MC_CHANGECART     = Change Cart
MC_LOUD           = Crazy Uproar / Loud Exclamation
MC_CARTDECORATE   = Cart Decoration
MC_BUYINGSTORE    = Open Buying Store

iRO Wiki lista Cart Revolution, Change Cart, Crazy Uproar, Open Buying Store y Cart Decoration dentro de Merchant Skill Quest, así que quedan fuera.

Incluyo únicamente el árbol normal de Merchant primera clase:

MC_INCCARRY    = Enlarge Weight Limit
MC_DISCOUNT    = Discount
MC_OVERCHARGE  = Overcharge
MC_PUSHCART    = Pushcart
MC_IDENTIFY    = Item Appraisal / Identify
MC_VENDING     = Vending
MC_MAMMONITE   = Mammonite
1. Enlarge Weight Limit — MC_INCCARRY
Tipo
Pasiva
MaxLv: 10
Prereq: ninguno
Fórmula
WeightLimitBonus = 200 * SkillLv
Lv	Weight Limit
1	+200
2	+400
3	+600
4	+800
5	+1000
6	+1200
7	+1400
8	+1600
9	+1800
10	+2000

RateMyServer Pre-Re y iRO Wiki listan el bonus como +200 por nivel hasta +2000.

Integración
FinalWeightLimit =
BaseWeightLimit
+ STRWeightBonus
+ 200 * MC_INCCARRY_Lv
+ equipment_bonus
Importancia real

Para Merchant, esta skill no es solo “comodidad”. Afecta:

más potions
más loot
más munición/item consumible
menos viajes a NPC/Kafra
menos riesgo de overweight

Pero no aumenta el peso del cart. El cart tiene su propio límite de 8000 weight; Enlarge Weight Limit aumenta el inventario del personaje, no el cart.

2. Discount — MC_DISCOUNT
Tipo
Pasiva económica
MaxLv: 10
Prereq: Enlarge Weight Limit Lv3
Required for: Overcharge Lv3
Efecto

Reduce el precio de compra en tiendas NPC. No afecta deals, tiendas de otros jugadores, vending shops ni NPCs especiales tipo upgrade/shop custom. RateMyServer indica que el precio final se redondea hacia abajo y tiene mínimo de 1 zeny.

Tabla
Lv	Discount
1	7%
2	9%
3	11%
4	13%
5	15%
6	17%
7	19%
8	21%
9	23%
10	24%

Fórmula
DiscountedPrice =
max(1, floor(BaseNPCBuyPrice * (1 - DiscountRate)))

Ejemplo:

BaseNPCBuyPrice = 1000z
Discount Lv10 = 24%

DiscountedPrice = floor(1000 * 0.76)
DiscountedPrice = 760z
No lo confundas con Overcharge
Discount  = compras más barato a NPC
Overcharge = vendes más caro a NPC

Discount no modifica daño, stats, peso, cart ni comercio player-to-player.

3. Overcharge — MC_OVERCHARGE
Tipo
Pasiva económica
MaxLv: 10
Prereq: Discount Lv3
Efecto

Aumenta el zeny recibido al vender ítems a NPC. No afecta deals especiales. RateMyServer indica que el precio final se redondea hacia abajo y tiene mínimo de 0 zeny.

Tabla
Lv	Overcharge
1	+7%
2	+9%
3	+11%
4	+13%
5	+15%
6	+17%
7	+19%
8	+21%
9	+23%
10	+24%

Fórmula
OverchargedSellPrice =
floor(BaseNPCSellPrice * (1 + OverchargeRate))

Ejemplo:

BaseNPCSellPrice = 1000z
Overcharge Lv10 = +24%

FinalSellPrice = floor(1000 * 1.24)
FinalSellPrice = 1240z
Impacto económico

Overcharge Lv10 es una de las razones principales para tener Merchant mule:

sin Overcharge: vendes a precio base
con Overcharge Lv10: vendes a 124% del precio base
4. Pushcart — MC_PUSHCART
Tipo
Pasiva / habilitadora
MaxLv: 10
Prereq: Enlarge Weight Limit Lv5
Required for: Vending Lv3
Efecto

Permite rentar y usar un Pushcart desde Kafra. El cart tiene:

Weight capacity: 8000
Distinct item slots: 100

iRO Wiki y RateMyServer coinciden en que el cart tiene 8000 de peso y 100 slots, y que usarlo penaliza la velocidad de movimiento según el nivel de Pushcart.

Fórmula de movimiento
MovementSpeedWithCart% = 50 + 5 * SkillLv
Lv	Movement Speed
1	55%
2	60%
3	65%
4	70%
5	75%
6	80%
7	85%
8	90%
9	95%
10	100%

Reglas
El cart se renta en Kafra.
Se abre con Alt+W.
Los ítems dentro del cart no se usan ni equipan directamente.
Si remueves el cart, los ítems no se pierden, pero no puedes accederlos hasta rentar otro cart.

RateMyServer especifica que los ítems del cart no se pierden si se remueve el cart, pero no se pueden recuperar hasta comprar/rentar uno nuevo.

Modelo
int pushcart_movement_speed_percent(int lv) {
    return 50 + 5 * lv; // 55..100
}

bool can_use_cart(const Character& c) {
    return c.skill_level(MC_PUSHCART) > 0 && c.has_rented_cart;
}
5. Item Appraisal / Identify — MC_IDENTIFY
Tipo
Activa / support
MaxLv: 1
Target: self
SP: 10
Efecto

Identifica un ítem desconocido del inventario. RateMyServer indica que el ítem debe estar en el inventario, no en el cart, y que Magnifier duplica el efecto de esta skill.

Fórmula

No tiene fórmula de combate.

SPCost = 10
Modelo
void cast_item_appraisal(Character& c, Item& item) {
    if (!item.is_unidentified)
        return;

    if (!c.inventory.contains(item))
        return; // no cart

    consume_sp(c, 10);
    item.is_unidentified = false;
}
Restricción clave
No identifica ítems dentro del cart.

Eso importa para implementación UI/inventario.

6. Vending — MC_VENDING
Tipo
Activa / comercio
MaxLv: 10
Target: self
SP: 30
Prereq: Pushcart Lv3
Required state: Pushcart equipado
Efecto

Abre una tienda de venta para otros jugadores. Solo puedes vender ítems que estén dentro del Pushcart. El número máximo de stacks distintos que puedes vender es 2 + SkillLv; Discount no aplica a vending shops.

Fórmula
MaxVendingStacks = 2 + SkillLv
Lv	Stacks vendibles
1	3
2	4
3	5
4	6
5	7
6	8
7	9
8	10
9	11
10	12

Reglas
Requiere Pushcart equipado.
Solo vende ítems del cart.
No permite que otros jugadores te vendan ítems.
Discount no aplica.
La tienda se cierra si todo se vende o si el personaje muere.
SP cost: 30.

RateMyServer especifica que la tienda se cierra automáticamente si todos los ítems son vendidos o si el personaje muere.

Modelo
int vending_max_stacks(int lv) {
    return 2 + lv; // 3..12
}

bool can_open_vending(const Character& c) {
    return c.has_cart
        && c.skill_level(MC_VENDING) > 0
        && c.skill_level(MC_PUSHCART) >= 3;
}

void open_vending(Character& c, vector<CartItemStack> selected_items) {
    int lv = c.skill_level(MC_VENDING);

    if (!can_open_vending(c))
        fail();

    if (selected_items.size() > vending_max_stacks(lv))
        fail();

    consume_sp(c, 30);
    create_player_shop(c, selected_items);
}
Punto de implementación

No mezcles Vending con Open Buying Store. Vending vende tus ítems a otros jugadores; Open Buying Store compra ítems de otros jugadores, y aquí queda excluida por ser quest/no árbol normal.

7. Mammonite — MC_MAMMONITE
Tipo
Ofensiva física melee
MaxLv: 10
Target: enemy
Range: 1 celda
SP: 5
Property: Weapon Property
Fórmula Pre-Renewal
MammoniteSkillRatio% = 100 + 50 * SkillLv
ZenyCost = 100 * SkillLv
Lv	Daño	Zeny
1	150% ATK	100z
2	200% ATK	200z
3	250% ATK	300z
4	300% ATK	400z
5	350% ATK	500z
6	400% ATK	600z
7	450% ATK	700z
8	500% ATK	800z
9	550% ATK	900z
10	600% ATK	1000z

RateMyServer Pre-Re formula Mammonite como ATK = (100 + 50*SkillLv)% y coste de 100z * SkillLv; iRO Wiki muestra la misma tabla 150%–600% y 100–1000 zeny.

Integración en combate físico

Mammonite es una skill física normal con multiplicador alto. Usa:

STR / DEX / LUK para BaseATK melee
WeaponATK del arma
Size modifier del arma
Propiedad del arma
HIT vs FLEE
Hard DEF
Soft DEF
Cards / reducciones

Modelo simplificado:

RawPhysical =
BaseATK_melee + WeaponATK * SizeModifier

AfterSkill =
RawPhysical * ((100 + 50 * SkillLv) / 100)

AfterDEF =
AfterSkill * (1 - HardDEF / 100) - SoftDEF

Final =
AfterDEF
* ElementModifier
* DamageBonusModifiers
* DamageReductionModifiers
No es magia
Mammonite no usa MATK.
Mammonite no usa MDEF.
Mammonite no ignora FLEE.
Mammonite no ignora DEF.
Mammonite no es ranged.
Mammonite puede fallar si no tienes HIT suficiente.
Costo
SPCost = 5
ZenyCost = 100 * SkillLv

Para servidor/emulador, validaría un detalle: si el zeny se consume al intento, al hit efectivo o bajo qué condiciones en caso de miss/interrupción. Las fuentes públicas describen el coste por uso, pero la política exacta puede depender de implementación.

8. Orden de resolución de Mammonite
1. Validar target en rango melee.
2. Validar SP >= 5.
3. Validar Zeny >= 100 * SkillLv.
4. Consumir SP.
5. Consumir Zeny según regla del servidor.
6. Resolver HIT vs FLEE.
7. Si falla: miss.
8. Si acierta:
   a. Calcular BaseATK melee.
   b. Calcular WeaponATK.
   c. Aplicar size modifier.
   d. Aplicar skill ratio: 100 + 50*Lv.
   e. Aplicar Hard DEF.
   f. Restar Soft DEF.
   g. Aplicar elemento del arma.
   h. Aplicar bonus de cards.
   i. Aplicar reducciones.
9. Resumen de fórmulas — Merchant primera clase
Skill	Fórmula / efecto
Enlarge Weight Limit	+200 * Lv weight limit
Discount	compra NPC con descuento: 7,9,11,13,15,17,19,21,23,24%
Discount price	max(1, floor(BasePrice * (1 - rate)))
Overcharge	venta NPC con bonus: 7,9,11,13,15,17,19,21,23,24%
Overcharge price	floor(BaseSellPrice * (1 + rate))
Pushcart speed	50 + 5*Lv % movement speed
Pushcart capacity	8000 weight, 100 slots
Item Appraisal	identifica item desconocido; 10 SP
Vending stacks	2 + Lv stacks
Vending SP	30 SP
Mammonite damage	(100 + 50*Lv)% ATK
Mammonite zeny	100 * Lv zeny
Mammonite SP	5 SP
10. Pseudocódigo compacto
// Merchant Pre-Renewal, primera clase solamente.
// Sin skill quests: Cart Revolution, Change Cart, Crazy Uproar,
// Open Buying Store, Cart Decoration.

int enlarge_weight_limit_bonus(int lv) {
    return 200 * lv; // +200..+2000
}

double merchant_trade_rate(int lv) {
    static const double rate[11] = {
        0.00,
        0.07, 0.09, 0.11, 0.13, 0.15,
        0.17, 0.19, 0.21, 0.23, 0.24
    };
    return rate[lv];
}

int discounted_buy_price(int base_price, int discount_lv) {
    double r = merchant_trade_rate(discount_lv);
    return std::max(1, (int)floor(base_price * (1.0 - r)));
}

int overcharged_sell_price(int base_sell_price, int overcharge_lv) {
    double r = merchant_trade_rate(overcharge_lv);
    return std::max(0, (int)floor(base_sell_price * (1.0 + r)));
}

int pushcart_movement_speed_percent(int lv) {
    return 50 + 5 * lv; // 55..100
}

int pushcart_weight_capacity() {
    return 8000;
}

int pushcart_distinct_slots() {
    return 100;
}

int vending_max_stacks(int lv) {
    return 2 + lv; // 3..12
}

int item_appraisal_sp_cost() {
    return 10;
}

int vending_sp_cost() {
    return 30;
}

int mammonite_ratio_percent(int lv) {
    return 100 + 50 * lv; // 150..600
}

int mammonite_zeny_cost(int lv) {
    return 100 * lv; // 100..1000
}

int mammonite_sp_cost() {
    return 5;
}
11. Validaciones importantes
Cart Revolution no va aquí.
Aunque muchos Merchants la usan como skill central de leveo, es quest skill. Queda fuera por tu regla.
Merchant normal tiene solo una skill ofensiva del árbol: Mammonite.
Sin Cart Revolution ni Crazy Uproar, el árbol normal es mayormente económico/utilitario.
Pushcart no aumenta el peso del personaje.
Añade inventario separado: 8000 weight y 100 slots.
Vending no usa inventario normal.
Vende desde el cart.
Discount y Overcharge no afectan comercio entre jugadores.
Solo tiendas NPC / venta a NPC.
Mammonite es daño físico melee.
Usa HIT/FLEE, DEF física, tamaño, elemento del arma y cartas como cualquier skill física.