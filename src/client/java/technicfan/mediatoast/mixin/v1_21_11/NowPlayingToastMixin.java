package technicfan.mediatoast.mixin.v1_21_11;

import net.minecraft.client.gui.components.toasts.NowPlayingToast;
import technicfan.mediatoast.MediaTracker;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(NowPlayingToast.class)
public class NowPlayingToastMixin {
    @Inject(method = "tickMusicNotes", at = @At("HEAD"), cancellable = true)
    private static void pauseNotesColors(CallbackInfo ci) {
        if (MediaTracker.show() && !MediaTracker.playing()) {
            ci.cancel();
        }
    }

    // getCurrentSongName
    @Inject(method = "method_76618", at = @At("HEAD"), cancellable = true)
    private static void getCurrentSongName(CallbackInfoReturnable<String> cir) {
        if (MediaTracker.show()) {
            cir.setReturnValue(MediaTracker.track());
        }
    }
}
