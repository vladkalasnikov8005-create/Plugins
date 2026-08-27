package org.examplee.palePlugin.command;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.examplee.palePlugin.PalePlugin;
import org.examplee.palePlugin.util.MathUtil;

public final class PaleSpreadCommand implements org.bukkit.command.CommandExecutor, org.bukkit.command.TabCompleter {
    private final org.examplee.palePlugin.PalePlugin plugin;

    public PaleSpreadCommand(org.examplee.palePlugin.PalePlugin plugin) {
        super();
        this.plugin = plugin;
    }

    private boolean isAdmin(org.bukkit.command.CommandSender s) {
        if ((s instanceof org.bukkit.entity.Player)) {
            if (s.hasPermission("pale.admin")) {
            } else {
            }
        }
        return false;
    }

    public boolean onCommand(org.bukkit.command.CommandSender sender, org.bukkit.command.Command cmd, java.lang.String label, java.lang.String[] args) {
        if (cmd.getName().equalsIgnoreCase("palespread")) {
            if (args.length == 0) {
                sender.sendMessage(java.lang.String.valueOf(org.bukkit.ChatColor.GRAY) + "/palespread on|off|speed <1..5000>|info|map [r]|give <item> <player> [amount] [r/uses]|gui");
                return true;
            }
        }
        return true;
    }

    public java.util.List onTabComplete(org.bukkit.command.CommandSender sender, org.bukkit.command.Command cmd, java.lang.String alias, java.lang.String[] args) {
        if (cmd.getName().equalsIgnoreCase("palespread")) {
            if (args.length != 1) {
                if (args.length == 2) {
                    if (args[0].equalsIgnoreCase("give")) {
                        return filter(args[1], java.util.List.of("salt", "holywater", "ward", "flint", "purge", "map", "wand"));
                    }
                }
            }
            return filter(args[0], java.util.List.of("on", "off", "speed", "info", "map", "give", "gui"));
        }
        return java.util.Collections.emptyList();
    }

    private java.util.List filter(java.lang.String prefix, java.util.List opts) {
        if (prefix == null) {
        } else {
        }
        java.lang.String p = 0;
        java.util.List out = new java.util.ArrayList();
        java.util.Iterator local5 = opts.iterator();
        if (local5.hasNext()) {
            java.lang.String o = (java.lang.String) local5.next();
            if (o.toLowerCase(java.util.Locale.ROOT).startsWith(p)) {
                out.add(o);
            }
            /* continue */
        }
        return out;
    }

}
