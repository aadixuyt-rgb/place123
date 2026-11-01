package pl.adixuyt.toolpierce;

import net.minecraft.block.AbstractBannerBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.tag.ItemTags;

import java.util.function.Predicate;

/** Which block hitboxes the crosshair ray skips, depending on the held item. */
public final class PierceRules {
    private PierceRules() {}

    /** How the ray behaves for the held item. */
    public enum Mode {
        /** Skip only the listed blocks as if they had no hitbox. */
        IGNORE,
        /** Normal block raycast (no block is skipped); only a player blocking the view is transparent. */
        PLAYER_ONLY
    }

    public record Rule(Mode mode, Predicate<BlockState> blocks) {
        /** Predicate telling the raycast which blocks to skip. */
        public Predicate<BlockState> skip() {
            return mode == Mode.IGNORE ? blocks : s -> false;
        }
    }

    private static final Rule SHEARS = new Rule(Mode.PLAYER_ONLY, s -> false);
    private static final Rule BANNER_IGNORE = new Rule(Mode.IGNORE, PierceRules::isBanner);
    private static final Rule AXE = new Rule(Mode.PLAYER_ONLY, s -> false);

    public static boolean isBanner(BlockState s) {
        return s.getBlock() instanceof AbstractBannerBlock;
    }

    /** @return the rule for the held stack, or null if the item has none. */
    public static Rule forStack(ItemStack stack) {
        if (stack.isEmpty()) return null;
        if (stack.isIn(ItemTags.AXES)) return AXE;
        if (stack.isOf(Items.SHEARS)) return SHEARS;

        // These items keep normal banner targeting. Every other held item ignores banners.
        if (stack.isOf(Items.COBWEB)
                || stack.isIn(ItemTags.BANNERS)
                || stack.isOf(Items.ELYTRA)
                || stack.isOf(Items.FIREWORK_ROCKET)
                || stack.isOf(Items.GOLDEN_APPLE)
                || stack.isOf(Items.ENCHANTED_GOLDEN_APPLE)
                || stack.isOf(Items.BLAZE_ROD)) return null;

        return BANNER_IGNORE;
    }

    /**
     * Cobweb hitboxes are ignored while aiming/mining for any held item except an empty hand
     * or shears. Unlike the banner rule above, this has no other exceptions: it applies even to
     * the normally-excluded items (cobweb, banner, elytra, firework rocket, golden apples, blaze
     * rod) and to the axe.
     */
    public static boolean bypassesCobweb(ItemStack stack) {
        return !stack.isEmpty() && !stack.isOf(Items.SHEARS);
    }

    /** Items that can be placed through hitboxes (right click): banners, cobwebs, wool. */
    public static boolean isPlaceThrough(ItemStack stack) {
        return stack.isOf(Items.COBWEB) || stack.isIn(ItemTags.WOOL) || stack.isIn(ItemTags.BANNERS);
    }

    /** True when either hand holds a cobweb, i.e. the player is placing a cobweb. */
    public static boolean isPlacingCobweb(ItemStack mainHand, ItemStack offHand) {
        return mainHand.isOf(Items.COBWEB) || offHand.isOf(Items.COBWEB);
    }

    /**
     * Blocks whose hitbox is skipped when placing through them (thin decorative blocks).
     * Banners are always skipped. Cobwebs are only skipped when {@code skipCobwebs} is true,
     * which must be false while placing a cobweb itself, so a cobweb never passes through an
     * already-placed cobweb (e.g. one worn on the legs) - it stops there instead.
     */
    public static boolean isPlaceSkipped(BlockState s, boolean skipCobwebs) {
        return isBanner(s) || (skipCobwebs && s.isOf(Blocks.COBWEB));
    }
}
