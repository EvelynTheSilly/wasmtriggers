package name.wasmtriggers.mixin;

import name.wasmtriggers.event.KeyboardEvents;
import net.minecraft.client.KeyboardHandler;
import net.minecraft.client.input.KeyEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(KeyboardHandler.class)
public class KeyboardMixin {
    @Inject(method = "keyPress", at = @At("HEAD"))
    private void onKeyPress(long handle, int action, KeyEvent event, CallbackInfo ci) {
        KeyboardEvents.KEY_PRESS.invoker().onKeyPress(
            (KeyboardHandler) (Object) this, handle, action, event
        );
    }
}
