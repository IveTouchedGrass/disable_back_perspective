package ivetouchedgrass.disable_back_perspective;

import net.fabricmc.api.ModInitializer;

public class DisableBackPerspective implements ModInitializer {
    public static boolean hasPerspectiveKeyBeenPressed = false;
    public static boolean hasModToggleKeyBeenPressed = false;
    public static boolean isModEnabled = true;

    @Override
    public void onInitialize() {
        ModKeybinds.registerKeybinds();
    }
}
