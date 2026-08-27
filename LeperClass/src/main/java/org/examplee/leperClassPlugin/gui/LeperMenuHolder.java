package org.examplee.leperClassPlugin.gui;

import java.util.UUID;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

public final class LeperMenuHolder implements org.bukkit.inventory.InventoryHolder {
    private final java.util.UUID target;

    public LeperMenuHolder(java.util.UUID target) {
        super();
        this.target = target;
    }

    public java.util.UUID getTarget() {
        return target;
    }

    public org.bukkit.inventory.Inventory getInventory() {
        return null;
    }

}
