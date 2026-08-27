package org.examplee.palePlugin.core;

import org.bukkit.NamespacedKey;
import org.bukkit.plugin.Plugin;

public final class PaleKeys {
    public final org.bukkit.NamespacedKey KEY_SALT;
    public final org.bukkit.NamespacedKey KEY_HOLY_WATER;
    public final org.bukkit.NamespacedKey KEY_WARD;
    public final org.bukkit.NamespacedKey KEY_PURIFIER_FLINT;
    public final org.bukkit.NamespacedKey KEY_PURIFIER_FLINT_USES;
    public final org.bukkit.NamespacedKey KEY_MAP_ITEM;
    public final org.bukkit.NamespacedKey KEY_MAP_RADIUS;
    public final org.bukkit.NamespacedKey KEY_INFECT_WAND;
    public final org.bukkit.NamespacedKey KEY_INFECT_WAND_USES;
    public final org.bukkit.NamespacedKey KEY_GUI_ACTION;
    public final org.bukkit.NamespacedKey KEY_ADMIN_PURGE;

    public PaleKeys(org.bukkit.plugin.Plugin plugin) {
        super();
        this.KEY_SALT = new org.bukkit.NamespacedKey(plugin, "pale_salt");
        this.KEY_HOLY_WATER = new org.bukkit.NamespacedKey(plugin, "pale_holy_water");
        this.KEY_WARD = new org.bukkit.NamespacedKey(plugin, "pale_ward");
        this.KEY_PURIFIER_FLINT = new org.bukkit.NamespacedKey(plugin, "pale_purifier_flint");
        this.KEY_PURIFIER_FLINT_USES = new org.bukkit.NamespacedKey(plugin, "pale_purifier_flint_uses");
        this.KEY_MAP_ITEM = new org.bukkit.NamespacedKey(plugin, "pale_map_item");
        this.KEY_MAP_RADIUS = new org.bukkit.NamespacedKey(plugin, "pale_map_radius");
        this.KEY_INFECT_WAND = new org.bukkit.NamespacedKey(plugin, "pale_infect_wand");
        this.KEY_INFECT_WAND_USES = new org.bukkit.NamespacedKey(plugin, "pale_infect_wand_uses");
        this.KEY_GUI_ACTION = new org.bukkit.NamespacedKey(plugin, "pale_gui_action");
        this.KEY_ADMIN_PURGE = new org.bukkit.NamespacedKey(plugin, "pale_admin_purge");
    }

}
