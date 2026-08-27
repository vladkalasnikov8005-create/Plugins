package org.examplee.palePlugin.util;

import java.util.HashMap;
import java.util.Iterator;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public final class InvUtil {
    private InvUtil() {
        super();
    }

    public static void giveOrDrop(org.bukkit.entity.Player p, org.bukkit.inventory.ItemStack it) {
        org.bukkit.inventory.ItemStack[] tmp1 = new org.bukkit.inventory.ItemStack[1];
        tmp1[0] = it;
        java.util.HashMap left = p.getInventory().addItem(tmp1);
        java.util.Iterator local3 = left.values().iterator();
        if (local3.hasNext()) {
            org.bukkit.inventory.ItemStack rem = (org.bukkit.inventory.ItemStack) local3.next();
            p.getWorld().dropItemNaturally(p.getLocation(), rem);
            /* continue */
        }
    }

}
