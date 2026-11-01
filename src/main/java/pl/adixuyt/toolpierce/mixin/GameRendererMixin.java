package pl.adixuyt.toolpierce.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileUtil;
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

        HitResult vanilla = client.crosshairTarget;
        double blockRange = player.getBlockInteractionRange();
        double entityRange = player.getEntityInteractionRange();

        Vec3d start = camera.getCameraPosVec(tickDelta);
        Vec3d look = camera.getRotationVec(1.0F);
        Vec3d blockEnd = start.add(look.multiply(blockRange));
        Vec3d entityEnd = start.add(look.multiply(entityRange));

        // PLAYER_ONLY items retain normal block targeting and may only hit a player.
        if (rule.mode() == PierceRules.Mode.PLAYER_ONLY) {
            if (vanilla instanceof EntityHitResult eh && !(eh.getEntity() instanceof PlayerEntity)) return;
            if (vanilla instanceof BlockHitResult bh && bh.getType() == HitResult.Type.BLOCK) {
                client.crosshairTarget = bh;
                client.targetedEntity = null;
            }
            return;
        }

        // Find the first real block. A banner is skipped only if a player is actually
        // intersected at or behind its face; otherwise the banner remains the target.
        BlockHitResult firstBlock = PierceUtil.raycast(client.world, camera, start, blockEnd, state -> false);
        boolean bannerIsFirstBlock = firstBlock.getType() == HitResult.Type.BLOCK
                && client.world.getBlockState(firstBlock.getBlockPos()).getBlock()
                    instanceof net.minecraft.block.AbstractBannerBlock;

        if (!bannerIsFirstBlock) {
            // No banner is blocking the view; keep vanilla block targeting and only
            // replace it with a player hit when vanilla has a valid player target.
            if (vanilla instanceof EntityHitResult eh && eh.getEntity() instanceof PlayerEntity) {
                client.crosshairTarget = eh;
                client.targetedEntity = eh.getEntity();
            }
            return;
        }

        Box entityBox = camera.getBoundingBox().stretch(look.multiply(entityRange)).expand(1.0, 1.0, 1.0);
        EntityHitResult entityHit = ProjectileUtil.raycast(camera, start, entityEnd, entityBox,
                e -> e instanceof PlayerEntity && !e.isSpectator() && e.canHit(),
                entityRange * entityRange);

        double bannerDistance = firstBlock.getPos().squaredDistanceTo(start);
        double playerDistance = entityHit == null ? Double.POSITIVE_INFINITY
                : entityHit.getPos().squaredDistanceTo(start);
        // Small tolerance permits a player intersecting the banner's block volume,
        // while a player standing in front of it does not cause the banner to be skipped.
        boolean playerAtOrBehindBanner = entityHit != null
                && playerDistance + 0.36 >= bannerDistance
                && playerDistance <= entityRange * entityRange;

        if (playerAtOrBehindBanner) {
            client.crosshairTarget = entityHit;
            client.targetedEntity = entityHit.getEntity();
        } else {
            client.crosshairTarget = firstBlock;
            client.targetedEntity = null;
        }
    }
}
