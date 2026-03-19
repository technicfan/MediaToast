package technicfan.mediatoast.mixin;

import net.minecraft.client.gui.components.toasts.ToastManager;
import net.minecraft.client.sounds.MusicManager;
import technicfan.mediatoast.MediaTracker;

import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(MusicManager.class)
public class MusicManagerMixin {
    @Redirect(
        method = {
            "startPlaying",
            "showNowPlayingToastIfNeeded"
        },
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/gui/components/toasts/ToastManager;showNowPlayingToast()V"
        )
    )
    private void showNowPlayingToast(ToastManager manager) {
        if (!MediaTracker.shouldShowLonger()) {
            manager.showNowPlayingToast();
        }
    }

    @Redirect(
        method = {
            "startPlaying",
            "showNowPlayingToastIfNeeded"
        },
        at = @At(
            value = "FIELD",
            target = "Lnet/minecraft/client/sounds/MusicManager;toastShown:Z",
            opcode = Opcodes.PUTFIELD
        )
    )
    private void toastShown(MusicManager tracker, boolean shown) {
        if (!MediaTracker.shouldShowLonger()) {
            ((MusicManagerAccessor) tracker).toastShown(shown);
        }
    }
}
