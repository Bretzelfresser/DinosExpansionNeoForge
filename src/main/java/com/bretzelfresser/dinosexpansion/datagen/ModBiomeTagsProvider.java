package com.bretzelfresser.dinosexpansion.datagen;

import com.bretzelfresser.dinosexpansion.common.init.ModBiomes;
import com.bretzelfresser.dinosexpansion.common.init.ModStructures;
import com.bretzelfresser.dinosexpansion.common.init.Tags;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.BiomeTagsProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

public class ModBiomeTagsProvider extends BiomeTagsProvider {
    public ModBiomeTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> provider, @Nullable ExistingFileHelper existingFileHelper) {
        super(output, provider, com.bretzelfresser.dinosexpansion.DinosExpansion.MODID, existingFileHelper);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        tag(Tags.Biomes.HAS_CAVE_DUNGEON).addOptional(ModBiomes.FERN_PLAINS.location());
        tag(Tags.Biomes.HAS_OASIS).addOptional(ModBiomes.BONE_DESERT.location());
    }
}
