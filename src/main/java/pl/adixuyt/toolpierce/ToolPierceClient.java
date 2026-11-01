package pl.adixuyt.toolpierce;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.Items;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import org.lwjgl.glfw.GLFW;

public final class ToolPierceClient implements ClientModInitializer {
    private static boolean featuresEnabled = true;
    private static boolean equalWasDown;

    public static boolean areFeaturesEnabled() {
        return featuresEnabled;
    }

    @Override
    public void onInitializeClient() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.getWindow() == null || !client.getWindow().isWindowFocused()) {
                equalWasDown = false;
                return;
            }

            boolean equalDown = GLFW.glfwGetKey(client.getWindow().getHandle(), GLFW.GLFW_KEY_EQUAL) == GLFW.GLFW_PRESS;
            if (equalDown && !equalWasDown) {
                featuresEnabled = !featuresEnabled;
                if (client.player != null) {
                    client.player.sendMessage(Text.literal("§7[§bWeb Guard§7] Funkcje: "
                            + (featuresEnabled ? "§aWŁĄCZONE" : "§cWYŁĄCZONE")
                            + " §8(=)"), true);
                }
            }
            equalWasDown = equalDown;
        });

        // This safety rule is intentionally independent from the = master switch.
        // Match the reference mod in webb: reject off-hand cobweb block use with a blaze rod in main hand.
        UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
            if (!world.isClient() || hand != Hand.OFF_HAND) return ActionResult.PASS;
            boolean blazeRodMain = player.getMainHandStack().isOf(Items.BLAZE_ROD);
            boolean cobwebOff = player.getOffHandStack().isOf(Items.COBWEB);
            return blazeRodMain && cobwebOff ? ActionResult.FAIL : ActionResult.PASS;
        });
    }
}
