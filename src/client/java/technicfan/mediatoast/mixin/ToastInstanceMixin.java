package technicfan.mediatoast.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.gui.components.toasts.NowPlayingToast;
import net.minecraft.client.gui.components.toasts.Toast;
import net.minecraft.client.gui.components.toasts.ToastManager;
import technicfan.mediatoast.MediaTracker;

@Mixin(targets = "net.minecraft.client.gui.components.toasts.ToastManager$ToastInstance")
public class ToastInstanceMixin {
    private boolean isNowPlayingToast;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void ToastInstance(ToastManager manager, Toast toast, int i, int j, CallbackInfo ci) {
        isNowPlayingToast = toast instanceof NowPlayingToast;
    }

    @Inject(
        method = "update",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/gui/components/toasts/Toast;onFinishedRendering()V",
            shift = At.Shift.AFTER
        )
    )
    private void onFinishedRendering(CallbackInfo ci) {
        if (isNowPlayingToast) MediaTracker.setToastShown(false);
    }
}
