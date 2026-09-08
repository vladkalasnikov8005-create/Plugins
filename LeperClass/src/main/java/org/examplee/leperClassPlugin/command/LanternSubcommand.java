package org.examplee.leperClassPlugin.command;

import java.util.ArrayList;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.examplee.leperClassPlugin.LeperClassPlugin;
import org.examplee.leperClassPlugin.util.InventoryUtil;

public final class LanternSubcommand implements Subcommand {
   private final LeperClassPlugin plugin;

   public LanternSubcommand(LeperClassPlugin plugin) {
      this.plugin = plugin;
   }

   @Override
   public String name() {
      return "lantern";
   }

   @Override
   public boolean execute(CommandSender sender, String[] args) {
      if (!sender.hasPermission("leper.admin")) {
         this.plugin.msg.error(sender, "Нет прав: leper.admin");
         return true;
      } else {
         Player target;
         if (args.length >= 2) {
            target = Bukkit.getPlayerExact(args[1]);
            if (target == null) {
               this.plugin.msg.error(sender, "Игрок не найден: " + args[1]);
               return true;
            }
         } else {
            if (!(sender instanceof Player p)) {
               this.plugin.msg.warn(sender, "Использование: /leper lantern <player>");
               return true;
            }

            target = p;
         }

         InventoryUtil.giveOrDrop(target, this.plugin.items.makeQuarantineLantern());
         this.plugin.msg.ok(sender, "Карантинный фонарь выдан: " + target.getName());
         return true;
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
