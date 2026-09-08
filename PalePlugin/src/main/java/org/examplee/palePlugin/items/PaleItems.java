package org.examplee.palePlugin.items;

import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.ShapelessRecipe;
import org.bukkit.inventory.RecipeChoice.MaterialChoice;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.examplee.palePlugin.PalePlugin;
import org.examplee.palePlugin.util.InvUtil;
import org.examplee.palePlugin.util.Msg;

public final class PaleItems {
   private final PalePlugin plugin;

   public PaleItems(PalePlugin plugin) {
      this.plugin = plugin;
   }

   /** Чёрный градиент для имени «Блока Тьмы»: от тёмно-серого к почти чёрному через §x-hex. */
   private static String blackGradient(String text) {
      int r1 = 130, g1 = 130, b1 = 140;   // тёмно-серый
      int r2 = 20, g2 = 15, b2 = 30;      // почти чёрный с фиолетовым отливом
      int len = text.length();
      StringBuilder sb = new StringBuilder();
      for (int i = 0; i < len; i++) {
         double t = len <= 1 ? 0.0 : (double)i / (len - 1);
         int r = (int)Math.round(r1 + (r2 - r1) * t);
         int g = (int)Math.round(g1 + (g2 - g1) * t);
         int b = (int)Math.round(b1 + (b2 - b1) * t);
         String hex = String.format("%02x%02x%02x", r, g, b);
         sb.append("§x");
         for (char c : hex.toCharArray()) sb.append('§').append(c);
         sb.append("§l").append(text.charAt(i));
      }
      return sb.toString();
   }

   /** ☠ Блок Тьмы — очаг чёрной заразы, привязан к владельцу. */
   public ItemStack makeDarkBlock(int amount, Player owner) {
      ItemStack it = new ItemStack(Material.BLACK_CONCRETE, Math.max(1, amount));
      ItemMeta meta = it.getItemMeta();
      if (meta != null) {
         meta.setDisplayName(blackGradient("☠ Блок Тьмы ☠"));
         meta.setLore(List.of(
               "§8Поставь — и тьма начнёт расползаться,",
               "§8заражая землю, грязь, деревья и выпивая воду.",
               "§8Редкие ветки тянутся вверх до 10 блоков.",
               "§7Очищается только §fсвятой водой§7.",
               "§7Оберег лишь §fзамедляет§7 её в 10 раз.",
               "§4Не стой на тьме — она пожирает живых.",
               "§0§m--------------------",
               owner != null ? "§8Владелец: §7" + owner.getName() : "§8Владелец: §7—",
               "§8Чужим тьма обжигает руки."
         ));
         PersistentDataContainer pdc = meta.getPersistentDataContainer();
         pdc.set(this.plugin.keys.KEY_DARK_BLOCK, PersistentDataType.BYTE, (byte)1);
         if (owner != null) {
            pdc.set(this.plugin.keys.KEY_DARK_BLOCK_OWNER, PersistentDataType.STRING, owner.getUniqueId().toString());
         }
         try { meta.setEnchantmentGlintOverride(true); } catch (Throwable ignored) {}
         it.setItemMeta(meta);
      }
      return it;
   }

   public boolean isDarkBlock(ItemStack it) {
      return this.hasByte(it, this.plugin.keys.KEY_DARK_BLOCK);
   }

   /** ☠ Карта Тьмы — показывает очаги тьмы по чанкам. */
   public ItemStack makeDarkMap(int amount, int radiusChunks) {
      ItemStack it = new ItemStack(Material.PAPER, Math.max(1, amount));
      ItemMeta meta = it.getItemMeta();
      if (meta != null) {
         meta.setDisplayName(blackGradient("☠ Карта Тьмы ☠"));
         meta.setLore(List.of("§7ПКМ: показать очаги тьмы", "§7Радиус: " + radiusChunks + " чанков"));
         PersistentDataContainer pdc = meta.getPersistentDataContainer();
         pdc.set(this.plugin.keys.KEY_DARK_MAP, PersistentDataType.BYTE, (byte)1);
         pdc.set(this.plugin.keys.KEY_MAP_RADIUS, PersistentDataType.INTEGER, radiusChunks);
         it.setItemMeta(meta);
      }
      return it;
   }

   public boolean isDarkMap(ItemStack it) {
      return this.hasByte(it, this.plugin.keys.KEY_DARK_MAP);
   }

   /** ✟ Жезл Света — админ-жезл очистки тьмы (ПКМ: радиус x2 по кругу 1..64, Shift+ПКМ: очистить). */
   public ItemStack makeDarkPurgeWand(int amount, int radiusChunks) {
      radiusChunks = Math.max(1, Math.min(64, radiusChunks));
      ItemStack it = new ItemStack(Material.BLAZE_ROD, Math.max(1, amount));
      ItemMeta meta = it.getItemMeta();
      if (meta != null) {
         meta.setDisplayName(Msg.g("✟ Жезл Света [Админ]"));
         meta.setLore(darkPurgeWandLore(radiusChunks));
         PersistentDataContainer pdc = meta.getPersistentDataContainer();
         pdc.set(this.plugin.keys.KEY_DARK_PURGE_WAND, PersistentDataType.BYTE, (byte)1);
         pdc.set(this.plugin.keys.KEY_DARK_PURGE_RADIUS, PersistentDataType.INTEGER, radiusChunks);
         try { meta.setEnchantmentGlintOverride(true); } catch (Throwable ignored) {}
         it.setItemMeta(meta);
      }
      return it;
   }

   private static List<String> darkPurgeWandLore(int radiusChunks) {
      return List.of(
            "§7ПКМ: §fрадиус x2 §7(по кругу 1..64)",
            "§7Shift+ПКМ: §fВЫЖЕЧЬ тьму §7в радиусе",
            "§eТекущий радиус: §f" + radiusChunks + " чанков",
            "§8Только для админов (pale.admin)."
      );
   }

   public boolean isDarkPurgeWand(ItemStack it) {
      return this.hasByte(it, this.plugin.keys.KEY_DARK_PURGE_WAND);
   }

   public int getDarkPurgeWandRadius(ItemStack it) {
      if (it == null || !it.hasItemMeta()) return 8;
      Integer r = it.getItemMeta().getPersistentDataContainer().get(this.plugin.keys.KEY_DARK_PURGE_RADIUS, PersistentDataType.INTEGER);
      return r == null ? 8 : Math.max(1, Math.min(64, r));
   }

   public void setDarkPurgeWandRadius(ItemStack it, int radiusChunks) {
      if (it == null || !it.hasItemMeta()) return;
      radiusChunks = Math.max(1, Math.min(64, radiusChunks));
      ItemMeta meta = it.getItemMeta();
      meta.getPersistentDataContainer().set(this.plugin.keys.KEY_DARK_PURGE_RADIUS, PersistentDataType.INTEGER, radiusChunks);
      meta.setLore(darkPurgeWandLore(radiusChunks));
      it.setItemMeta(meta);
   }

   public ItemStack makeSalt(int amount) {
      ItemStack it = new ItemStack(Material.GLOWSTONE_DUST, amount);
      ItemMeta meta = it.getItemMeta();
      if (meta != null) {
         meta.setDisplayName(Msg.g("[Pale] Очищающая соль"));
         meta.setLore(List.of("ПКМ: очищает заражение в радиусе " + this.plugin.cfg.saltRadius));
         meta.getPersistentDataContainer().set(this.plugin.keys.KEY_SALT, PersistentDataType.BYTE, (byte)1);
         it.setItemMeta(meta);
      }

      return it;
   }

   public boolean isSalt(ItemStack it) {
      return this.hasByte(it, this.plugin.keys.KEY_SALT);
   }

   public ItemStack makeHolyWater(int amount) {
      ItemStack it = new ItemStack(Material.SPLASH_POTION, amount);
      if (it.getItemMeta() instanceof PotionMeta pm) {
         pm.setDisplayName(Msg.g("[Pale] Святая вода"));
         pm.setLore(List.of("Брось: очищает заражение в радиусе " + this.plugin.cfg.holyWaterRadius));
         pm.getPersistentDataContainer().set(this.plugin.keys.KEY_HOLY_WATER, PersistentDataType.BYTE, (byte)1);
         it.setItemMeta(pm);
      }

      return it;
   }

   public boolean isHolyWater(ItemStack it) {
      return this.hasByte(it, this.plugin.keys.KEY_HOLY_WATER);
   }

   public ItemStack makeWard(int amount) {
      ItemStack it = new ItemStack(Material.AMETHYST_BLOCK, amount);
      ItemMeta meta = it.getItemMeta();
      if (meta != null) {
         meta.setDisplayName(Msg.g("[Pale] Оберег"));
         meta.setLore(List.of("Ставь: блокирует заражение в радиусе " + this.plugin.cfg.wardRadius));
         meta.getPersistentDataContainer().set(this.plugin.keys.KEY_WARD, PersistentDataType.BYTE, (byte)1);
         it.setItemMeta(meta);
      }

      return it;
   }

   public boolean isWard(ItemStack it) {
      return this.hasByte(it, this.plugin.keys.KEY_WARD);
   }

   public ItemStack makeGreaterWard(int amount) {
      return this.makeGreaterWard(amount, 0);
   }

   public ItemStack makeGreaterWard(int amount, int chargeSec) {
      ItemStack it = new ItemStack(Material.SEA_LANTERN, amount);
      ItemMeta meta = it.getItemMeta();
      if (meta != null) {
         meta.setDisplayName(Msg.g("[Pale] Великий оберег"));
         meta.setLore(
            List.of(
               "Ставь: блокирует заражение в радиусе " + this.plugin.cfg.greaterWardRadius,
               "Очищает 1 блок раз в " + this.plugin.cfg.greaterWardCleanIntervalSec + " сек. (тратит заряд)",
               "Заряжается святой водой: ПКМ по оберегу",
               "Заряд: " + chargeSec / 60 + " мин. " + chargeSec % 60 + " сек."
            )
         );
         PersistentDataContainer pdc = meta.getPersistentDataContainer();
         pdc.set(this.plugin.keys.KEY_GREATER_WARD, PersistentDataType.BYTE, (byte)1);
         pdc.set(this.plugin.keys.KEY_GREATER_WARD_CHARGE, PersistentDataType.INTEGER, Math.max(0, chargeSec));
         it.setItemMeta(meta);
      }

      return it;
   }

   public int greaterWardCharge(ItemStack it) {
      if (it == null) {
         return 0;
      } else {
         ItemMeta meta = it.getItemMeta();
         if (meta == null) {
            return 0;
         } else {
            Integer v = meta.getPersistentDataContainer().get(this.plugin.keys.KEY_GREATER_WARD_CHARGE, PersistentDataType.INTEGER);
            return v == null ? 0 : Math.max(0, v);
         }
      }
   }

   public boolean isGreaterWard(ItemStack it) {
      return this.hasByte(it, this.plugin.keys.KEY_GREATER_WARD);
   }

   public ItemStack makePurifierFlint(int amount) {
      ItemStack it = new ItemStack(Material.FLINT_AND_STEEL, amount);
      ItemMeta meta = it.getItemMeta();
      if (meta != null) {
         meta.setDisplayName(Msg.g("[Pale] Очищающее огниво"));
         meta.setLore(
            List.of("ПКМ по заражению: очищает радиус " + this.plugin.cfg.purifierFlintRadius, "Использований: " + this.plugin.cfg.purifierFlintUsesDefault)
         );
         PersistentDataContainer pdc = meta.getPersistentDataContainer();
         pdc.set(this.plugin.keys.KEY_PURIFIER_FLINT, PersistentDataType.BYTE, (byte)1);
         pdc.set(this.plugin.keys.KEY_PURIFIER_FLINT_USES, PersistentDataType.INTEGER, this.plugin.cfg.purifierFlintUsesDefault);
         it.setItemMeta(meta);
      }

      return it;
   }

   public boolean isPurifierFlint(ItemStack it) {
      return it != null && it.getType() == Material.FLINT_AND_STEEL ? this.hasByte(it, this.plugin.keys.KEY_PURIFIER_FLINT) : false;
   }

   public int getPurifierFlintUses(ItemStack it) {
      ItemMeta meta = it.getItemMeta();
      if (meta == null) {
         return 0;
      } else {
         Integer v = (Integer)meta.getPersistentDataContainer().get(this.plugin.keys.KEY_PURIFIER_FLINT_USES, PersistentDataType.INTEGER);
         return v == null ? 0 : v;
      }
   }

   public void setPurifierFlintUses(ItemStack it, int usesLeft) {
      ItemMeta meta = it.getItemMeta();
      if (meta != null) {
         PersistentDataContainer pdc = meta.getPersistentDataContainer();
         pdc.set(this.plugin.keys.KEY_PURIFIER_FLINT_USES, PersistentDataType.INTEGER, usesLeft);
         meta.setLore(List.of("ПКМ по заражению: очищает радиус " + this.plugin.cfg.purifierFlintRadius, "Использований: " + usesLeft));
         it.setItemMeta(meta);
      }
   }

   public ItemStack makeInfectionMap(int amount, int radiusChunks) {
      ItemStack it = new ItemStack(Material.PAPER, amount);
      ItemMeta meta = it.getItemMeta();
      if (meta != null) {
         meta.setDisplayName(Msg.g("[Pale] Карта заражения"));
         meta.setLore(List.of("ПКМ: показать карту", "Радиус: " + radiusChunks + " чанков"));
         PersistentDataContainer pdc = meta.getPersistentDataContainer();
         pdc.set(this.plugin.keys.KEY_MAP_ITEM, PersistentDataType.BYTE, (byte)1);
         pdc.set(this.plugin.keys.KEY_MAP_RADIUS, PersistentDataType.INTEGER, radiusChunks);
         it.setItemMeta(meta);
      }

      return it;
   }

   public boolean isInfectionMap(ItemStack it) {
      return it != null && it.getType() == Material.PAPER ? this.hasByte(it, this.plugin.keys.KEY_MAP_ITEM) : false;
   }

   public int getMapRadius(ItemStack it) {
      ItemMeta meta = it.getItemMeta();
      if (meta == null) {
         return this.plugin.cfg.mapItemDefaultRadiusChunks;
      } else {
         Integer r = (Integer)meta.getPersistentDataContainer().get(this.plugin.keys.KEY_MAP_RADIUS, PersistentDataType.INTEGER);
         return r == null ? this.plugin.cfg.mapItemDefaultRadiusChunks : r;
      }
   }

   public ItemStack makeInfectWand(int amount, int uses) {
      ItemStack it = new ItemStack(Material.CARROT_ON_A_STICK, amount);
      ItemMeta meta = it.getItemMeta();
      if (meta != null) {
         meta.setDisplayName(Msg.g("[Pale] Палочка заразы"));
         meta.setLore(List.of("ПКМ: заражает радиус " + this.plugin.cfg.infectWandRadius, "Использований: " + uses));
         PersistentDataContainer pdc = meta.getPersistentDataContainer();
         pdc.set(this.plugin.keys.KEY_INFECT_WAND, PersistentDataType.BYTE, (byte)1);
         pdc.set(this.plugin.keys.KEY_INFECT_WAND_USES, PersistentDataType.INTEGER, uses);
         it.setItemMeta(meta);
      }

      return it;
   }

   public boolean isInfectWand(ItemStack it) {
      return it != null && it.getType() == Material.CARROT_ON_A_STICK ? this.hasByte(it, this.plugin.keys.KEY_INFECT_WAND) : false;
   }

   public int getInfectWandUses(ItemStack it) {
      ItemMeta meta = it.getItemMeta();
      if (meta == null) {
         return 0;
      } else {
         Integer v = (Integer)meta.getPersistentDataContainer().get(this.plugin.keys.KEY_INFECT_WAND_USES, PersistentDataType.INTEGER);
         return v == null ? 0 : v;
      }
   }

   public void setInfectWandUses(ItemStack it, int usesLeft) {
      ItemMeta meta = it.getItemMeta();
      if (meta != null) {
         meta.getPersistentDataContainer().set(this.plugin.keys.KEY_INFECT_WAND_USES, PersistentDataType.INTEGER, usesLeft);
         meta.setLore(List.of("ПКМ: заражает радиус " + this.plugin.cfg.infectWandRadius, "Использований: " + usesLeft));
         it.setItemMeta(meta);
      }
   }

   public ItemStack makeAdminPurgeWand(int amount) {
      ItemStack it = new ItemStack(Material.BLAZE_ROD, amount);
      ItemMeta meta = it.getItemMeta();
      if (meta != null) {
         meta.setDisplayName(Msg.g("[Pale] Админ: Жезл очищения"));
         meta.setLore(
            List.of(
               "Только для админов",
               "ПКМ: массовая очистка",
               "Радиус: " + this.plugin.cfg.adminPurgeRadiusChunks + " чанков",
               "Глубина: " + this.plugin.cfg.adminPurgeDepth
            )
         );
         meta.getPersistentDataContainer().set(this.plugin.keys.KEY_ADMIN_PURGE, PersistentDataType.BYTE, (byte)1);
         it.setItemMeta(meta);
      }

      return it;
   }

   public boolean isAdminPurgeWand(ItemStack it) {
      return it != null && it.getType() == Material.BLAZE_ROD ? this.hasByte(it, this.plugin.keys.KEY_ADMIN_PURGE) : false;
   }

   public void giveOrDrop(Player p, ItemStack it) {
      InvUtil.giveOrDrop(p, it);
   }

   private boolean hasByte(ItemStack it, NamespacedKey key) {
      if (it == null) {
         return false;
      } else {
         ItemMeta meta = it.getItemMeta();
         if (meta == null) {
            return false;
         } else {
            Byte v = (Byte)meta.getPersistentDataContainer().get(key, PersistentDataType.BYTE);
            return v != null && v == 1;
         }
      }
   }
}
