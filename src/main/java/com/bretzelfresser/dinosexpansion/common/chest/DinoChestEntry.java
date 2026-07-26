package com.bretzelfresser.dinosexpansion.common.chest;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;

import java.util.ArrayList;
import java.util.List;

public record DinoChestEntry(ResourceKey<EntityType<?>> dinoType, List<ChestEntry> chests) {

    public record ChestEntry(HolderSet<Item> items, int slots) {
        public static final Codec<ChestEntry> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                RegistryCodecs.homogeneousList(Registries.ITEM).fieldOf("items").forGetter(ChestEntry::items),
                Codec.INT.fieldOf("slots").forGetter(ChestEntry::slots)
        ).apply(instance, ChestEntry::new));
    }

    public static final Codec<DinoChestEntry> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ResourceKey.codec(Registries.ENTITY_TYPE).fieldOf("dino_type").forGetter(DinoChestEntry::dinoType),
            ChestEntry.CODEC.listOf().fieldOf("chests").forGetter(DinoChestEntry::chests)
    ).apply(instance, DinoChestEntry::new));

    public static class Builder {
        private final ResourceKey<EntityType<?>> dinoType;
        private final List<ChestEntry> chests = new ArrayList<>();

        public Builder(ResourceKey<EntityType<?>> dinoType) {
            this.dinoType = dinoType;
        }

        public Builder addChest(HolderSet<Item> items, int slots) {
            this.chests.add(new ChestEntry(items, slots));
            return this;
        }

        public Builder addChest(Item item, int slots) {
            return addChest(HolderSet.direct(item.builtInRegistryHolder()), slots);
        }

        public DinoChestEntry build() {
            return new DinoChestEntry(this.dinoType, List.copyOf(this.chests));
        }
    }
}
