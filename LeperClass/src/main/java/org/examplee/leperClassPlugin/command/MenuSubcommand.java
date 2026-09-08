package org.examplee.leperClassPlugin.command;

import java.util.ArrayList;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.examplee.leperClassPlugin.LeperClassPlugin;

public final class MenuSubcommand implements Subcommand {
   private final LeperClassPlugin plugin;

   public MenuSubcommand(LeperClassPlugin plugin) {
      this.plugin = plugin;
   }

   @Override
   public String name() {
      return "menu";
   }

   @Override
   public boolean execute(CommandSender sender, String[] args) {
      if (sender instanceof Player admin) {
         Player target = admin;
         if (args.length >= 2) {
            target = Bukkit.getPlayerExact(args[1]);
            if (target == null) {
               this.plugin.msg.error(sender, "Игрок не найден: " + args[1]);
               return true;
            }
         }

         this.plugin.menu.open(admin, target);
         return true;
      } else {
         this.plugin.msg.error(sender, "Меню можно открыть только игроком.");
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
