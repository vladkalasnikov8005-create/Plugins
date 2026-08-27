package org.examplee.leperClassPlugin.core;

import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Player;

public final class BalanceService {
    private final org.examplee.leperClassPlugin.core.PluginSettings settings;

    public BalanceService(org.examplee.leperClassPlugin.core.PluginSettings settings) {
        super();
        this.settings = settings;
    }

    public void heal(org.bukkit.entity.Player p, double amount) {
        if (p == null) {
            return;
        }
        if (Double.compare(amount, 0.0) <= 0) {
            return;
        }
        double max = 20.0;
        try {
            org.bukkit.attribute.AttributeInstance attr = p.getAttribute(org.bukkit.attribute.Attribute.GENERIC_MAX_HEALTH);
            if (attr != null) {
                max = attr.getValue();
            }
        }
        catch (java.lang.Throwable attr) {
            p.setHealth(java.lang.Math.min(max, p.getHealth() + amount));
            return;
        }
    }

    public void hurt(org.bukkit.entity.Player p, double amount) {
        if (p == null) {
            return;
        }
        if (Double.compare(amount, 0.0) <= 0) {
            return;
        }
        p.damage(amount);
    }

    public org.examplee.leperClassPlugin.core.PluginSettings settings() {
        return settings;
    }

}
