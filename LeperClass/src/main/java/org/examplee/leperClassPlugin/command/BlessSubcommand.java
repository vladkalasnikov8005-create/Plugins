package org.examplee.leperClassPlugin.command;

import java.util.ArrayList;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.examplee.leperClassPlugin.LeperClassPlugin;

public final class BlessSubcommand implements Subcommand {
   private final LeperClassPlugin plugin;
   private final boolean bless;

   public BlessSubcommand(LeperClassPlugin plugin, boolean bless) {
      this.plugin = plugin;
      this.bless = bless;
   }

   @Override
   public String name() {
      return this.bless ? "bless" : "unbless";
   }

   @Override
   public boolean execute(CommandSender sender, String[] args) {
      if (args.length < 2) {
         this.plugin.msg.warn(sender, "Использование: /leper " + this.name() + " <player>");
         return true;
      } else {
         Player t = Bukkit.getPlayerExact(args[1]);
         if (t == null) {
            this.plugin.msg.error(sender, "Игрок не найден: " + args[1]);
            return true;
         } else {
            this.plugin.data.setDangerBlessed(t, this.bless);
            if (this.bless) {
               this.plugin.msg.ok(sender, t.getName() + " получил благословление Денжер.");
               this.plugin.msg.info(t, "Вы получили благословление Денжер.");
            } else {
               this.plugin.msg.ok(sender, t.getName() + " лишен благословления Денжер.");
               this.plugin.msg.info(t, "Благословление Денжер снято.");
            }

            this.plugin.log.info(sender.getName() + " " + this.name() + " " + t.getName());
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
