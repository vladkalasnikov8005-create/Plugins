package org.examplee.leperClassPlugin.core;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public final class MovementLock {
    private final java.util.Map locks;
    private final org.bukkit.potion.PotionEffectType slow;

    public MovementLock(org.bukkit.potion.PotionEffectType slow) {
        super();
        this.locks = new java.util.HashMap();
        this.slow = slow;
    }

    public void lock(org.bukkit.entity.Player p) {
        org.examplee.leperClassPlugin.core.MovementLock$State st = (org.examplee.leperClassPlugin.core.MovementLock$State) locks.computeIfAbsent(p.getUniqueId(), x -> lambda$lock$0(p, x));
        st.locks = st.locks + 1;
        p.setSprinting(false);
        p.setWalkSpeed(0.0F);
        p.setFlySpeed(0.0F);
        if (slow != null) {
            p.addPotionEffect(new org.bukkit.potion.PotionEffect(slow, 40, 10, false, false, false));
        }
    }

    public void unlock(org.bukkit.entity.Player p) {
        org.examplee.leperClassPlugin.core.MovementLock$State st = (org.examplee.leperClassPlugin.core.MovementLock$State) locks.get(p.getUniqueId());
        if (st == null) {
            return;
        }
        st.locks = st.locks - 1;
        if (st.locks <= 0) {
            p.setWalkSpeed(st.walk);
            p.setFlySpeed(st.fly);
            locks.remove(p.getUniqueId());
        }
    }

    public void release(org.bukkit.entity.Player p) {
        org.examplee.leperClassPlugin.core.MovementLock$State st = (org.examplee.leperClassPlugin.core.MovementLock$State) locks.remove(p.getUniqueId());
        if (st != null) {
            p.setWalkSpeed(st.walk);
            p.setFlySpeed(st.fly);
        }
    }

    private static org.examplee.leperClassPlugin.core.MovementLock$State lambda$lock$0(org.bukkit.entity.Player p, java.util.UUID x) {
        return new org.examplee.leperClassPlugin.core.MovementLock$State(p.getWalkSpeed(), p.getFlySpeed());
    }

}
