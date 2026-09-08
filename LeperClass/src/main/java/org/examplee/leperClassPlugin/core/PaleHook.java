package org.examplee.leperClassPlugin.core;

import java.lang.reflect.Method;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.plugin.Plugin;

public final class PaleHook {
   private Plugin pluginRef;
   private Method apiInfect;
   private Method apiGetStageAt;

   public void hook() {
      this.ensureHooked();
   }

   private boolean ensureHooked() {
      try {
         Plugin pl = Bukkit.getPluginManager().getPlugin("PalePlugin");
         if (pl == null || !pl.isEnabled()) {
            this.unhook();
            return false;
         }

         if (pl != this.pluginRef || this.apiInfect == null) {
            this.apiInfect = pl.getClass().getMethod("apiInfect", Location.class, int.class, int.class);

            try {
               this.apiGetStageAt = pl.getClass().getMethod("apiGetStageAt", Location.class);
            } catch (Throwable t) {
               this.apiGetStageAt = null;
            }

            this.pluginRef = pl;
         }

         return true;
      } catch (Throwable t) {
         this.unhook();
         return false;
      }
   }

   private void unhook() {
      this.pluginRef = null;
      this.apiInfect = null;
      this.apiGetStageAt = null;
   }

   public int infect(Location loc, int radius, int maxBlocks) {
      if (loc == null || !this.ensureHooked()) {
         return 0;
      } else {
         try {
            return this.apiInfect.invoke(this.pluginRef, loc, radius, maxBlocks) instanceof Integer i ? i : 0;
         } catch (Throwable t) {
            this.unhook();
            return 0;
         }
      }
   }

   /** Стадия заражения чанка в точке (0..5); 0 если PalePlugin недоступен. */
   public int getStageAt(Location loc) {
      if (loc == null || !this.ensureHooked() || this.apiGetStageAt == null) {
         return 0;
      } else {
         try {
            return this.apiGetStageAt.invoke(this.pluginRef, loc) instanceof Integer i ? i : 0;
         } catch (Throwable t) {
            this.unhook();
            return 0;
         }
      }
   }
}
