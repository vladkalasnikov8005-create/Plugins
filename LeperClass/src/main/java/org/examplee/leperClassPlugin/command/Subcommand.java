package org.examplee.leperClassPlugin.command;

import java.util.List;
import org.bukkit.command.CommandSender;

public interface Subcommand {
    public abstract java.lang.String name();

    public abstract boolean execute(org.bukkit.command.CommandSender arg0, java.lang.String[] arg1);

    public java.util.List tab(org.bukkit.command.CommandSender sender, java.lang.String[] args) {
        return java.util.List.of();
    }

}
