package com.bretzelfresser.dinosexpansion.common.event;

import com.bretzelfresser.dinosexpansion.DinosExpansion;
import com.bretzelfresser.dinosexpansion.common.entity.base.BaseDinoEntity;
import com.bretzelfresser.dinosexpansion.common.entity.base.DinoEquipment;
import com.bretzelfresser.dinosexpansion.common.network.DinoEquipmentSyncPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.network.PacketDistributor;

@EventBusSubscriber(modid = DinosExpansion.MODID)
public class LevelEvents {

    @SubscribeEvent
    public static void onEntityKilled(LivingDeathEvent event) {
        DamageSource source = event.getSource();
        if (source.getEntity() instanceof BaseDinoEntity dino) {
            LivingEntity victim = event.getEntity();
            // Base XP: 50% of the victim's max health
            float xpGained = victim.getMaxHealth() * 0.5f;

            // Bonus XP if the victim is also a dinosaur
            if (victim instanceof BaseDinoEntity victimDino) {
                xpGained += victimDino.getDinoLevel() * 2.0f;
            }

            dino.gainXp(xpGained);
        }
    }

    @SubscribeEvent
    public static void onStartTracking(PlayerEvent.StartTracking event) {
        if (event.getTarget() instanceof BaseDinoEntity dino) {
            if (event.getEntity() instanceof ServerPlayer player) {
                for (DinoEquipment eq : DinoEquipment.values()) {
                    if (dino.getEquipmentInventory().hasEquipment(eq)) {
                        ItemStack stack = dino.getEquipmentInventory().getEquipment(eq);
                        PacketDistributor.sendToPlayer(player, new DinoEquipmentSyncPayload(dino.getId(), eq.ordinal(), stack));
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public static void onRightClickItem(PlayerInteractEvent.RightClickItem event) {
        Player player = event.getEntity();
        if (player.getVehicle() instanceof BaseDinoEntity dino) {
            ItemStack stack = event.getItemStack();
            if (!dino.canUseItem(stack)) {
                event.setCanceled(true);
            }
        }
    }

    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        Player player = event.getEntity();
        if (player.getVehicle() instanceof BaseDinoEntity dino) {
            ItemStack stack = event.getItemStack();
            if (!stack.isEmpty() && !dino.canUseItem(stack)) {
                event.setCanceled(true);
            }
        }
    }

    @SubscribeEvent
    public static void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
        Player player = event.getEntity();
        if (player.getVehicle() instanceof BaseDinoEntity dino) {
            ItemStack stack = event.getItemStack();
            if (!stack.isEmpty() && !dino.canUseItem(stack)) {
                event.setCanceled(true);
            }
        }
    }
}
