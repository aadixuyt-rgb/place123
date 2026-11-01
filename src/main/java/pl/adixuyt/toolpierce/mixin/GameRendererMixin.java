package pl.adixuyt.toolpierce.mixin;

import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.item.ItemStack;
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
        // The '=' kill switch turns off everything here (cobweb bypass, banner bypass,
        // shears/axe player-skip). It never touches the Blaze Rod + offhand cobweb placement
        // restriction, which lives elsewhere and stays active regardless.
        if (!ToolPierceClient.areFeaturesEnabled()) return;

        ClientPlayerEntity player = client.player;
        Entity camera = client.getCameraEntity();
        if (player == null || camera == null || client.world == null || client.interactionManager == null) return;

        ItemStack mainHand = player.getMainHandStack();
        PierceRules.Rule rule = PierceRules.forStack(mainHand);
        boolean cobwebBypass = PierceRules.bypassesCobweb(mainHand);
        if (rule == null && !cobwebBypass) return;

        boolean playerOnly = rule != null && rule.mode() == PierceRules.Mode.PLAYER_ONLY;
        boolean bannerRule = rule != null && rule.mode() == PierceRules.Mode.IGNORE;

        double blockRange = player.getBlockInteractionRange();
        double entityRange = player.getEntityInteractionRange();
        Vec3d start = camera.getCameraPosVec(tickDelta);
        Vec3d look = camera.getRotationVec(1.0F);
        Vec3d blockEnd = start.add(look.multiply(blockRange));
        Vec3d entityEnd = start.add(look.multiply(entityRange));
        Box entityBox = camera.getBoundingBox().stretch(look.multiply(entityRange)).expand(1.0, 1.0, 1.0);

        // First pass: normal block raycast, except cobwebs are skipped outright whenever this
        // item bypasses them (anything but an empty hand or shears).
        BlockHitResult firstBlock = PierceUtil.raycast(client.world, camera, start, blockEnd,
                state -> cobwebBypass && state.isOf(Blocks.COBWEB));
        boolean hasBlock = firstBlock.getType() == HitResult.Type.BLOCK
                && firstBlock.getPos().squaredDistanceTo(start) <= blockRange * blockRange;

        BlockHitResult finalBlock = firstBlock;
        boolean finalHasBlock = hasBlock;

        if (bannerRule && hasBlock && PierceRules.isBanner(client.world.getBlockState(firstBlock.getBlockPos()))) {
            // A banner is only skipped if a player or mob is intersected at or behind its
            // face on this exact sightline - otherwise it keeps its normal hitbox.
            EntityHitResult behind = ProjectileUtil.raycast(camera, start, entityEnd, entityBox,
                    e -> (e instanceof PlayerEntity || e instanceof MobEntity) && !e.isSpectator() && e.canHit(),
                    entityRange * entityRange);
            double bannerDistSq = firstBlock.getPos().squaredDistanceTo(start);
            boolean entityAtOrBehindBanner = behind != null
                    && behind.getPos().squaredDistanceTo(start) + 0.36 >= bannerDistSq
                    && behind.getPos().squaredDistanceTo(start) <= entityRange * entityRange;
            if (entityAtOrBehindBanner) {
                BlockHitResult pierced = PierceUtil.raycast(client.world, camera, start, blockEnd,
                        state -> (cobwebBypass && state.isOf(Blocks.COBWEB)) || PierceRules.isBanner(state));
                finalBlock = pierced;
                finalHasBlock = pierced.getType() == HitResult.Type.BLOCK
                        && pierced.getPos().squaredDistanceTo(start) <= blockRange * blockRange;
            }
        }

        // Redo vanilla's entity-vs-block priority with the (possibly pierced) block target.
        // Shears/axe never let a player become the target: they skip the player hitbox and
        // target whatever is actually behind (block or another entity).
        double entityMaxSq = entityRange * entityRange;
        if (finalHasBlock) entityMaxSq = Math.min(entityMaxSq, finalBlock.getPos().squaredDistanceTo(start));
        EntityHitResult entityHit = ProjectileUtil.raycast(camera, start, entityEnd, entityBox,
                e -> (!playerOnly || !(e instanceof PlayerEntity)) && !e.isSpectator() && e.canHit(), entityMaxSq);

        if (entityHit != null) {
            client.crosshairTarget = entityHit;
            client.targetedEntity = entityHit.getEntity();
        } else {
            client.crosshairTarget = finalHasBlock ? finalBlock : BlockHitResult.createMissed(blockEnd,
                    Direction.getFacing(look.x, look.y, look.z), BlockPos.ofFloored(blockEnd));
            client.targetedEntity = null;
        }
    }
}
