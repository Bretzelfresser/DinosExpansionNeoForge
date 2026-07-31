package com.bretzelfresser.dinosexpansion.client.key;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.neoforged.neoforge.client.settings.KeyConflictContext;
import org.lwjgl.glfw.GLFW;

public class ModKeyBindings {

    public static final String DINO_KEY_CATEGORY = "key.categories.dinosexpansion";

    public static final KeyMapping DINO_INVENTORY_KEY = new KeyMapping(
            "key.dinosexpansion.dino_inventory",
            KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_R,
            DINO_KEY_CATEGORY
    );

    public static final KeyMapping DINO_ATTACK_KEY = new KeyMapping(
            "key.dinosexpansion.dino_attack",
            KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_C,
            DINO_KEY_CATEGORY
    );
}
