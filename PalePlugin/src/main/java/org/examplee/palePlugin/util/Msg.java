package org.examplee.palePlugin.util;

import net.md_5.bungee.api.ChatColor;

public final class Msg {
    private Msg() {
        super();
    }

    private static java.lang.String rgb(java.lang.String hex) {
        try {
        }
        catch (java.lang.Throwable ex) {
            return org.bukkit.ChatColor.GREEN.toString();
        }
        return null;
    }

    public static java.lang.String g(java.lang.String s) {
        return org.examplee.palePlugin.util.Msg.rgb("#39FF14") + s + java.lang.String.valueOf(org.bukkit.ChatColor.RESET);
    }

}
