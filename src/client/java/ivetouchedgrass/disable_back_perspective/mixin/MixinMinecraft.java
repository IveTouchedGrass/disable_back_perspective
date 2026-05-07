package ivetouchedgrass.disable_back_perspective.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import ivetouchedgrass.disable_back_perspective.DisableFrontPerspective;
import net.minecraft.client.CameraType;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(CameraType.class)
public abstract class MixinMinecraft {
    @ModifyReturnValue(method = "cycle", at = @At("RETURN"))
    private CameraType next(CameraType original) {
        if (DisableFrontPerspective.isModEnabled) {
            return switch ((CameraType) (Object) this) {
                case FIRST_PERSON, THIRD_PERSON_BACK -> CameraType.THIRD_PERSON_FRONT;
                case THIRD_PERSON_FRONT -> CameraType.FIRST_PERSON;
            };
        }
        return original;
    }
}