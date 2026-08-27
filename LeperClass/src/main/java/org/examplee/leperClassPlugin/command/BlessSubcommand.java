package org.examplee.leperClassPlugin.command;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.examplee.leperClassPlugin.LeperClassPlugin;

import java.util.ArrayList;
import java.util.List;

public final class BlessSubcommand implements Subcommand {
    private final LeperClassPlugin plugin;
    private final boolean bless;

    public BlessSubcommand(LeperClassPlugin plugin, boolean bless) {
        this.plugin = plugin;
        this.bless = bless;
    }

    @Override
    public String name() {
        return bless ? "bless" : "unbless";
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if (args.length < 2) {
            plugin.msg.warn(sender, "Использование: /leper " + name() + " <player>");
            return true;
        }
        Player t = Bukkit.getPlayerExact(args[1]);
        if (t == null) {
            plugin.msg.error(sender, "Игрок не найден: " + args[1]);
            return true;
        }
        if (bless) {
            plugin.data.setDangerBlessed(t, true);
            plugin.msg.ok(sender, "Выдано благословение: " + t.getName());
        } else {
            plugin.data.setDangerBlessed(t, false);
            plugin.msg.ok(sender, "Снято благословение: " + t.getName());
        }
        return true;
    }

    @Override
    public List<String> tab(CommandSender sender, String[] args) {
        if (args.length != 2) {
            return List.of();
        }
        List<String> out = new ArrayList<>();
        for (Player p : Bukkit.getOnlinePlayers()) {
            out.add(p.getName());
        }
        return out;
    }
}
