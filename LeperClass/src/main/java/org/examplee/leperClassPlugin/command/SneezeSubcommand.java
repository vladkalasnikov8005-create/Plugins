package org.examplee.leperClassPlugin.command;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.persistence.PersistentDataType;
import org.examplee.leperClassPlugin.LeperClassPlugin;
import org.examplee.leperClassPlugin.util.Compat;

public final class SneezeSubcommand implements org.examplee.leperClassPlugin.command.Subcommand {
    private final org.examplee.leperClassPlugin.LeperClassPlugin plugin;
    private final java.util.Map cooldown;

    public SneezeSubcommand(org.examplee.leperClassPlugin.LeperClassPlugin plugin) {
        super();
        this.cooldown = new java.util.concurrent.ConcurrentHashMap();
        this.plugin = plugin;
    }

    public java.lang.String name() {
        return "sneeze";
    }

    public boolean execute(org.bukkit.command.CommandSender sender, java.lang.String[] args) {
        if (sender.hasPermission("leper.sneeze")) {
            if (args.length < 2) {
                if (!((sender instanceof org.bukkit.entity.Player))) {
                    plugin.msg.warn(sender, "Использование: /leper sneeze <player>");
                    return true;
                }
                org.bukkit.entity.Player p = (org.bukkit.entity.Player) sender;
                org.bukkit.entity.Player source = p;
                plugin.msg.warn(sender, "Использование: /leper sneeze <player>");
                return true;
            }
            source = org.bukkit.Bukkit.getPlayerExact(args[1]);
            if (source == null) {
                plugin.msg.error(sender, "Игрок не найден: " + args[1]);
                return true;
            }
        }
        plugin.msg.error(sender, "Нет прав: leper.sneeze");
        return true;
    }

    public java.util.List tab(org.bukkit.command.CommandSender sender, java.lang.String[] args) {
        if (args.length != 2) {
            return java.util.List.of();
        }
        java.util.List out = new java.util.ArrayList();
        org.bukkit.Bukkit.getOnlinePlayers().forEach(p -> lambda$tab$0(out, p));
        return out;
    }

    private static void lambda$tab$0(java.util.List out, org.bukkit.entity.Player p) {
        out.add(p.getName());
    }

}
