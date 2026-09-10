package org.examplee.plague.client;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import org.examplee.plague.network.ModNetworking;

/** Chunk-grid zone map (replaces the plugin's chat map). M = open while holding a map. */
public class PlagueMapScreen extends Screen {
    private final boolean dark;
    private int radius;
    private byte[] levels;
    private int max;
    private int extra;

    public PlagueMapScreen(boolean dark, int radius) {
        super(Component.translatable(dark ? "gui.plague.dark_map" : "gui.plague.pale_map"));
        this.dark = dark;
        this.radius = Math.max(1, Math.min(8, radius));
    }

    @Override
    protected void init() {
        request();
        int cx = width / 2;
        addRenderableWidget(Button.builder(Component.literal("-"), b -> {
            radius = Math.max(1, radius - 1);
            request();
        }).bounds(cx - 70, height - 28, 20, 20).build());
        addRenderableWidget(Button.builder(Component.literal("+"), b -> {
            radius = Math.min(8, radius + 1);
            request();
        }).bounds(cx + 50, height - 28, 20, 20).build());
        addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, b -> onClose())
                .bounds(cx - 30, height - 28, 60, 20).build());
    }

    private void request() {
        levels = null;
        ClientPlayNetworking.send(new ModNetworking.MapRequest(dark, radius));
    }

    public void onData(ModNetworking.MapData d) {
        if (d.dark() != dark) return;
        radius = d.radius();
        max = d.max();
        extra = d.extra();
        levels = d.levels();
    }

    @Override
    public void render(GuiGraphics ctx, int mouseX, int mouseY, float delta) {
        super.render(ctx, mouseX, mouseY, delta);
        int size = radius * 2 + 1;
        int cell = Math.max(4, Math.min(18, Math.min((width - 60) / size, (height - 110) / size)));
        int gw = cell * size;
        int gx = (width - gw) / 2;
        int gy = 46;
        ctx.drawCenteredString(font, getTitle(), width / 2, 12, 0xFFFFFF);
        String sub = (dark ? "darkness" : "pale") + " r=" + radius + " max=" + max
                + (dark ? " total=" + extra : " stage=" + extra);
        ctx.drawCenteredString(font, sub, width / 2, 26, 0xAAAAAA);
        ctx.fill(gx - 2, gy - 2, gx + gw + 2, gy + gw + 2, 0xFF000000);
        if (levels == null || levels.length != size * size) {
            ctx.drawCenteredString(font, "...", width / 2, gy + gw / 2, 0x888888);
            return;
        }
        for (int dz = 0; dz < size; dz++) {
            for (int dx = 0; dx < size; dx++) {
                int lvl = levels[dz * size + dx] & 0xFF;
                int color = cellColor(lvl);
                int x0 = gx + dx * cell, y0 = gy + dz * cell;
                ctx.fill(x0, y0, x0 + cell - 1, y0 + cell - 1, 0xFF000000 | color);
            }
        }
        // Center marker (player chunk).
        int c0x = gx + radius * cell, c0y = gy + radius * cell;
        ctx.fill(c0x, c0y, c0x + cell - 1, c0y + 1, 0xFFFFFFFF);
        ctx.fill(c0x, c0y + cell - 2, c0x + cell - 1, c0y + cell - 1, 0xFFFFFFFF);
        ctx.fill(c0x, c0y, c0x + 1, c0y + cell - 1, 0xFFFFFFFF);
        ctx.fill(c0x + cell - 2, c0y, c0x + cell - 1, c0y + cell - 1, 0xFFFFFFFF);
    }

    private int cellColor(int lvl) {
        float t = Math.min(1.0f, lvl / 9.0f);
        if (dark) {
            int g = (int) (20 + t * 120);
            return (g << 16) | (g << 8) | Math.min(255, g + 40);
        }
        int g = (int) (30 + t * 150);
        int r = (int) (20 + t * 60);
        return (r << 16) | (g << 8) | 20;
    }

    @Override
    public void onClose() {
        PlagueClient.lastRadius = radius;
        super.onClose();
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
