package com.bretzelfresser.dinosexpansion.common.entity.base;

import com.bretzelfresser.dinosexpansion.DinosExpansion;
import com.bretzelfresser.dinosexpansion.common.init.ModAttributes;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.util.random.Weight;
import net.minecraft.util.random.WeightedEntry;
import net.minecraft.util.random.WeightedRandomList;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

import java.util.function.BiConsumer;

public enum DinoStat implements WeightedEntry {
    HEALTH(6, Attributes.MAX_HEALTH, DinosExpansion.modLoc("stat_health"), 2),
    TORPOR(10, ModAttributes.MAX_TORPOR, DinosExpansion.modLoc("stat_torpor"), 6),
    HUNGER(10, ModAttributes.MAX_HUNGER, DinosExpansion.modLoc("stat_hunger"), 6),
    STAMINA(8, ModAttributes.MAX_STAMINA, DinosExpansion.modLoc("stat_stamina"), 10),
    DAMAGE(5, Attributes.ATTACK_DAMAGE, DinosExpansion.modLoc("stat_damage"), 0.5),
    CARRYING_CAPACITY(3, ModAttributes.CARRYING_CAPACITY, DinosExpansion.modLoc("stat_carrying_capacity"), 1);

    private final int wildPointWeight;

    private final BiConsumer<BaseDinoEntity, Integer> onApply;


    DinoStat(int wildPointWeight, Holder<Attribute> attribute, ResourceLocation modifierId, double scalingPerPoint) {
        this(wildPointWeight, ((baseDinoEntity, integer) -> updateAttributeModifier(baseDinoEntity, attribute, modifierId, scalingPerPoint, integer)));
    }

    DinoStat(int wildPointWeight, BiConsumer<BaseDinoEntity, Integer> onApply) {
        this.wildPointWeight = wildPointWeight;
        this.onApply = onApply;
    }

    public void apply(BaseDinoEntity dino, int points) {
        onApply.accept(dino, points);
    }

    @Override
    public Weight getWeight() {
        return Weight.of(wildPointWeight);
    }


    private static void updateAttributeModifier(BaseDinoEntity dino, Holder<Attribute> attribute, ResourceLocation modifierId, double amountPerPoint, int points) {
        var instance = dino.getAttribute(attribute);
        if (instance != null) {
            instance.removeModifier(modifierId);
            if (points > 0) {
                double totalAmount = points * amountPerPoint;
                instance.addOrReplacePermanentModifier(new AttributeModifier(modifierId, totalAmount, AttributeModifier.Operation.ADD_VALUE));
            }
        }
    }

    public static WeightedRandomList<DinoStat> getWeightedList() {
        return WeightedRandomList.create(values());
    }

    public static DinoStat sampleWeightedRandom(RandomSource random) {
        return getWeightedList().getRandom(random).orElseThrow();
    }
}
