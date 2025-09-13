package ivetouchedgrass.disable_back_perspective.mixin;


import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import ivetouchedgrass.disable_back_perspective.DisableFrontPerspective;
import net.minecraft.client.option.Perspective;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Perspective.class)
public abstract class MixinMinecraft {
    @ModifyReturnValue(method = "next", at = @At("RETURN"))
    private Perspective next(Perspective original) {
        if (DisableFrontPerspective.isModEnabled) {
            return switch ((Perspective) (Object) this) {
                case FIRST_PERSON, THIRD_PERSON_BACK -> Perspective.THIRD_PERSON_FRONT;
                case THIRD_PERSON_FRONT -> Perspective.FIRST_PERSON;
            };
        }
        return original;
    }
}