package pl.adixuyt.toolpierce.mixin;

import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BannerBlockEntityRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import pl.adixuyt.toolpierce.BannerAlphaProvider;

/**
 * Wraps the provider at the shared canvas-rendering entry point.
 * Both placed banners and the item renderer flow through renderCanvas,
 * so the opacity slider now applies to both paths.
 */
@Mixin(BannerBlockEntityRenderer.class)
public abstract class BannerBlockEntityRendererMixin {
    @ModifyVariable(
            method = {
                    "renderCanvas(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;IILnet/minecraft/client/model/ModelPart;Lnet/minecraft/client/util/SpriteIdentifier;ZLnet/minecraft/util/DyeColor;Lnet/minecraft/component/type/BannerPatternsComponent;)V",
                    "renderCanvas(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;IILnet/minecraft/client/model/ModelPart;Lnet/minecraft/client/util/SpriteIdentifier;ZLnet/minecraft/util/DyeColor;Lnet/minecraft/component/type/BannerPatternsComponent;ZZ)V"
            },
            at = @At("HEAD"),
            argsOnly = true,
            index = 1
    )
    private static VertexConsumerProvider webguard$applyBannerOpacity(VertexConsumerProvider original) {
        return BannerAlphaProvider.wrap(original);
    }
}
