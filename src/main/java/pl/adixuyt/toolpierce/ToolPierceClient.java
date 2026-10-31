package pl.adixuyt.toolpierce;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.item.Items;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import org.lwjgl.glfw.GLFW;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/** Blocks off-hand cobweb placement while a blaze rod is held in the main hand. */
public final class ToolPierceClient implements ClientModInitializer {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static Path configPath;
    private static boolean enabled = true;
    private static KeyBinding toggleKey;

    @Override
    public void onInitializeClient() {
        configPath = MinecraftClient.getInstance().runDirectory.toPath().resolve("config/web_guard.json");
        loadConfig();

        toggleKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.webguard.toggle", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_UNKNOWN, "key.category.webguard"));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (toggleKey.wasPressed()) {
                enabled = !enabled;
                saveConfig();
                if (client.player != null) {
                    client.player.sendMessage(Text.literal("§7[§bWeb Guard§7] " + (enabled ? "§aWłączony" : "§cWyłączony")), true);
                }
            }
        });

        UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
            if (!world.isClient() || !enabled || hand != Hand.OFF_HAND) return ActionResult.PASS;
            boolean mainIsBlazeRod = !player.getMainHandStack().isEmpty()
                    && player.getMainHandStack().getItem() == Items.BLAZE_ROD;
            boolean offIsWeb = !player.getOffHandStack().isEmpty()
                    && player.getOffHandStack().getItem() == Items.COBWEB;
            return mainIsBlazeRod && offIsWeb ? ActionResult.FAIL : ActionResult.PASS;
        });
    }

    private static void saveConfig() {
        if (configPath == null) return;
        try {
            Files.createDirectories(configPath.getParent());
            JsonObject json = new JsonObject();
            json.addProperty("enabled", enabled);
            Files.writeString(configPath, GSON.toJson(json));
        } catch (IOException ignored) {}
    }

    private static void loadConfig() {
        try {
            if (configPath == null || !Files.exists(configPath)) return;
            JsonObject json = JsonParser.parseString(Files.readString(configPath)).getAsJsonObject();
            if (json.has("enabled")) enabled = json.get("enabled").getAsBoolean();
        } catch (Exception ignored) {}
    }
}
