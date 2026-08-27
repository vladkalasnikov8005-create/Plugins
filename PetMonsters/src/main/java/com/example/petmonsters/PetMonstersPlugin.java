package com.example.petmonsters;

import com.example.petmonsters.command.PetCommands;
import com.example.petmonsters.config.PetsConfig;
import com.example.petmonsters.gui.RenameGui;
import com.example.petmonsters.listener.PetBehaviorListener;
import com.example.petmonsters.listener.TamingListener;
import com.example.petmonsters.manager.PetManager;
import org.bukkit.plugin.java.JavaPlugin;

public class PetMonstersPlugin extends JavaPlugin {

    private PetsConfig petsConfig;
    private PetManager petManager;
    private RenameGui renameGui;

    @Override
    public void onEnable() {
        petsConfig = new PetsConfig(this);
        petManager = new PetManager(this, petsConfig);
        renameGui = new RenameGui(this);

        getServer().getPluginManager().registerEvents(new TamingListener(this), this);
        getServer().getPluginManager().registerEvents(new PetBehaviorListener(this), this);

        new PetCommands(this).register();

        getLogger().info("PetMonsters enabled. Tame mobs and make them follow you!");
        getLogger().info("Commands: /petsummon /petfree /petmode /petname");
    }

    @Override
    public void onDisable() {
        if (petManager != null) {
            petManager.shutdown();
        }
        getLogger().info("PetMonsters disabled.");
    }

    public PetsConfig getPetsConfig() {
        return petsConfig;
    }

    public PetManager getPetManager() {
        return petManager;
    }

    public RenameGui getRenameGui() {
        return renameGui;
    }
}
