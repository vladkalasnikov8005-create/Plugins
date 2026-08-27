package com.example.petmonsters.config;

import com.example.petmonsters.PetMonstersPlugin;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.EntityType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Loads and caches the plugin configuration values.
 */
public class PetsConfig {

    private final PetMonstersPlugin plugin;
    private boolean universalEnabled = false;
    private Material universalMaterial = null;
    private final Map<EntityType, Set<Material>> tamingItems = new HashMap<>();
    private int delaySeconds = 3;
    private boolean autoRenameGui = true;
    private boolean tameEffects = true;
    private boolean summonEffects = true;

    public PetsConfig(PetMonstersPlugin plugin) {
        this.plugin = plugin;
        reload();
    }

    public void reload() {
        plugin.saveDefaultConfig();
        plugin.reloadConfig();
        FileConfiguration cfg = plugin.getConfig();

        universalEnabled = cfg.getBoolean("universal-tame-item.enabled", false);
        String uniMat = cfg.getString("universal-tame-item.material", "AIR");
        universalMaterial = materialOf(uniMat);

        tamingItems.clear();
        Map<String, Object> section = cfg.getConfigurationSection("taming-items") != null
                ? cfg.getConfigurationSection("taming-items").getValues(false)
                : Collections.emptyMap();

        for (Map.Entry<String, Object> entry : section.entrySet()) {
            EntityType type = entityTypeOf(entry.getKey());
            if (type == null) {
                plugin.getLogger().warning("Unknown entity type in config: " + entry.getKey());
                continue;
            }
            Set<Material> materials = new HashSet<>();
            if (entry.getValue() instanceof List<?> list) {
                for (Object o : list) {
                    Material m = materialOf(String.valueOf(o));
                    if (m != null) {
                        materials.add(m);
                    } else {
                        plugin.getLogger().warning("Invalid material '" + o + "' for mob " + entry.getKey());
                    }
                }
            }
            if (!materials.isEmpty()) {
                tamingItems.put(type, materials);
            }
        }

        delaySeconds = Math.max(0, cfg.getInt("delay-seconds", 3));
        autoRenameGui = cfg.getBoolean("auto-rename-gui", true);
        tameEffects = cfg.getBoolean("effects.tame", true);
        summonEffects = cfg.getBoolean("effects.summon", true);
    }

    private Material materialOf(String name) {
        try {
            Material m = Material.matchMaterial(name);
            return m != null && m.isItem() ? m : null;
        } catch (Exception e) {
            return null;
        }
    }

    private EntityType entityTypeOf(String name) {
        try {
            return EntityType.valueOf(name.toUpperCase());
        } catch (Exception e) {
            return null;
        }
    }

    public boolean isUniversalTame() {
        return universalEnabled && universalMaterial != null;
    }

    public Material getUniversalMaterial() {
        return universalMaterial;
    }

    public Set<Material> getTamingItems(EntityType type) {
        Set<Material> set = tamingItems.get(type);
        return set != null ? set : Collections.emptySet();
    }

    public boolean canTame(EntityType type, Material item) {
        if (getTamingItems(type).contains(item)) {
            return true;
        }
        return isUniversalTame() && universalMaterial == item;
    }

    public int getDelaySeconds() {
        return delaySeconds;
    }

    public boolean isAutoRenameGui() {
        return autoRenameGui;
    }

    public boolean isTameEffects() {
        return tameEffects;
    }

    public boolean isSummonEffects() {
        return summonEffects;
    }

    public String getMessage(String key, String def) {
        return plugin.getConfig().getString("messages." + key, def);
    }
}
