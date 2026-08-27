package org.examplee.leperClassPlugin.listeners;

import java.util.EnumSet;
import java.util.Set;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.examplee.leperClassPlugin.LeperClassPlugin;

public final class ConsumeListener implements org.bukkit.event.Listener {
    private static java.util.Set MEAT_AND_FISH;
    private static java.util.Set GOLDEN_FOOD;
    private final org.examplee.leperClassPlugin.LeperClassPlugin plugin;

    public ConsumeListener(org.examplee.leperClassPlugin.LeperClassPlugin plugin) {
        super();
        this.plugin = plugin;
    }

    @org.bukkit.event.EventHandler(priority = org.bukkit.event.EventPriority.HIGHEST, ignoreCancelled = true)
    public void onConsume(org.bukkit.event.player.PlayerItemConsumeEvent e) {
        org.bukkit.entity.Player p = e.getPlayer();
        org.bukkit.inventory.ItemStack item = e.getItem();
        if (plugin.tags.isLeperBlood(item)) {
            p.addPotionEffect(new org.bukkit.potion.PotionEffect(plugin.effects.POISON, 240, 1));
            if (plugin.data.isLeper(p)) {
                return;
            }
            plugin.infection.startInfection(p);
            return;
        }
        if (!(plugin.tags.isThickLeperBlood(item))) {
            if (!(plugin.tags.isSterileLeperBlood(item))) {
                if (plugin.data.isLeper(p)) {
                    org.bukkit.Material type = item.getType();
                    if (type.isEdible()) {
                        if (!(org.examplee.leperClassPlugin.listeners.ConsumeListener.MEAT_AND_FISH.contains(type))) {
                            if (!(org.examplee.leperClassPlugin.listeners.ConsumeListener.GOLDEN_FOOD.contains(type))) {
                                e.setCancelled(true);
                                plugin.msg.error(p, "Прокаженные могут есть только мясо/рыбу и золотую еду.");
                                return;
                            }
                        }
                    }
                }
                return;
            }
            return;
        }
        p.addPotionEffect(new org.bukkit.potion.PotionEffect(plugin.effects.POISON, 160, 1));
        if (plugin.effects.NAUSEA == null) {
            return;
        }
        p.addPotionEffect(new org.bukkit.potion.PotionEffect(plugin.effects.NAUSEA, 240, 0));
        if (!(plugin.tags.isSterileLeperBlood(item))) {
            if (plugin.data.isLeper(p)) {
                type = item.getType();
                if (type.isEdible()) {
                    if (!(org.examplee.leperClassPlugin.listeners.ConsumeListener.MEAT_AND_FISH.contains(type))) {
                        if (!(org.examplee.leperClassPlugin.listeners.ConsumeListener.GOLDEN_FOOD.contains(type))) {
                            e.setCancelled(true);
                            plugin.msg.error(p, "Прокаженные могут есть только мясо/рыбу и золотую еду.");
                            return;
                        }
                    }
                }
            }
            return;
        }
        if (plugin.data.isLeper(p)) {
            type = item.getType();
            if (type.isEdible()) {
                if (!(org.examplee.leperClassPlugin.listeners.ConsumeListener.MEAT_AND_FISH.contains(type))) {
                    if (!(org.examplee.leperClassPlugin.listeners.ConsumeListener.GOLDEN_FOOD.contains(type))) {
                        e.setCancelled(true);
                        plugin.msg.error(p, "Прокаженные могут есть только мясо/рыбу и золотую еду.");
                        return;
                    }
                }
            }
        }
        type = item.getType();
        if (type.isEdible()) {
            if (!(org.examplee.leperClassPlugin.listeners.ConsumeListener.MEAT_AND_FISH.contains(type))) {
                if (!(org.examplee.leperClassPlugin.listeners.ConsumeListener.GOLDEN_FOOD.contains(type))) {
                    e.setCancelled(true);
                    plugin.msg.error(p, "Прокаженные могут есть только мясо/рыбу и золотую еду.");
                    return;
                }
            }
        }
        if (plugin.effects.FIRE_RES != null) {
            plugin.getServer().getScheduler().runTask(plugin, () -> lambda_onConsume_0(p));
        }
        if (type == org.bukkit.Material.ROTTEN_FLESH) {
            plugin.getServer().getScheduler().runTask(plugin, () -> lambda_onConsume_1(p));
        }
        boolean poisonPotion = item.getItemMeta();
        if ((poisonPotion instanceof org.bukkit.inventory.meta.PotionMeta)) {
            org.bukkit.inventory.meta.PotionMeta pm = (org.bukkit.inventory.meta.PotionMeta) poisonPotion;
            poisonPotion = potionHas(pm, "POISON");
            if (!(potionHas(pm, "HARM"))) {
                if (potionHas(pm, "INSTANT_DAMAGE")) {
                } else {
                }
            }
            boolean harmPotion = false;
            if (!(potionHas(pm, "HEAL"))) {
                if (!(potionHas(pm, "INSTANT_HEALTH"))) {
                    if (potionHas(pm, "REGEN")) {
                    } else {
                    }
                }
            }
            boolean healingPotion = false;
            if (poisonPotion) {
                plugin.getServer().getScheduler().runTask(plugin, () -> lambda_onConsume_2(p));
            }
            if (harmPotion) {
                plugin.getServer().getScheduler().runTask(plugin, () -> lambda_onConsume_3(p));
            }
            if (healingPotion) {
                plugin.getServer().getScheduler().runTask(plugin, () -> lambda_onConsume_4(p));
            }
            if (plugin.effects.FIRE_RES != null) {
                if (potionHas(pm, "FIRE_RES")) {
                    e.setCancelled(true);
                    plugin.msg.error(p, "Прокаженные не могут пить огнестойкость.");
                }
            }
        }
    }

    private boolean potionHas(org.bukkit.inventory.meta.PotionMeta meta, java.lang.String token) {
        java.lang.String token = token.toUpperCase();
        java.lang.reflect.Method m = meta.getCustomEffects().iterator();
        if (!(m.hasNext())) {
            try {
                m = meta.getClass().getMethod("getBasePotionType", new java.lang.Class[0]);
                org.bukkit.potion.PotionEffect pe = m.invoke(meta, new java.lang.Object[0]);
                if (base != null) {
                    if (base.toString().toUpperCase().contains(token)) {
                        return true;
                    }
                }
            }
            catch (java.lang.Throwable ex) {
            }
            return false;
        }
        pe = (org.bukkit.potion.PotionEffect) m.next();
        if (pe.getType().getName().toUpperCase().contains(token)) {
            return true;
        }
        /* continue */
        try {
            m = meta.getClass().getMethod("getBasePotionType", new java.lang.Class[0]);
            pe = m.invoke(meta, new java.lang.Object[0]);
            if (base != null) {
                if (base.toString().toUpperCase().contains(token)) {
                    return true;
                }
            }
        }
        catch (java.lang.Throwable m) {
            return false;
        }
        return false;
    }

    private void lambda_onConsume_4(org.bukkit.entity.Player p) {
        plugin.balance.hurt(p, plugin.settings.leperDamageFromHeal);
        plugin.msg.error(p, "Для прокаженного это зелье обернулось болью.");
    }

    private void lambda_onConsume_3(org.bukkit.entity.Player p) {
        plugin.balance.heal(p, plugin.settings.leperHealFromHarm);
    }

    private void lambda_onConsume_2(org.bukkit.entity.Player p) {
        plugin.balance.heal(p, plugin.settings.leperHealFromPoison);
        p.removePotionEffect(org.bukkit.potion.PotionEffectType.POISON);
    }

    private static void lambda_onConsume_1(org.bukkit.entity.Player p) {
        p.removePotionEffect(org.bukkit.potion.PotionEffectType.HUNGER);
    }

    private void lambda_onConsume_0(org.bukkit.entity.Player p) {
        p.removePotionEffect(plugin.effects.FIRE_RES);
    }

    static {
        org.bukkit.Material[] tmp1 = new org.bukkit.Material[16];
        tmp1[0] = org.bukkit.Material.COOKED_BEEF;
        tmp1[1] = org.bukkit.Material.PORKCHOP;
        tmp1[2] = org.bukkit.Material.COOKED_PORKCHOP;
        tmp1[3] = org.bukkit.Material.CHICKEN;
        tmp1[4] = org.bukkit.Material.COOKED_CHICKEN;
        tmp1[5] = org.bukkit.Material.MUTTON;
        tmp1[6] = org.bukkit.Material.COOKED_MUTTON;
        tmp1[7] = org.bukkit.Material.RABBIT;
        tmp1[8] = org.bukkit.Material.COOKED_RABBIT;
        tmp1[9] = org.bukkit.Material.ROTTEN_FLESH;
        tmp1[10] = org.bukkit.Material.COD;
        tmp1[11] = org.bukkit.Material.COOKED_COD;
        tmp1[12] = org.bukkit.Material.SALMON;
        tmp1[13] = org.bukkit.Material.COOKED_SALMON;
        tmp1[14] = org.bukkit.Material.TROPICAL_FISH;
        tmp1[15] = org.bukkit.Material.PUFFERFISH;
        org.examplee.leperClassPlugin.listeners.ConsumeListener.MEAT_AND_FISH = java.util.EnumSet.of(org.bukkit.Material.BEEF, tmp1);
        org.examplee.leperClassPlugin.listeners.ConsumeListener.GOLDEN_FOOD = java.util.EnumSet.of(org.bukkit.Material.GOLDEN_APPLE, org.bukkit.Material.ENCHANTED_GOLDEN_APPLE, org.bukkit.Material.GOLDEN_CARROT);
    }

}
