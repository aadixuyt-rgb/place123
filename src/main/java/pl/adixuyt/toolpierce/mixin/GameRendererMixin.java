package pl.adixuyt.toolpierce.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import pl.adixuyt.toolpierce.PierceRules;
import pl.adixuyt.toolpierce.PierceUtil;
import pl.adixuyt.toolpierce.ToolPierceClient;

@Mixin(GameRenderer.class)
public abstract class GameRendererMixin {
    @Shadow @Final MinecraftClient client;

    @Inject(method = "updateCrosshairTarget", at = @At("RETURN"))
    private void toolpierce$adjustTarget(float tickDelta, CallbackInfo ci) {
        if (!ToolPierceClient.areFeaturesEnabled()) return;

        ClientPlayerEntity player = client.player;
        Entity camera = client.getCameraEntity();
        if (player == null || camera == null || client.world == null || client.interactionManager == null) return;

        PierceRules.Rule rule = PierceRules.forStack(player.getMainHandStack());
        if (rule == null) return;

        double blockRange = player.getBlockInteractionRange();
        double entityRange = player.getEntityInteractionRange();
        double maxRange = Math.max(blockRange, entityRange);

        Vec3d start = camera.getCameraPosVec(tickDelta);
        Vec3d look = camera.getRotationVec(1.0F);
        Vec3d end = start.add(look.multiply(maxRange));

        // IGNORE rules skip eligible banners unconditionally; no entity needs to be behind them.
        BlockHitResult hit = PierceUtil.raycast(client.world, camera, start, end, rule.skip());
        boolean hasBlock = hit.getType() == HitResult.Type.BLOCK
                && hit.getPos().squaredDistanceTo(start) <= blockRange * blockRange;

        Vec3d entityEnd = start.add(look.multiply(entityRange));
        Box entityBox = camera.getBoundingBox().stretch(look.multiply(entityRange)).expand(1.0, 1.0, 1.0);
        double entityMaxSq = entityRange * entityRange;
        if (hasBlock) entityMaxSq = Math.min(entityMaxSq, hit.getPos().squaredDistanceTo(start));

        EntityHitResult entityHit = ProjectileUtil.raycast(camera, start, entityEnd, entityBox,
                e -> !e.isSpectator() && e.canHit()
                        && (rule.mode() != PierceRules.Mode.PLAYER_ONLY || !(e instanceof PlayerEntity)),
                entityMaxSq);

        if (entityHit != null) {
            client.crosshairTarget = entityHit;
            client.targetedEntity = entityHit.getEntity();
        } else {
            client.crosshairTarget = hasBlock ? hit : BlockHitResult.createMissed(end,
                    Direction.getFacing(look.x, look.y, look.z), BlockPos.ofFloored(end));
            client.targetedEntity = null;
        }
    }
}
