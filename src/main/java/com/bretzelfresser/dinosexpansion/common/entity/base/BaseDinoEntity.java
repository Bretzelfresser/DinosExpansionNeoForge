package com.bretzelfresser.dinosexpansion.common.entity.base;

import com.bretzelfresser.dinosexpansion.common.chest.DinoChestCache;
import com.bretzelfresser.dinosexpansion.common.entity.ai.DinoBrain;
import com.bretzelfresser.dinosexpansion.common.entity.inventory.DinoEquipmentInventory;
import com.bretzelfresser.dinosexpansion.common.entity.inventory.DinoInventory;
import com.bretzelfresser.dinosexpansion.common.entity.inventory.DynamicInventory;
import com.bretzelfresser.dinosexpansion.common.food.DinoFoodEntry;
import com.bretzelfresser.dinosexpansion.common.init.ModAttributes;
import com.bretzelfresser.dinosexpansion.common.init.ModDataComponents;
import com.bretzelfresser.dinosexpansion.common.init.ModMemoryModules;
import com.bretzelfresser.dinosexpansion.common.init.ModParticles;
import com.bretzelfresser.dinosexpansion.common.menu.DinoContainerMenu;
import com.bretzelfresser.dinosexpansion.common.network.DinoEquipmentSyncPayload;
import com.bretzelfresser.dinosexpansion.config.Config;
import com.bretzelfresser.dinosexpansion.util.NbtUtils;
import com.bretzelfresser.dinosexpansion.util.PlayerTeamUtils;
import com.bretzelfresser.dinosexpansion.util.RandomUtils;
import com.mojang.serialization.Dynamic;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Unit;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.EnumMap;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Predicate;

public abstract class BaseDinoEntity extends Animal implements GeoEntity, OwnableEntity, Saddleable {
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    private static final EntityDataAccessor<Float> CURRENT_TORPOR = SynchedEntityData.defineId(BaseDinoEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> CURRENT_HUNGER = SynchedEntityData.defineId(BaseDinoEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Integer> DINO_FLAGS = SynchedEntityData.defineId(BaseDinoEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Float> TAMING_PROGRESS = SynchedEntityData.defineId(BaseDinoEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> TAMING_EFFECTIVENESS = SynchedEntityData.defineId(BaseDinoEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Optional<UUID>> UNCONSCIOUS_OWNER = SynchedEntityData.defineId(BaseDinoEntity.class, EntityDataSerializers.OPTIONAL_UUID);
    private static final EntityDataAccessor<Optional<UUID>> OWNER = SynchedEntityData.defineId(BaseDinoEntity.class, EntityDataSerializers.OPTIONAL_UUID);
    private static final EntityDataAccessor<Byte> GENDER = SynchedEntityData.defineId(BaseDinoEntity.class, EntityDataSerializers.BYTE);
    private static final EntityDataAccessor<Integer> DINO_LEVEL = SynchedEntityData.defineId(BaseDinoEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Float> DINO_XP = SynchedEntityData.defineId(BaseDinoEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Integer> TAMED_LEVEL_POINTS = SynchedEntityData.defineId(BaseDinoEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> AVAILABLE_POINTS = SynchedEntityData.defineId(BaseDinoEntity.class, EntityDataSerializers.INT);

    protected final DinoInventory inventory;

    // Attack duration tracker (server ticks)
    protected int attackTicks = 0;
    protected boolean isAttacking = false;

    protected final SleepBehaviour sleepBehaviour;
    protected final TamingBehaviour tamingBehaviour;
    protected final SurvivalBehaviour survivalBehaviour;

    private int sleepParticleCooldown = 0;

    protected final EnumMap<DinoStat, Integer> statPoints = new EnumMap<>(DinoStat.class);

    protected BaseDinoEntity(EntityType<? extends BaseDinoEntity> entityType, Level level) {
        this(entityType, level, 2);
    }


    protected BaseDinoEntity(EntityType<? extends BaseDinoEntity> entityType, Level level, int basInventorySize) {
        super(entityType, level);
        for (DinoStat stat : DinoStat.values()) {
            this.statPoints.put(stat, 0);
        }
        this.sleepBehaviour = new SleepBehaviour(this, SleepRhythm.DIURNAL);
        this.tamingBehaviour = new TamingBehaviour(this);
        this.survivalBehaviour = new SurvivalBehaviour(this);
        this.inventory = new DinoInventory(this, basInventorySize);
        if (!level.isClientSide()) {
            this.getEquipmentInventory().addListener(this::syncEquipment);
        }
        this.getEquipmentInventory().addListener(equipment -> {
            //only play the sound if we actually add/replace a saddle
            if (equipment == DinoEquipment.SADDLE && isSaddled())
                this.playSound(getSaddleSoundEvent(), 0.5F, 1.0F);
        });

        // Randomize gender on server spawn
        if (!level.isClientSide()) {
            this.setGender(level.random.nextBoolean() ? DinoGender.MALE : DinoGender.FEMALE);
        }
    }

    public EnumMap<DinoEquipment, Predicate<ItemStack>> getEquipments() {
        return new EnumMap<>(DinoEquipment.class);
    }

    public boolean isValidChest(ItemStack stack) {
        return DinoChestCache.isValidChest(this.getType(), stack, this.level().registryAccess());
    }

    public int getChestSize(ItemStack stack) {
        return Math.round((float) this.getAttributeValue(ModAttributes.CARRYING_CAPACITY)) +
                DinoChestCache.getSlotsFor(this.getType(), stack, this.level().registryAccess()).orElse(0);
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
                .add(ModAttributes.CARRYING_CAPACITY, 4.0D)
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
        builder.define(DINO_LEVEL, 1);
        builder.define(DINO_XP, 0.0f);
        builder.define(TAMED_LEVEL_POINTS, 0);
        builder.define(AVAILABLE_POINTS, 0);
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

    /**
     *
     * @return true when this entity is unconscious and currently on the progress of taming
     */
    public boolean currentlyTaming() {
        return !isTamed() && isUnconscious() && getUnconsciousOwner().isPresent();
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

    public boolean isChested() {
        return this.getEquipmentInventory().hasEquipment(DinoEquipment.CHEST) && !this.getEquipmentInventory().getEquipment(DinoEquipment.CHEST).isEmpty();
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

    public int getDinoLevel() {
        return this.entityData.get(DINO_LEVEL);
    }

    public void setDinoLevel(int val) {
        this.entityData.set(DINO_LEVEL, val);
    }

    public float getDinoXp() {
        return this.entityData.get(DINO_XP);
    }

    public void setDinoXp(float val) {
        this.entityData.set(DINO_XP, val);
    }

    public int getTamedLevelPoints() {
        return this.entityData.get(TAMED_LEVEL_POINTS);
    }

    public void setTamedLevelPoints(int val) {
        this.entityData.set(TAMED_LEVEL_POINTS, val);
    }

    public int getAvailablePoints() {
        return this.entityData.get(AVAILABLE_POINTS);
    }

    public void setAvailablePoints(int val) {
        this.entityData.set(AVAILABLE_POINTS, val);
    }

    public int getStatPoints(DinoStat stat) {
        return this.statPoints.getOrDefault(stat, 0);
    }

    public void setStatPoints(DinoStat stat, int val) {
        this.statPoints.put(stat, val);
    }

    public void addStatPoints(DinoStat stat, int val) {
        this.statPoints.put(stat, statPoints.getOrDefault(stat, 0) + val);
    }

    public void distributeWildPoints(int points) {
        for (int i = 0; i < points; i++) {
            this.addStatPoints(DinoStat.sampleWeightedRandom(this.random), 1);
        }
        this.updateAttributesFromLevels();
    }

    public void updateAttributesFromLevels() {
        for (var entry : this.statPoints.entrySet()) {
            entry.getKey().apply(this, entry.getValue());
        }

        this.updateInventorySizeFromAttributes();
    }

    public void updateInventorySizeFromAttributes() {
        var chestStack = this.getEquipmentInventory().getEquipment(DinoEquipment.CHEST);
        var equipmentAddition = DinoChestCache.getSlotsFor(this.getType(), chestStack, this.level().registryAccess()).orElse(0);
        int newSize = (int) this.getAttributeValue(ModAttributes.CARRYING_CAPACITY) + equipmentAddition;
        if (this.inventory.getChestInventory().getSlots() != newSize) {
            var items = this.inventory.updateInventorySize(newSize);
            if (!this.level().isClientSide()) {
                for (ItemStack item : items) {
                    this.spawnAtLocation(item, 1.0f);
                }
            }
        }
    }

    public float getXpNeededForNextLevel() {
        int currentTamed = this.getTamedLevelPoints();
        return 100.0f + currentTamed * 50.0f;
    }

    public void gainXp(float amount) {
        if (!this.isTamed()) return;

        float currentXp = this.getDinoXp() + amount;
        float needed = this.getXpNeededForNextLevel();

        boolean leveledUp = false;
        while (currentXp >= needed) {
            currentXp -= needed;
            this.setTamedLevelPoints(this.getTamedLevelPoints() + 1);
            this.setDinoLevel(this.getDinoLevel() + 1);
            this.setAvailablePoints(this.getAvailablePoints() + 1);
            needed = this.getXpNeededForNextLevel();
            leveledUp = true;
        }

        this.setDinoXp(currentXp);

        if (leveledUp && !this.level().isClientSide()) {
            this.level().playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.PLAYER_LEVELUP, SoundSource.NEUTRAL, 1.0f, 1.0f);
        }
    }

    public void upgradeStat(DinoStat stat) {
        if (this.getAvailablePoints() <= 0) return;

        addStatPoints(stat, 1);

        this.setAvailablePoints(this.getAvailablePoints() - 1);
        this.updateAttributesFromLevels();
    }

    public void onTameCompleted(float effectiveness, @Nullable UUID owner) {
        this.setTamedBy(owner);
        this.setUnconsciousFrom((UUID) null); // Wake up
        this.survivalBehaviour.setStackedTorpor(0f);//ensure no stacked torpor is left so it directly goes back to sleep

        // Add taming bonus levels
        int currentLevel = this.getDinoLevel();
        int bonusLevels = Math.round((currentLevel * 0.5f) * effectiveness);
        if (bonusLevels > 0) {
            this.setDinoLevel(currentLevel + bonusLevels);
            this.distributeWildPoints(bonusLevels);
        }

        // Reset taming progress and effectiveness for a clean tamed state
        this.setTamingProgress(0.0f);
        this.setTamingEffectiveness(1.0f);
    }

    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, MobSpawnType spawnType, @Nullable SpawnGroupData spawnGroupData) {
        spawnGroupData = super.finalizeSpawn(level, difficulty, spawnType, spawnGroupData);

        // Resolve level configs
        int minLvl = Math.max(1, Config.DINOSAUR_CONFIG.MIN_LEVEL.get());
        int maxLvl = Math.max(minLvl + 4, Config.DINOSAUR_CONFIG.MAX_LEVEL.get());
        int avgLvl = Config.DINOSAUR_CONFIG.AVERAGE_LEVEL.get();
        if (avgLvl < 0) {
            avgLvl = minLvl + (maxLvl - minLvl) / 2;
        } else {
            avgLvl = Math.clamp(avgLvl, minLvl, maxLvl);
        }

        // Generate Gaussian split-normal wild level
        int wildLevel = RandomUtils.generateLevel(this.random, minLvl, maxLvl, avgLvl);
        this.setDinoLevel(wildLevel);

        // Distribute points among stats
        this.distributeWildPoints(wildLevel - 1);

        // Fill status stats to max/min
        this.setHealth(this.getMaxHealth());
        this.setHunger((float) this.getAttributeValue(ModAttributes.MAX_HUNGER));
        this.setTorpor(0.0f);

        return spawnGroupData;
    }

    public float getMissingHunger() {
        float max = (float) this.getAttributeValue(ModAttributes.MAX_HUNGER);
        return Math.max(0, max - getHunger());
    }

    public boolean canEat(DinoFoodEntry.FoodValues value) {
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
     * 1 = (unused, previously saddled)
     * 2 = sleep
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

    @Override
    public boolean isSaddled() {
        return this.getEquipmentInventory().hasEquipment(DinoEquipment.SADDLE) && !this.getEquipmentInventory().getEquipment(DinoEquipment.SADDLE).isEmpty();
    }

    @Override
    public boolean isSaddleable() {
        return this.isAlive() && !this.isBaby() && this.isTamed();
    }

    @Override
    public void equipSaddle(ItemStack stack, @Nullable SoundSource source) {
        this.getEquipmentInventory().setEquipment(DinoEquipment.SADDLE, stack);
    }

    public DynamicInventory getChestInventory() {
        this.updateInventorySizeFromAttributes();
        return this.inventory.getChestInventory();
    }

    public DinoEquipmentInventory getEquipmentInventory() {
        return this.inventory.getEquipmentInventory();
    }

    public DinoInventory getTotalInventory() {
        this.updateInventorySizeFromAttributes();
        return this.inventory;
    }

    public void syncEquipment(DinoEquipment equipment) {
        if (!this.level().isClientSide()) {
            ItemStack stack = this.getEquipmentInventory().getEquipment(equipment);
            PacketDistributor.sendToPlayersTrackingEntity(this, new DinoEquipmentSyncPayload(this.getId(), equipment.ordinal(), stack));
        }
    }

    @Nullable
    @Override
    public LivingEntity getControllingPassenger() {
        if (this.isSaddled() && this.getFirstPassenger() instanceof LivingEntity passenger) {
            return passenger;
        }
        return null;
    }

    @Override
    protected void tickRidden(@NotNull Player rider, @NotNull Vec3 travelVector) {
        super.tickRidden(rider, travelVector);
        this.setRot(rider.getYRot(), rider.getXRot() * 0.5F);
        this.yRotO = this.yBodyRot = this.yHeadRot = this.getYRot();
    }

    @Override
    protected @NotNull Vec3 getRiddenInput(@NotNull Player player, @NotNull Vec3 travelVector) {
        if (!this.onGround()) {
            return Vec3.ZERO;
        } else {
            float f = player.xxa * 0.5F;
            float f1 = player.zza;
            if (f1 <= 0.0F) {
                f1 *= 0.25F;
            }

            return new Vec3((double)f, 0.0, (double)f1);
        }
    }

    @Override
    protected float getRiddenSpeed(Player player) {
        return (float) this.getAttributeValue(Attributes.MOVEMENT_SPEED);
    }

    @Override
    public boolean isEffectiveAi() {
        return super.isEffectiveAi();
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
    public void aiStep() {
        super.aiStep();

        if (!this.level().isClientSide()) {
            this.sleepBehaviour.tick();
            this.survivalBehaviour.tick();
            this.tamingBehaviour.tick();

            if (this.isTamed()) {
                this.gainXp(0.01f); // Passive XP
            }

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
            } else {
                var heldItem = player.getItemInHand(hand);
                for (var eq : this.getEquipments().keySet()) {
                    if (this.inventory.getEquipmentInventory().isEquipmentValid(eq, heldItem)) {
                        var previousEquipment = this.inventory.getEquipmentInventory().getEquipment(eq);
                        //no need to switch the same item, but will make the switch if any components mismatch
                        if (ItemStack.isSameItemSameComponents(heldItem, previousEquipment))
                            continue;
                        this.inventory.getEquipmentInventory().setEquipment(eq, heldItem);
                        player.setItemInHand(hand, previousEquipment);
                        return InteractionResult.sidedSuccess(this.level().isClientSide());
                    }
                }
                if (this.isSaddled() && !this.isBaby()) {
                    // Right click: ride
                    if (!this.level().isClientSide()) {
                        player.startRiding(this);
                    }
                    return InteractionResult.sidedSuccess(this.level().isClientSide());
                }
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
    protected void dropEquipment() {
        super.dropEquipment();
        for (int i = 0; i < this.inventory.getSlots(); i++) {
            var stack = this.inventory.getStackInSlot(i);
            if (!stack.isEmpty()){
                this.spawnAtLocation(stack);
            }
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
        tag.putInt("DinoLevel", this.getDinoLevel());
        tag.putFloat("DinoXp", this.getDinoXp());
        tag.putInt("TamedLevelPoints", this.getTamedLevelPoints());
        tag.putInt("AvailablePoints", this.getAvailablePoints());
        this.survivalBehaviour.save(tag);
        NbtUtils.putIfPresent(tag, "owner", CompoundTag::putUUID, this.entityData.get(OWNER));
        NbtUtils.putIfPresent(tag, "unconscious_owner", CompoundTag::putUUID, this.entityData.get(UNCONSCIOUS_OWNER));
        tag.put("inventory", this.inventory.serializeNBT(level().registryAccess()));

        CompoundTag pointsTag = new CompoundTag();
        for (DinoStat stat : DinoStat.values()) {
            pointsTag.putInt(stat.name(), this.getStatPoints(stat));
        }
        tag.put("StatPoints", pointsTag);
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
        NbtUtils.setIfExists(tag, "DinoLevel", CompoundTag::getInt, this::setDinoLevel);
        NbtUtils.setIfExists(tag, "DinoXp", CompoundTag::getFloat, this::setDinoXp);
        NbtUtils.setIfExists(tag, "TamedLevelPoints", CompoundTag::getInt, this::setTamedLevelPoints);
        NbtUtils.setIfExists(tag, "AvailablePoints", CompoundTag::getInt, this::setAvailablePoints);
        this.survivalBehaviour.load(tag);
        NbtUtils.setIfExists(tag, "owner", CompoundTag::getUUID, uuid -> entityData.set(OWNER, Optional.of(uuid)));
        NbtUtils.setIfExists(tag, "unconscious_owner", CompoundTag::getUUID, this::setUnconsciousFrom);

        NbtUtils.setIfExists(tag, "inventory", CompoundTag::getCompound, t -> inventory.deserializeNBT(level().registryAccess(), t));
        NbtUtils.setIfExists(tag, "StatPoints", CompoundTag::getCompound, t -> {
            for (DinoStat stat : DinoStat.values()) {
                if (t.contains(stat.name())) {
                    this.setStatPoints(stat, t.getInt(stat.name()));
                }
            }
        });

        // Sync attributes after reading data
        this.updateAttributesFromLevels();

        // Sync equipment when loaded from NBT data on the server
        if (!this.level().isClientSide()) {
            for (DinoEquipment eq : DinoEquipment.values()) {
                if (this.getEquipmentInventory().hasEquipment(eq)) {
                    this.syncEquipment(eq);
                }
            }
        }
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
