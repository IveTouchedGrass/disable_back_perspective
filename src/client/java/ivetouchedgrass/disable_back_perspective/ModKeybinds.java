package ivetouchedgrass.disable_back_perspective;

import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

public class ModKeybinds {
    public static KeyBinding TOGGLE_MOD;
    public static void registerKeybinds() {
        TOGGLE_MOD = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.disable_back_perspective.toggle",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_BACKSLASH,
                "key.categories.gameplay"
        ) {
            private boolean oldPressed = false;

            @Override
            public void setPressed(boolean pressed) {
                super.setPressed(pressed);
                if (pressed && !oldPressed && MinecraftClient.getInstance().player != null) {
                    DisableFrontPerspective.isModEnabled = !DisableFrontPerspective.isModEnabled;
                    MinecraftClient.getInstance().player.sendMessage(DisableFrontPerspective.isModEnabled ? Text.translatable("disable_back_perspective.enabled_mod").withColor(0x00FF00) : Text.translatable("disable_back_perspective.disabled_mod").withColor(0xFF0000), true);
                }
                oldPressed = pressed;
            }
        });
    }
}