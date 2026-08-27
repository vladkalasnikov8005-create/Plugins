package com.example.petmonsters.manager;

import com.example.petmonsters.PetMonstersPlugin;
import com.example.petmonsters.config.PetsConfig;
import com.example.petmonsters.model.Pet;
import com.example.petmonsters.model.PetMode;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Tracks, controls and persists tamed pets.
 */
public class PetManager {

    private final PetMonstersPlugin plugin;
    private final PetsConfig config;

    private final NamespacedKey OWNER_KEY;
    private final NamespacedKey PET_KEY;

    private final Map<UUID, Pet> pets = new HashMap<>();           // owner uuid -> pet
    private final Map<UUID, UUID> petEntityUuids = new HashMap<>();// owner uuid -> mob uuid
    private final Map<UUID, Long> tameCooldown = new HashMap<>();  // owner uuid -> timestamp

    private final File dataFile;
    private BukkitTask behaviorTask;

    public PetManager(PetMonstersPlugin plugin, PetsConfig config) {
        this.plugin = plugin;
        this.config = config;
        this.OWNER_KEY = new NamespacedKey(plugin, "pet_owner");
        this.PET_KEY = new NamespacedKey(plugin, "is_pet");
        this.dataFile = new File(plugin.getDataFolder(), "pets.yml");
        load();
        startBehaviorTask();
    }

    // ---------------------------------------------------------------
    // Persistence
    // ---------------------------------------------------------------

    private void save() {
        YamlConfiguration yml = new YamlConfiguration();
        for (Map.Entry<UUID, Pet> e : pets.entrySet()) {
            String path = "pets." + e.getKey();
            Pet pet = e.getValue();
            yml.set(path + ".entity-uuid", petEntityUuids.get(e.getKey()) != null
                    ? petEntityUuids.get(e.getKey()).toString() : null);
            yml.set(path + ".name", pet.getPetName());
            yml.set(path + ".mode", pet.getMode().name());
            yml.set(path + ".type", pet.getType() != null ? pet.getType().name() : null);
            yml.set(path + ".base-scale", pet.getBaseScale());
            yml.set(path + ".scale-multiplier", pet.getScaleMultiplier());
        }
        try {
            yml.save(dataFile);
        } catch (Exception ex) {
            plugin.getLogger().warning("Could not save pets.yml: " + ex.getMessage());
        }
    }

    private void load() {
        if (!dataFile.exists()) {
            return;
        }
        YamlConfiguration yml = YamlConfiguration.loadConfiguration(dataFile);
        var section = yml.getConfigurationSection("pets");
        if (section == null) {
            return;
        }
        for (String ownerStr : section.getKeys(false)) {
            UUID owner;
            try {
                owner = UUID.fromString(ownerStr);
            } catch (Exception ex) {
                continue;
            }
            String path = "pets." + ownerStr;
            Pet pet = new Pet(owner);
            String name = yml.getString(path + ".name");
            if (name != null) {
                pet.setPetName(name);
            }
            String mode = yml.getString(path + ".mode");
            if (mode != null) {
                try {
                    pet.setMode(PetMode.valueOf(mode));
                } catch (Exception ignored) {
                }
            }
            String typeStr = yml.getString(path + ".type");
            if (typeStr != null) {
                try {
                    pet.setType(EntityType.valueOf(typeStr));
                } catch (Exception ignored) {
                }
            }
            pet.setBaseScale(yml.getDouble(path + ".base-scale", 1.0));
            pet.setScaleMultiplier(yml.getDouble(path + ".scale-multiplier", 1.0));

            String uuidStr = yml.getString(path + ".entity-uuid");
            if (uuidStr != null) {
                try {
                    petEntityUuids.put(owner, UUID.fromString(uuidStr));
                } catch (Exception ignored) {
                }
            }
            pets.put(owner, pet);
        }
        plugin.getLogger().info("Loaded " + pets.size() + " saved pet(s).");
    }

    public void saveAll() {
        save();
    }

    private void startBehaviorTask() {
        behaviorTask = new BukkitRunnable() {
            @Override
            public void run() {
                updateBehavior();
            }
        }.runTaskTimer(plugin, 0L, 5L);
    }

    public void shutdown() {
        if (behaviorTask != null) {
            behaviorTask.cancel();
        }
        save();
    }

    // ---------------------------------------------------------------
    // Taming
    // ---------------------------------------------------------------

    public boolean tryTame(Player player, LivingEntity target) {
        UUID owner = player.getUniqueId();

        if (pets.containsKey(owner)) {
            player.sendMessage(color(config.getMessage("already-have-pet", "&cYou can only tame one mob at a time.")));
            return false;
        }

        long now = System.currentTimeMillis();
        long cooldown = tameCooldown.getOrDefault(owner, 0L);
        long delayMs = config.getDelaySeconds() * 1000L;
        if (now - cooldown < delayMs) {
            long wait = (delayMs - (now - cooldown)) / 1000L + 1;
            player.sendMessage(color(config.getMessage("cooldown", "&cWait {time}s.").replace("{time}", String.valueOf(wait))));
            return false;
        }

        UUID mobOwner = getPetOwner(target);
        if (mobOwner != null) {
            Player other = Bukkit.getPlayer(mobOwner);
            String ownerName = other != null ? other.getName() : mobOwner.toString();
            player.sendMessage(color(config.getMessage("tamed-by-other", "&cAlready tamed by {player}.")
                    .replace("{player}", ownerName)));
            return false;
        }

        tameCooldown.put(owner, now);
        doTame(player, target);
        return true;
    }

    private void doTame(Player player, LivingEntity target) {
        UUID owner = player.getUniqueId();

        target.getPersistentDataContainer().set(OWNER_KEY, PersistentDataType.STRING, owner.toString());
        target.getPersistentDataContainer().set(PET_KEY, PersistentDataType.BYTE, (byte) 1);
        target.setPersistent(true);
        target.setInvulnerable(true);

        Pet pet = new Pet(owner);
        pet.setType(target.getType());
        pet.setBaseScale(1.0);
        pet.setScaleMultiplier(1.0);

        String display = target.getCustomName() != null ? target.getCustomName() : target.getName();
        pet.setPetName(display);

        pets.put(owner, pet);
        petEntityUuids.put(owner, target.getUniqueId());

        if (config.isTameEffects()) {
            target.getWorld().spawnParticle(Particle.HEART, target.getLocation().add(0, target.getHeight(), 0),
                    10, 0.4, 0.4, 0.4, 0.1);
            target.getWorld().playSound(target.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);
        }

        player.sendMessage(color(config.getMessage("tamed", "&aYou tamed {pet}!")
                .replace("{pet}", display)));

        if (config.isAutoRenameGui()) {
            plugin.getRenameGui().open(player, target);
        } else {
            player.sendMessage(color("&aRun &e/petscale <multiplier> &ato resize your pet, &e/petname <name> &ato name it."));
        }

        // autosave shortly after taming
        new BukkitRunnable() {
            @Override
            public void run() {
                save();
            }
        }.runTaskLater(plugin, 20L);
    }

    // ---------------------------------------------------------------
    // Pet lookup
    // ---------------------------------------------------------------

    public Pet getPet(UUID owner) {
        return pets.get(owner);
    }

    public LivingEntity getPetEntity(UUID owner) {
        UUID id = petEntityUuids.get(owner);
        if (id == null) {
            return null;
        }
        Entity e = Bukkit.getEntity(id);
        return (e instanceof LivingEntity le && !le.isDead()) ? le : null;
    }

    public boolean isPet(Entity entity) {
        return entity.getPersistentDataContainer().has(PET_KEY, PersistentDataType.BYTE);
    }

    public UUID getPetOwner(Entity entity) {
        String s = entity.getPersistentDataContainer().get(OWNER_KEY, PersistentDataType.STRING);
        if (s == null) {
            return null;
        }
        try {
            return UUID.fromString(s);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    // ---------------------------------------------------------------
    // Commands
    // ---------------------------------------------------------------

    public boolean toggleMode(Player player, PetMode mode) {
        Pet pet = getPet(player.getUniqueId());
        if (pet == null) {
            player.sendMessage(color(config.getMessage("no-pet", "&cYou don't have a pet.")));
            return false;
        }
        pet.setMode(mode);
        String display = pet.getPetName() != null ? pet.getPetName() : "Your pet";
        player.sendMessage(color(config.getMessage("mode-changed", "&a{pet} is now {mode}.")
                .replace("{pet}", display)
                .replace("{mode}", mode.name().toLowerCase())));
        save();
        return true;
    }

    public boolean rename(Player player, String name) {
        Pet pet = getPet(player.getUniqueId());
        if (pet == null) {
            player.sendMessage(color(config.getMessage("no-pet", "&cYou don't have a pet.")));
            return false;
        }
        pet.setPetName(name);
        LivingEntity entity = getPetEntity(player.getUniqueId());
        if (entity != null) {
            entity.setCustomName(name);
            entity.setCustomNameVisible(true);
        }
        player.sendMessage(color(config.getMessage("name-changed", "&aName changed to {pet}.")
                .replace("{pet}", name)));
        save();
        return true;
    }

    public boolean setScale(Player player, double multiplier) {
        if (multiplier <= 0 || multiplier > 10) {
            player.sendMessage(color(config.getMessage("scale-usage", "&cUsage: /petscale <1.0-10>")));
            return false;
        }
        Pet pet = getPet(player.getUniqueId());
        if (pet == null) {
            player.sendMessage(color(config.getMessage("no-pet", "&cYou don't have a pet.")));
            return false;
        }
        pet.setScaleMultiplier(multiplier);
        LivingEntity entity = getPetEntity(player.getUniqueId());
        if (entity != null) {
            applyScale(entity, pet);
        }
        String display = pet.getPetName() != null ? pet.getPetName() : "Your pet";
        player.sendMessage(color(config.getMessage("scale-changed", "&a{pet} rescaled to {scale}x.")
                .replace("{pet}", display)
                .replace("{scale}", String.valueOf(multiplier))));
        save();
        return true;
    }

    private void applyScale(LivingEntity entity, Pet pet) {
        var attr = entity.getAttribute(org.bukkit.attribute.Attribute.SCALE);
        if (attr != null) {
            attr.setBaseValue(pet.getScaleMultiplier());
        }
    }

    public boolean summon(Player player) {
        Pet pet = getPet(player.getUniqueId());
        if (pet == null) {
            player.sendMessage(color(config.getMessage("no-pet", "&cYou don't have a pet.")));
            return false;
        }
        LivingEntity entity = getPetEntity(player.getUniqueId());
        String display = pet.getPetName() != null ? pet.getPetName() : "Your pet";

        if (entity == null) {
            // Pet is missing -> try to recreate it from saved data
            entity = respawnPet(pet, player);
            if (entity == null) {
                player.sendMessage(color(config.getMessage("pet-not-found", "&cYour pet is missing.")));
                return false;
            }
            player.sendMessage(color(config.getMessage("pet-respawned", "&a{pet} has been recreated.")
                    .replace("{pet}", display)));
        } else {
            entity.teleport(player.getLocation().add(0, 1, 0));
            if (config.isSummonEffects()) {
                player.getWorld().spawnParticle(Particle.PORTAL, entity.getLocation().add(0, 1, 0), 40, 0.6, 0.6, 0.6, 0.5);
                player.getWorld().playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 1.0f);
            }
            player.sendMessage(color(config.getMessage("pet-summoned", "&a{pet} summoned.").replace("{pet}", display)));
        }
        return true;
    }

    private LivingEntity respawnPet(Pet pet, Player player) {
        if (pet.getType() == null) {
            return null;
        }
        Entity created = player.getWorld().spawnEntity(player.getLocation().add(0, 1, 0), pet.getType());
        if (!(created instanceof LivingEntity le)) {
            return null;
        }
        le.getPersistentDataContainer().set(OWNER_KEY, PersistentDataType.STRING, pet.getOwnerId().toString());
        le.getPersistentDataContainer().set(PET_KEY, PersistentDataType.BYTE, (byte) 1);
        le.setPersistent(true);
        le.setInvulnerable(true);
        if (pet.getPetName() != null) {
            le.setCustomName(pet.getPetName());
            le.setCustomNameVisible(true);
        }
        applyScale(le, pet);
        pets.put(pet.getOwnerId(), pet);
        petEntityUuids.put(pet.getOwnerId(), le.getUniqueId());
        save();
        return le;
    }

    public boolean free(Player player) {
        Pet pet = getPet(player.getUniqueId());
        if (pet == null) {
            player.sendMessage(color(config.getMessage("no-pet", "&cYou don't have a pet.")));
            return false;
        }
        LivingEntity entity = getPetEntity(player.getUniqueId());
        String display = pet.getPetName() != null ? pet.getPetName()
                : (entity != null ? entity.getName() : "Your pet");
        if (entity != null) {
            entity.getPersistentDataContainer().remove(OWNER_KEY);
            entity.getPersistentDataContainer().remove(PET_KEY);
            entity.setInvulnerable(false);
            entity.setPersistent(false);
            // reset the scale attribute back to normal
            var attr = entity.getAttribute(org.bukkit.attribute.Attribute.SCALE);
            if (attr != null) {
                attr.setBaseValue(1.0);
            }
        }
        pets.remove(player.getUniqueId());
        petEntityUuids.remove(player.getUniqueId());
        player.sendMessage(color(config.getMessage("pet-freed", "&cReleased {pet}.").replace("{pet}", display)));
        save();
        return true;
    }

    // ---------------------------------------------------------------
    // Behavior
    // ---------------------------------------------------------------

    private void updateBehavior() {
        pets.forEach((owner, pet) -> {
            Player player = Bukkit.getPlayer(owner);
            LivingEntity entity = getPetEntity(owner);
            if (player == null || entity == null) {
                return;
            }
            switch (pet.getMode()) {
                case IDLE -> idle(entity);
                case FOLLOWING -> follow(player, entity);
                case DEFENSE -> defense(player, entity);
            }
        });
    }

    private void idle(LivingEntity entity) {
        entity.setAI(false);
        entity.setVelocity(entity.getVelocity().multiply(0));
    }

    private void follow(Player player, LivingEntity entity) {
        entity.setAI(true);
        double dist = entity.getLocation().distance(player.getLocation());
        if (dist > 3) {
            if (entity instanceof org.bukkit.entity.Mob m) {
                m.getPathfinder().moveTo(player.getLocation(), 0.35);
            } else if (dist > 24) {
                entity.teleport(player.getLocation().add(0, 1, 0));
            }
        }
    }

    private void defense(Player player, LivingEntity entity) {
        entity.setAI(true);

        double dist = entity.getLocation().distance(player.getLocation());
        if (dist > 5) {
            if (entity instanceof org.bukkit.entity.Mob m) {
                m.getPathfinder().moveTo(player.getLocation(), 0.35);
            } else if (dist > 24) {
                entity.teleport(player.getLocation().add(0, 1, 0));
            }
        }

        LivingEntity attacker = getPlayerAttacker(player);
        if (attacker != null && entity instanceof org.bukkit.entity.Mob m) {
            m.setTarget(attacker);
            m.getPathfinder().moveTo(attacker.getLocation(), 0.5);
        }
    }

    private LivingEntity getPlayerAttacker(Player player) {
        var damager = player.getLastDamageCause();
        if (damager != null && damager.getEntity() instanceof LivingEntity attacker && attacker != player) {
            return attacker;
        }
        return null;
    }

    private String color(String s) {
        return ChatColor.translateAlternateColorCodes('&', s);
    }
}
