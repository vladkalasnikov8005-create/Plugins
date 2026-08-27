package org.examplee.leperClassPlugin.listeners;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.examplee.leperClassPlugin.LeperClassPlugin;
import org.examplee.leperClassPlugin.util.InventoryUtil;

public final class LeperBloodListener implements org.bukkit.event.Listener {
    private final org.examplee.leperClassPlugin.LeperClassPlugin plugin;
    private final java.util.Map knifeCd;

    public LeperBloodListener(org.examplee.leperClassPlugin.LeperClassPlugin plugin) {
        super();
        this.knifeCd = new java.util.HashMap();
        this.plugin = plugin;
    }

    @org.bukkit.event.EventHandler
    public void onKnifeUse(org.bukkit.event.player.PlayerInteractEvent e) {
        org.bukkit.entity.Player p = null;
        org.bukkit.inventory.ItemStack hand = null;
        org.bukkit.event.block.Action a = e.getAction();
        if (a == org.bukkit.event.block.Action.RIGHT_CLICK_AIR) {
            p = e.getPlayer();
            hand = e.getItem();
            if (hand == null) {
                return;
            }
            if (plugin.tags.isSacrificialKnife(hand)) {
                if (!(plugin.data.isLeper(p))) {
                    plugin.msg.error(p, "Только прокаженный может добыть кровь этим ножом.");
                    return;
                }
            }
            return;
        }
        if (a == org.bukkit.event.block.Action.RIGHT_CLICK_BLOCK) {
            p = e.getPlayer();
            hand = e.getItem();
            if (hand == null) {
                return;
            }
            if (plugin.tags.isSacrificialKnife(hand)) {
                if (!(plugin.data.isLeper(p))) {
                    plugin.msg.error(p, "Только прокаженный может добыть кровь этим ножом.");
                    return;
                }
            }
            return;
        }
        p = e.getPlayer();
        hand = e.getItem();
        if (hand == null) {
            return;
        }
        if (plugin.tags.isSacrificialKnife(hand)) {
            if (!(plugin.data.isLeper(p))) {
                plugin.msg.error(p, "Только прокаженный может добыть кровь этим ножом.");
                return;
            }
        }
        if (!(plugin.data.isLeper(p))) {
            plugin.msg.error(p, "Только прокаженный может добыть кровь этим ножом.");
            return;
        }
        long now = java.lang.System.currentTimeMillis();
        long last = ((java.lang.Long) knifeCd.getOrDefault(p.getUniqueId(), java.lang.Long.valueOf(0L))).longValue();
        long cd = plugin.settings.knifeCooldownMs;
        if (Long.compare(now - last, cd) < 0) {
            long sec = (cd - (now - last)) / 1000L;
            plugin.msg.warn(p, "Ножик еще не готов. Осталось: " + sec + " сек.");
            return;
        }
        knifeCd.put(p.getUniqueId(), java.lang.Long.valueOf(now));
        p.damage(2.0);
        org.examplee.leperClassPlugin.util.InventoryUtil.giveOrDrop(p, plugin.items.makeLeperBlood());
        if (plugin.effects.MINING_FATIGUE != null) {
            p.addPotionEffect(new org.bukkit.potion.PotionEffect(plugin.effects.MINING_FATIGUE, plugin.settings.knifeFatigueTicks, 0));
        }
        if (plugin.effects.WEAKNESS != null) {
            p.addPotionEffect(new org.bukkit.potion.PotionEffect(plugin.effects.WEAKNESS, plugin.settings.knifeWeakTicks, 0));
        }
        if (plugin.effects.SLOW != null) {
            p.addPotionEffect(new org.bukkit.potion.PotionEffect(plugin.effects.SLOW, plugin.settings.knifeSlowTicks, 0));
        }
        plugin.msg.error(p, "Вы добыли кровь. Цена высока.");
        plugin.log.info("Self blood extracted by " + p.getName());
    }

    @org.bukkit.event.EventHandler
    public void onLeperKilled(org.bukkit.event.entity.EntityDeathEvent e) {
        org.bukkit.entity.Player killer = e.getEntity();
        if (!((killer instanceof org.bukkit.entity.Player))) {
            return;
        }
        org.bukkit.entity.Player victim = (org.bukkit.entity.Player) killer;
        if (plugin.data.isLeper(victim)) {
            killer = victim.getKiller();
            if (killer == null) {
                return;
            }
        }
        killer = victim.getKiller();
        if (killer == null) {
            return;
        }
        if (Double.compare(java.util.concurrent.ThreadLocalRandom.current().nextDouble(), 0.6) < 0) {
            e.getDrops().add(plugin.items.makeLeperBlood());
            plugin.log.info("Blood dropped from killed leper: " + victim.getName());
        }
        if (!(plugin.data.isLeper(killer))) {
            if (Double.compare(java.util.concurrent.ThreadLocalRandom.current().nextDouble(), 0.3) < 0) {
                plugin.infection.addHit(killer);
                plugin.msg.warn(killer, "Кровь прокаженного попала в раны. Вы могли заразиться.");
            }
        }
    }

}
