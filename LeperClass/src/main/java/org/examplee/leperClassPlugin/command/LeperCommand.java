package org.examplee.leperClassPlugin.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.examplee.leperClassPlugin.LeperClassPlugin;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public final class LeperCommand implements CommandExecutor {
    private final LeperClassPlugin plugin;
    private final Map<String, Subcommand> subs;

    public LeperCommand(LeperClassPlugin plugin) {
        this.subs = new LinkedHashMap<>();
        this.plugin = plugin;
        register(new MenuSubcommand(plugin));
        register(new AddSubcommand(plugin));
        register(new RemoveSubcommand(plugin));
        register(new BlessSubcommand(plugin, true));
        register(new BlessSubcommand(plugin, false));
        register(new SneezeSubcommand(plugin));
        register(new StatusSubcommand(plugin));
        register(new MigrateItemsSubcommand(plugin));
    }

    private void register(Subcommand s) {
        subs.put(s.name().toLowerCase(), s);
    }

    public Set<String> names() {
        return subs.keySet();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!command.getName().equalsIgnoreCase("leper")) {
            return false;
        }
        if (!sender.hasPermission("leper.admin")) {
            plugin.msg.error(sender, "Нет прав: leper.admin");
            return true;
        }
        if (args.length == 0) {
            if (!(sender instanceof Player p)) {
                plugin.msg.warn(sender, "Использование: /leper menu|add|remove|bless|unbless|sneeze|status");
                return true;
            }
            plugin.menu.open(p, p);
            return true;
        }
        Subcommand sub = subs.get(args[0].toLowerCase());
        if (sub == null) {
            plugin.msg.warn(sender, "Неизвестная подкоманда. Доступно: " + String.join(", ", subs.keySet()));
            return true;
        }
        return sub.execute(sender, args);
    }
}
