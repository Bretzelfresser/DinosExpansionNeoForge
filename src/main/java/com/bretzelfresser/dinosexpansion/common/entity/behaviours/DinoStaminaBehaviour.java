package com.bretzelfresser.dinosexpansion.common.entity.behaviours;

import com.bretzelfresser.dinosexpansion.common.entity.base.BaseDinoEntity;
import com.bretzelfresser.dinosexpansion.common.init.ModAttributes;
import net.minecraft.util.Mth;

public class DinoStaminaBehaviour {

    protected final BaseDinoEntity<?> dino;
    private boolean staminaConsumedThisTick = false;

    public DinoStaminaBehaviour(BaseDinoEntity<?> dino) {
        this.dino = dino;
    }

    /**
     * @param addition the amount we want to add, can be negative to reduce stamina
     */
    public void addStamina(float addition) {
        if (addition < 0) {
            this.staminaConsumedThisTick = true;
        }
        this.dino.setStamina(this.dino.getStamina() + Mth.clamp(addition, -dino.getStamina(), dino.getMissingStamina()));
    }

    public boolean canAddStamina(float addition) {
        return addition == 0 || (addition > -dino.getStamina() && addition < dino.getMissingStamina());
    }

    public boolean consumeStamina(float amount) {
        if (amount <= 0) {
            return true;
        }
        if (this.dino.getStamina() >= amount) {
            this.dino.setStamina(this.dino.getStamina() - amount);
            this.staminaConsumedThisTick = true;
            return true;
        }
        return false;
    }


    public boolean canSprint(){
        var sprintStaminaCost = this.dino.getAttributeValue(ModAttributes.SPRINT_STAMINA_COST);
        return sprintStaminaCost <= dino.getStamina();
    }

    public void tick() {
        if (this.dino.isSprinting() && !this.dino.isUnconscious() && !this.dino.isSleeping()) {
            float sprintStamina = (float) this.dino.getAttributeValue(ModAttributes.SPRINT_STAMINA_COST);
            if (!consumeStamina(sprintStamina)) {
                this.dino.setSprinting(false);
            }
        }
        if (!this.staminaConsumedThisTick && !this.dino.isUnconscious() && !this.dino.isSleeping()) {
            float currentStamina = this.dino.getStamina();
            float maxStamina = (float) this.dino.getAttributeValue(ModAttributes.MAX_STAMINA);
            if (currentStamina < maxStamina) {
                float staminaRegen = (float) this.dino.getAttributeValue(ModAttributes.STAMINA_REGENERATION);
                float actualGained = Math.min(staminaRegen, maxStamina - currentStamina);
                if (actualGained > 0) {
                    this.dino.setStamina(currentStamina + actualGained);
                    float regenHungerCost = actualGained * (float) this.dino.getAttributeValue(ModAttributes.STAMINA_REGEN_HUNGER_COST);
                    this.dino.getFoodBehaviour().addHunger(regenHungerCost);
                }
            }
        }
        this.staminaConsumedThisTick = false;
    }
}
