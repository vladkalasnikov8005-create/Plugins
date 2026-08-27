package com.example.petmonsters.command;

import com.example.petmonsters.PetMonstersPlugin;
import com.example.petmonsters.model.PetMode;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class PetCommands implements CommandExecutor, TabCompleter {

    private final PetMonstersPlugin plugin;

    public PetCommands(PetMonstersPlugin plugin) {
        this.plugin = plugin;
    }

    public void register() {
        plugin.getCommand("petsummon").setExecutor(this);
        plugin.getCommand("petfree").setExecutor(this);
        plugin.getCommand("petmode").setExecutor(this);
        plugin.getCommand("petname").setExecutor(this);
        plugin.getCommand("petscale").setExecutor(this);
        plugin.getCommand("petsummon").setTabCompleter(this);
        plugin.getCommand("petmode").setTabCompleter(this);
        plugin.getCommand("petscale").setTabCompleter(this);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                             @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Only players can use pet commands.");
            return true;
        }

        switch (command.getName()) {
            case "petsummon" -> plugin.getPetManager().summon(player);
            case "petfree" -> plugin.getPetManager().free(player);
            case "petmode" -> handleMode(player, args);
            case "petname" -> handleName(player, args);
            case "petscale" -> handleScale(player, args);
            default -> {
                return false;
            }
        }
        return true;
    }

    private void handleMode(Player player, String[] args) {
        if (args.length < 1) {
            player.sendMessage("§cUsage: /petmode <idle|following|defense>");
            return;
        }
        PetMode mode;
        try {
            mode = PetMode.valueOf(args[0].toUpperCase());
        } catch (IllegalArgumentException ex) {
            player.sendMessage("§cUsage: /petmode <idle|following|defense>");
            return;
        }
        plugin.getPetManager().toggleMode(player, mode);
    }

    private void handleName(Player player, String[] args) {
        if (args.length < 1) {
            player.sendMessage("§cUsage: /petname <name>");
            return;
        }
        plugin.getPetManager().rename(player, String.join(" ", args));
    }

    private void handleScale(Player player, String[] args) {
        if (args.length < 1) {
            player.sendMessage("§cUsage: /petscale <multiplier>  (0.1 - 10, 1.0 = original)");
            return;
        }
        double scale;
        try {
            scale = Double.parseDouble(args[0]);
        } catch (NumberFormatException ex) {
            player.sendMessage("§cUsage: /petscale <multiplier>");
            return;
        }
        plugin.getPetManager().setScale(player, scale);
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command,
                                      @NotNull String alias, @NotNull String[] args) {
        if (command.getName().equals("petmode") && args.length == 1) {
            List<String> list = new ArrayList<>();
            for (PetMode m : PetMode.values()) {
                if (m.name().toLowerCase().startsWith(args[0].toLowerCase())) {
                    list.add(m.name().toLowerCase());
                }
            }
            return list;
        }
        return List.of();
    }
}
