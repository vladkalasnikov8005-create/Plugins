package org.examplee.leperClassPlugin.command;

import java.util.ArrayList;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.examplee.leperClassPlugin.LeperClassPlugin;

public final class RemoveSubcommand implements org.examplee.leperClassPlugin.command.Subcommand {
    private final org.examplee.leperClassPlugin.LeperClassPlugin plugin;

    public RemoveSubcommand(org.examplee.leperClassPlugin.LeperClassPlugin plugin) {
        super();
        this.plugin = plugin;
    }

    public java.lang.String name() {
        return "remove";
    }

    public boolean execute(org.bukkit.command.CommandSender sender, java.lang.String[] args) {
        if (args.length >= 2) {
            org.bukkit.entity.Player t = org.bukkit.Bukkit.getPlayerExact(args[1]);
            if (t == null) {
                plugin.msg.error(sender, "Игрок не найден: " + args[1]);
                return true;
            }
        }
        plugin.msg.warn(sender, "Использование: /leper remove <player>");
        return true;
    }

    public java.util.List tab(org.bukkit.command.CommandSender sender, java.lang.String[] args) {
        if (args.length != 2) {
            return java.util.List.of();
        }
        java.util.List out = new java.util.ArrayList();
        org.bukkit.Bukkit.getOnlinePlayers().forEach(p -> lambda_tab_0(out, p));
        return out;
    }

    private static void lambda_tab_0(java.util.List out, org.bukkit.entity.Player p) {
        out.add(p.getName());
    }

}
