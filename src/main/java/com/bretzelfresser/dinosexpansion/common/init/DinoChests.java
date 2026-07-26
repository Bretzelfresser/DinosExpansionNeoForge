package com.bretzelfresser.dinosexpansion.common.init;

import com.bretzelfresser.dinosexpansion.DinosExpansion;
import com.bretzelfresser.dinosexpansion.common.chest.DinoChestEntry;
import net.minecraft.core.Registry;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;

public class DinoChests {
    public static final ResourceKey<Registry<DinoChestEntry>> DINO_CHEST_REGISTRY_KEY = ResourceKey.createRegistryKey(
            ResourceLocation.fromNamespaceAndPath(DinosExpansion.MODID, "dino_chest")
    );

    public static final ResourceKey<DinoChestEntry> CERATOSAURUS_CHEST = create("ceratosaurus_chests");

    public static ResourceKey<DinoChestEntry> create(String name) {
        return ResourceKey.create(DINO_CHEST_REGISTRY_KEY, ResourceLocation.fromNamespaceAndPath(DinosExpansion.MODID, name));
    }

    public static void bootstrap(BootstrapContext<DinoChestEntry> context) {
        context.register(CERATOSAURUS_CHEST, new DinoChestEntry.Builder(ModEntities.CERATOSAURS.getKey())
                .addChest(Items.CHEST, 36)
                .build()
        );
    }
}
