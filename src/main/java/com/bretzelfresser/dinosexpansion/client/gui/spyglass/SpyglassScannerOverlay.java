package com.bretzelfresser.dinosexpansion.client.gui.spyglass;

import com.bretzelfresser.dinosexpansion.DinosExpansion;
import com.bretzelfresser.dinosexpansion.client.util.DinoScannerCache;
import com.bretzelfresser.dinosexpansion.common.entity.base.BaseDinoEntity;
import com.bretzelfresser.dinosexpansion.common.item.ZoomItem;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.LayeredDraw;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import java.util.Arrays;
import java.util.Comparator;

@OnlyIn(Dist.CLIENT)
public class SpyglassScannerOverlay implements LayeredDraw.Layer {

    private float scopeScale = 0.5f;

    @Override
    public void render(GuiGraphics guiGraphics, DeltaTracker deltaTracker) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) return;


        RenderSystem.enableBlend();
        ItemStack spyglass = mc.player.getUseItem();
        boolean isVanillaSpyglass = spyglass.is(Items.SPYGLASS);
        boolean isCustomZoomItem = spyglass.getItem() instanceof ZoomItem;

        if (!isVanillaSpyglass && !isCustomZoomItem) {
            this.scopeScale = 0.5F;
            return;
        }

        // 1. Render Spyglass Scope Texture only for custom ZoomItem (since vanilla handles its own overlay)
        if (mc.options.getCameraType().isFirstPerson()) {
            if (isCustomZoomItem) {
                float deltaFrame = deltaTracker.getGameTimeDeltaTicks();
                this.scopeScale = Mth.lerp(0.5F * deltaFrame, this.scopeScale, 1.125F);
                renderSpyglassOverlay(ResourceLocation.fromNamespaceAndPath("minecraft", "textures/misc/spyglass_scope.png"), guiGraphics, this.scopeScale);
            }
        } else {
            this.scopeScale = 0.5F;
            return;
        }

        // 2. Scan for Targeted Dinosaur
        BaseDinoEntity dino = DinoScannerCache.getTargetedDinosaur(mc, 70.0F);
        if (dino != null) {
            DinoStatTypes[] stats = getStatsFor(spyglass);
            renderScannerPanel(guiGraphics, mc.font, dino, stats);
        }
        RenderSystem.disableBlend();
    }

    private DinoStatTypes[] getStatsFor(ItemStack spyglass) {
        if (spyglass.getItem() instanceof ZoomItem zoomItem) {
            if (zoomItem.canScrollZoom()) {
                // Advanced zoom item shows all stats
                return DinoStatTypes.values();
            } else {
                // Standard zoom item shows level, gender, owner
                return new DinoStatTypes[]{DinoStatTypes.LEVEL, DinoStatTypes.GENDER, DinoStatTypes.OWNER};
            }
        } else if (spyglass.is(Items.SPYGLASS)) {
            // Vanilla spyglass shows only level
            return new DinoStatTypes[]{DinoStatTypes.LEVEL};
        }
        return new DinoStatTypes[0];
    }

    private void renderSpyglassOverlay(ResourceLocation spyglassTexture, GuiGraphics guiGraphics, float scale) {
        int width = guiGraphics.guiWidth();
        int height = guiGraphics.guiHeight();
        float minDimension = (float) Math.min(width, height);
        float adjustedScale = Math.min((float) width / minDimension, (float) height / minDimension) * scale;
        int overlayWidth = Mth.floor(minDimension * adjustedScale);
        int overlayHeight = Mth.floor(minDimension * adjustedScale);
        int xStart = (width - overlayWidth) / 2;
        int yStart = (height - overlayHeight) / 2;
        int xEnd = xStart + overlayWidth;
        int yEnd = yStart + overlayHeight;

        guiGraphics.blit(spyglassTexture, xStart, yStart, -90, 0.0F, 0.0F, overlayWidth, overlayHeight, overlayWidth, overlayHeight);
        guiGraphics.fill(RenderType.guiOverlay(), 0, yEnd, width, height, -90, -16777216);
        guiGraphics.fill(RenderType.guiOverlay(), 0, 0, width, yStart, -90, -16777216);
        guiGraphics.fill(RenderType.guiOverlay(), 0, yStart, xStart, yEnd, -90, -16777216);
        guiGraphics.fill(RenderType.guiOverlay(), xEnd, yStart, width, yEnd, -90, -16777216);
    }

    private void renderScannerPanel(GuiGraphics graphics, Font font, BaseDinoEntity dino, DinoStatTypes[] supportedStats) {
        if (supportedStats == null || supportedStats.length == 0) return;

        //ensures relative order persists, and all string stats are before the bar stats
        Arrays.sort(supportedStats, Comparator.comparing(DinoStatTypes::isBar));

        //insets
        int marginTop = 4;
        int marginBot = 4;
        int marginBetweenStringBar = 6;
        int marginBetweenStrings = 3;
        int marginBetweenBars = 2;

        int marginHeader = 6;

        int barHeight = 11;
        int stringHeight = font.lineHeight;

        int panelWidth = 140;
        //16 for the race at the top
        int panelHeight = marginTop + marginBot + marginHeader + 16;

        for (int i = 0; i < supportedStats.length; i++) {
            var currentStat = supportedStats[i];

            if (currentStat.isBar()) {
                panelHeight += barHeight;
            } else {
                panelHeight += stringHeight;
            }

            if (i < supportedStats.length - 1) {
                var nexStat = supportedStats[i + 1];

                if (currentStat.isBar() && nexStat.isBar()) {
                    panelHeight += marginBetweenBars;
                }
                if (!currentStat.isBar() && nexStat.isBar()) {
                    panelHeight += marginBetweenStringBar;
                } else {
                    panelHeight += marginBetweenStrings;
                }
            }
        }

        // Position panel at the right-middle section of the screen, just outside center scope
        int x = graphics.guiWidth() / 2 + 100;
        int y = (graphics.guiHeight() - panelHeight) / 2;

        // 1. Semi-transparent modern panel backing
        graphics.fill(x, y, x + panelWidth, y + panelHeight, 0x990A0F1D);

        // Outer cyan scanner outline
        graphics.renderOutline(x, y, panelWidth, panelHeight, 0xFF0EA5E9);

        // 2. Panel Title Header
        Component name = dino.getType().getDescription();
        graphics.drawString(font, Component.translatable("spyglass." + DinosExpansion.MODID + ".stat.race", name), x + 6, y + 6, 0xFF38BDF8, false);
        y += 16 + marginHeader;

        // 3. Render each individual statistic
        for (int i = 0; i < supportedStats.length; i++) {
            var stat = supportedStats[i];
            if (stat.isBar()) {
                var text = Component.translatable("spyglass." + DinosExpansion.MODID + ".stat.bar", stat.getLabelTranslationComponent(), stat.getFormattedFloatValue(dino), stat.getFormattedMaxValue(dino));
                drawStatBar(graphics, font, x + 6, y, panelWidth - 12, barHeight, stat.getPercentage(dino), stat.getColor(), text);
                y += barHeight;
            } else {
                var textComponent = Component.translatable("spyglass." + DinosExpansion.MODID + ".stat.label", stat.getLabelTranslationComponent(), stat.getValueComponent(dino));
                graphics.drawString(font, textComponent, x + 6, y, 0xFFE2E8F0, false);
                y += stringHeight;
            }
            if (i < supportedStats.length - 1) {
                var nexStat = supportedStats[i + 1];

                if (stat.isBar() && nexStat.isBar()) {
                    y += marginBetweenBars;
                }
                if (!stat.isBar() && nexStat.isBar()) {
                    y += marginBetweenStringBar;
                } else {
                    y += marginBetweenStrings;
                }
            }

        }
    }


    private void drawStatBar(GuiGraphics graphics, Font font, int x, int y, int width, int height, float percentage, int barColor, Component label) {
        // Dark bar background
        graphics.fill(x, y, x + width, y + height, 0xFF1E293B);

        // Calculate width based on percentage
        int fillWidth = (int) (width * Math.clamp(percentage, 0.0f, 1.0f));

        // Render fill portion
        graphics.fill(x, y, x + fillWidth, y + height, barColor);

        // Thin border outline
        graphics.renderOutline(x, y, width, height, 0x33FFFFFF);

        // Center overlay text showing absolute stats (e.g. Health: 50/100)
        graphics.drawString(font, label, x + (width - font.width(label)) / 2, y + (height - font.lineHeight) / 2, 0xFFFFFFFF, false);
    }
}
