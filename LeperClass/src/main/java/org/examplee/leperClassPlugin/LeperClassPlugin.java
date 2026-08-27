package org.examplee.leperClassPlugin;

import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public final class LeperClassPlugin extends org.bukkit.plugin.java.JavaPlugin {
    public org.examplee.leperClassPlugin.core.LeperKeys keys;
    public org.examplee.leperClassPlugin.core.EffectRegistry effects;
    public org.examplee.leperClassPlugin.data.LeperData data;
    public org.examplee.leperClassPlugin.core.PluginSettings settings;
    public org.examplee.leperClassPlugin.core.BalanceService balance;
    public org.examplee.leperClassPlugin.core.MovementLock movementLock;
    public org.examplee.leperClassPlugin.core.PaleHook paleHook;
    public org.examplee.leperClassPlugin.items.ItemTags tags;
    public org.examplee.leperClassPlugin.items.ItemFactory items;
    public org.examplee.leperClassPlugin.infection.InfectionManager infection;
    public org.examplee.leperClassPlugin.umbrella.UmbrellaManager umbrella;
    public org.examplee.leperClassPlugin.gui.LeperMenu menu;
    public org.examplee.leperClassPlugin.util.MessageService msg;
    public org.examplee.leperClassPlugin.util.LogService log;
    public org.examplee.leperClassPlugin.core.ItemMigrationService migration;
    private org.examplee.leperClassPlugin.tasks.SunAndInfectionTask sunTask;

    public LeperClassPlugin() {
        super();
    }

    public void onEnable() {
        saveDefaultConfig();
        this.settings = new org.examplee.leperClassPlugin.core.PluginSettings(this);
        this.msg = new org.examplee.leperClassPlugin.util.MessageService();
        this.log = new org.examplee.leperClassPlugin.util.LogService(this);
        this.keys = new org.examplee.leperClassPlugin.core.LeperKeys(this);
        this.effects = new org.examplee.leperClassPlugin.core.EffectRegistry();
        if (effects.POISON == null) {
            getLogger().severe("Missing required effects.");
            org.bukkit.Bukkit.getPluginManager().disablePlugin(this);
            return;
        }
        if (effects.SLOW == null) {
            getLogger().severe("Missing required effects.");
            org.bukkit.Bukkit.getPluginManager().disablePlugin(this);
            return;
        }
        this.data = new org.examplee.leperClassPlugin.data.LeperData(keys);
        this.balance = new org.examplee.leperClassPlugin.core.BalanceService(settings);
        this.movementLock = new org.examplee.leperClassPlugin.core.MovementLock(effects.SLOW);
        this.paleHook = new org.examplee.leperClassPlugin.core.PaleHook();
        paleHook.hook();
        this.tags = new org.examplee.leperClassPlugin.items.ItemTags(keys);
        this.items = new org.examplee.leperClassPlugin.items.ItemFactory(keys);
        this.infection = new org.examplee.leperClassPlugin.infection.InfectionManager(this);
        this.umbrella = new org.examplee.leperClassPlugin.umbrella.UmbrellaManager(this);
        this.migration = new org.examplee.leperClassPlugin.core.ItemMigrationService(this);
        this.menu = new org.examplee.leperClassPlugin.gui.LeperMenu(this);
        org.bukkit.command.PluginCommand cmd = getCommand("leper");
        if (cmd != null) {
            cmd.setExecutor(new org.examplee.leperClassPlugin.command.LeperCommand(this));
            cmd.setTabCompleter(new org.examplee.leperClassPlugin.command.LeperTabCompleter());
        }
        org.bukkit.plugin.PluginManager pm = org.bukkit.Bukkit.getPluginManager();
        pm.registerEvents(new org.examplee.leperClassPlugin.listeners.CombatListener(this), this);
        pm.registerEvents(new org.examplee.leperClassPlugin.listeners.ConsumeListener(this), this);
        pm.registerEvents(new org.examplee.leperClassPlugin.listeners.HungerListener(this), this);
        pm.registerEvents(new org.examplee.leperClassPlugin.listeners.VaccineListener(this), this);
        pm.registerEvents(new org.examplee.leperClassPlugin.listeners.EffectBlockListener(this), this);
        pm.registerEvents(new org.examplee.leperClassPlugin.listeners.JoinQuitDeathListener(this), this);
        pm.registerEvents(new org.examplee.leperClassPlugin.listeners.MobIgnoreListener(this), this);
        pm.registerEvents(new org.examplee.leperClassPlugin.listeners.PlagueStickListener(this), this);
        pm.registerEvents(new org.examplee.leperClassPlugin.listeners.PlagueBombListener(this), this);
        pm.registerEvents(new org.examplee.leperClassPlugin.gui.LeperMenuListener(this), this);
        pm.registerEvents(new org.examplee.leperClassPlugin.listeners.UmbrellaSyncListener(this), this);
        pm.registerEvents(new org.examplee.leperClassPlugin.listeners.InstantBrewingListener(this), this);
        pm.registerEvents(new org.examplee.leperClassPlugin.listeners.LeperBloodListener(this), this);
        pm.registerEvents(new org.examplee.leperClassPlugin.listeners.ContactInfectionListener(this), this);
        pm.registerEvents(new org.examplee.leperClassPlugin.listeners.SneezeListener(this), this);
        pm.registerEvents(new org.examplee.leperClassPlugin.listeners.UndeadPotionInversionListener(this), this);
        this.sunTask = new org.examplee.leperClassPlugin.tasks.SunAndInfectionTask(this);
        sunTask.start();
    }

    public void onDisable() {
        if (sunTask != null) {
            sunTask.stop();
        }
        if (umbrella != null) {
            umbrella.flushAllOnline();
        }
        org.bukkit.Bukkit.getOnlinePlayers().forEach((org.bukkit.entity.Player p0) -> lambda$onDisable$0(p0));
    }

    private void lambda$onDisable$0(org.bukkit.entity.Player p) {
        movementLock.release(p);
    }

}
