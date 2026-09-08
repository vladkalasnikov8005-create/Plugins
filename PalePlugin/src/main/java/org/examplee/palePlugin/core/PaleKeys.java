package org.examplee.palePlugin.core;

import org.bukkit.NamespacedKey;
import org.bukkit.plugin.Plugin;

public final class PaleKeys {
   public final NamespacedKey KEY_SALT;
   public final NamespacedKey KEY_HOLY_WATER;
   public final NamespacedKey KEY_WARD;
   public final NamespacedKey KEY_GREATER_WARD;
   public final NamespacedKey KEY_GREATER_WARD_CHARGE;
   public final NamespacedKey KEY_PURIFIER_FLINT;
   public final NamespacedKey KEY_PURIFIER_FLINT_USES;
   public final NamespacedKey KEY_MAP_ITEM;
   public final NamespacedKey KEY_MAP_RADIUS;
   public final NamespacedKey KEY_INFECT_WAND;
   public final NamespacedKey KEY_INFECT_WAND_USES;
   public final NamespacedKey KEY_GUI_ACTION;
   public final NamespacedKey KEY_ADMIN_PURGE;
   public final NamespacedKey KEY_DARK_BLOCK;
   public final NamespacedKey KEY_DARK_BLOCK_OWNER;
   public final NamespacedKey KEY_DARK_MAP;
   public final NamespacedKey KEY_DARK_PURGE_WAND;
   public final NamespacedKey KEY_DARK_PURGE_RADIUS;

   public PaleKeys(Plugin plugin) {
      this.KEY_SALT = new NamespacedKey(plugin, "pale_salt");
      this.KEY_HOLY_WATER = new NamespacedKey(plugin, "pale_holy_water");
      this.KEY_WARD = new NamespacedKey(plugin, "pale_ward");
      this.KEY_GREATER_WARD = new NamespacedKey(plugin, "pale_greater_ward");
      this.KEY_GREATER_WARD_CHARGE = new NamespacedKey(plugin, "pale_greater_ward_charge");
      this.KEY_PURIFIER_FLINT = new NamespacedKey(plugin, "pale_purifier_flint");
      this.KEY_PURIFIER_FLINT_USES = new NamespacedKey(plugin, "pale_purifier_flint_uses");
      this.KEY_MAP_ITEM = new NamespacedKey(plugin, "pale_map_item");
      this.KEY_MAP_RADIUS = new NamespacedKey(plugin, "pale_map_radius");
      this.KEY_INFECT_WAND = new NamespacedKey(plugin, "pale_infect_wand");
      this.KEY_INFECT_WAND_USES = new NamespacedKey(plugin, "pale_infect_wand_uses");
      this.KEY_GUI_ACTION = new NamespacedKey(plugin, "pale_gui_action");
      this.KEY_ADMIN_PURGE = new NamespacedKey(plugin, "pale_admin_purge");
      this.KEY_DARK_BLOCK = new NamespacedKey(plugin, "pale_dark_block");
      this.KEY_DARK_BLOCK_OWNER = new NamespacedKey(plugin, "pale_dark_block_owner");
      this.KEY_DARK_MAP = new NamespacedKey(plugin, "pale_dark_map");
      this.KEY_DARK_PURGE_WAND = new NamespacedKey(plugin, "pale_dark_purge_wand");
      this.KEY_DARK_PURGE_RADIUS = new NamespacedKey(plugin, "pale_dark_purge_radius");
   }
}
