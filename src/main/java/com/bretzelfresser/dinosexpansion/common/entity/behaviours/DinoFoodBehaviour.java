package com.bretzelfresser.dinosexpansion.common.entity.behaviours;

import com.bretzelfresser.dinosexpansion.common.entity.base.BaseDinoEntity;
import com.bretzelfresser.dinosexpansion.common.init.ModAttributes;
import com.bretzelfresser.dinosexpansion.config.Config;
import com.bretzelfresser.dinosexpansion.util.NbtUtils;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.common.util.INBTSerializable;

public class DinoFoodBehaviour implements INBTSerializable<CompoundTag> {

    protected float foodToEat = 0;

    protected final BaseDinoEntity dino;

    public DinoFoodBehaviour(BaseDinoEntity dino) {
        this.dino = dino;
    }

    @OnlyIn(Dist.DEDICATED_SERVER)
    public void tick() {
        naturalRegeneration();
        foodConsumption();

    }

    protected void foodConsumption() {
        float hunger = dino.getHunger();
        if (hunger > 0) {
            dino.setHunger(hunger - (float) dino.getAttributeValue(ModAttributes.HUNGER_DECREASE));

            float actuallyReducedHunger = Math.min(foodToEat, dino.getHunger());
            foodToEat -= actuallyReducedHunger;
            dino.setHunger(dino.getHunger() - actuallyReducedHunger);

            if (foodToEat > 0) {
                dino.hurt(dino.damageSources().starve(), foodToEat);
            }

        } else if (dino.tickCount % 10 == 0) {//2 per second
            dino.hurt(dino.damageSources().starve(), 1.0F + foodToEat);
        }
        foodToEat = 0;
    }

    protected void naturalRegeneration() {
        float hunger = dino.getHunger();
        float maxHunger = (float) dino.getAttributeValue(ModAttributes.MAX_HUNGER);
        float threshold = (float) Config.DINOSAUR_CONFIG.NATURAL_REGENERATION_HUNGER_THRESHOLD.getAsDouble();
        if (hunger >= maxHunger * threshold && dino.getHealth() < dino.getMaxHealth()) {
            float regen = (float) dino.getAttributeValue(ModAttributes.NATURAL_REGENERATION);
            if (regen > 0) {
                float regenHungerCost = regen * (float) dino.getAttributeValue(ModAttributes.HEALTH_REGEN_HUNGER_COST);
                addHunger(regenHungerCost);
                dino.heal(regen);
            }
        }
    }

    /**
     *
     * @param hungerToReduce the amount of hunger we want to reduce to, this will buffer the hunger to reduce it once every tick
     */
    @OnlyIn(Dist.DEDICATED_SERVER)
    public void addHunger(float hungerToReduce) {
        this.foodToEat += hungerToReduce;
    }


    @Override
    public CompoundTag serializeNBT(HolderLookup.Provider provider) {
        CompoundTag tag = new CompoundTag();
        tag.putFloat("toEatHunger", this.foodToEat);

        return tag;
    }

    @Override
    public void deserializeNBT(HolderLookup.Provider provider, CompoundTag nbt) {
        NbtUtils.setIfExists(nbt, "toEatHunger", CompoundTag::getFloat, f -> foodToEat = f);
    }
}
