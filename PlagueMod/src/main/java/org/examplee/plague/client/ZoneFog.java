package org.examplee.plague.client;

import net.minecraft.client.gui.GuiGraphics;

/**
 * Zone atmosphere on the HUD: screen-edge vignette per pale stage / darkness.
 * True 3D fog + sky recolor needs a FogRenderer mixin (planned, not in v1);
 * stage-5 pale and darkness blocks already apply the vanilla Darkness effect server-side.
 */
public class ZoneFog {
    public static void render(GuiGraphics ctx) {
        int rgb;
        float intensity;
        if (ClientState.dark) {
            rgb = 0x0a0a18;
            intensity = 0.55f;
        } else if (ClientState.paleStage >= 2) {
            rgb = 0x1a4d1a;
            intensity = 0.10f + 0.07f * ClientState.paleStage;
        } else {
            return;
        }
        int w = ctx.guiWidth();
        int h = ctx.guiHeight();
        int bands = 4;
        int maxBand = (int) (Math.min(w, h) * 0.10);
        for (int i = 0; i < bands; i++) {
            float t = (float) (bands - i) / bands;
            int a = (int) (255 * intensity * t * t * 0.5f);
            if (a <= 0) continue;
            int color = (a << 24) | rgb;
            int b = maxBand * (i + 1) / bands;
            ctx.fill(0, 0, w, b, color); // top
            ctx.fill(0, h - b, w, h, color); // bottom
            ctx.fill(0, b, b, h - b, color); // left
            ctx.fill(w - b, b, w, h - b, color); // right
        }
    }
}
