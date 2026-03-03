package technicfan.mpristoast.mixin;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.toasts.NowPlayingToast;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import technicfan.mpristoast.MediaTracker;

@Mixin(NowPlayingToast.class)
public class NowPlayingToastMixin {
    @Inject(method = "getNowPlayingString", at = @At("HEAD"), cancellable = true)
    private static void getNowPlayingString(CallbackInfoReturnable<Component> cir) {
        if (MediaTracker.show()) cir.setReturnValue(Component.nullToEmpty(MediaTracker.track()));
    }

    @Redirect(
        method = "getWidth",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/gui/Font;width(Lnet/minecraft/network/chat/FormattedText;)I"
        )
    )
    private static int width(Font font, FormattedText text) {
        if (MediaTracker.show()) {
            return MediaTracker.getScroller().width();
        } else {
            return font.width(text);
        }
    }

    @Redirect(
        method = "renderToast",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/gui/GuiGraphics;drawString(Lnet/minecraft/client/gui/Font;Lnet/minecraft/network/chat/Component;III)V"
        )
    )
    private static void renderToast(GuiGraphics gui, Font font, Component text, int x, int y, int color) {
        if (MediaTracker.show()) {
            MediaTracker.getScroller().draw(gui, font, x, y, color);
        } else {
            gui.drawString(font, text, x, y, color);
        }
    }
}
