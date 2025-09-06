package ivetouchedgrass.disable_front_perspective;

import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

public class ModKeybinds {
    public static KeyBinding TOGGLE_MOD;
    public static void registerKeybinds() {
        TOGGLE_MOD = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.disable_front_perspective.toggle",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_BACKSLASH,
                "key.categories.gameplay"
        ));
    }
}