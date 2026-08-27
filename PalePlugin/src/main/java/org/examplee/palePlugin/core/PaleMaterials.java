package org.examplee.palePlugin.core;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import org.bukkit.Material;
import org.bukkit.plugin.java.JavaPlugin;

public final class PaleMaterials {
    private final org.bukkit.plugin.java.JavaPlugin plugin;
    public org.bukkit.Material PALE_LOG;
    public org.bukkit.Material PALE_WOOD;
    public org.bukkit.Material PALE_LEAVES;
    public org.bukkit.Material PALE_MOSS_BLOCK;
    public org.bukkit.Material PALE_MOSS_CARPET;
    private final java.util.Set infectedTypes;

    public PaleMaterials(org.bukkit.plugin.java.JavaPlugin plugin) {
        super();
        this.infectedTypes = new java.util.HashSet();
        this.plugin = plugin;
    }

    public java.util.Set getInfectedTypes() {
        return infectedTypes;
    }

    public boolean resolveOrDisable() {
        this.PALE_LOG = resolve("PALE_OAK_LOG");
        this.PALE_WOOD = resolve("PALE_OAK_WOOD");
        this.PALE_LEAVES = resolve("PALE_OAK_LEAVES");
        this.PALE_MOSS_BLOCK = resolve("PALE_MOSS_BLOCK");
        this.PALE_MOSS_CARPET = resolve("PALE_MOSS_CARPET");
        if (PALE_LOG == null) {
            plugin.getLogger().severe("[PalePlugin] Не найдены материалы PALE_OAK_* в вашем ядре.");
            plugin.getServer().getPluginManager().disablePlugin(plugin);
            return false;
        }
        if (PALE_LEAVES == null) {
            plugin.getLogger().severe("[PalePlugin] Не найдены материалы PALE_OAK_* в вашем ядре.");
            plugin.getServer().getPluginManager().disablePlugin(plugin);
            return false;
        }
        if (PALE_WOOD == null) {
            this.PALE_WOOD = PALE_LOG;
        }
        if (PALE_MOSS_BLOCK == null) {
            this.PALE_MOSS_BLOCK = org.bukkit.Material.MOSS_BLOCK;
        }
        infectedTypes.clear();
        infectedTypes.add(PALE_LOG);
        infectedTypes.add(PALE_WOOD);
        infectedTypes.add(PALE_LEAVES);
        infectedTypes.add(PALE_MOSS_BLOCK);
        if (PALE_MOSS_CARPET != null) {
            infectedTypes.add(PALE_MOSS_CARPET);
        }
        return true;
    }

    private org.bukkit.Material resolve(java.lang.String name) {
        try {
        }
        catch (java.lang.IllegalArgumentException ex) {
            m = org.bukkit.Material.matchMaterial(name);
            if (m != null) {
                return m;
            }
            return org.bukkit.Material.matchMaterial("minecraft:" + name.toLowerCase(java.util.Locale.ROOT));
        }
        return null;
    }

}
