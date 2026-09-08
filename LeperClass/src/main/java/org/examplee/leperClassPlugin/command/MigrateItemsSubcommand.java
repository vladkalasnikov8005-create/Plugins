package org.examplee.leperClassPlugin.command;

import java.util.ArrayList;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.examplee.leperClassPlugin.LeperClassPlugin;

public final class MigrateItemsSubcommand implements Subcommand {
   private final LeperClassPlugin plugin;

   public MigrateItemsSubcommand(LeperClassPlugin plugin) {
      this.plugin = plugin;
   }

   @Override
   public String name() {
      return "migrateitems";
   }

   @Override
   public boolean execute(CommandSender sender, String[] args) {
      if (args.length < 2) {
         this.plugin.msg.warn(sender, "Использование: /leper migrateitems <player|all>");
         return true;
      } else if (!args[1].equalsIgnoreCase("all")) {
         Player t = Bukkit.getPlayerExact(args[1]);
         if (t == null) {
            this.plugin.msg.error(sender, "Игрок не найден: " + args[1]);
            return true;
         } else {
            int changed = this.plugin.migration.migratePlayer(t);
            this.plugin.msg.ok(sender, "Игрок: " + t.getName() + ", обновлено слотов: " + changed);
            return true;
         }
      } else {
         int players = 0;
         int total = 0;

         for (Player p : Bukkit.getOnlinePlayers()) {
            players++;
            total += this.plugin.migration.migratePlayer(p);
         }

         this.plugin.msg.ok(sender, "Проверено игроков: " + players + ", обновлено слотов: " + total);
         return true;
      }
   }

   @Override
   public List<String> tab(CommandSender sender, String[] args) {
      if (args.length != 2) {
         return List.of();
      } else {
         List<String> out = new ArrayList<>();
         out.add("all");
         Bukkit.getOnlinePlayers().forEach(p -> out.add(p.getName()));
         return out;
      }
   }
}
