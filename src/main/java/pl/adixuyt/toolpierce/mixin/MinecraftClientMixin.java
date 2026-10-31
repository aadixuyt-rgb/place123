package pl.adixuyt.toolpierce.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.item.Items;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import pl.adixuyt.toolpierce.PierceRules;
import pl.adixuyt.toolpierce.PierceUtil;

/** Right click with banner / cobweb / wool: place through entities, banners and cobwebs. */
@Mixin(MinecraftClient.class)
public abstract class MinecraftClientMixin {
    @Unique private HitResult toolpierce$original;
    @Unique private boolean toolpierce$replaced;

    @Inject(method = "doItemUse", at = @At("HEAD"))
    private void toolpierce$placeThrough(CallbackInfo ci) {
        MinecraftClient client = (MinecraftClient) (Object) this;
        toolpierce$replaced = false;
        toolpierce$original = null;

        ClientPlayerEntity player = client.player;
        if (player == null || client.world == null) return;
        if (!PierceRules.isPlaceThrough(player.getMainHandStack())
                && !PierceRules.isPlaceThrough(player.getOffHandStack())) return;

        HitResult target = client.crosshairTarget;
        boolean blocked = target instanceof EntityHitResult
                || (target instanceof BlockHitResult b && b.getType() == HitResult.Type.BLOCK
                    && PierceRules.isPlaceSkipped(client.world.getBlockState(b.getBlockPos())));
        if (!blocked) return;

        Entity camera = client.getCameraEntity() != null ? client.getCameraEntity() : player;
        Vec3d start = camera.getCameraPosVec(1.0F);
        Vec3d end = start.add(camera.getRotationVec(1.0F).multiply(player.getBlockInteractionRange()));
        BlockHitResult hit = PierceUtil.raycast(client.world, camera, start, end, PierceRules::isPlaceSkipped);

        if (hit.getType() == HitResult.Type.BLOCK) {
            toolpierce$original = target;
            client.crosshairTarget = hit;
            toolpierce$replaced = true;
        }
    }

    @Inject(method = "doItemUse", at = @At("RETURN"))
    private void toolpierce$restore(CallbackInfo ci) {
        if (!toolpierce$replaced) return;
        ((MinecraftClient) (Object) this).crosshairTarget = toolpierce$original;
        toolpierce$original = null;
        toolpierce$replaced = false;
    }
}
