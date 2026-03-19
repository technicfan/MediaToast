package technicfan.mediatoast.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.client.gui.components.toasts.NowPlayingToast;
import net.minecraft.resources.ResourceLocation;

@Mixin(NowPlayingToast.class)
public interface NowPlayingToastAccessor {
    @Accessor("MUSIC_NOTES_SPRITE")
    static ResourceLocation MUSIC_NOTES_SPRITE() {
        throw new AssertionError();
    }
}
