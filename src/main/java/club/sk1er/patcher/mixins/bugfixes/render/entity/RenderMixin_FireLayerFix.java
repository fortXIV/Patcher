package club.sk1er.patcher.mixins.bugfixes.render.entity;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Render.class)
public class RenderMixin_FireLayerFix {

    @Inject(method = "renderEntityOnFire", at = @At("HEAD"))
    private void fix(Entity entity, double x, double y, double z, float partialTicks, CallbackInfo ci) {
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(770, 771);
        GlStateManager.enableAlpha();
    }
}
