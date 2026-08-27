package com.example.petmonsters.listener;

import com.example.petmonsters.PetMonstersPlugin;
import com.example.petmonsters.config.PetsConfig;
import com.example.petmonsters.model.PetMode;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.EquipmentSlot;

public class TamingListener implements Listener {

    private final PetMonstersPlugin plugin;
    private final PetsConfig config;

    public TamingListener(PetMonstersPlugin plugin) {
        this.plugin = plugin;
        this.config = plugin.getPetsConfig();
    }

    @EventHandler
    public void onInteract(PlayerInteractEntityEvent e) {
        if (e.getHand() != EquipmentSlot.HAND) {
            return;
        }
        Player player = e.getPlayer();
        Entity target = e.getRightClicked();

        if (!(target instanceof LivingEntity living)) {
            return;
        }

        // Right-click your own pet -> cycle mode
        if (plugin.getPetManager().isPet(target)) {
            var pet = plugin.getPetManager().getPet(player.getUniqueId());
            if (pet != null) {
                e.setCancelled(true);
                PetMode next = switch (pet.getMode()) {
                    case IDLE -> PetMode.FOLLOWING;
                    case FOLLOWING -> PetMode.DEFENSE;
                    case DEFENSE -> PetMode.IDLE;
                };
                plugin.getPetManager().toggleMode(player, next);
            }
            return;
        }

        if (!player.hasPermission("petmonsters.tame")) {
            return;
        }

        var item = player.getInventory().getItemInMainHand();
        if (item.getType().isAir()) {
            return;
        }

        if (config.canTame(target.getType(), item.getType())) {
            e.setCancelled(true);
            plugin.getPetManager().tryTame(player, living);
        }
    }
}
