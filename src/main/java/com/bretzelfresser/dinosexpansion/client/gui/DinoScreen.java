package com.bretzelfresser.dinosexpansion.client.gui;

import com.bretzelfresser.dinosexpansion.common.entity.base.BaseDinoEntity;
import com.bretzelfresser.dinosexpansion.common.entity.base.DinoGender;
import com.bretzelfresser.dinosexpansion.common.menu.DinoContainerMenu;
import com.bretzelfresser.dinosexpansion.common.init.ModAttributes;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;

public class DinoScreen extends AbstractContainerScreen<DinoContainerMenu> {
    
    public DinoScreen(DinoContainerMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = 200;
        this.imageHeight = 222;
        this.inventoryLabelY = this.imageHeight - 94;
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics, mouseX, mouseY, partialTick);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        this.renderTooltip(guiGraphics, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        int left = this.leftPos;
        int top = this.topPos;

        // Draw premium dark mode main frame
        guiGraphics.fill(left, top, left + this.imageWidth, top + this.imageHeight, 0xEE1A1A1A); // Dark charcoal background
        guiGraphics.fill(left - 1, top - 1, left + this.imageWidth + 1, top, 0xFF444444);      // Top border
        guiGraphics.fill(left - 1, top + this.imageHeight, left + this.imageWidth + 1, top + this.imageHeight + 1, 0xFF444444); // Bottom border
        guiGraphics.fill(left - 1, top, left, top + this.imageHeight, 0xFF444444);              // Left border
        guiGraphics.fill(left + this.imageWidth, top, left + this.imageWidth + 1, top + this.imageHeight, 0xFF444444);          // Right border

        // Draw slot boxes for active slots and locked overlays for inactive ones
        for (int i = 0; i < this.menu.slots.size(); i++) {
            Slot slot = this.menu.slots.get(i);
            int slotX = left + slot.x;
            int slotY = top + slot.y;

            if (slot.isActive()) {
                // Active slot box (18x18)
                guiGraphics.fill(slotX - 1, slotY - 1, slotX + 17, slotY + 17, 0xFF333333); // Border
                guiGraphics.fill(slotX, slotY, slotX + 16, slotY + 16, 0xFF121212);         // Slot slot background
            } else if (i < 38) {
                // Inactive dino inventory slots: draw slot background but overlay with dark cross pattern
                guiGraphics.fill(slotX - 1, slotY - 1, slotX + 17, slotY + 17, 0xFF252525);
                guiGraphics.fill(slotX, slotY, slotX + 16, slotY + 16, 0xFF0D0D0D);
                // Draw a locked visual overlay
                guiGraphics.drawString(this.font, "x", slotX + 5, slotY + 4, 0xFF555555, false);
            }
        }

        // Draw equipment labels on left panel
        // Saddle Slot (8, 18) - draw a subtle saddle outline if empty
        if (!this.menu.slots.get(0).hasItem()) {
            guiGraphics.drawString(this.font, "S", left + 8 + 5, top + 18 + 4, 0xFF555555, false);
        }
        // Armor Slot (8, 36) - draw shield/armor outline if empty
        if (!this.menu.slots.get(1).hasItem()) {
            guiGraphics.drawString(this.font, "C", left + 8 + 5, top + 36 + 4, 0xFF555555, false);
        }

        // RENDER STATS (Left Side, under equipment slots)
        BaseDinoEntity dino = this.menu.dino;
        int statsX = left + 8;
        int statsY = top + 60;

        // Health Bar
        float maxHealth = dino.getMaxHealth();
        float health = dino.getHealth();
        drawStatBar(guiGraphics, statsX, statsY, 64, 8, health / maxHealth, 0xFFFF2222, "HP: " + FormattingUtils.DEFAULT_FLOAT_FORMAT.format(health));

        // Torpor Bar
        float maxTorpor = (float) dino.getAttributeValue(ModAttributes.MAX_TORPOR);
        float torpor = dino.getTorpor();
        drawStatBar(guiGraphics, statsX, statsY + 14, 64, 8, torpor / maxTorpor, 0xFFA020F0, "Torpor: " + FormattingUtils.DEFAULT_FLOAT_FORMAT.format(torpor));

        // Hunger Bar
        float maxHunger = (float) dino.getAttributeValue(ModAttributes.MAX_HUNGER);
        float hunger = dino.getHunger();
        drawStatBar(guiGraphics, statsX, statsY + 28, 64, 8, hunger / maxHunger, 0xFFFFA500, "Hunger: " + FormattingUtils.DEFAULT_FLOAT_FORMAT.format(hunger));

        // Stamina Bar
        float maxStamina = (float) dino.getAttributeValue(ModAttributes.MAX_STAMINA);
        float stamina = dino.getStamina();
        drawStatBar(guiGraphics, statsX, statsY + 42, 64, 8, stamina / maxStamina, 0xFF00FFFF, "Stamina: " + FormattingUtils.DEFAULT_FLOAT_FORMAT.format(stamina));

        // Taming Bar (Only show if wild and unconscious)
        if (dino.currentlyTaming()) {
            float taming = dino.getTamingProgress();
            drawStatBar(guiGraphics, statsX, statsY + 56, 64, 8, taming, 0xFFFFFF00, "Taming: " + FormattingUtils.DEFAULT_FLOAT_FORMAT.format(taming * 100f) + "%");
        }
    }

    private void drawStatBar(GuiGraphics guiGraphics, int x, int y, int width, int height, float ratio, int color, String label) {
        // Track background
        guiGraphics.fill(x, y, x + width, y + height, 0xFF444444);
        
        // Progress fill
        int fillWidth = (int) (width * ratio);
        if (fillWidth > 0) {
            guiGraphics.fill(x, y, x + fillWidth, y + height, color);
        }

        // Label overlay
        // Draw tiny text above the bar
        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(x, y - 5, 0);
        guiGraphics.pose().scale(0.5f, 0.5f, 0.5f);
        guiGraphics.drawString(this.font, label, 0, 0, 0xFFFFFFFF, true);
        guiGraphics.pose().popPose();
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        // Draw Dino Name/Title
        guiGraphics.drawString(this.font, this.title, 8, 6, 0xFFFFFFFF, false);

        BaseDinoEntity dino = this.menu.dino;
        if (dino != null) {
            String genderText = dino.getGender() == DinoGender.FEMALE ? "♀" : "♂";
            int genderColor = dino.getGender() == DinoGender.FEMALE ? 0xFFFF69B4 : 0xFF1E90FF; // Pink vs Light Blue
            int width = this.font.width(this.title);
            guiGraphics.drawString(this.font, genderText, 8 + width + 5, 6, genderColor, false);
        }

        // Draw Player Inventory Title
        guiGraphics.drawString(this.font, this.playerInventoryTitle, 8, this.inventoryLabelY, 0xFF888888, false);
    }
}
