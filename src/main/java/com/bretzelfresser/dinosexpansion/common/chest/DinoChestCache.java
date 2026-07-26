package com.bretzelfresser.dinosexpansion.common.chest;

import com.bretzelfresser.dinosexpansion.DinosExpansion;
import com.bretzelfresser.dinosexpansion.common.init.DinoChests;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.TagsUpdatedEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.OptionalInt;

@EventBusSubscriber(modid = DinosExpansion.MODID)
public class DinoChestCache {
    private static Map<EntityType<?>, Map<Item, Integer>> cache = null;

    @SubscribeEvent
    public static void onTagsUpdated(TagsUpdatedEvent event) {
        // Invalidate the cache when datapacks reload or sync
        cache = null;
    }

    public static OptionalInt getSlotsFor(EntityType<?> type, ItemStack stack, RegistryAccess registryAccess) {
        if (stack.isEmpty()) return OptionalInt.empty();
        return getSlotsFor(type, stack.getItem(), registryAccess);
    }

    public static OptionalInt getSlotsFor(EntityType<?> type, Item item, RegistryAccess registryAccess) {
        if (cache == null) {
            rebuild(registryAccess);
        }
        Map<Item, Integer> itemMap = cache.get(type);
        if (itemMap != null && itemMap.containsKey(item)) {
            return OptionalInt.of(itemMap.get(item));
        }
        return OptionalInt.empty();
    }

    public static boolean isValidChest(EntityType<?> type, Item item, RegistryAccess registryAccess) {
        return getSlotsFor(type, item, registryAccess).isPresent();
    }

    private static synchronized void rebuild(RegistryAccess registryAccess) {
        if (cache != null) return;

        Map<EntityType<?>, Map<Item, Integer>> newCache = new HashMap<>();
        Registry<DinoChestEntry> registry = registryAccess.registryOrThrow(DinoChests.DINO_CHEST_REGISTRY_KEY);
        Registry<EntityType<?>> entityTypes = registryAccess.registryOrThrow(Registries.ENTITY_TYPE);

        for (DinoChestEntry entry : registry) {
            EntityType<?> entityType = entityTypes.get(entry.dinoType());
            if (entityType == null) continue;

            Map<Item, Integer> itemMap = newCache.computeIfAbsent(entityType, k -> new HashMap<>());

            for (DinoChestEntry.ChestEntry chest : entry.chests()) {
                int slots = chest.slots();
                for (Holder<Item> holder : chest.items()) {
                    Item item = holder.value();
                    itemMap.put(item, slots);
                }
            }
        }
        cache = Map.copyOf(newCache);
    }
}
