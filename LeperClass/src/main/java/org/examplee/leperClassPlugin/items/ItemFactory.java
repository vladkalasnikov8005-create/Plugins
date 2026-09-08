package org.examplee.leperClassPlugin.items;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.examplee.leperClassPlugin.core.LeperKeys;
import org.examplee.leperClassPlugin.util.Compat;
import org.examplee.leperClassPlugin.util.TextUtil;

public final class ItemFactory {
   public static final int ITEM_VERSION = 2;
   private final LeperKeys keys;

   public ItemFactory(LeperKeys keys) {
      this.keys = keys;
   }

   public ItemStack makePlagueStick() {
      ItemStack it = new ItemStack(Material.STICK);
      ItemMeta meta = it.getItemMeta();
      if (meta != null) {
         meta.setDisplayName(TextUtil.gPurpleGreen("ПАЛКА ПРОКАЗЫ"));
         meta.setLore(List.of(TextUtil.loreHint("Удар: яд и короткое оглушение"), TextUtil.loreWarn("Заражение работает только с благословлением Денжер")));
         meta.addItemFlags(new ItemFlag[]{ItemFlag.HIDE_ATTRIBUTES});
         this.markVersion(meta);
         meta.getPersistentDataContainer().set(this.keys.plagueStickKey, PersistentDataType.BYTE, (byte)1);
         it.setItemMeta(meta);
      }

      return it;
   }

   public ItemStack makePlagueBomb() {
      Material mat = Compat.materialFirst("SLIME_BALL", "FERMENTED_SPIDER_EYE", "SPIDER_EYE");
      ItemStack it = new ItemStack(mat);
      ItemMeta meta = it.getItemMeta();
      if (meta != null) {
         meta.setDisplayName(TextUtil.gGreenGray("ОБЛАКО ПРОКАЗЫ"));
         meta.setLore(List.of(TextUtil.loreHint("ПКМ: ядовитое облако и оглушение"), TextUtil.loreWarn("Очаг и заражение только с благословлением Денжер")));
         this.markVersion(meta);
         meta.getPersistentDataContainer().set(this.keys.plagueBombKey, PersistentDataType.BYTE, (byte)1);
         it.setItemMeta(meta);
      }

      return it;
   }

   public ItemStack makeVaccine() {
      return this.makePotion(
         ChatColor.AQUA + "ВАКЦИНА",
         List.of(TextUtil.loreHint("Полностью излечивает прокажение на ранних стадиях (1-2)")),
         Color.fromRGB(150, 255, 255),
         this.keys.vaccineKey,
         "INSTANT_HEAL"
      );
   }

   public ItemStack makeLeperBlood() {
      return this.makePotion(
         ChatColor.DARK_RED + "Кровь прокаженного",
         List.of(TextUtil.loreWarn("Не пей эту дрянь (получишь прокажение)")),
         Color.fromRGB(180, 0, 0),
         this.keys.leperBloodKey,
         "WATER"
      );
   }

   public ItemStack makeThickLeperBlood() {
      return this.makePotion(
         ChatColor.GRAY + "Густая кровь прокаженного",
         List.of(TextUtil.loreHint("Кажется, это лучше не пить")),
         Color.fromRGB(90, 90, 90),
         this.keys.thickBloodKey,
         "AWKWARD"
      );
   }

   public ItemStack makeSterileLeperBlood() {
      return this.makePotion(
         ChatColor.GOLD + "Стерильная кровь прокаженного",
         List.of(TextUtil.loreHint("Бесполезна")),
         Color.fromRGB(120, 70, 35),
         this.keys.sterileBloodKey,
         "AWKWARD"
      );
   }

   public ItemStack makeQuarantineLantern() {
      ItemStack it = new ItemStack(Material.SOUL_LANTERN);
      ItemMeta meta = it.getItemMeta();
      if (meta != null) {
         meta.setDisplayName(TextUtil.gGoldYellow("КАРАНТИННЫЙ ФОНАРЬ"));
         meta.setLore(
            List.of(
               TextUtil.loreHint("Ставь: прокажённые в радиусе слабеют,"),
               TextUtil.loreHint("заражение не прогрессирует"),
               TextUtil.loreWarn("Требует топливо: светопыль (ПКМ по фонарю)")
            )
         );
         this.markVersion(meta);
         meta.getPersistentDataContainer().set(this.keys.quarantineLanternKey, PersistentDataType.BYTE, (byte)1);
         it.setItemMeta(meta);
      }

      return it;
   }

   public ItemStack makeSacrificialKnife() {
      ItemStack it = new ItemStack(Compat.materialFirst("IRON_SWORD", "STONE_SWORD"));
      ItemMeta meta = it.getItemMeta();
      if (meta != null) {
         meta.setDisplayName(TextUtil.gRedGray("ЖЕРТВЕННЫЙ НОЖИК"));
         meta.setLore(List.of(TextUtil.loreHint("ПКМ: добыть кровь (только для прокаженного)"), ChatColor.DARK_GRAY + "• КД: 1 час"));
         this.markVersion(meta);
         meta.getPersistentDataContainer().set(this.keys.sacrificialKnifeKey, PersistentDataType.BYTE, (byte)1);
         it.setItemMeta(meta);
      }

      return it;
   }

   public ItemStack makeUmbrellaTiny() {
      return this.makeUmbrella(0, 150);
   }

   public ItemStack makeUmbrellaWeak() {
      return this.makeUmbrella(1, 600);
   }

   public ItemStack makeUmbrellaNormal() {
      return this.makeUmbrella(2, 1500);
   }

   public ItemStack makeUmbrellaStrong() {
      return this.makeUmbrella(3, 3000);
   }

   private ItemStack makeUmbrella(int tier, int lifetimeSeconds) {
      ItemStack it = new ItemStack(Material.STICK, 1);
      ItemMeta meta = it.getItemMeta();
      if (meta != null) {
         meta.setDisplayName("Зонт");
         meta.setLore(
            Arrays.asList(
               ChatColor.GRAY + "Держи в левой руке",
               ChatColor.GRAY + "Защищает от солнца",
               ChatColor.GRAY + "Уровень: " + tier,
               ChatColor.GRAY + "Осталось: " + this.formatSeconds(lifetimeSeconds)
            )
         );
         PersistentDataContainer pdc = meta.getPersistentDataContainer();
         this.markVersion(meta);
         pdc.set(this.keys.umbrellaKey, PersistentDataType.BYTE, (byte)1);
         pdc.set(this.keys.umbrellaTierKey, PersistentDataType.INTEGER, tier);
         pdc.set(this.keys.umbrellaLifetimeKey, PersistentDataType.INTEGER, lifetimeSeconds);
         pdc.set(this.keys.umbrellaRemainingKey, PersistentDataType.INTEGER, lifetimeSeconds);
         it.setItemMeta(meta);
      }

      return it;
   }

   private String formatSeconds(int total) {
      int s = Math.max(0, total);
      int min = s / 60;
      int sec = s % 60;
      return String.format("%02d:%02d", min, sec);
   }

   public ItemStack bgPane() {
      ItemStack bg = new ItemStack(Compat.materialFirst("PURPLE_STAINED_GLASS_PANE", "BLACK_STAINED_GLASS_PANE", "GRAY_STAINED_GLASS_PANE", "GLASS_PANE"));
      ItemMeta m = bg.getItemMeta();
      if (m != null) {
         m.setDisplayName(ChatColor.DARK_GRAY + "•");
         bg.setItemMeta(m);
      }

      return bg;
   }

   public ItemStack button(Material mat, String name, List<String> lore) {
      ItemStack it = new ItemStack(mat);
      ItemMeta meta = it.getItemMeta();
      if (meta != null) {
         meta.setDisplayName(name);
         meta.setLore(lore);
         it.setItemMeta(meta);
      }

      return it;
   }

   private ItemStack makePotion(String name, List<String> lore, Color color, NamespacedKey key, String baseTypeName) {
      ItemStack it = new ItemStack(Material.POTION);
      PotionMeta meta = (PotionMeta)it.getItemMeta();
      if (meta != null) {
         meta.setDisplayName(name);
         meta.setLore(lore);
         meta.setColor(color);
         this.setBasePotionCompat(meta, baseTypeName);

         try {
            meta.addItemFlags(new ItemFlag[]{ItemFlag.valueOf("HIDE_POTION_EFFECTS")});
         } catch (IllegalArgumentException var9) {
         }

         this.markVersion(meta);
         meta.getPersistentDataContainer().set(key, PersistentDataType.BYTE, (byte)1);
         it.setItemMeta(meta);
      }

      return it;
   }

   private void markVersion(ItemMeta meta) {
      if (meta != null) {
         meta.getPersistentDataContainer().set(this.keys.itemVersionKey, PersistentDataType.INTEGER, 2);
      }
   }

   private void setBasePotionCompat(PotionMeta meta, String potionTypeName) {
      if (meta != null && potionTypeName != null) {
         try {
            Class<?> potionTypeClass = Class.forName("org.bukkit.potion.PotionType");
            Object potionType = Enum.valueOf(potionTypeClass.asSubclass(Enum.class), potionTypeName);

            try {
               Method setBasePotionType = meta.getClass().getMethod("setBasePotionType", potionTypeClass);
               setBasePotionType.invoke(meta, potionType);
               return;
            } catch (Throwable var8) {
               Class<?> potionDataClass = Class.forName("org.bukkit.potion.PotionData");
               Object potionData = potionDataClass.getConstructor(potionTypeClass).newInstance(potionType);
               Method setBasePotionData = meta.getClass().getMethod("setBasePotionData", potionDataClass);
               setBasePotionData.invoke(meta, potionData);
            }
         } catch (Throwable var9) {
         }
      }
   }
}
