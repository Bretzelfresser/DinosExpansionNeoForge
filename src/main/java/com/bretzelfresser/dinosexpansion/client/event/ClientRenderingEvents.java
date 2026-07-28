package com.bretzelfresser.dinosexpansion.client.event;

import com.bretzelfresser.dinosexpansion.DinosExpansion;
import com.bretzelfresser.dinosexpansion.common.init.ModDataComponents;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;

@EventBusSubscriber(modid = DinosExpansion.MODID, value = Dist.CLIENT)
public class ClientRenderingEvents {
    @SubscribeEvent
    public static void addTooltipInformation(ItemTooltipEvent event) {
        if (event.getEntity() != null) {
            ItemStack stack = event.getItemStack();
            if (stack.has(ModDataComponents.NARCOTIC_VALUE.get())) {
                float value = stack.get(ModDataComponents.NARCOTIC_VALUE.get());
                event.getToolTip().add(Component.translatable("tooltip." + DinosExpansion.MODID + ".narcotic_value", value)
                        .withStyle(ChatFormatting.DARK_PURPLE));
            }
        }
    }
}
