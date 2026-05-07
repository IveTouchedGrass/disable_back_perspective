package ivetouchedgrass.disable_back_perspective;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

public class ModKeybinds {
    public static KeyMapping TOGGLE_MOD;
    public static void registerKeybinds() {
        TOGGLE_MOD = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "key.disable_back_perspective.toggle",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_BACKSLASH,
                KeyMapping.Category.MISC
        ) {
            private boolean oldPressed = false;

            @Override
            public void setDown(boolean pressed) {
                super.setDown(pressed);
                if (pressed && !oldPressed && Minecraft.getInstance().player != null) {
                    DisableFrontPerspective.isModEnabled = !DisableFrontPerspective.isModEnabled;
                    Minecraft.getInstance().player.sendOverlayMessage(DisableFrontPerspective.isModEnabled ? Component.translatable("disable_back_perspective.enabled_mod").withColor(0x00FF00) : Component.translatable("disable_back_perspective.disabled_mod").withColor(0xFF0000));
                }
                oldPressed = pressed;
            }
        });
    }
}