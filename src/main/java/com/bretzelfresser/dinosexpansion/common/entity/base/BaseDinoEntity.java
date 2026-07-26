package com.bretzelfresser.dinosexpansion.common.entity.base;

import com.bretzelfresser.dinosexpansion.common.entity.ai.DinoBrain;
import com.bretzelfresser.dinosexpansion.common.food.DinoFoodEntry;
import com.bretzelfresser.dinosexpansion.common.init.*;
import com.bretzelfresser.dinosexpansion.common.menu.DinoContainerMenu;
import com.bretzelfresser.dinosexpansion.util.NbtUtils;
import com.bretzelfresser.dinosexpansion.util.PlayerTeamUtils;
import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Dynamic;
import net.minecraft.util.Unit;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.*;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.sensing.SensorType;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.EnumMap;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Predicate;

public abstract class BaseDinoEntity extends Animal implements GeoEntity, ContainerListener, OwnableEntity {
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    private static final EntityDataAccessor<Float> CURRENT_TORPOR = SynchedEntityData.defineId(BaseDinoEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> CURRENT_HUNGER = SynchedEntityData.defineId(BaseDinoEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Integer> DINO_FLAGS = SynchedEntityData.defineId(BaseDinoEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Float> TAMING_PROGRESS = SynchedEntityData.defineId(BaseDinoEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> TAMING_EFFECTIVENESS = SynchedEntityData.defineId(BaseDinoEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Optional<UUID>> UNCONSCIOUS_OWNER = SynchedEntityData.defineId(BaseDinoEntity.class, EntityDataSerializers.OPTIONAL_UUID);
    private static final EntityDataAccessor<Optional<UUID>> OWNER = SynchedEntityData.defineId(BaseDinoEntity.class, EntityDataSerializers.OPTIONAL_UUID);
    private static final EntityDataAccessor<Byte> GENDER = SynchedEntityData.defineId(BaseDinoEntity.class, EntityDataSerializers.BYTE);

    protected final SimpleContainer inventory;

    // Attack duration tracker (server ticks)
    protected int attackTicks = 0;
    protected boolean isAttacking = false;

    protected final SleepBehaviour sleepBehaviour;
    protected final TamingBehaviour tamingBehaviour;
    protected final SurvivalBehaviour survivalBehaviour;

    private int sleepParticleCooldown = 0;

    protected BaseDinoEntity(EntityType<? extends BaseDinoEntity> entityType, Level level) {
        super(entityType, level);
        this.sleepBehaviour = new SleepBehaviour(this, SleepRhythm.DIURNAL);
        this.tamingBehaviour = new TamingBehaviour(this);
        this.survivalBehaviour = new SurvivalBehaviour(this);
        this.inventory = new SimpleContainer(38); // Slot 0: Saddle, Slot 1: Armor, Slots 2-37: Main Dino Inventory
        this.inventory.addListener(this);

        // Randomize gender on server spawn
        if (!level.isClientSide()) {
            this.setGender(level.random.nextBoolean() ? DinoGender.MALE : DinoGender.FEMALE);
        }
    }

    public EnumMap<DinoEquipment, Predicate<ItemStack>> getEquipments(){
        return new EnumMap<>(DinoEquipment.class);
    }

    public SleepBehaviour getSleepBehaviour() {
        return this.sleepBehaviour;
    }

    public TamingBehaviour getTamingBehaviour() {
        return this.tamingBehaviour;
    }

    public SurvivalBehaviour getSurvivalBehaviour() {
        return this.survivalBehaviour;
    }

    public DinoGender getGender() {
        return DinoGender.byId(this.entityData.get(GENDER));
    }

    public void setGender(DinoGender gender) {
        this.entityData.set(GENDER, (byte) gender.ordinal());
    }

    public static AttributeSupplier.Builder createDinoDefaultAttributes() {
        return Animal.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 20.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.25D)
                .add(Attributes.ATTACK_DAMAGE, 3.0D)
                .add(ModAttributes.MAX_TORPOR, 100.0D)
                .add(ModAttributes.MAX_HUNGER, 100.0D)
                .add(ModAttributes.HUNGER_DECREASE)
                .add(ModAttributes.TORPOR_DECREASE)
                .add(ModAttributes.TORPOR_WAKE_UP_THRESHOLD)
                ;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(CURRENT_TORPOR, 0.0f);
        builder.define(CURRENT_HUNGER, 100.0f);
        builder.define(DINO_FLAGS, 0);
        builder.define(TAMING_PROGRESS, 0.0f);
        builder.define(TAMING_EFFECTIVENESS, 1.0f);
        builder.define(OWNER, Optional.empty());
        builder.define(UNCONSCIOUS_OWNER, Optional.empty());
        builder.define(GENDER, (byte) 0);
    }

    /**
     *
     * @param player the player this should be owned to, null of set to untamed, will overwrite existing owners
     */
    public void setTamedBy(@Nullable Player player) {
        this.setTamedBy(player == null ? null : player.getUUID());
    }

    protected void setTamedBy(@Nullable UUID player) {
        this.entityData.set(OWNER, Optional.ofNullable(player));
    }

    public boolean isTamed() {
        return this.getOwnerUUID() != null;
    }

    @Override
    public @Nullable UUID getOwnerUUID() {
        return this.entityData.get(OWNER).orElse(null);
    }

    public Optional<UUID> getUnconsciousOwnerUUID() {
        return this.entityData.get(UNCONSCIOUS_OWNER);
    }

    public Optional<LivingEntity> getUnconsciousOwner() {
        return this.entityData.get(UNCONSCIOUS_OWNER).map(id -> level().getPlayerByUUID(id));
    }

    // Getters and Setters for stats
    public float getTorpor() {
        return this.entityData.get(CURRENT_TORPOR);
    }

    public float getMissingTorpor() {
        float max = (float) this.getAttributeValue(ModAttributes.MAX_TORPOR);
        return Math.max(0, max - getTorpor());
    }

    public void setTorpor(float val) {
        float max = (float) this.getAttributeValue(ModAttributes.MAX_TORPOR);
        this.entityData.set(CURRENT_TORPOR, Math.clamp(val, 0.0f, max));
    }

    public float getHunger() {
        return this.entityData.get(CURRENT_HUNGER);
    }

    public void setHunger(float val) {
        float max = (float) this.getAttributeValue(ModAttributes.MAX_HUNGER);
        this.entityData.set(CURRENT_HUNGER, Math.clamp(val, 0.0f, max));
    }

    public float getMissingHunger() {
        float max = (float) this.getAttributeValue(ModAttributes.MAX_HUNGER);
        return Math.max(0, max - getHunger());
    }

    public boolean canEat(DinoFoodEntry.FoodValues value){
        return getMissingHunger() >= value.hungerValue();
    }

    protected boolean getDinoFlag(int bitPos) {
        if (bitPos < 0 || bitPos > 31) {
            throw new IllegalArgumentException("Bit position must be between 0 and 31");
        }
        return (this.entityData.get(DINO_FLAGS) & (1 << bitPos)) != 0;
    }

    /**
     * 0 = unconscious
     * 1 = saddled
     * 2 = sleep
     * 3 = saddled
     * 4 = armored
     *
     * @param bitPos
     * @param value
     */
    protected void setDinoFlag(int bitPos, boolean value) {
        if (bitPos < 0 || bitPos > 31) {
            throw new IllegalArgumentException("Bit position must be between 0 and 31");
        }
        int flags = this.entityData.get(DINO_FLAGS);
        if (value) {
            flags |= (1 << bitPos);
        } else {
            flags &= ~(1 << bitPos);
        }
        this.entityData.set(DINO_FLAGS, flags);
    }

    public void setUnconsciousFrom(@Nullable Player owner) {
        setUnconsciousFrom(owner == null ? null : owner.getUUID());
    }

    /**
     *
     * @param entity only use UUIDS from players, cause there is no way to retrieve another entity from UUID, null to wake up the entity
     */
    protected void setUnconsciousFrom(@Nullable UUID entity) {
        this.entityData.set(UNCONSCIOUS_OWNER, Optional.ofNullable(entity));
        this.setUnconscious(entity != null);
    }

    public boolean isUnconscious() {
        return this.getDinoFlag(0);
    }

    private void setUnconscious(boolean unconscious) {
        this.setDinoFlag(0, unconscious);
        if (unconscious) {
            // Remove passengers when falling unconscious
            this.ejectPassengers();
            // Stop any active navigation/movement immediately
            this.getNavigation().stop();
            // Write unconscious status to brain memory
            this.getBrain().setMemory(ModMemoryModules.UNCONSCIOUS.get(), Unit.INSTANCE);
        } else {
            // Erase unconscious status from brain memory
            this.getBrain().eraseMemory(ModMemoryModules.UNCONSCIOUS.get());
            if (getUnconsciousOwnerUUID().isPresent()) {
                this.entityData.set(UNCONSCIOUS_OWNER, Optional.empty());
            }
        }
    }

    public float getTamingProgress() {
        return this.entityData.get(TAMING_PROGRESS);
    }

    public void setTamingProgress(float val) {
        this.entityData.set(TAMING_PROGRESS, Math.clamp(val, 0.0f, 1.0f));
    }

    public float getTamingEffectiveness() {
        return this.entityData.get(TAMING_EFFECTIVENESS);
    }

    public void setTamingEffectiveness(float val) {
        this.entityData.set(TAMING_EFFECTIVENESS, Math.clamp(val, 0.0f, 1.0f));
    }

    public boolean isSaddled() {
        return this.getDinoFlag(1);
    }

    public void setSaddled(boolean saddled) {
        this.setDinoFlag(1, saddled);
    }

    public int getInventorySize() {
        // If saddled, we have 36 slots available
        return this.isSaddled() ? 36 : 0;
    }

    public SimpleContainer getInventory() {
        return this.inventory;
    }

    @Override
    public void containerChanged(Container container) {
        ItemStack saddle = container.getItem(0);
        this.setSaddled(!saddle.isEmpty() && saddle.is(ModItems.TEST_DINO_SADDLE.get()));
    }

    @Override
    protected Brain.Provider<?> brainProvider() {
        return Brain.provider(
                DinoBrain.baseDinoMemoryModules().build(),
                ImmutableList.of(
                        SensorType.NEAREST_PLAYERS,
                        SensorType.NEAREST_LIVING_ENTITIES
                )
        );
    }

    @SuppressWarnings("unchecked")
    @Override
    protected Brain<?> makeBrain(Dynamic<?> dynamic) {
        return DinoBrain.makeBrain((Brain<BaseDinoEntity>) this.brainProvider().makeBrain(dynamic));
    }

    @SuppressWarnings("unchecked")
    @Override
    public Brain<BaseDinoEntity> getBrain() {
        return (Brain<BaseDinoEntity>) super.getBrain();
    }

    @Override
    protected void customServerAiStep() {
        this.level().getProfiler().push("dinoBrain");
        this.getBrain().tick((ServerLevel) this.level(), this);
        this.level().getProfiler().pop();

        DinoBrain.updateActivity(this);
        super.customServerAiStep();
    }

    @Override
    public void aiStep() {
        super.aiStep();

        if (!this.level().isClientSide()) {
            this.sleepBehaviour.tick();
            this.survivalBehaviour.tick();

            // Attack ticks handling
            if (this.isAttacking) {
                this.attackTicks--;
                if (this.attackTicks == 10) { // Perform damage sweep at frame 10 (halfway through 20 tick animation)
                    this.doMeleeDamage();
                }
                if (this.attackTicks <= 0) {
                    this.isAttacking = false;
                }
            }
        } else if (this.sleepBehaviour.isSleeping()) {
            spawnSleepingParticles();
        }
    }

    @Override
    public boolean isFood(ItemStack stack) {
        return this.tamingBehaviour.isPreferredFood(stack);
    }

    @Override
    protected void actuallyHurt(DamageSource damageSource, float damageAmount) {
        if (this.sleepBehaviour.isSleeping() && damageAmount > 0) {
            this.sleepBehaviour.forceAwake(200 + this.random.nextInt(200));
        }
        this.survivalBehaviour.onHurt(damageSource, damageAmount);
        super.actuallyHurt(damageSource, damageAmount);
    }

    public void applyNarcotics(float amount) {
        // Narcotics increase torpor
        this.setTorpor(this.getTorpor() + amount);
    }

    /**
     * applys torpor value to the entity but in a stacked way.
     * this value will be added to a buffer, and then slowly added to the real entity torpor, this also prevents the entity from reducing torpor while something is buffered
     *
     * @param amount
     */
    public void applyBufferedNarcotics(float amount) {
        this.survivalBehaviour.applyBufferedNarcotics(amount);
    }

    protected void doMeleeDamage() {
        // Deal attack damage to attack target
        if (this.getTarget() != null) {
            this.doHurtTarget(this.getTarget());
        }
    }

    public void triggerBiteAttack() {
        if (!this.level().isClientSide()) {
            this.triggerAnim("dino_controller", "attack");
            this.isAttacking = true;
            this.attackTicks = 20; // 1 second total
        }
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (this.isUnconscious()) {
            if (!this.canPlayerAccess(player)) {
                if (!this.level().isClientSide()) {
                    player.sendSystemMessage(Component.literal("You do not have access to this unconscious dinosaur!"));
                }
                return InteractionResult.FAIL;
            }

            // If unconscious, we can force-feed narcotics or preferred food directly
            if (!stack.isEmpty() && stack.has(ModDataComponents.NARCOTIC_VALUE.get())) {
                float val = stack.getOrDefault(ModDataComponents.NARCOTIC_VALUE.get(), 0f);
                this.applyNarcotics((int) val);
                if (!player.getAbilities().instabuild) {
                    stack.shrink(1);
                }
                return InteractionResult.SUCCESS;
            }

            // Otherwise, open its inventory/taming menu
            if (!this.level().isClientSide()) {
                this.openDinoInventory(player);
            }
            return InteractionResult.sidedSuccess(this.level().isClientSide());
        }

        // Tamed and active interaction
        if (this.isTamed()) {
            if (!this.canPlayerAccess(player)) {
                if (!this.level().isClientSide()) {
                    player.sendSystemMessage(Component.literal("You do not own this dinosaur!"));
                }
                return InteractionResult.FAIL;
            }

            if (player.isSecondaryUseActive()) {
                // Shift-right click: open inventory
                if (!this.level().isClientSide()) {
                    this.openDinoInventory(player);
                }
                return InteractionResult.sidedSuccess(this.level().isClientSide());
            } else if (this.isSaddled() && !this.isBaby()) {
                // Right click: ride
                if (!this.level().isClientSide()) {
                    player.startRiding(this);
                }
                return InteractionResult.sidedSuccess(this.level().isClientSide());
            }
        }

        return super.mobInteract(player, hand);
    }

    public void openDinoInventory(Player player) {
        if (player instanceof ServerPlayer serverPlayer) {
            serverPlayer.openMenu(new MenuProvider() {
                @Override
                public Component getDisplayName() {
                    return BaseDinoEntity.this.getDisplayName();
                }

                @Nullable
                @Override
                public AbstractContainerMenu createMenu(int windowId, Inventory playerInv, Player p) {
                    return new DinoContainerMenu(windowId, playerInv, BaseDinoEntity.this.getId());
                }
            }, buf -> buf.writeInt(this.getId()));
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putFloat("Torpor", this.getTorpor());
        tag.putFloat("Hunger", this.getHunger());
        tag.putBoolean("Unconscious", this.isUnconscious());
        tag.putFloat("TamingProgress", this.getTamingProgress());
        tag.putFloat("TamingEffectiveness", this.getTamingEffectiveness());
        tag.putByte("Gender", (byte) this.getGender().ordinal());
        this.survivalBehaviour.save(tag);
        NbtUtils.putIfPresent(tag, "owner", CompoundTag::putUUID, this.entityData.get(OWNER));
        NbtUtils.putIfPresent(tag, "unconscious_owner", CompoundTag::putUUID, this.entityData.get(UNCONSCIOUS_OWNER));
        var inventoryTag = new CompoundTag();
        ContainerHelper.saveAllItems(inventoryTag, this.inventory.getItems(), this.level().registryAccess());
        tag.put("inventory", inventoryTag);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        NbtUtils.setIfExists(tag, "Torpor", CompoundTag::getFloat, this::setTorpor);
        NbtUtils.setIfExists(tag, "Hunger", CompoundTag::getFloat, this::setHunger);
        NbtUtils.setIfExists(tag, "TamingProgress", CompoundTag::getFloat, this::setTamingProgress);
        NbtUtils.setIfExists(tag, "TamingEffectiveness", CompoundTag::getFloat, this::setTamingEffectiveness);
        NbtUtils.setIfExists(tag, "Unconscious", CompoundTag::getBoolean, this::setUnconscious);
        NbtUtils.setIfExists(tag, "Gender", CompoundTag::getByte, b -> this.setGender(DinoGender.byId(b)));
        this.survivalBehaviour.load(tag);
        NbtUtils.setIfExists(tag, "inventory", CompoundTag::getCompound, t -> ContainerHelper.loadAllItems(t, inventory.getItems(), level().registryAccess()));

        NbtUtils.setIfExists(tag, "owner", CompoundTag::getUUID, uuid -> entityData.set(OWNER, Optional.of(uuid)));
        NbtUtils.setIfExists(tag, "unconscious_owner", CompoundTag::getUUID, this::setUnconsciousFrom);
    }

    @Override
    public boolean isSleeping() {
        return this.sleepBehaviour.isSleeping();
    }

    protected void spawnSleepingParticles() {
        if (!this.sleepBehaviour.isSleeping()) return;
        if (sleepParticleCooldown > 0) {
            sleepParticleCooldown--;
            return;
        }
        sleepParticleCooldown = 40 + this.random.nextInt(40);
        double x = this.getX();
        double y = this.getY() + this.getBbHeight() + 0.15D;
        double z = this.getZ();
        this.level().addParticle(ModParticles.SLEEPING_PARTICLES.get(), x, y, z, 0f, 0.4f, 0);
    }

    @Nullable
    @Override
    public AgeableMob getBreedOffspring(ServerLevel level, AgeableMob otherParent) {
        return null;
    }

    public boolean canPlayerAccess(Player player) {
        // OP bypass
        if (player.hasPermissions(2)) {
            return true;
        }

        UUID targetUUID = null;
        if (this.isUnconscious()) {
            targetUUID = this.getUnconsciousOwnerUUID().orElse(null);
        } else if (this.isTamed()) {
            targetUUID = this.getOwnerUUID();
        }

        if (targetUUID == null) {
            return true;
        }

        return PlayerTeamUtils.arePlayersInSameTeam(this.level(), player.getUUID(), targetUUID);
    }

    // GeckoLib Implementation
    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }
}
