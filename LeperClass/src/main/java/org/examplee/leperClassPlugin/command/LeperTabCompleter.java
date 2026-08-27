package org.examplee.leperClassPlugin.command;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

public final class LeperTabCompleter implements org.bukkit.command.TabCompleter {
    public LeperTabCompleter() {
        super();
    }

    public java.util.List onTabComplete(org.bukkit.command.CommandSender sender, org.bukkit.command.Command command, java.lang.String alias, java.lang.String[] args) {
        if (command.getName().equalsIgnoreCase("leper")) {
            if (sender.hasPermission("leper.admin")) {
                if (args.length == 1) {
                    java.lang.String[] tmp1 = new java.lang.String[8];
                    tmp1[0] = "add";
                    tmp1[1] = "remove";
                    tmp1[2] = "menu";
                    tmp1[3] = "bless";
                    tmp1[4] = "unbless";
                    tmp1[5] = "sneeze";
                    tmp1[6] = "status";
                    tmp1[7] = "migrateitems";
                    return filter(java.util.Arrays.asList(tmp1), args[0]);
                }
            }
            return java.util.Collections.emptyList();
        }
        return java.util.Collections.emptyList();
    }

    private java.util.List filter(java.util.Collection options, java.lang.String prefix) {
        if (prefix == null) {
        } else {
        }
        java.lang.String p = 0;
        java.util.List out = new java.util.ArrayList();
        java.util.Iterator local5 = options.iterator();
        if (local5.hasNext()) {
            java.lang.String s = (java.lang.String) local5.next();
            if (s.toLowerCase(java.util.Locale.ROOT).startsWith(p)) {
                out.add(s);
            }
            /* continue */
        }
        return out;
    }

}
