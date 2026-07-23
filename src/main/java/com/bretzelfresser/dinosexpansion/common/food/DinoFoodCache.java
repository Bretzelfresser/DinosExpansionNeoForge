package com.bretzelfresser.dinosexpansion.common.food;

import com.bretzelfresser.dinosexpansion.DinosExpansion;
import com.bretzelfresser.dinosexpansion.common.init.DinoFoods;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.TagsUpdatedEvent;

import java.util.HashMap;
import java.util.Map;

@EventBusSubscriber(modid = DinosExpansion.MODID)
public class DinoFoodCache {
    private static Map<EntityType<?>, Map<Item, DinoFoodEntry.FoodValues>> cache = null;

    @SubscribeEvent
    public static void onTagsUpdated(TagsUpdatedEvent event) {
        // Invalidate the cache when datapacks reload or sync
        cache = null;
    }

    public static Map<Item, DinoFoodEntry.FoodValues> getFoodsFor(EntityType<?> type, RegistryAccess registryAccess) {
        if (cache == null) {
            rebuild(registryAccess);
        }
        return cache.getOrDefault(type, Map.of());
    }

    private static synchronized void rebuild(RegistryAccess registryAccess) {
        if (cache != null) return;

        Map<EntityType<?>, Map<Item, DinoFoodEntry.FoodValues>> newCache = new HashMap<>();
        Registry<DinoFoodEntry> registry = registryAccess.registryOrThrow(DinoFoods.DINO_FOOD_REGISTRY_KEY);
        Registry<EntityType<?>> entityTypes = registryAccess.registryOrThrow(Registries.ENTITY_TYPE);
        Registry<Item> items = registryAccess.registryOrThrow(Registries.ITEM);

        for (DinoFoodEntry entry : registry) {
            EntityType<?> entityType = entityTypes.get(entry.dinoType());
            if (entityType == null) continue;

            Map<Item, DinoFoodEntry.FoodValues> itemMap = newCache.computeIfAbsent(entityType, k -> new HashMap<>());

            for (Map.Entry<ResourceKey<Item>, DinoFoodEntry.FoodValues> food : entry.foods().entrySet()) {
                Item item = items.get(food.getKey());
                if (item != null) {
                    itemMap.put(item, food.getValue());
                }
            }
        }
        cache = Map.copyOf(newCache);
    }
}
