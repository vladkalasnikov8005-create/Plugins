package org.examplee.leperClassPlugin.listeners;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.scheduler.BukkitRunnable;
import org.examplee.leperClassPlugin.util.Compat;
import org.examplee.leperClassPlugin.util.ParticlesUtil;

class PlagueBombListener$1 extends BukkitRunnable {
    final PlagueBombListener this$0;
    final int durationTicks;
    final World w;
    final Location center;
    int t;

    PlagueBombListener$1(PlagueBombListener this$0, int durationTicks, World w, Location center) {
        this.this$0 = this$0;
        this.durationTicks = durationTicks;
        this.w = w;
        this.center = center;
        this.t = 0;
    }

    @Override
    public void run() {
        if (t >= durationTicks) {
            cancel();
            return;
        }
        this.t = t + 10;
        w.spawnParticle(Compat.particleFirst(new String[]{"CAMPFIRE_COSY_SMOKE", "SMOKE", "CLOUD"}), center, 6, 0.6, 0.2, 0.6, 0.01);
        ParticlesUtil.greenDust(w, center, 10, 0.9, 0.35, 0.9, 1.4F);
    }
}
