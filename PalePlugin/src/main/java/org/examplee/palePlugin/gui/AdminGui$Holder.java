package org.examplee.palePlugin.gui;

import java.util.UUID;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

final class AdminGui$Holder implements org.bukkit.inventory.InventoryHolder {
    final java.util.UUID owner;

    AdminGui$Holder(java.util.UUID owner) {
        super();
        this.owner = owner;
    }

    public org.bukkit.inventory.Inventory getInventory() {
        return null;
    }

}
