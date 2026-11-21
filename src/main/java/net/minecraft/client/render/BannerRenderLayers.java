package net.minecraft.client.render;

/**
 * Banner render layer that blends with the scene without writing to the depth buffer.
 * This lets block entities and block details behind a translucent banner remain visible.
 */
public final class BannerRenderLayers {
    private static final RenderLayer TRANSLUCENT_BANNER = RenderLayer.of(
            "web_guard_translucent_banner",
            VertexFormats.POSITION_COLOR_TEXTURE_OVERLAY_LIGHT_NORMAL,
            VertexFormat.DrawMode.QUADS,
            256,
            true,
            true,
            RenderLayer.MultiPhaseParameters.builder()
                    .program(RenderPhase.ENTITY_TRANSLUCENT_PROGRAM)
                    .texture(new RenderPhase.Texture(TexturedRenderLayers.BANNER_PATTERNS_ATLAS_TEXTURE, false, false))
                    .transparency(RenderPhase.TRANSLUCENT_TRANSPARENCY)
                    .depthTest(RenderPhase.LEQUAL_DEPTH_TEST)
                    .cull(RenderPhase.DISABLE_CULLING)
                    .lightmap(RenderPhase.ENABLE_LIGHTMAP)
                    .overlay(RenderPhase.ENABLE_OVERLAY_COLOR)
                    .layering(RenderPhase.NO_LAYERING)
                    .target(RenderPhase.MAIN_TARGET)
                    .texturing(RenderPhase.DEFAULT_TEXTURING)
                    .writeMaskState(RenderPhase.COLOR_MASK)
                    .build(false)
    );

    private BannerRenderLayers() {}

    public static RenderLayer translucentBanner() {
        return TRANSLUCENT_BANNER;
    }
}
