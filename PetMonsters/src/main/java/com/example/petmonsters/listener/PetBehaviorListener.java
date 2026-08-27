package com.example.petmonsters.listener;

import com.example.petmonsters.PetMonstersPlugin;
import com.example.petmonsters.model.PetMode;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class PetBehaviorListener implements Listener {

    private final PetMonstersPlugin plugin;

    public PetBehaviorListener(PetMonstersPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerDamaged(EntityDamageByEntityEvent e) {
        if (!(e.getEntity() instanceof Player victim)) {
            return;
        }
        if (!(e.getDamager() instanceof LivingEntity attacker)) {
            return;
        }
        if (attacker == victim) {
            return;
        }
        var pet = plugin.getPetManager().getPet(victim.getUniqueId());
        if (pet == null || pet.getMode() != PetMode.DEFENSE) {
            return;
        }
        LivingEntity petEntity = plugin.getPetManager().getPetEntity(victim.getUniqueId());
        if (petEntity instanceof Mob mob) {
            mob.setTarget(attacker);
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {
        if (e.getWhoClicked() instanceof Player p) {
            plugin.getRenameGui().handleClick(p, e);
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent e) {
        if (e.getPlayer() instanceof Player p) {
            plugin.getRenameGui().handleClose(p);
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        // Nothing to clean here; pet stays bound to the player.
    }
}
