package technicfan.mediatoast.mixin;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.toasts.NowPlayingToast;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import technicfan.mediatoast.MediaToastClient;
import technicfan.mediatoast.MediaTracker;

import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(NowPlayingToast.class)
public class NowPlayingToastMixin {
    private static int width;
    //? if <=1.21.10 {
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
        currentSong = MediaTracker.shouldShowLonger() ? MediaTracker.track() : key;
    }
    //?} else {
    /*@Inject(method = "getCurrentSongName", at = @At("HEAD"), cancellable = true)
    private static void getCurrentSongName(CallbackInfoReturnable<String> cir) {
        if (MediaTracker.shouldShowLonger()) {
            cir.setReturnValue(MediaTracker.track());
        }
    }*/
    //?}

    @Inject(method = "getNowPlayingString", at = @At("HEAD"), cancellable = true)
    private static void getNowPlayingString(String key, CallbackInfoReturnable<Component> cir) {
        if (MediaTracker.shouldShowLonger())
                cir.setReturnValue(Component.nullToEmpty(key));
    }

    //? if <=1.21.10 {
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
    //?} else
    /*@Inject(method = "tickMusicNotes", at = @At("HEAD"), cancellable = true)*/
    private static void tickMusicNotes(CallbackInfo ci) {
        if (MediaTracker.shouldShowLonger() && !MediaTracker.isPlaying()) {
            ci.cancel();
        }
    }

    @Redirect(
        method = "getWidth",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/gui/Font;width(Lnet/minecraft/network/chat/FormattedText;)I"
        )
    )
    private static int width(Font font, FormattedText text) {
        if (MediaTracker.shouldShowLonger()) {
            width = font.width(text);
            return width <= MediaTracker.maxWidth ? width : MediaTracker.maxWidth;
        } else {
            return font.width(text);
        }
    }

    @Redirect(
        //? if <=1.21.11 {
        method = "renderToast",
        //?} else
        /*method = "extractToast",*/
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/gui/GuiGraphics;drawString(Lnet/minecraft/client/gui/Font;Lnet/minecraft/network/chat/Component;III)V"
        )
    )
    private static void drawString(GuiGraphics gui, Font font, Component text, int x, int y, int color) {
        if ((MediaTracker.shouldShowLonger() || text.getString().equals(MediaTracker.track())) && width > MediaTracker.maxWidth) {
            gui.enableScissor(x, y, x + MediaTracker.maxWidth + translationOffset(x, gui.pose().m20), y + font.lineHeight);
            gui.pose().pushMatrix();
            gui.pose().translate(x - MediaTracker.currentScrollOffset(width), y);
            gui.drawString(font, text, 0, 0, color);
            gui.pose().popMatrix();
            gui.disableScissor();
        } else {
            gui.drawString(font, text, x, y, color);
        }
    }

    // In VulkanMod a matrix translation seems to not be applied to `enableScissor()`
    // while in Vanilla it does, so I roughly apply it manually (float -> int, but it's enough ig)
    private static int translationOffset(int start, float offset) {
        if (MediaToastClient.hasVulkanMod) {
            if (-offset >= start) {
                return (int) offset + start;
            } else {
                return offset > 0 ? start + (int) offset : 0;
            }
        } else {
            return 0;
        }
    }
}
