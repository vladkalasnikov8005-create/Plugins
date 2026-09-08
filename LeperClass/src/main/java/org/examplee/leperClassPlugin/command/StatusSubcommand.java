package org.examplee.leperClassPlugin.command;

import java.util.ArrayList;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.examplee.leperClassPlugin.LeperClassPlugin;

public final class StatusSubcommand implements Subcommand {
   private final LeperClassPlugin plugin;

   public StatusSubcommand(LeperClassPlugin plugin) {
      this.plugin = plugin;
   }

   @Override
   public String name() {
      return "status";
   }

   @Override
   public boolean execute(CommandSender sender, String[] args) {
      if (args.length < 2) {
         this.plugin.msg.warn(sender, "Использование: /leper status <player>");
         return true;
      } else {
         Player t = Bukkit.getPlayerExact(args[1]);
         if (t == null) {
            this.plugin.msg.error(sender, "Игрок не найден: " + args[1]);
            return true;
         } else {
            boolean leper = this.plugin.data.isLeper(t);
            int stage = this.plugin.data.getInfectionStage(t);
            int hits = this.plugin.data.getInfectionHits(t);
            boolean blessed = this.plugin.data.isDangerBlessed(t);
            long rage = Math.max(0L, this.plugin.data.getRageUntil(t) - System.currentTimeMillis());
            int umb = -1;
            ItemStack off = t.getInventory().getItemInOffHand();
            if (this.plugin.tags.isUmbrella(off) && off.getItemMeta() != null) {
               umb = (Integer)off.getItemMeta().getPersistentDataContainer().getOrDefault(this.plugin.keys.umbrellaRemainingKey, PersistentDataType.INTEGER, 0);
            }

            this.plugin
               .msg
               .info(
                  sender,
                  "Статус "
                     + t.getName()
                     + ": class="
                     + (leper ? "leper" : "normal")
                     + ", stage="
                     + stage
                     + ", hits="
                     + hits
                     + ", bless="
                     + blessed
                     + ", rage="
                     + rage / 1000L
                     + "s, umbrella="
                     + (umb < 0 ? "none" : umb + "s")
               );
            return true;
         }
      }
   }

   @Override
   public List<String> tab(CommandSender sender, String[] args) {
      if (args.length != 2) {
         return List.of();
      } else {
         List<String> out = new ArrayList<>();
         Bukkit.getOnlinePlayers().forEach(p -> out.add(p.getName()));
         return out;
      }
   }
}
