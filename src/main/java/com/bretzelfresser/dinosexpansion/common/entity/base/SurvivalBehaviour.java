package com.bretzelfresser.dinosexpansion.common.entity.base;

import com.bretzelfresser.dinosexpansion.common.init.ModAttributes;
import com.bretzelfresser.dinosexpansion.config.Config;
import com.bretzelfresser.dinosexpansion.util.NbtUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.player.Player;

import java.util.Optional;
import java.util.UUID;

public class SurvivalBehaviour {
    private final BaseDinoEntity dino;
    private float stackedTorpor = 0;
    private Optional<UUID> lastHitPlayer = Optional.empty();

    public SurvivalBehaviour(BaseDinoEntity dino) {
        this.dino = dino;
    }

    public float getStackedTorpor() {
        return this.stackedTorpor;
    }

    /**
     *
     * @return the total missing torpor also taking the stacked torpor into account, this can never be less then 0 even tho the torpor might be stacked
     */
    public float getTotalMissingTorpor() {
        float totalTorpor = dino.getTorpor() + this.stackedTorpor;
        return Math.max(0f, (float) dino.getAttributeValue(ModAttributes.MAX_TORPOR) - totalTorpor);
    }

    public void setStackedTorpor(float val) {
        this.stackedTorpor = val;
    }

    public Optional<UUID> getLastHitPlayer() {
        return this.lastHitPlayer;
    }

    public void setLastHitPlayer(Optional<UUID> lastHitPlayer) {
        this.lastHitPlayer = lastHitPlayer;
    }

    public void applyBufferedNarcotics(float amount) {
        this.stackedTorpor += amount;
    }

    public void onHurt(DamageSource damageSource, float damageAmount) {
        if (damageSource.getEntity() instanceof Player player) {
            this.lastHitPlayer = Optional.of(player.getUUID());
        }
    }

    public boolean shouldWakeUpFromUnconscious() {
        return shouldWakeUpFromUnconscious(0f);
    }

    /**
     *
     * @param offset allows for setting an offset into this calculation, when offset = 0 this will tell u when exactly the entity would wake up from unconscious, with this offset u can get an earlier true
     * @return
     */
    public boolean shouldWakeUpFromUnconscious(float offset) {
        return dino.isUnconscious() && dino.getTorpor() + offset <= getWakeUpTorporThreshold();
    }

    public float getWakeUpTorporThreshold() {
        float maxTorpor = (float) dino.getAttributeValue(ModAttributes.MAX_TORPOR);
        return maxTorpor * (float) dino.getAttributeValue(ModAttributes.TORPOR_WAKE_UP_THRESHOLD);
    }

    public void tick() {
        // Torpor draining over time
        float torpor = dino.getTorpor();
        if (torpor > 0 && (this.stackedTorpor <= 0 || dino.isUnconscious())) {
            dino.setTorpor(torpor - (float) dino.getAttributeValue(ModAttributes.TORPOR_DECREASE));
        }

        if (this.stackedTorpor > 0) {
            float max = (float) dino.getAttributeValue(ModAttributes.MAX_TORPOR);
            float missingTorpor = Math.max(0, max - dino.getTorpor());
            float stackedTorporReduction = Math.min(
                    missingTorpor,
                    Math.min(
                            this.stackedTorpor,
                            Math.max(
                                    (float) Config.DINOSAUR_CONFIG.MIN_BUFFERED_TORPOR_REDUCTION.getAsDouble(),
                                    this.stackedTorpor * (float) Config.DINOSAUR_CONFIG.BUFFERED_TORPOR_REDUCTION.getAsDouble()
                            )
                    )
            );
            this.stackedTorpor -= stackedTorporReduction;
            dino.applyNarcotics(stackedTorporReduction);
        } else if (this.lastHitPlayer.isPresent()) {
            this.lastHitPlayer = Optional.empty();
        }

        // Unconsciousness state machine
        float maxTorpor = (float) dino.getAttributeValue(ModAttributes.MAX_TORPOR);
        if (!dino.isUnconscious() && dino.getTorpor() >= maxTorpor) {
            onTorporFull();
        } else if (shouldWakeUpFromUnconscious()) {
            onWakeUpFromTorpor();
        }

        // Hunger depletion over time
        float hunger = dino.getHunger();
        if (hunger > 0) {
            dino.setHunger(hunger - (float) dino.getAttributeValue(ModAttributes.HUNGER_DECREASE));
        } else {
            dino.hurt(dino.damageSources().starve(), 1.0F);
        }
    }

    protected void onTorporFull() {
        dino.setUnconsciousFrom(this.lastHitPlayer.orElse(null));
        dino.setTamedBy((UUID) null);//when this dino was previously tamed, now it isnt anymore
    }

    protected void onWakeUpFromTorpor() {
        dino.setUnconsciousFrom((UUID) null);
        if (!dino.isTamed()) {
            dino.setTamingProgress(dino.getTamingProgress() * 0.5f);
        }
    }

    public void save(CompoundTag tag) {
        tag.putFloat("stackedTorpor", this.stackedTorpor);
        NbtUtils.putIfPresent(tag, "last_hit_player", CompoundTag::putUUID, this.lastHitPlayer);
    }

    public void load(CompoundTag tag) {
        NbtUtils.setIfExists(tag, "stackedTorpor", CompoundTag::getFloat, f -> this.stackedTorpor = f);
        NbtUtils.setIfExists(tag, "last_hit_player", CompoundTag::getUUID, id -> this.lastHitPlayer = Optional.of(id));
    }
}
