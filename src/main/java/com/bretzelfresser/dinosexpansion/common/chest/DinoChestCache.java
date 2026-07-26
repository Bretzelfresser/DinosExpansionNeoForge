package com.bretzelfresser.dinosexpansion.common.chest;

import com.bretzelfresser.dinosexpansion.DinosExpansion;
import com.bretzelfresser.dinosexpansion.common.init.DinoChests;
import com.mojang.datafixers.util.Pair;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.TagsUpdatedEvent;

import java.util.*;

@EventBusSubscriber(modid = DinosExpansion.MODID)
public class DinoChestCache {
    private static Map<EntityType<?>, List<Pair<HolderSet<Item>, Integer>>> cache = null;

    @SubscribeEvent
    public static void onTagsUpdated(TagsUpdatedEvent event) {
        // Invalidate the cache when datapacks reload or sync
        cache = null;
    }

    public static OptionalInt getSlotsFor(EntityType<?> type, ItemStack item, RegistryAccess registryAccess) {
        if (cache == null) {
            rebuild(registryAccess);
        }
        var chests = cache.get(type);
        if (chests != null) {
            for (var entry : chests){
                if (item.is(entry.getFirst())){
                    return OptionalInt.of(entry.getSecond());
                }
            }

        }
        return OptionalInt.empty();
    }

    public static boolean isValidChest(EntityType<?> type, ItemStack item, RegistryAccess registryAccess) {
        return getSlotsFor(type, item, registryAccess).isPresent();
    }

    private static synchronized void rebuild(RegistryAccess registryAccess) {
        if (cache != null) return;

        Map<EntityType<?>, List<Pair<HolderSet<Item>, Integer>>> newCache = new HashMap<>();
        Registry<DinoChestEntry> registry = registryAccess.registryOrThrow(DinoChests.DINO_CHEST_REGISTRY_KEY);
        Registry<EntityType<?>> entityTypes = registryAccess.registryOrThrow(Registries.ENTITY_TYPE);

        for (DinoChestEntry entry : registry) {
            EntityType<?> entityType = entityTypes.get(entry.dinoType());
            if (entityType == null) continue;

            var chestList = newCache.computeIfAbsent(entityType, k -> new LinkedList<>());

            for (DinoChestEntry.ChestEntry chest : entry.chests()) {
                chestList.add(Pair.of(chest.items(), chest.slots()));
            }
        }
        cache = Map.copyOf(newCache);
    }
}
