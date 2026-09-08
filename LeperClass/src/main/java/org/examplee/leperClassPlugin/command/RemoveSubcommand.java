package org.examplee.leperClassPlugin.command;

import java.util.ArrayList;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.examplee.leperClassPlugin.LeperClassPlugin;

public final class RemoveSubcommand implements Subcommand {
   private final LeperClassPlugin plugin;

   public RemoveSubcommand(LeperClassPlugin plugin) {
      this.plugin = plugin;
   }

   @Override
   public String name() {
      return "remove";
   }

   @Override
   public boolean execute(CommandSender sender, String[] args) {
      if (args.length < 2) {
         this.plugin.msg.warn(sender, "Использование: /leper remove <player>");
         return true;
      } else {
         Player t = Bukkit.getPlayerExact(args[1]);
         if (t == null) {
            this.plugin.msg.error(sender, "Игрок не найден: " + args[1]);
            return true;
         } else {
            this.plugin.data.setLeper(t, false);
            this.plugin.infection.cureDataOnly(t);
            this.plugin.msg.ok(sender, t.getName() + " больше не Прокаженный.");
            this.plugin.log.info(sender.getName() + " removed leper: " + t.getName());
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
