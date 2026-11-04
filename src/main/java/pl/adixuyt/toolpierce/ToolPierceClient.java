package pl.adixuyt.toolpierce;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.Items;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import org.lwjgl.glfw.GLFW;

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

public final class ToolPierceClient implements ClientModInitializer {
    private static boolean featuresEnabled = true;
    private static boolean cobwebEventEnabled = true;
    private static int bannerOpacity = 100;
    private static boolean equalWasDown;

    private static final Path SETTINGS_FILE =
            FabricLoader.getInstance().getConfigDir().resolve("web_guard.properties");

    public static boolean areFeaturesEnabled() {
        return featuresEnabled;
    }

    public static boolean isCobwebEventEnabled() {
        return cobwebEventEnabled;
    }

    public static int getBannerOpacity() {
        return bannerOpacity;
    }

    public static void setBannerOpacity(int opacity) {
        bannerOpacity = Math.max(0, Math.min(100, opacity));
    }

    public static void saveSettings() {
        Properties properties = new Properties();
        properties.setProperty("featuresEnabled", Boolean.toString(featuresEnabled));
        properties.setProperty("cobwebEventEnabled", Boolean.toString(cobwebEventEnabled));
        properties.setProperty("bannerOpacity", Integer.toString(bannerOpacity));

        try {
            Files.createDirectories(SETTINGS_FILE.getParent());
            try (OutputStream output = Files.newOutputStream(SETTINGS_FILE)) {
                properties.store(output, "Web Guard settings");
            }
        } catch (Exception exception) {
            System.err.println("[Web Guard] Could not save settings: " + exception.getMessage());
        }
    }

    private static void loadSettings() {
        if (!Files.exists(SETTINGS_FILE)) return;
        Properties properties = new Properties();
        try (InputStream input = Files.newInputStream(SETTINGS_FILE)) {
            properties.load(input);
            featuresEnabled = Boolean.parseBoolean(properties.getProperty("featuresEnabled", "true"));
            cobwebEventEnabled = Boolean.parseBoolean(properties.getProperty("cobwebEventEnabled", "true"));
            try {
                setBannerOpacity(Integer.parseInt(properties.getProperty("bannerOpacity", "100")));
            } catch (NumberFormatException ignored) {
                bannerOpacity = 100;
            }
        } catch (Exception exception) {
            System.err.println("[Web Guard] Could not load settings: " + exception.getMessage());
        }
    }

    @Override
    public void onInitializeClient() {
        loadSettings();

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.getWindow() == null
                    || GLFW.glfwGetWindowAttrib(client.getWindow().getHandle(), GLFW.GLFW_FOCUSED) != GLFW.GLFW_TRUE) {
                equalWasDown = false;
                return;
            }

            boolean equalDown = GLFW.glfwGetKey(client.getWindow().getHandle(), GLFW.GLFW_KEY_EQUAL) == GLFW.GLFW_PRESS;
            if (equalDown && !equalWasDown && client.currentScreen == null) {
                client.setScreen(new ToolPierceScreen());
            }
            equalWasDown = equalDown;
        });

        // This optional rule can be switched separately in the settings screen.
        UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
            if (!world.isClient() || hand != Hand.OFF_HAND
                    || !featuresEnabled || !cobwebEventEnabled) return ActionResult.PASS;
            boolean blazeRodMain = player.getMainHandStack().isOf(Items.BLAZE_ROD);
            boolean cobwebOff = player.getOffHandStack().isOf(Items.COBWEB);
            return blazeRodMain && cobwebOff ? ActionResult.FAIL : ActionResult.PASS;
        });
    }

    static void toggleFeatures() {
        featuresEnabled = !featuresEnabled;
        saveSettings();
    }

    static void toggleCobwebEvent() {
        cobwebEventEnabled = !cobwebEventEnabled;
        saveSettings();
    }
}
