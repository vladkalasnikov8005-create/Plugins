package org.examplee.leperClassPlugin.util;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

public final class MessageService {
    public MessageService() {
        super();
    }

    public void info(org.bukkit.command.CommandSender s, java.lang.String msg) {
        if (s != null) {
            s.sendMessage(org.examplee.leperClassPlugin.util.TextUtil.ui(java.lang.String.valueOf(org.bukkit.ChatColor.GRAY) + msg));
        }
    }

    public void ok(org.bukkit.command.CommandSender s, java.lang.String msg) {
        if (s != null) {
            s.sendMessage(org.examplee.leperClassPlugin.util.TextUtil.ui(java.lang.String.valueOf(org.bukkit.ChatColor.GREEN) + msg));
        }
    }

    public void warn(org.bukkit.command.CommandSender s, java.lang.String msg) {
        if (s != null) {
            s.sendMessage(org.examplee.leperClassPlugin.util.TextUtil.ui(java.lang.String.valueOf(org.bukkit.ChatColor.YELLOW) + msg));
        }
    }

    public void error(org.bukkit.command.CommandSender s, java.lang.String msg) {
        if (s != null) {
            s.sendMessage(org.examplee.leperClassPlugin.util.TextUtil.ui(java.lang.String.valueOf(org.bukkit.ChatColor.RED) + msg));
        }
    }

}
