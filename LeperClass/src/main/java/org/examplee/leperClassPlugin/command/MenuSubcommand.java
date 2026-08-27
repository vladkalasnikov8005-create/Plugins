package org.examplee.leperClassPlugin.command;

import java.util.ArrayList;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.examplee.leperClassPlugin.LeperClassPlugin;

public final class MenuSubcommand implements org.examplee.leperClassPlugin.command.Subcommand {
    private final org.examplee.leperClassPlugin.LeperClassPlugin plugin;

    public MenuSubcommand(org.examplee.leperClassPlugin.LeperClassPlugin plugin) {
        super();
        this.plugin = plugin;
    }

    public java.lang.String name() {
        return "menu";
    }

    public boolean execute(org.bukkit.command.CommandSender sender, java.lang.String[] args) {
        if (!((sender instanceof org.bukkit.entity.Player))) {
            plugin.msg.error(sender, "Меню можно открыть только игроком.");
            return true;
        }
        org.bukkit.entity.Player admin = (org.bukkit.entity.Player) sender;
        plugin.msg.error(sender, "Меню можно открыть только игроком.");
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
