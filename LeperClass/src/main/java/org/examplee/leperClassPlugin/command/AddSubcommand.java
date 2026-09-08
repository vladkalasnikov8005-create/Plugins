package org.examplee.leperClassPlugin.command;

import java.util.ArrayList;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.examplee.leperClassPlugin.LeperClassPlugin;
import org.examplee.leperClassPlugin.util.EntityUtil;

public final class AddSubcommand implements Subcommand {
   private final LeperClassPlugin plugin;

   public AddSubcommand(LeperClassPlugin plugin) {
      this.plugin = plugin;
   }

   @Override
   public String name() {
      return "add";
   }

   @Override
   public boolean execute(CommandSender sender, String[] args) {
      if (args.length < 2) {
         this.plugin.msg.warn(sender, "Использование: /leper add <player>");
         return true;
      } else {
         Player t = Bukkit.getPlayerExact(args[1]);
         if (t == null) {
            this.plugin.msg.error(sender, "Игрок не найден: " + args[1]);
            return true;
         } else {
            this.plugin.data.setLeper(t, true);
            this.plugin.infection.cureDataOnly(t);
            if (this.plugin.effects.FIRE_RES != null) {
               t.removePotionEffect(this.plugin.effects.FIRE_RES);
            }

            EntityUtil.clearHostileTargets(t, 32.0);
            this.plugin.msg.ok(sender, t.getName() + " теперь Прокаженный.");
            this.plugin.log.info(sender.getName() + " set leper: " + t.getName());
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
