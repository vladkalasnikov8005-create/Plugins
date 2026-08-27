package org.examplee.leperClassPlugin.command;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.examplee.leperClassPlugin.LeperClassPlugin;

public final class MigrateItemsSubcommand implements org.examplee.leperClassPlugin.command.Subcommand {
    private final org.examplee.leperClassPlugin.LeperClassPlugin plugin;

    public MigrateItemsSubcommand(org.examplee.leperClassPlugin.LeperClassPlugin plugin) {
        super();
        this.plugin = plugin;
    }

    public java.lang.String name() {
        return "migrateitems";
    }

    public boolean execute(org.bukkit.command.CommandSender sender, java.lang.String[] args) {
        if (args.length < 2) {
            plugin.msg.warn(sender, "Использование: /leper migrateitems <player|all>");
            return true;
        }
        if (!(args[1].equalsIgnoreCase("all"))) {
            org.bukkit.entity.Player t = org.bukkit.Bukkit.getPlayerExact(args[1]);
            if (t == null) {
                plugin.msg.error(sender, "Игрок не найден: " + args[1]);
                return true;
            }
        }
        int players = 0;
        int total = 0;
        java.util.Iterator local5 = org.bukkit.Bukkit.getOnlinePlayers().iterator();
        if (!(local5.hasNext())) {
            plugin.msg.ok(sender, "Проверено игроков: " + players + ", обновлено слотов: " + total);
            return true;
        }
        org.bukkit.entity.Player p = (org.bukkit.entity.Player) local5.next();
        players++;
        total = total + plugin.migration.migratePlayer(p);
        /* continue */
        plugin.msg.ok(sender, "Проверено игроков: " + players + ", обновлено слотов: " + total);
        return true;
    }

    public java.util.List tab(org.bukkit.command.CommandSender sender, java.lang.String[] args) {
        if (args.length != 2) {
            return java.util.List.of();
        }
        java.util.List out = new java.util.ArrayList();
        out.add("all");
        org.bukkit.Bukkit.getOnlinePlayers().forEach(p -> lambda_tab_0(out, p));
        return out;
    }

    private static void lambda_tab_0(java.util.List out, org.bukkit.entity.Player p) {
        out.add(p.getName());
    }

}
