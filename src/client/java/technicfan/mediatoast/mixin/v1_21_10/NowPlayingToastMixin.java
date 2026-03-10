package technicfan.mediatoast.mixin.v1_21_10;

import net.minecraft.client.gui.components.toasts.NowPlayingToast;
import technicfan.mediatoast.MediaTracker;

import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(NowPlayingToast.class)
public class NowPlayingToastMixin {
    @Shadow
    private static String currentSong;

    @Redirect(
        method = "tickMusicNotes",
        at = @At(
            value = "FIELD",
            target = "Lnet/minecraft/client/gui/components/toasts/NowPlayingToast;currentSong:Ljava/lang/String;",
            opcode = Opcodes.PUTSTATIC
        )
    )
    private static void currentSong(String key) {
        currentSong = MediaTracker.show() ? MediaTracker.track() : key;
    }

    @Inject(
        method = "tickMusicNotes",
        at = @At(
            value = "FIELD",
            target = "Lnet/minecraft/client/gui/components/toasts/NowPlayingToast;currentSong:Ljava/lang/String;",
            opcode = Opcodes.PUTSTATIC,
            shift = At.Shift.AFTER
        ),
        cancellable = true
    )
    private static void pauseNotesColors(CallbackInfo ci) {
        if (MediaTracker.show() && !MediaTracker.playing()) {
            ci.cancel();
        }
    }
}
