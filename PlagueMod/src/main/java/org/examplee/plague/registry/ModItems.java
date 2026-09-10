package org.examplee.plague.registry;

import org.examplee.plague.darkness.item.DarkItems;
import org.examplee.plague.leper.item.LeperItems;
import org.examplee.plague.pale.item.PaleItems;

public class ModItems {
    public static void register() {
        LeperItems.register();
        PaleItems.register();
        DarkItems.register();
    }
}
