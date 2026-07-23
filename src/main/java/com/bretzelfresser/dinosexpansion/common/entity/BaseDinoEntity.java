package com.bretzelfresser.dinosexpansion.common.entity;

import com.bretzelfresser.dinosexpansion.common.entity.ai.DinoBrain;
import com.bretzelfresser.dinosexpansion.common.init.ModDataComponents;
import com.bretzelfresser.dinosexpansion.common.menu.DinoContainerMenu;
import com.bretzelfresser.dinosexpansion.common.init.ModAttributes;
import com.bretzelfresser.dinosexpansion.common.init.ModItems;
import com.google.common.collect.ImmutableList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.ContainerListener;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.TamableAnimal;
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
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.util.GeckoLibUtil;

public abstract class BaseDinoEntity extends TamableAnimal implements GeoEntity, ContainerListener {
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    private static final EntityDataAccessor<Float> CURRENT_TORPOR = SynchedEntityData.defineId(BaseDinoEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> CURRENT_HUNGER = SynchedEntityData.defineId(BaseDinoEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Boolean> IS_UNCONSCIOUS = SynchedEntityData.defineId(BaseDinoEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Float> TAMING_PROGRESS = SynchedEntityData.defineId(BaseDinoEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> TAMING_EFFECTIVENESS = SynchedEntityData.defineId(BaseDinoEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Boolean> IS_SADDLED = SynchedEntityData.defineId(BaseDinoEntity.class, EntityDataSerializers.BOOLEAN);

    protected final SimpleContainer inventory;
    
    // Attack duration tracker (server ticks)
    protected int attackTicks = 0;
    protected boolean isAttacking = false;

    protected BaseDinoEntity(EntityType<? extends BaseDinoEntity> entityType, Level level) {
        super(entityType, level);
        this.inventory = new SimpleContainer(38); // Slot 0: Saddle, Slot 1: Armor, Slots 2-37: Main Dino Inventory
        this.inventory.addListener(this);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Animal.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 20.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.25D)
                .add(Attributes.ATTACK_DAMAGE, 3.0D)
                .add(ModAttributes.MAX_TORPOR, 100.0D)
                .add(ModAttributes.MAX_HUNGER, 100.0D);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(CURRENT_TORPOR, 0.0f);
        builder.define(CURRENT_HUNGER, 100.0f);
        builder.define(IS_UNCONSCIOUS, false);
        builder.define(TAMING_PROGRESS, 0.0f);
        builder.define(TAMING_EFFECTIVENESS, 1.0f);
        builder.define(IS_SADDLED, false);
    }

    // Getters and Setters for stats
    public float getTorpor() {
        return this.entityData.get(CURRENT_TORPOR);
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

    public boolean isUnconscious() {
        return this.entityData.get(IS_UNCONSCIOUS);
    }

    public void setUnconscious(boolean unconscious) {
        this.entityData.set(IS_UNCONSCIOUS, unconscious);
        if (unconscious) {
            // Remove passengers when falling unconscious
            this.ejectPassengers();
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
        return this.entityData.get(IS_SADDLED);
    }

    public void setSaddled(boolean saddled) {
        this.entityData.set(IS_SADDLED, saddled);
    }

    public int getInventorySize() {
        // If saddled, we have 36 slots available
        return this.isSaddled() ? 36 : 0;
    }

    public SimpleContainer getInventory() {
        return this.inventory;
    }

    @Override
    public void containerChanged(net.minecraft.world.Container container) {
        ItemStack saddle = container.getItem(0);
        this.setSaddled(!saddle.isEmpty() && saddle.is(ModItems.TEST_DINO_SADDLE.get()));
    }

    @Override
    protected Brain.Provider<?> brainProvider() {
        return Brain.provider(
                ImmutableList.of(
                        MemoryModuleType.WALK_TARGET,
                        MemoryModuleType.LOOK_TARGET,
                        MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE,
                        MemoryModuleType.PATH
                ),
                ImmutableList.of(
                        SensorType.NEAREST_PLAYERS,
                        SensorType.NEAREST_LIVING_ENTITIES
                )
        );
    }

    @SuppressWarnings("unchecked")
    @Override
    protected Brain<?> makeBrain(com.mojang.serialization.Dynamic<?> dynamic) {
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
            // Torpor draining over time
            float torpor = this.getTorpor();
            if (torpor > 0) {
                // Drain torpor slowly (e.g. 0.1 per tick, can be customized)
                this.setTorpor(torpor - 0.1f);
            }

            // Unconsciousness state machine
            float maxTorpor = (float) this.getAttributeValue(ModAttributes.MAX_TORPOR);
            if (!this.isUnconscious() && this.getTorpor() >= maxTorpor * 0.8f) {
                this.setUnconscious(true);
            } else if (this.isUnconscious() && this.getTorpor() <= 0.0f) {
                this.setUnconscious(false);
                // When waking up wild, reset taming progress slightly
                if (!this.isTame()) {
                    this.setTamingProgress(this.getTamingProgress() * 0.5f);
                }
            }

            // Hunger depletion over time
            float hunger = this.getHunger();
            if (hunger > 0) {
                this.setHunger(hunger - 0.02f); // Drain hunger slowly
            } else {
                // Starving - lose health
                this.hurt(this.damageSources().starve(), 1.0F);
            }

            // Dino eats preferred food when hungry and unconscious (wild or tamed)
            if (this.getHunger() <= (float) this.getAttributeValue(ModAttributes.MAX_HUNGER) - 50.0f) {
                this.tryToEatFromInventory();
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
        }
    }

    protected void tryToEatFromInventory() {
        for (int i = 2; i < 38; i++) {
            ItemStack stack = this.inventory.getItem(i);
            if (!stack.isEmpty() && this.isPreferredFood(stack)) {
                // Eat food
                this.setHunger(this.getHunger() + 50.0f); // Restore hunger
                
                if (!this.isTame() && this.isUnconscious()) {
                    // Wild & asleep: eating increases taming progress
                    float progressGain = 0.05f * this.getTamingEffectiveness();
                    this.setTamingProgress(this.getTamingProgress() + progressGain);
                    if (this.getTamingProgress() >= 1.0f) {
                        this.tame(null); // Tame it!
                        this.setUnconscious(false); // Wake up
                    }
                }
                
                stack.shrink(1);
                break;
            }
        }
    }

    @Override
    public boolean isFood(ItemStack stack) {
        return this.isPreferredFood(stack);
    }

    protected boolean isPreferredFood(ItemStack stack) {
        // Can be overridden per dino subclass. Default: eats any food (has FOOD data component in 1.21.1)
        return stack.has(DataComponents.FOOD);
    }

    public void applyNarcotics(int amount) {
        // Narcotics increase torpor
        this.setTorpor(this.getTorpor() + amount);
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
            // If unconscious, we can force-feed narcotics or preferred food directly
            if (!stack.isEmpty() && stack.has(ModDataComponents.NARCOTIC_VALUE.get())) {
                float val = stack.get(ModDataComponents.NARCOTIC_VALUE.get());
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
        if (this.isTame()) {
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
        if (player instanceof net.minecraft.server.level.ServerPlayer serverPlayer) {
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
        
        // Save items
        ListTag listTag = new ListTag();
        for (int i = 0; i < this.inventory.getContainerSize(); ++i) {
            ItemStack itemstack = this.inventory.getItem(i);
            if (!itemstack.isEmpty()) {
                CompoundTag itemTag = new CompoundTag();
                itemTag.putByte("Slot", (byte) i);
                itemstack.save(this.registryAccess(), itemTag);
                listTag.add(itemTag);
            }
        }
        tag.put("Inventory", listTag);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.setTorpor(tag.getFloat("Torpor"));
        this.setHunger(tag.getFloat("Hunger"));
        this.setUnconscious(tag.getBoolean("Unconscious"));
        this.setTamingProgress(tag.getFloat("TamingProgress"));
        this.setTamingEffectiveness(tag.getFloat("TamingEffectiveness"));
        
        // Load items
        ListTag listTag = tag.getList("Inventory", 10);
        for (int i = 0; i < listTag.size(); ++i) {
            CompoundTag itemTag = listTag.getCompound(i);
            int slot = itemTag.getByte("Slot") & 255;
            if (slot < this.inventory.getContainerSize()) {
                this.inventory.setItem(slot, ItemStack.parseOptional(this.registryAccess(), itemTag));
            }
        }
    }

    // GeckoLib Implementation
    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar registrar) {
        registrar.add(new AnimationController<>(this, "dino_controller", 10, event -> {
            if (this.isUnconscious()) {
                return event.setAndContinue(software.bernie.geckolib.animation.RawAnimation.begin().thenLoop("sleep"));
            }
            if (event.isMoving()) {
                return event.setAndContinue(software.bernie.geckolib.animation.RawAnimation.begin().thenLoop("walk"));
            }
            return event.setAndContinue(software.bernie.geckolib.animation.RawAnimation.begin().thenLoop("idle"));
        }));
    }

    // AgeableMob required method
    @Nullable
    @Override
    public AgeableMob getBreedOffspring(ServerLevel level, AgeableMob ageableMob) {
        return null; // Implemented by concrete dino species
    }
}
