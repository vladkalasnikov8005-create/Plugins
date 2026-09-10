package org.examplee.plague.client;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;

/** Infection bar, zone badges, ward charge, cinematic banner. */
public class PlagueHud {
    public static void render(GuiGraphics ctx, DeltaTracker tick) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.options.hideGui) return;
        int w = ctx.guiWidth();
        int h = ctx.guiHeight();

        ZoneFog.render(ctx);

        // Infection panel (bottom-left).
        if (ClientState.leper || ClientState.stage > 0 || ClientState.hits > 0) {
            int x = 10, y = h - 64;
            String title = ClientState.leper ? "LEPER" : (ClientState.stage > 0 ? "INFECTED s" + ClientState.stage : "EXPOSED");
            int titleColor = ClientState.leper ? 0xFF5555 : 0x55FF55;
            ctx.drawString(mc.font, title, x, y, titleColor);
            if (!ClientState.leper && ClientState.stage == 0) {
                for (int i = 0; i < 3; i++) {
                    int c = i < ClientState.hits ? 0x39FF14 : 0x333333;
                    ctx.fill(x + i * 26, y + 12, x + i * 26 + 22, y + 20, 0xFF000000 | c);
                }
            } else {
                String sub = ClientState.leper
                        ? (ClientState.rageLeftMs > 0 ? "RAGE " + (ClientState.rageLeftMs / 1000) + "s" : "stable")
                        : "hits " + ClientState.hits + "/3";
                ctx.drawString(mc.font, sub, x, y + 12, 0xAAAAAA);
            }
            if (ClientState.blessed) ctx.drawString(mc.font, "blessed", x, y + 24, 0xFFD700);
        }

        // Zone badges (top-left).
        int by = 10;
        if (ClientState.paleStage >= 1) {
            ctx.drawString(mc.font, "PALE s" + ClientState.paleStage, 10, by, 0x7CFC00);
            by += 12;
        }
        if (ClientState.dark) {
            ctx.drawString(mc.font, "DARKNESS", 10, by, 0x8A7CFF);
            by += 12;
        }
        if (ClientState.lantern) {
            ctx.drawString(mc.font, "LANTERN", 10, by, 0xFFD700);
            by += 12;
        }

        // Ward charge (under badges).
        if (ClientState.wardFresh()) {
            var info = ClientState.ward;
            String label = "great".equals(info.kind()) ? "GREAT WARD" : "LANTERN";
            ctx.drawString(mc.font, label, 10, by, 0x55FFFF);
            by += 12;
            int bw = 90;
            ctx.fill(10, by, 10 + bw, by + 6, 0xFF222222);
            int fill = info.max() > 0 ? (int) (bw * Math.min(1.0, info.charge() / (double) info.max())) : 0;
            ctx.fill(10, by, 10 + fill, by + 6, 0xFF55FFFF);
            ctx.drawString(mc.font, (info.charge() / 60) + "m " + (info.charge() % 60) + "s", 10 + bw + 5, by - 2, 0xAAAAAA);
        }

        // Cinematic banner.
        if (ClientState.cinematicActive()) {
            ctx.drawString(mc.font, "THE PLAGUE TAKES YOU", w / 2 - mc.font.width("THE PLAGUE TAKES YOU") / 2, h / 3, 0xFF2222);
        }
    }
}
