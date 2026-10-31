package pl.adixuyt.toolpierce;

import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.entity.Entity;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

import java.util.function.Predicate;

public final class PierceUtil {
    private PierceUtil() {}

    /** Block-only raycast (outline shapes) that treats blocks matching {@code skip} as if they had no hitbox. */
    public static BlockHitResult raycast(World world, Entity entity, Vec3d start, Vec3d end,
                                         Predicate<BlockState> skip) {
        ShapeContext shapeContext = ShapeContext.of(entity);
        return BlockView.raycast(start, end, shapeContext, (ctx, pos) -> {
            BlockState state = world.getBlockState(pos);
            if (skip.test(state)) return null;
            VoxelShape shape = state.getOutlineShape(world, pos, ctx);
            return world.raycastBlock(start, end, pos, shape, state);
        }, ctx -> {
            Vec3d d = start.subtract(end);
            return BlockHitResult.createMissed(end, Direction.getFacing(d.x, d.y, d.z), BlockPos.ofFloored(end));
        });
    }
}
