package com.logicweaver.nmspell.util;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraftforge.client.settings.KeyConflictContext;
import org.lwjgl.glfw.GLFW;

public class KeyBinding {
    public static final String KEY_CATEGORY = "key.category.nmspell";

    public static final String KEY_TOGGLE_PERCEPTION = "key.nmspell.toggle_perception";
    public static final String KEY_TOGGLE_RUNES = "key.nmspell.toggle_runes";

    public static final KeyMapping PERCEPTION_KEY = new KeyMapping(KEY_TOGGLE_PERCEPTION, KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_O, KEY_CATEGORY);

    public static final KeyMapping RUNES_KEY = new KeyMapping(KEY_TOGGLE_RUNES, KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_P, KEY_CATEGORY);


}
