package com.bretzelfresser.dinosexpansion.entity;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

public class TestDinoEntity extends BaseDinoEntity {
    public TestDinoEntity(EntityType<? extends TestDinoEntity> entityType, Level level) {
        super(entityType, level);
    }
}
