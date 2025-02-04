package com.aetherteam.aether.mixin.mixins.client;

import com.aetherteam.aether.client.WorldDisplayHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.PanoramaRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PanoramaRenderer.class)
public class PanoramaRendererMixin {
    /**
     * Used by the world preview system.<br>
     * Prevents the {@link net.minecraft.client.gui.screens.TitleScreen} panorama from rendering when a world preview is active.
     *
     * @param guiGraphics The rendering {@link GuiGraphics}.
     * @param width The {@link Integer} for the screen width.
     * @param height The {@link Integer} for the screen height.
     * @param fade The {@link Float} for the panorama fade-in.
     * @param partialTick The {@link Float} for the partial tick of the game.
     * @param ci        The {@link CallbackInfo} for the void method return.
     * @see WorldDisplayHelper#isActive()
     */
    @Inject(at = @At(value = "HEAD"), method = "render(Lnet/minecraft/client/gui/GuiGraphics;IIFF)V", cancellable = true)
    public void render(GuiGraphics guiGraphics, int width, int height, float fade, float partialTick, CallbackInfo ci) {
        if (Minecraft.getInstance().level != null && WorldDisplayHelper.isActive()) {
            ci.cancel();
        }
    }
}
