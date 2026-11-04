package pl.adixuyt.toolpierce;

import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.TexturedRenderLayers;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;

/**
 * Uses the banner-pattern atlas with a translucent entity layer and scales vertex alpha.
 * This provider is only passed to the banner block-entity renderer.
 */
public final class BannerAlphaProvider implements VertexConsumerProvider {
    private final VertexConsumerProvider delegate;
    private final int opacity;

    private BannerAlphaProvider(VertexConsumerProvider delegate, int opacity) {
        this.delegate = delegate;
        this.opacity = Math.max(0, Math.min(100, opacity));
    }

    public static VertexConsumerProvider wrap(VertexConsumerProvider original) {
        int opacity = ToolPierceClient.getBannerOpacity();
        if (opacity >= 100) return original;
        return new BannerAlphaProvider(original, opacity);
    }

    @Override
    public VertexConsumer getBuffer(RenderLayer ignoredLayer) {
        // Banner base and pattern UVs belong to the banner-pattern atlas, not the block atlas.
        VertexConsumer buffer = delegate.getBuffer(RenderLayer.getEntityTranslucent(
                TexturedRenderLayers.BANNER_PATTERNS_ATLAS_TEXTURE));
        return new AlphaConsumer(buffer, opacity);
    }

    private static final class AlphaConsumer implements VertexConsumer {
        private final VertexConsumer delegate;
        private final int opacity;

        private AlphaConsumer(VertexConsumer delegate, int opacity) {
            this.delegate = delegate;
            this.opacity = opacity;
        }

        @Override
        public VertexConsumer vertex(float x, float y, float z) {
            delegate.vertex(x, y, z);
            return this;
        }

        @Override
        public VertexConsumer color(int red, int green, int blue, int alpha) {
            delegate.color(red, green, blue, Math.round(alpha * opacity / 100.0f));
            return this;
        }

        @Override
        public VertexConsumer texture(float u, float v) {
            delegate.texture(u, v);
            return this;
        }

        @Override
        public VertexConsumer overlay(int u, int v) {
            delegate.overlay(u, v);
            return this;
        }

        @Override
        public VertexConsumer light(int u, int v) {
            delegate.light(u, v);
            return this;
        }

        @Override
        public VertexConsumer normal(float x, float y, float z) {
            delegate.normal(x, y, z);
            return this;
        }
    }
}
