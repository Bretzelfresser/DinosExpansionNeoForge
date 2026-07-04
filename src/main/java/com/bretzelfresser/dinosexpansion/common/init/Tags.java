package com.bretzelfresser.dinosexpansion.common.init;

import com.bretzelfresser.dinosexpansion.DinosExpansion;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.biome.Biome;

public class Tags {


    public static final class Biomes{

        public static final TagKey<Biome> HAS_CAVE_DUNGEON = create("has_structure/cave_dungeon");


        public static TagKey<Biome> create(String name){
            return TagKey.create(Registries.BIOME, ResourceLocation.fromNamespaceAndPath(DinosExpansion.MODID, name));
        }
    }
}
