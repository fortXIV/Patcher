package club.sk1er.patcher.mixins.bugfixes.render.entity;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderArrow;
import net.minecraft.entity.projectile.EntityArrow;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(RenderArrow.class)
public class RenderArrowMixin_AlphaFix {

    @Inject(method = "doRender(Lnet/minecraft/entity/projectile/EntityArrow;DDDFF)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/GlStateManager;translate(FFF)V"))
    private void alphaFix(EntityArrow entity, double x, double y, double z, float entityYaw, float partialTicks, CallbackInfo ci) {
        GlStateManager.enableAlpha();
    }

}
