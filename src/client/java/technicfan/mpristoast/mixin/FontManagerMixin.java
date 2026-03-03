package technicfan.mpristoast.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.gui.font.FontManager;
import technicfan.mpristoast.MediaTracker;

@Mixin(FontManager.class)
public class FontManagerMixin {
    @Inject(method = "updateOptions", at = @At("TAIL"))
    private void updateFontOptions(CallbackInfo ci) {
        MediaTracker.refreshScroller();
    }
}
