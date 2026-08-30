package com.bretzelfresser.dinosexpansion.client.event;

import com.bretzelfresser.dinosexpansion.DinosExpansion;
import com.bretzelfresser.dinosexpansion.common.init.ModDataComponents;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;
import org.joml.Vector3f;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

@EventBusSubscriber(modid = DinosExpansion.MODID, value = Dist.CLIENT)
public class ClientRenderingEvents {


    public static final Map<Integer, Vec3> TRAVEL_VECTORS = new TreeMap<>();

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


    @SubscribeEvent
    public static void onRenderLevelStage(RenderLevelStageEvent event) {
        // Stage AFTER_PARTICLES is appropriate for drawing debug overlays
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_PARTICLES) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        EntityRenderDispatcher dispatcher = mc.getEntityRenderDispatcher();

        // Only render when F3 + B is enabled
        if (!dispatcher.shouldRenderHitBoxes()) {
            return;
        }
        PoseStack poseStack = event.getPoseStack();
        MultiBufferSource.BufferSource bufferSource = mc.renderBuffers().bufferSource();
        VertexConsumer vertexConsumer = bufferSource.getBuffer(RenderType.lines());
        var level = mc.level;
        assert level != null;

        for (var entry : TRAVEL_VECTORS.entrySet()) {
            poseStack.pushPose();
            var entity = level.getEntity(entry.getKey());
            if (entity != null) {
                poseStack.translate(entity.getX(), entity.getY(), entity.getZ());
                var pose = poseStack.last();
                var travelVec = entry.getValue().normalize().scale(2);
                vertexConsumer.addVertex(pose, new Vector3f())
                        .setColor(0, 255, 0, 255)
                        .setNormal(pose, (float) travelVec.x, (float) travelVec.y, (float) travelVec.z);
                vertexConsumer.addVertex(pose, (float) travelVec.x, (float) travelVec.y, (float) travelVec.z)
                        .setColor(0, 255, 0, 255)
                        .setNormal(pose, (float) travelVec.x, (float) travelVec.y, (float) travelVec.z);
            }
        }
    }
}
