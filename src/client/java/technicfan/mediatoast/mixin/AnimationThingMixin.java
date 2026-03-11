package technicfan.mediatoast.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

//? if >1.21.10 {
/*import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.textures.GpuTextureView;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;*/
//?}

import net.minecraft.client.renderer.texture.SpriteContents;
import technicfan.mediatoast.MediaTracker;

//? if <=1.21.10 {
@Mixin(targets = "net.minecraft.client.renderer.texture.SpriteContents$Ticker")
//?} else
/*@Mixin(targets = "net.minecraft.client.renderer.texture.SpriteContents$AnimationState")*/
public class AnimationThingMixin {
    private boolean musicNotes;

    @Inject(method = "<init>", at = @At("TAIL"))
    //? if <=1.21.10 {
    private void checkId(SpriteContents contents, @Coerce Object x, @Coerce Object y, CallbackInfo ci) {
    //?} else
    /*private void checkId(SpriteContents contents, @Coerce Object x, Int2ObjectMap<GpuTextureView> y, GpuBufferSlice[] z, CallbackInfo ci) {*/
        musicNotes = contents.name().getPath().equals("icon/music_notes");
    }

    @Inject(
        //? if <=1.21.10 {
        method = "tickAndUpload",
        //?} else
        /*method = "tick",*/
        at = @At("HEAD"), cancellable = true
    )
    private void pauseNotes(CallbackInfo ci) {
        if (musicNotes && MediaTracker.show() && !MediaTracker.playing()) {
            ci.cancel();
        }
    }
}
