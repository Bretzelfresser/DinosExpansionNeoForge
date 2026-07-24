package com.bretzelfresser.dinosexpansion.common.entity;

import com.bretzelfresser.dinosexpansion.common.init.ModEntities;
import com.bretzelfresser.dinosexpansion.common.init.ModItems;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public class TranquilizerArrow extends AbstractArrow {
    public TranquilizerArrow(EntityType<? extends TranquilizerArrow> entityType, Level level) {
        super(entityType, level);
    }

    public TranquilizerArrow(Level level, LivingEntity shooter, ItemStack pickupItemStack, @Nullable ItemStack weapon) {
        super(ModEntities.TRANQUILIZER_ARROW.get(), shooter, level, pickupItemStack, weapon);
    }

    public TranquilizerArrow(Level level, double x, double y, double z, ItemStack pickupItemStack, @Nullable ItemStack weapon) {
        super(ModEntities.TRANQUILIZER_ARROW.get(), x, y, z, level, pickupItemStack, weapon);
    }

    @Override
    protected ItemStack getDefaultPickupItem() {
        return new ItemStack(ModItems.TRANQUILIZER_ARROW.get());
    }
}
