package com.etema.ragnarmmo.entity.mob;

import com.etema.ragnarmmo.common.api.mobs.data.MobDefinition;
import com.etema.ragnarmmo.common.api.mobs.data.RagnarAggroType;
import com.etema.ragnarmmo.common.api.mobs.data.RagnarAiFlags;
import com.etema.ragnarmmo.common.api.mobs.data.RagnarMovementConfig;
import com.etema.ragnarmmo.common.api.mobs.data.RagnarMovementProfile;
import com.etema.ragnarmmo.common.api.mobs.data.RagnarLootBehavior;
import com.etema.ragnarmmo.common.api.mobs.data.RagnarMetamorphosis;
import com.etema.ragnarmmo.common.api.mobs.data.load.MobDefinitionRegistry;
import com.etema.ragnarmmo.common.init.RagnarEntities;
import com.etema.ragnarmmo.entity.mob.goal.RagnarLooterGoal;
import com.etema.ragnarmmo.entity.mob.goal.RagnarAerialRoamGoal;
import com.etema.ragnarmmo.entity.mob.goal.RagnarMetamorphosisGoal;
import com.etema.ragnarmmo.entity.mob.goal.RagnarHopRoamGoal;
import com.etema.ragnarmmo.entity.mob.goal.RagnarRetaliateWhenHurtGoal;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.FlyingMoveControl;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraft.world.item.ItemStack;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.ArrayList;
import java.util.List;
import org.jetbrains.annotations.Nullable;

public abstract class AbstractRagnarMobEntity extends PathfinderMob implements GeoEntity {
    private static final String ROBBED_ITEMS_TAG = "RobbedItems";
    private static final String HOME_ANCHOR_TAG = "HomeAnchor";

    protected static final RawAnimation PORING_IDLE = RawAnimation.begin().thenLoop("animation.poring.idle");
    protected static final RawAnimation PORING_WALK = RawAnimation.begin().thenLoop("animation.poring.walk");
    protected static final RawAnimation PORING_ATTACK = RawAnimation.begin().thenLoop("animation.poring.attack");

    protected static final RawAnimation LUNATIC_IDLE = RawAnimation.begin().thenLoop("animation.ragnarmmo.lunatic.idle");
    protected static final RawAnimation LUNATIC_WALK = RawAnimation.begin().thenLoop("animation.ragnarmmo.lunatic.walk");
    protected static final RawAnimation LUNATIC_ATTACK = RawAnimation.begin().thenLoop("animation.ragnarmmo.lunatic.attack");

    protected static final RawAnimation FABRE_IDLE = RawAnimation.begin().thenLoop("animation.ragnarmmo.fabre.idle");
    protected static final RawAnimation FABRE_WALK = RawAnimation.begin().thenLoop("animation.ragnarmmo.fabre.walk");
    protected static final RawAnimation FABRE_ATTACK = RawAnimation.begin().thenLoop("animation.ragnarmmo.fabre.attack");

    protected static final RawAnimation MUKA_IDLE = RawAnimation.begin().thenLoop("animation.ragnarmmo.muka.idle");
    protected static final RawAnimation MUKA_WALK = RawAnimation.begin().thenLoop("animation.ragnarmmo.muka.walk");
    protected static final RawAnimation MUKA_ATTACK = RawAnimation.begin().thenLoop("animation.ragnarmmo.muka.attack");

    protected static final RawAnimation PUPA_IDLE = RawAnimation.begin().thenLoop("animation.ragnarmmo.pupa.idle");

    protected static final RawAnimation CREAMY_IDLE = RawAnimation.begin().thenLoop("walk/stading");
    protected static final RawAnimation CREAMY_WALK = RawAnimation.begin().thenLoop("walk/stading");
    protected static final RawAnimation CREAMY_ATTACK = RawAnimation.begin().thenLoop("attack");

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private final List<ItemStack> robbedItems = new ArrayList<>();
    private @Nullable BlockPos homeAnchor;

    protected AbstractRagnarMobEntity(EntityType<? extends PathfinderMob> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return PathfinderMob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 8.0D)
                .add(Attributes.ATTACK_DAMAGE, 2.0D)
                .add(Attributes.FLYING_SPEED, 0.33D)
                .add(Attributes.MOVEMENT_SPEED, 0.18D)
                .add(Attributes.FOLLOW_RANGE, 12.0D);
    }

    @Override
    protected void registerGoals() {
        MobDefinition definition = mobDefinition();
        RagnarAiFlags ai = definition != null ? definition.ai() : null;
        if (ai == null) {
            registerLegacyGoals();
            return;
        }

        boolean canMove = ai.canMove() && !ai.immobile();
        RagnarMetamorphosis metamorphosis = definition != null ? definition.metamorphosis() : null;
        if (canMove) {
            this.goalSelector.addGoal(0, new FloatGoal(this));
        }

        if (metamorphosis != null) {
            this.goalSelector.addGoal(1, new RagnarMetamorphosisGoal(this, metamorphosis));
        }

        if (ai.canAttack()) {
            this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.0D, true));
        }

        if (ai.looter()) {
            this.goalSelector.addGoal(3, new RagnarLooterGoal(this, lootBehavior()));
        }

        Goal idleRoamGoal = createIdleRoamGoal();
        if (canMove && shouldAddIdleRoamGoal(ai) && idleRoamGoal != null) {
            this.goalSelector.addGoal(4, idleRoamGoal);
        }

        this.goalSelector.addGoal(5, new LookAtPlayerGoal(this, Player.class, 6.0F));
        this.goalSelector.addGoal(6, new RandomLookAroundGoal(this));

        if (ai.retaliates() && ai.canAttack()) {
            this.targetSelector.addGoal(1, new RagnarRetaliateWhenHurtGoal(this));
        }

        if (ai.aggroType() == RagnarAggroType.AGGRESSIVE) {
            this.targetSelector.addGoal(2, new net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal<Player>(
                    this,
                    Player.class,
                    true,
                    living -> living instanceof Player player && this.canAggroPlayer(player)));
        }
    }

    private void registerLegacyGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new WaterAvoidingRandomStrollGoal(this, 0.8D));
        this.goalSelector.addGoal(2, new LookAtPlayerGoal(this, Player.class, 6.0F));
        this.goalSelector.addGoal(3, new RandomLookAroundGoal(this));
    }

    @Override
    protected net.minecraft.world.entity.ai.navigation.PathNavigation createNavigation(Level level) {
        if (usesFlyingMovement()) {
            FlyingPathNavigation navigation = new FlyingPathNavigation(this, level);
            navigation.setCanFloat(true);
            return navigation;
        }
        return super.createNavigation(level);
    }

    @Override
    public int getMaxFallDistance() {
        return usesFlyingMovement() ? 0 : super.getMaxFallDistance();
    }

    @Override
    public boolean causeFallDamage(float fallDistance, float multiplier, DamageSource source) {
        if (usesFlyingMovement()) {
            return false;
        }
        return super.causeFallDamage(fallDistance, multiplier, source);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "movement", 5, state -> state.setAndContinue(animationForType())));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }

    protected RawAnimation animationForType() {
        EntityType<?> type = this.getType();
        if (type == RagnarEntities.PORING.get()
                || type == RagnarEntities.POPORING.get()
                || type == RagnarEntities.DROP.get()
                || type == RagnarEntities.MARIN.get()) {
            return animationForPoringFamily();
        }
        if (type == RagnarEntities.LUNATIC.get()) {
            return animationForLunatic();
        }
        if (type == RagnarEntities.FABRE.get()) {
            return animationForFabre();
        }
        if (type == RagnarEntities.MUKA.get()) {
            return animationForMuka();
        }
        if (type == RagnarEntities.PUPA.get()) {
            return animationForPupa();
        }
        if (type == RagnarEntities.CREAMY.get() || type == RagnarEntities.CREAMY_FEAR.get()) {
            return animationForCreamy();
        }
        return PORING_IDLE;
    }

    public MobDefinition mobDefinition() {
        ResourceLocation entityId = ForgeRegistries.ENTITY_TYPES.getKey(this.getType());
        if (entityId == null) {
            return null;
        }
        return MobDefinitionRegistry.getInstance().getDefinition(entityId).orElse(null);
    }

    public RagnarAiFlags aiFlags() {
        MobDefinition definition = mobDefinition();
        return definition != null ? definition.ai() : null;
    }

    public boolean canAttack() {
        RagnarAiFlags ai = aiFlags();
        return ai == null || ai.canAttack();
    }

    public boolean canMove() {
        RagnarAiFlags ai = aiFlags();
        return ai == null || (ai.canMove() && !ai.immobile());
    }

    public RagnarLootBehavior lootBehavior() {
        MobDefinition definition = mobDefinition();
        if (definition == null) {
            return null;
        }
        RagnarLootBehavior behavior = definition.lootBehavior();
        RagnarAiFlags ai = definition.ai();
        if (behavior == null && ai != null && ai.looter()) {
            return RagnarLootBehavior.DEFAULT;
        }
        return behavior;
    }

    public RagnarMovementConfig movementConfig() {
        MobDefinition definition = mobDefinition();
        if (definition == null || definition.movement() == null) {
            return RagnarMovementConfig.defaults();
        }
        return definition.movement();
    }

    public RagnarMovementProfile movementProfile() {
        return movementConfig().profile();
    }

    public BlockPos movementAnchor() {
        if (this.homeAnchor == null) {
            this.homeAnchor = this.blockPosition();
        }
        return this.homeAnchor;
    }

    public boolean canAggroPlayer(Player player) {
        return player != null
                && player.isAlive()
                && !player.isSpectator()
                && !player.isCreative();
    }

    public void addRobbedItem(ItemStack stack) {
        if (stack != null && !stack.isEmpty()) {
            this.robbedItems.add(stack.copy());
        }
    }

    public boolean hasRobbedItems() {
        return !this.robbedItems.isEmpty();
    }

    public List<ItemStack> robbedItemsSnapshot() {
        List<ItemStack> snapshot = new ArrayList<>(this.robbedItems.size());
        for (ItemStack stack : this.robbedItems) {
            snapshot.add(stack.copy());
        }
        return snapshot;
    }

    public void clearRobbedItems() {
        this.robbedItems.clear();
    }

    public void markMetamorphosisRemoval() {
        // Kept for future removal-cause differentiation.
    }

    public void transferRobbedItemsTo(LivingEntity target) {
        if (target instanceof AbstractRagnarMobEntity other) {
            other.robbedItems.clear();
            for (ItemStack stack : this.robbedItems) {
                other.robbedItems.add(stack.copy());
            }
            this.robbedItems.clear();
        } else {
            this.robbedItems.clear();
        }
    }

    protected void dropRobbedItems() {
        if (this.level().isClientSide || this.robbedItems.isEmpty()) {
            return;
        }

        for (ItemStack stack : this.robbedItems) {
            if (!stack.isEmpty()) {
                this.spawnAtLocation(stack.copy());
            }
        }
        this.robbedItems.clear();
    }

    @Override
    protected void dropCustomDeathLoot(net.minecraft.world.damagesource.DamageSource source, int lootingMultiplier, boolean wasRecentlyHit) {
        super.dropCustomDeathLoot(source, lootingMultiplier, wasRecentlyHit);
        RagnarLootBehavior loot = lootBehavior();
        boolean dropLootedItems = loot == null || loot.dropLootedItemsOnDeath();
        if (dropLootedItems) {
            dropRobbedItems();
        } else {
            this.robbedItems.clear();
        }
    }

    @Override
    public void remove(RemovalReason reason) {
        if (reason == RemovalReason.DISCARDED && !this.level().isClientSide) {
            this.robbedItems.clear();
        }
        super.remove(reason);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        if (this.homeAnchor != null) {
            tag.putLong(HOME_ANCHOR_TAG, this.homeAnchor.asLong());
        }
        if (!this.robbedItems.isEmpty()) {
            ListTag list = new ListTag();
            for (ItemStack stack : this.robbedItems) {
                CompoundTag itemTag = new CompoundTag();
                stack.save(itemTag);
                list.add(itemTag);
            }
            tag.put(ROBBED_ITEMS_TAG, list);
        }
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.contains(HOME_ANCHOR_TAG, net.minecraft.nbt.Tag.TAG_LONG)) {
            this.homeAnchor = BlockPos.of(tag.getLong(HOME_ANCHOR_TAG));
        }
        this.robbedItems.clear();
        if (tag.contains(ROBBED_ITEMS_TAG, net.minecraft.nbt.Tag.TAG_LIST)) {
            ListTag list = tag.getList(ROBBED_ITEMS_TAG, net.minecraft.nbt.Tag.TAG_COMPOUND);
            for (int i = 0; i < list.size(); i++) {
                ItemStack stack = ItemStack.of(list.getCompound(i));
                if (!stack.isEmpty()) {
                    this.robbedItems.add(stack);
                }
            }
        }
    }

    @Override
    public void tick() {
        super.tick();
        if (this.homeAnchor == null) {
            this.homeAnchor = this.blockPosition();
        }
    }

    protected RawAnimation animationForPoringFamily() {
        return selectStatefulAnimation(PORING_IDLE, PORING_WALK, PORING_ATTACK);
    }

    protected RawAnimation animationForLunatic() {
        return selectStatefulAnimation(LUNATIC_IDLE, LUNATIC_WALK, LUNATIC_ATTACK);
    }

    protected RawAnimation animationForFabre() {
        return selectStatefulAnimation(FABRE_IDLE, FABRE_WALK, FABRE_ATTACK);
    }

    protected RawAnimation animationForMuka() {
        return selectStatefulAnimation(MUKA_IDLE, MUKA_WALK, MUKA_ATTACK);
    }

    protected RawAnimation animationForPupa() {
        return PUPA_IDLE;
    }

    protected RawAnimation animationForCreamy() {
        return selectStatefulAnimation(CREAMY_IDLE, CREAMY_WALK, CREAMY_ATTACK);
    }

    protected RawAnimation selectStatefulAnimation(RawAnimation idle, RawAnimation walk, RawAnimation attack) {
        if (shouldPlayAttackAnimation()) {
            return attack;
        }
        if (isMoving()) {
            return walk;
        }
        return idle;
    }

    protected boolean isMoving() {
        return this.getDeltaMovement().horizontalDistanceSqr() > 1.0E-4D;
    }

    protected boolean shouldPlayAttackAnimation() {
        var target = this.getTarget();
        return target != null && target.isAlive() && this.distanceToSqr(target) <= 9.0D;
    }

    protected boolean shouldAddIdleRoamGoal(RagnarAiFlags ai) {
        return ai != null && ai.canMove() && !ai.immobile() && movementProfile() != RagnarMovementProfile.STATIONARY;
    }

    protected Goal createIdleRoamGoal() {
        RagnarMovementConfig movement = movementConfig();
        return switch (movement.profile()) {
            case SLIME_HOP -> new RagnarHopRoamGoal(this, movement, 0.32D, 20, 60);
            case RABBIT_HOP -> new RagnarHopRoamGoal(this, movement, 0.38D, 10, 30);
            case GROUND_CRAWL -> new WaterAvoidingRandomStrollGoal(this, movement.speedClass().speed());
            case BUTTERFLY_FLIGHT -> new RagnarAerialRoamGoal(this, movement);
            case STATIONARY -> null;
        };
    }

    protected boolean usesFlyingMovement() {
        return movementProfile() == RagnarMovementProfile.BUTTERFLY_FLIGHT;
    }
}
