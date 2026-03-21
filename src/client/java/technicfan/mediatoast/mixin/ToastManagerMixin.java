package technicfan.mediatoast.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.gui.components.toasts.ToastManager;
import technicfan.mediatoast.MediaTracker;

@Mixin(ToastManager.class)
public class ToastManagerMixin {
    @Inject(
        method = "showNowPlayingToast",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/gui/components/toasts/NowPlayingToast;showToast(Lnet/minecraft/client/Options;)V"
        )
    )
    private void showNowPlayingToast(CallbackInfo ci) {
        if (MediaTracker.shouldShow()) MediaTracker.setToastShown(true);
    }
}
