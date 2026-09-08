package org.examplee.palePlugin.core;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import org.bukkit.Material;
import org.bukkit.plugin.java.JavaPlugin;

public final class PaleMaterials {
   private final JavaPlugin plugin;
   public Material PALE_LOG;
   public Material PALE_WOOD;
   public Material PALE_LEAVES;
   public Material PALE_MOSS_BLOCK;
   public Material PALE_MOSS_CARPET;
   private final Set<Material> infectedTypes = new HashSet<>();

   public PaleMaterials(JavaPlugin plugin) {
      this.plugin = plugin;
   }

   public Set<Material> getInfectedTypes() {
      return this.infectedTypes;
   }

   public boolean resolveOrDisable() {
      this.PALE_LOG = this.resolve("PALE_OAK_LOG");
      this.PALE_WOOD = this.resolve("PALE_OAK_WOOD");
      this.PALE_LEAVES = this.resolve("PALE_OAK_LEAVES");
      this.PALE_MOSS_BLOCK = this.resolve("PALE_MOSS_BLOCK");
      this.PALE_MOSS_CARPET = this.resolve("PALE_MOSS_CARPET");
      if (this.PALE_LOG != null && this.PALE_LEAVES != null) {
         if (this.PALE_WOOD == null) {
            this.PALE_WOOD = this.PALE_LOG;
         }

         if (this.PALE_MOSS_BLOCK == null) {
            this.PALE_MOSS_BLOCK = Material.MOSS_BLOCK;
         }

         this.infectedTypes.clear();
         this.infectedTypes.add(this.PALE_LOG);
         this.infectedTypes.add(this.PALE_WOOD);
         this.infectedTypes.add(this.PALE_LEAVES);
         this.infectedTypes.add(this.PALE_MOSS_BLOCK);
         if (this.PALE_MOSS_CARPET != null) {
            this.infectedTypes.add(this.PALE_MOSS_CARPET);
         }

         return true;
      } else {
         this.plugin.getLogger().severe("[PalePlugin] Не найдены материалы PALE_OAK_* в вашем ядре.");
         this.plugin.getServer().getPluginManager().disablePlugin(this.plugin);
         return false;
      }
   }

   private Material resolve(String name) {
      try {
         return Material.valueOf(name);
      } catch (IllegalArgumentException var3) {
         Material m = Material.matchMaterial(name);
         return m != null ? m : Material.matchMaterial("minecraft:" + name.toLowerCase(Locale.ROOT));
      }
   }
}
