package com.bretzelfresser.dinosexpansion.common.effect;

import com.bretzelfresser.dinosexpansion.DinosExpansion;
import com.bretzelfresser.dinosexpansion.common.init.ModAttributes;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

public class CeratosaurusRoarEffect extends MobEffect {
    public CeratosaurusRoarEffect() {
        super(MobEffectCategory.BENEFICIAL, 0x982303);

        this.addAttributeModifier(Attributes.ATTACK_DAMAGE,
                DinosExpansion.modLoc("effect.ceratosaurus_roar.attack_damage"),
                0.2D,
                AttributeModifier.Operation.ADD_MULTIPLIED_BASE);

        this.addAttributeModifier(Attributes.ARMOR,
                DinosExpansion.modLoc("effect.ceratosaurus_roar.resistance"),
                4.0D,
                AttributeModifier.Operation.ADD_VALUE);

        this.addAttributeModifier(ModAttributes.MAX_TORPOR,
                DinosExpansion.modLoc("effect.ceratosaurus_roar.max_torpor"),
                0.25D,
                AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
    }
}
