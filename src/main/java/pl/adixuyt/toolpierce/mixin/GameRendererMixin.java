package pl.adixuyt.toolpierce.mixin;

import net.minecraft.block.BlockState;
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

import java.util.function.Predicate;

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
        // Cobweb hitbox is only skipped while hitting/mining - and only when a player or mob
        // is standing in or behind the cobweb on this exact sightline. Shears (and an empty
        // hand) never skip it.
        boolean cobwebConditional = PierceRules.bypassesCobweb(mainHand);
        boolean bannerConditional = rule != null && rule.mode() == PierceRules.Mode.IGNORE;
        boolean playerOnly = rule != null && rule.mode() == PierceRules.Mode.PLAYER_ONLY;
        if (!cobwebConditional && !bannerConditional && !playerOnly) return;

        double blockRange = player.getBlockInteractionRange();
        double entityRange = player.getEntityInteractionRange();
        Vec3d start = camera.getCameraPosVec(tickDelta);
        Vec3d look = camera.getRotationVec(1.0F);
        Vec3d blockEnd = start.add(look.multiply(blockRange));
        Vec3d entityEnd = start.add(look.multiply(entityRange));
        Box entityBox = camera.getBoundingBox().stretch(look.multiply(entityRange)).expand(1.0, 1.0, 1.0);

        // True first block - nothing is skipped yet.
        BlockHitResult firstBlock = PierceUtil.raycast(client.world, camera, start, blockEnd, state -> false);
        boolean hasBlock = firstBlock.getType() == HitResult.Type.BLOCK
                && firstBlock.getPos().squaredDistanceTo(start) <= blockRange * blockRange;

        BlockHitResult finalBlock = firstBlock;
        boolean finalHasBlock = hasBlock;

        if (hasBlock) {
            BlockState firstState = client.world.getBlockState(firstBlock.getBlockPos());
            boolean isCobwebFirst = cobwebConditional && firstState.isOf(Blocks.COBWEB);
            boolean isBannerFirst = bannerConditional && PierceRules.isBanner(firstState);

            if (isCobwebFirst || isBannerFirst) {
                // Pierce it only if a player or mob is intersected at or behind its face on
                // this sightline - otherwise it keeps its normal hitbox.
                EntityHitResult behind = ProjectileUtil.raycast(camera, start, entityEnd, entityBox,
                        e -> (e instanceof PlayerEntity || e instanceof MobEntity) && !e.isSpectator() && e.canHit(),
                        entityRange * entityRange);
                double blockDistSq = firstBlock.getPos().squaredDistanceTo(start);
                boolean entityAtOrBehind = behind != null
                        && behind.getPos().squaredDistanceTo(start) + 0.36 >= blockDistSq
                        && behind.getPos().squaredDistanceTo(start) <= entityRange * entityRange;
                if (entityAtOrBehind) {
                    Predicate<BlockState> skip = isCobwebFirst
                            ? state -> state.isOf(Blocks.COBWEB)
                            : PierceRules::isBanner;
                    BlockHitResult pierced = PierceUtil.raycast(client.world, camera, start, blockEnd, skip);
                    finalBlock = pierced;
                    finalHasBlock = pierced.getType() == HitResult.Type.BLOCK
                            && pierced.getPos().squaredDistanceTo(start) <= blockRange * blockRange;
                }
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
