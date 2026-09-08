package org.examplee.palePlugin.listeners;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.PotionSplashEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.examplee.palePlugin.PalePlugin;

public final class ItemUseListener implements Listener {
   private final PalePlugin plugin;
   private final Map<UUID, Long> lastSaltUse = new HashMap<>();
   private final Map<UUID, Long> lastWandUse = new HashMap<>();

   public ItemUseListener(PalePlugin plugin) {
      this.plugin = plugin;
   }

   @EventHandler
   public void onQuit(PlayerQuitEvent e) {
      this.lastSaltUse.remove(e.getPlayer().getUniqueId());
      this.lastWandUse.remove(e.getPlayer().getUniqueId());
   }

   @EventHandler
   public void onSaltUse(PlayerInteractEvent e) {
      if (e.getHand() == EquipmentSlot.HAND) {
         ItemStack item = e.getItem();
         if (this.plugin.items.isSalt(item)) {
            long now = System.currentTimeMillis();
            long last = this.lastSaltUse.getOrDefault(e.getPlayer().getUniqueId(), 0L);
            if (now - last < this.plugin.cfg.saltCooldownMs) {
               e.setCancelled(true);
            } else {
               this.lastSaltUse.put(e.getPlayer().getUniqueId(), now);
               Location center = e.getClickedBlock() != null ? e.getClickedBlock().getLocation().add(0.5, 0.5, 0.5) : e.getPlayer().getLocation();
               int cleaned = this.plugin.engine.cleanse(center, this.plugin.cfg.saltRadius);
               if (cleaned > 0) {
                  this.plugin.spread.addCleansed(cleaned);
               }

               this.consumeOne(e.getPlayer(), item);
               e.setCancelled(true);
            }
         }
      }
   }

   /** Зарядка великого оберега святой водой: ПКМ по блоку оберега. */
   @EventHandler(priority = org.bukkit.event.EventPriority.LOWEST)
   public void onGreaterWardCharge(PlayerInteractEvent e) {
      if (e.getAction() == org.bukkit.event.block.Action.RIGHT_CLICK_BLOCK && e.getHand() == EquipmentSlot.HAND) {
         org.bukkit.block.Block b = e.getClickedBlock();
         if (b != null) {
            org.examplee.palePlugin.store.GreaterWardStore gw = this.plugin.engine.greaterWards(b.getWorld());
            org.examplee.palePlugin.store.GreaterWardStore.Entry entry = gw.get(b.getX(), b.getY(), b.getZ());
            if (entry != null) {
               ItemStack item = e.getItem();
               if (item != null && this.plugin.items.isHolyWater(item)) {
                  e.setCancelled(true);
                  int max = this.plugin.cfg.greaterWardChargeMaxSec;
                  if (entry.charge >= max) {
                     e.getPlayer().sendMessage(org.examplee.palePlugin.util.Msg.g("Оберег заряжен полностью (" + max / 60 + " мин.)"));
                  } else {
                     int newCharge = gw.addCharge(b.getX(), b.getY(), b.getZ(), this.plugin.cfg.greaterWardChargePerBottleSec, max);
                     this.consumeOne(e.getPlayer(), item);
                     b.getWorld().playSound(b.getLocation(), org.bukkit.Sound.BLOCK_BREWING_STAND_BREW, 1.0F, 1.5F);
                     b.getWorld()
                        .spawnParticle(org.bukkit.Particle.END_ROD, b.getLocation().add(0.5, 1.0, 0.5), 25, 0.4, 0.6, 0.4, 0.02);
                     this.plugin.greaterWardsStorage.saveAsync(this.plugin.engine);
                     e.getPlayer()
                        .sendMessage(
                           org.examplee.palePlugin.util.Msg.g("Оберег заряжен: " + newCharge / 60 + " мин. " + newCharge % 60 + " сек.")
                        );
                  }
               } else if (item == null || item.getType() == org.bukkit.Material.AIR) {
                  e.getPlayer()
                     .sendMessage(
                        org.examplee.palePlugin.util.Msg.g(
                           entry.charge > 0
                              ? "Заряд оберега: " + entry.charge / 60 + " мин. " + entry.charge % 60 + " сек."
                              : "Оберег разряжен. Зарядите святой водой (ПКМ)."
                        )
                     );
               }
            }
         }
      }
   }

   @EventHandler
   public void onHolyWaterSplash(PotionSplashEvent e) {
      ItemStack item = e.getPotion().getItem();
      if (this.plugin.items.isHolyWater(item)) {
         int cleaned = this.plugin.engine.cleanse(e.getPotion().getLocation(), this.plugin.cfg.holyWaterRadius);
         if (cleaned > 0) {
            this.plugin.spread.addCleansed(cleaned);
         }

         // Святая вода — единственное средство против ТЬМЫ
         if (this.plugin.darkness != null) {
            int darkCleaned = this.plugin.darkness.cleanse(e.getPotion().getLocation(), this.plugin.cfg.holyWaterRadius);
            if (darkCleaned > 0 && e.getPotion().getShooter() instanceof org.bukkit.entity.Player p) {
               p.sendMessage(org.examplee.palePlugin.util.Msg.g("Святая вода выжгла тьму: " + darkCleaned + " бл."));
            }
         }
      }
   }

   @EventHandler
   public void onPurifierFlintUse(PlayerInteractEvent e) {
      if (e.getHand() == EquipmentSlot.HAND) {
         if (e.getClickedBlock() != null) {
            ItemStack item = e.getItem();
            if (this.plugin.items.isPurifierFlint(item)) {
               Block clicked = e.getClickedBlock();
               if (!this.plugin.engine.infectedTypes().contains(clicked.getType()) && !this.plugin.engine.hasInfectedNear(clicked)) {
                  e.setCancelled(true);
               } else {
                  int cleaned = this.plugin.engine.cleanse(clicked.getLocation().add(0.5, 0.5, 0.5), this.plugin.cfg.purifierFlintRadius);
                  if (cleaned > 0) {
                     this.plugin.spread.addCleansed(cleaned);
                  }

                  int usesLeft = this.plugin.items.getPurifierFlintUses(item) - 1;
                  if (usesLeft <= 0) {
                     e.getPlayer().playSound(e.getPlayer().getLocation(), Sound.ENTITY_ITEM_BREAK, 1.0F, 1.0F);
                     e.getPlayer().getInventory().setItemInMainHand(new ItemStack(Material.AIR));
                  } else {
                     this.plugin.items.setPurifierFlintUses(item, usesLeft);
                     e.getPlayer().playSound(e.getPlayer().getLocation(), Sound.BLOCK_FIRE_EXTINGUISH, 0.7F, 1.2F);
                  }

                  e.setCancelled(true);
               }
            }
         }
      }
   }

   @EventHandler
   public void onMapUse(PlayerInteractEvent e) {
      if (e.getHand() == EquipmentSlot.HAND) {
         ItemStack item = e.getItem();
         if (this.plugin.items.isInfectionMap(item)) {
            int r = this.plugin.items.getMapRadius(item);
            r = Math.max(1, Math.min(this.plugin.cfg.mapMaxRadiusChunks, r));
            this.plugin.engine.sendMap(e.getPlayer(), r);
            e.setCancelled(true);
         } else if (this.plugin.items.isDarkMap(item)) {
            int r = this.plugin.items.getMapRadius(item);
            r = Math.max(1, Math.min(this.plugin.cfg.mapMaxRadiusChunks, r));
            this.plugin.darkness.sendDarkMap(e.getPlayer(), r);
            e.setCancelled(true);
         }
      }
   }

   @EventHandler
   public void onDarkPurgeWandUse(PlayerInteractEvent e) {
      if (e.getHand() != EquipmentSlot.HAND) return;
      Action a = e.getAction();
      if (a != Action.RIGHT_CLICK_AIR && a != Action.RIGHT_CLICK_BLOCK) return;
      ItemStack item = e.getItem();
      if (!this.plugin.items.isDarkPurgeWand(item)) return;

      e.setCancelled(true);
      var p = e.getPlayer();
      if (!p.hasPermission("pale.admin")) {
         p.sendMessage(org.bukkit.ChatColor.RED + "Жезл Света слушается только админов (pale.admin).");
         return;
      }

      int r = this.plugin.items.getDarkPurgeWandRadius(item);

      if (p.isSneaking()) {
         // Shift+ПКМ: ВЫЖЕЧЬ тьму в радиусе r чанков
         int ccx = p.getLocation().getBlockX() >> 4;
         int ccz = p.getLocation().getBlockZ() >> 4;
         int cleaned = this.plugin.darkness.cleanseChunks(p.getWorld(), ccx, ccz, r);
         if (cleaned > 0) {
            p.sendMessage(org.bukkit.ChatColor.GOLD + "✟ " + org.bukkit.ChatColor.YELLOW + "Свет выжег тьму: "
                  + org.bukkit.ChatColor.WHITE + cleaned + org.bukkit.ChatColor.YELLOW + " бл. в радиусе " + r + " чанков.");
            try {
               p.getWorld().playSound(p.getLocation(), Sound.BLOCK_BEACON_ACTIVATE, 1.0F, 1.4F);
               p.getWorld().spawnParticle(org.bukkit.Particle.END_ROD, p.getLocation().add(0.0, 1.0, 0.0), 60, 3.0, 2.0, 3.0, 0.05);
            } catch (Throwable ignored) {}
         } else {
            p.sendMessage(org.bukkit.ChatColor.GRAY + "В радиусе " + r + " чанков тьмы не найдено.");
            try {
               p.playSound(p.getLocation(), Sound.BLOCK_BEACON_DEACTIVATE, 0.6F, 1.8F);
            } catch (Throwable ignored) {}
         }
      } else {
         // ПКМ: радиус x2 по кругу 1 -> 2 -> 4 -> ... -> 64 -> 1
         int next = r >= 64 ? 1 : Math.min(64, r * 2);
         this.plugin.items.setDarkPurgeWandRadius(item, next);
         p.sendMessage(org.bukkit.ChatColor.GRAY + "Радиус Жезла Света: " + org.bukkit.ChatColor.WHITE + next
               + org.bukkit.ChatColor.GRAY + " чанков (" + (next * 2 + 1) + "x" + (next * 2 + 1) + " чанков зона).");
         try {
            p.playSound(p.getLocation(), Sound.UI_BUTTON_CLICK, 0.5F, 1.0F + next / 64.0F);
         } catch (Throwable ignored) {}
      }
   }

   @EventHandler
   public void onWandUse(PlayerInteractEvent e) {
      if (e.getHand() == EquipmentSlot.HAND) {
         Action a = e.getAction();
         if (a == Action.RIGHT_CLICK_AIR || a == Action.RIGHT_CLICK_BLOCK) {
            ItemStack item = e.getItem();
            if (this.plugin.items.isInfectWand(item)) {
               long now = System.currentTimeMillis();
               long last = this.lastWandUse.getOrDefault(e.getPlayer().getUniqueId(), 0L);
               if (now - last < this.plugin.cfg.infectWandCooldownMs) {
                  e.setCancelled(true);
               } else {
                  this.lastWandUse.put(e.getPlayer().getUniqueId(), now);
                  Block centerBlock = e.getClickedBlock();
                  if (centerBlock == null) {
                     try {
                        centerBlock = e.getPlayer().getTargetBlockExact(40);
                     } catch (Throwable var12) {
                     }
                  }

                  Location center = centerBlock != null ? centerBlock.getLocation().add(0.5, 0.5, 0.5) : e.getPlayer().getLocation();
                  int infected = this.plugin.engine.infectAreaWand(center);
                  if (infected > 0) {
                     this.plugin.spread.addInfected(infected);
                     e.getPlayer().playSound(e.getPlayer().getLocation(), Sound.BLOCK_SCULK_CATALYST_BLOOM, 0.8F, 1.15F);
                  } else {
                     e.getPlayer().playSound(e.getPlayer().getLocation(), Sound.BLOCK_AMETHYST_BLOCK_HIT, 0.5F, 0.6F);
                  }

                  int usesLeft = this.plugin.items.getInfectWandUses(item) - 1;
                  if (usesLeft <= 0) {
                     e.getPlayer().playSound(e.getPlayer().getLocation(), Sound.ENTITY_ITEM_BREAK, 1.0F, 1.0F);
                     e.getPlayer().getInventory().setItemInMainHand(new ItemStack(Material.AIR));
                  } else {
                     this.plugin.items.setInfectWandUses(item, usesLeft);
                  }

                  e.setCancelled(true);
               }
            }
         }
      }
   }

   private void consumeOne(Player p, ItemStack it) {
      if (p.getGameMode() != GameMode.CREATIVE) {
         int amt = it.getAmount() - 1;
         if (amt <= 0) {
            p.getInventory().setItemInMainHand(new ItemStack(Material.AIR));
         } else {
            it.setAmount(amt);
         }
      }
   }
}
