package ivetouchedgrass.disable_front_perspective.mixin;


import ivetouchedgrass.disable_front_perspective.DisableFrontPerspective;
import ivetouchedgrass.disable_front_perspective.ModKeybinds;
import net.minecraft.client.gui.screen.Overlay;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.advancement.AdvancementsScreen;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.client.gui.screen.multiplayer.SocialInteractionsScreen;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.option.HotbarStorage;
import net.minecraft.client.option.HotbarStorageEntry;
import net.minecraft.client.option.Perspective;
import net.minecraft.client.toast.TutorialToast;
import net.minecraft.client.tutorial.TutorialManager;
import net.minecraft.client.util.NarratorManager;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.*;
import net.minecraft.client.MinecraftClient;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(MinecraftClient.class)
public abstract class MixinMinecraft {
    @Shadow
    protected abstract boolean isConnectedToServer();

    @Shadow
    @Final
    private static Text SOCIAL_INTERACTIONS_NOT_AVAILABLE;

    @Shadow
    @Final
    private NarratorManager narratorManager;

    @Shadow
    @Nullable
    private TutorialToast socialInteractionsToast;

    @Shadow
    @Final
    private TutorialManager tutorialManager;

    @Shadow
    protected abstract void handleBlockBreaking(boolean breaking);

    @Shadow
    private int itemUseCooldown;

    @Shadow
    protected abstract void doItemUse();

    @Shadow
    protected abstract void doItemPick();

    @Shadow
    protected abstract void openChatScreen(String text);

    @Shadow
    protected abstract boolean doAttack();

    @Shadow
    private @Nullable Overlay overlay;
    
    @Inject(method = "handleInputEvents", at = @At("HEAD"), cancellable = true)
    private void handleInputEvents(CallbackInfo ci) {
        if (ModKeybinds.TOGGLE_MOD.isPressed() && !DisableFrontPerspective.hasModToggleKeyBeenPressed && MinecraftClient.getInstance().player != null) {
            DisableFrontPerspective.isModEnabled = !DisableFrontPerspective.isModEnabled;
            MinecraftClient.getInstance().player.sendMessage(DisableFrontPerspective.isModEnabled ? Text.translatable("disable_back_perspective.enabled_mod").withColor(0x00FF00) : Text.translatable("disable_back_perspective.disabled_mod").withColor(0xFF0000), true);
        }
        DisableFrontPerspective.hasModToggleKeyBeenPressed = ModKeybinds.TOGGLE_MOD.isPressed();
        if (!DisableFrontPerspective.isModEnabled)
            return;
        ci.cancel();
        MinecraftClient instance = ((MinecraftClient) (Object) this);

        if (instance.options.togglePerspectiveKey.wasPressed() && !DisableFrontPerspective.hasPerspectiveKeyBeenPressed) {
            Perspective perspective = instance.options.getPerspective();
            instance.options.setPerspective(instance.options.getPerspective().next());
            if (instance.options.getPerspective().equals(Perspective.THIRD_PERSON_BACK) && DisableFrontPerspective.isModEnabled) {
                instance.options.setPerspective(Perspective.THIRD_PERSON_FRONT);
            }
            if (perspective.isFirstPerson() != instance.options.getPerspective().isFirstPerson()) {
                instance.gameRenderer.onCameraEntitySet(instance.options.getPerspective().isFirstPerson() ? instance.getCameraEntity() : null);
            }
        }

        DisableFrontPerspective.hasPerspectiveKeyBeenPressed = instance.options.togglePerspectiveKey.isPressed();

        while (instance.options.togglePerspectiveKey.wasPressed()) {}

        while(instance.options.smoothCameraKey.wasPressed()) {
            instance.options.smoothCameraEnabled = !instance.options.smoothCameraEnabled;
        }

        for(int i = 0; i < 9; ++i) {
            boolean bl = instance.options.saveToolbarActivatorKey.isPressed();
            boolean bl2 = instance.options.loadToolbarActivatorKey.isPressed();
            if (instance.options.hotbarKeys[i].wasPressed()) {
                if (instance.player.isSpectator()) {
                    instance.inGameHud.getSpectatorHud().selectSlot(i);
                } else if (!instance.player.isCreative() || instance.currentScreen != null || !bl2 && !bl) {
                    instance.player.getInventory().selectedSlot = i;
                } else {
                    onCreativeHotbarKeyPress(instance, i, bl2, bl);
                }
            }
        }

        while(instance.options.socialInteractionsKey.wasPressed()) {
            if (!isConnectedToServer()) {
                instance.player.sendMessage(SOCIAL_INTERACTIONS_NOT_AVAILABLE, true);
                narratorManager.narrate(SOCIAL_INTERACTIONS_NOT_AVAILABLE);
            } else {
                if (socialInteractionsToast != null) {
                    tutorialManager.remove(socialInteractionsToast);
                    socialInteractionsToast = null;
                }

                instance.setScreen(new SocialInteractionsScreen());
            }
        }

        while(instance.options.inventoryKey.wasPressed()) {
            if (instance.interactionManager.hasRidingInventory()) {
                instance.player.openRidingInventory();
            } else {
                tutorialManager.onInventoryOpened();
                instance.setScreen(new InventoryScreen(instance.player));
            }
        }

        while(instance.options.advancementsKey.wasPressed()) {
            instance.setScreen(new AdvancementsScreen(instance.player.networkHandler.getAdvancementHandler()));
        }

        while(instance.options.swapHandsKey.wasPressed()) {
            if (!instance.player.isSpectator()) {
                instance.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket.Action.SWAP_ITEM_WITH_OFFHAND, BlockPos.ORIGIN, Direction.DOWN));
            }
        }

        while(instance.options.dropKey.wasPressed()) {
            if (!instance.player.isSpectator() && instance.player.dropSelectedItem(Screen.hasControlDown())) {
                instance.player.swingHand(Hand.MAIN_HAND);
            }
        }

        while(instance.options.chatKey.wasPressed()) {
            openChatScreen("");
        }

        if (instance.currentScreen == null && overlay == null && instance.options.commandKey.wasPressed()) {
            openChatScreen("/");
        }

        boolean bl3 = false;
        if (instance.player.isUsingItem()) {
            if (!instance.options.useKey.isPressed()) {
                instance.interactionManager.stopUsingItem(instance.player);
            }

            while(instance.options.attackKey.wasPressed()) {
            }

            while(instance.options.useKey.wasPressed()) {
            }

            while(instance.options.pickItemKey.wasPressed()) {
            }
        } else {
            while(instance.options.attackKey.wasPressed()) {
                bl3 |= doAttack();
            }

            while(instance.options.useKey.wasPressed()) {
                doItemUse();
            }

            while(instance.options.pickItemKey.wasPressed()) {
                doItemPick();
            }
        }

        if (instance.options.useKey.isPressed() && itemUseCooldown == 0 && !instance.player.isUsingItem()) {
            doItemUse();
        }

        handleBlockBreaking(instance.currentScreen == null && !bl3 && instance.options.attackKey.isPressed() && instance.mouse.isCursorLocked());
    }

    @Unique
    private static void onCreativeHotbarKeyPress(MinecraftClient client, int index, boolean restore, boolean save) {
        ClientPlayerEntity clientPlayerEntity = client.player;
        DynamicRegistryManager dynamicRegistryManager = clientPlayerEntity.getWorld().getRegistryManager();
        HotbarStorage hotbarStorage = client.getCreativeHotbarStorage();
        HotbarStorageEntry hotbarStorageEntry = hotbarStorage.getSavedHotbar(index);
        if (restore) {
            List<ItemStack> list = hotbarStorageEntry.deserialize(dynamicRegistryManager);

            for (int i = 0; i < PlayerInventory.getHotbarSize(); i++) {
                ItemStack itemStack = (ItemStack)list.get(i);
                clientPlayerEntity.getInventory().setStack(i, itemStack);
                client.interactionManager.clickCreativeStack(itemStack, 36 + i);
            }

            clientPlayerEntity.playerScreenHandler.sendContentUpdates();
        } else if (save) {
            hotbarStorageEntry.serialize(clientPlayerEntity.getInventory(), dynamicRegistryManager);
            Text text = client.options.hotbarKeys[index].getBoundKeyLocalizedText();
            Text text2 = client.options.loadToolbarActivatorKey.getBoundKeyLocalizedText();
            Text text3 = Text.translatable("inventory.hotbarSaved", new Object[]{text2, text});
            client.inGameHud.setOverlayMessage(text3, false);
            client.getNarratorManager().narrate(text3);
            hotbarStorage.save();
        }
    }
}