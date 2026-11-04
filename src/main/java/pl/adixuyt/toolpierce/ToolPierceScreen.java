package pl.adixuyt.toolpierce;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.text.Text;

/** Settings opened with the equals key. */
public final class ToolPierceScreen extends Screen {
    private static final int PANEL_WIDTH = 310;
    private static final int PANEL_HEIGHT = 176;

    private final class OpacitySlider extends SliderWidget {
        OpacitySlider(int x, int y, int width, int height) {
            super(x, y, width, height, Text.empty(), ToolPierceClient.getBannerOpacity() / 100.0);
            updateMessage();
        }

        @Override
        protected void updateMessage() {
            setMessage(Text.literal("Przezroczystość sztandarów: "
                    + ToolPierceClient.getBannerOpacity() + "%"));
        }

        @Override
        protected void applyValue() {
            int percent = (int) Math.round(this.value * 100.0);
            ToolPierceClient.setBannerOpacity(percent);
            this.value = percent / 100.0;
            updateMessage();
        }
    }

    public ToolPierceScreen() {
        super(Text.literal("Web Guard"));
    }

    private void rebuildButtons() {
        clearChildren();
        int left = this.width / 2 - PANEL_WIDTH / 2 + 24;
        int width = PANEL_WIDTH - 48;
        int top = this.height / 2 - PANEL_HEIGHT / 2;

        addDrawableChild(ButtonWidget.builder(
                Text.literal("Funkcje: " + (ToolPierceClient.areFeaturesEnabled() ? "ON" : "OFF")),
                button -> {
                    ToolPierceClient.toggleFeatures();
                    rebuildButtons();
                }).dimensions(left, top + 42, width, 20).build());

        addDrawableChild(ButtonWidget.builder(
                Text.literal("Ochrona eventu pajęczyny: "
                        + (ToolPierceClient.isCobwebEventEnabled() ? "ON" : "OFF")),
                button -> {
                    ToolPierceClient.toggleCobwebEvent();
                    rebuildButtons();
                }).dimensions(left, top + 70, width, 20).build());

        addDrawableChild(new OpacitySlider(left, top + 108, width, 20));

        addDrawableChild(ButtonWidget.builder(Text.literal("Gotowe"), button -> close())
                .dimensions(this.width / 2 - 50, top + 142, 100, 20).build());
    }

    @Override
    protected void init() {
        rebuildButtons();
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        boolean handled = super.mouseReleased(mouseX, mouseY, button);
        ToolPierceClient.saveSettings();
        return handled;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderBackground(context, mouseX, mouseY, delta);
        int left = this.width / 2 - PANEL_WIDTH / 2;
        int top = this.height / 2 - PANEL_HEIGHT / 2;
        int right = left + PANEL_WIDTH;
        int bottom = top + PANEL_HEIGHT;

        context.fill(left, top, right, bottom, 0xF014171D);
        context.fill(left, top, right, top + 2, 0xFF4BA3FF);
        context.drawCenteredTextWithShadow(this.textRenderer, Text.literal("WEB GUARD"),
                this.width / 2, top + 15, 0xFFFFFFFF);
        context.drawCenteredTextWithShadow(this.textRenderer,
                Text.literal("Ustawienia funkcji i wyglądu sztandarów"),
                this.width / 2, top + 29, 0xFFB8C0CC);
        context.drawCenteredTextWithShadow(this.textRenderer,
                Text.literal("0% = niewidoczny   •   50% = półprzezroczysty   •   100% = normalny"),
                this.width / 2, top + 96, 0xFFB8C0CC);

        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}
