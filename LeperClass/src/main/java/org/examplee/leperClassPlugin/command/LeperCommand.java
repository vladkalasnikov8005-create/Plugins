package org.examplee.leperClassPlugin.command;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.examplee.leperClassPlugin.LeperClassPlugin;

public final class LeperCommand implements CommandExecutor {
   private final LeperClassPlugin plugin;
   private final Map<String, Subcommand> subs = new LinkedHashMap<>();

   public LeperCommand(LeperClassPlugin plugin) {
      this.plugin = plugin;
      this.register(new MenuSubcommand(plugin));
      this.register(new AddSubcommand(plugin));
      this.register(new RemoveSubcommand(plugin));
      this.register(new BlessSubcommand(plugin, true));
      this.register(new BlessSubcommand(plugin, false));
      this.register(new SneezeSubcommand(plugin));
      this.register(new LanternSubcommand(plugin));
      this.register(new StatusSubcommand(plugin));
      this.register(new MigrateItemsSubcommand(plugin));
   }

   private void register(Subcommand s) {
      this.subs.put(s.name().toLowerCase(), s);
   }

   public Set<String> names() {
      return this.subs.keySet();
   }

   public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
      if (!command.getName().equalsIgnoreCase("leper")) {
         return false;
      } else if (!sender.hasPermission("leper.admin")) {
         this.plugin.msg.error(sender, "Нет прав: leper.admin");
         return true;
      } else if (args.length == 0) {
         if (sender instanceof Player p) {
            this.plugin.menu.open(p, p);
            return true;
         } else {
            this.plugin.msg.warn(sender, "Использование: /leper menu|add|remove|bless|unbless|sneeze|status");
            return true;
         }
      } else {
         Subcommand sub = this.subs.get(args[0].toLowerCase());
         if (sub == null) {
            this.plugin.msg.warn(sender, "Неизвестная подкоманда. Доступно: " + String.join(", ", this.subs.keySet()));
            return true;
         } else {
            return sub.execute(sender, args);
         }
      }
   }
}
