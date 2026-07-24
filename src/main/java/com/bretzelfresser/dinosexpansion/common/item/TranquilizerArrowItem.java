package com.bretzelfresser.dinosexpansion.common.item;

import com.bretzelfresser.dinosexpansion.common.entity.TranquilizerArrow;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ArrowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public class TranquilizerArrowItem extends ArrowItem {
    public TranquilizerArrowItem(Properties properties) {
        super(properties);
    }

    @Override
    public AbstractArrow createArrow(Level level, ItemStack ammo, LivingEntity shooter, @Nullable ItemStack weapon) {
        return new TranquilizerArrow(level, shooter, ammo, weapon);
    }
}
