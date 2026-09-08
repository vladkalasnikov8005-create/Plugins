package org.examplee.palePlugin.darkness;

import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitTask;
import org.examplee.palePlugin.PalePlugin;
import org.examplee.palePlugin.util.MathUtil;

/**
 * ТЬМА — чёрная зараза для прокажённых.
 * Блок тьмы (чёрный бетон) заражает всё то же, что и бледный мох (земля, грязь, каменистая грязь,
 * деревья, листва), но с разбросом и своей скоростью (1..100, где 100 — ОЧЕНЬ быстро).
 * Редкие ветки растут вверх до 10 блоков. Святая вода очищает тьму, а оберег НЕ останавливает
 * её — лишь замедляет в 10 раз. Стоящий на тьме получает урон и букет дебаффов.
 */
public final class DarknessManager implements Listener {

   public static final Material DARK_BLOCK = Material.BLACK_CONCRETE;
   private static final int BRANCH_MAX_HEIGHT = 30;   // ветки растут до 30 блоков
   private static final int BRANCH_SIDE_START = 10;   // с 10-го блока — боковые отростки раз в 2 блока

   private final PalePlugin plugin;
   private final Random random = new Random();

   // Черный список для режима «заражать всё» (infectAll): эти материалы тьма НЕ трогает никогда
   private final Set<Material> uninfectable = new HashSet<>();

   // Настройки (конфиг darkness.*)
   public boolean enabled = true;
   public int infectSpeed = 40;   // 1..1000 — скорость заражения соседних блоков (после 100 — турбо-разгон)
   public int growthSpeed = 20;   // 1..1000 — скорость роста веток вверх
   public int maxBlocksPerWorld = 20000000; // лимит блоков тьмы на мир (20 млн по умолчанию)
   public boolean infectAll = false; // режим «АПОКАЛИПСИС»: тьма заражает все блоки, кроме чёрного списка

   // Все блоки тьмы: мир -> набор упакованных координат (+ список для быстрого случайного выбора)
   private final Map<UUID, Set<Long>> blocks = new HashMap<>();
   private final Map<UUID, ArrayList<Long>> blockList = new HashMap<>();

   // Аккумуляторы попыток (дробная скорость)
   private double infectAcc = 0.0;
   private double growthAcc = 0.0;

   // Кулдаун урона от стояния на тьме (uuid -> unix-ms последнего тика урона)
   private final Map<UUID, Long> damageCooldown = new HashMap<>();

   private BukkitTask spreadTask;
   private BukkitTask effectsTask;
   private boolean dirty = false;

   public DarknessManager(PalePlugin plugin) {
      this.plugin = plugin;
      // В режиме infectAll тьма заражает ВСЁ, кроме этого чёрного списка.
      // Стойкие блоки: обсидиан, бедрок, плачущий обсидиан, все сундуки/хранилища,
      // редстоун и компоненты, поршни, наблюдатели.
      this.uninfectable.add(Material.OBSIDIAN);
      this.uninfectable.add(Material.BEDROCK);
      this.uninfectable.add(Material.CRYING_OBSIDIAN);
      this.uninfectable.add(Material.CHEST);
      this.uninfectable.add(Material.TRAPPED_CHEST);
      this.uninfectable.add(Material.BARREL);
      this.uninfectable.add(Material.ENDER_CHEST);
      this.uninfectable.add(Material.SHULKER_BOX);
      this.uninfectable.add(Material.WHITE_SHULKER_BOX);
      this.uninfectable.add(Material.ORANGE_SHULKER_BOX);
      this.uninfectable.add(Material.MAGENTA_SHULKER_BOX);
      this.uninfectable.add(Material.LIGHT_BLUE_SHULKER_BOX);
      this.uninfectable.add(Material.YELLOW_SHULKER_BOX);
      this.uninfectable.add(Material.LIME_SHULKER_BOX);
      this.uninfectable.add(Material.PINK_SHULKER_BOX);
      this.uninfectable.add(Material.GRAY_SHULKER_BOX);
      this.uninfectable.add(Material.LIGHT_GRAY_SHULKER_BOX);
      this.uninfectable.add(Material.CYAN_SHULKER_BOX);
      this.uninfectable.add(Material.PURPLE_SHULKER_BOX);
      this.uninfectable.add(Material.BLUE_SHULKER_BOX);
      this.uninfectable.add(Material.BROWN_SHULKER_BOX);
      this.uninfectable.add(Material.GREEN_SHULKER_BOX);
      this.uninfectable.add(Material.RED_SHULKER_BOX);
      this.uninfectable.add(Material.BLACK_SHULKER_BOX);
      this.uninfectable.add(Material.HOPPER);
      this.uninfectable.add(Material.DISPENSER);
      this.uninfectable.add(Material.DROPPER);
      this.uninfectable.add(Material.FURNACE);
      this.uninfectable.add(Material.BLAST_FURNACE);
      this.uninfectable.add(Material.SMOKER);
      this.uninfectable.add(Material.BREWING_STAND);
      this.uninfectable.add(Material.REDSTONE_WIRE);
      this.uninfectable.add(Material.REDSTONE_TORCH);
      this.uninfectable.add(Material.REDSTONE_WALL_TORCH);
      this.uninfectable.add(Material.REDSTONE_BLOCK);
      this.uninfectable.add(Material.REDSTONE_LAMP);
      this.uninfectable.add(Material.REPEATER);
      this.uninfectable.add(Material.COMPARATOR);
      this.uninfectable.add(Material.PISTON);
      this.uninfectable.add(Material.STICKY_PISTON);
      this.uninfectable.add(Material.OBSERVER);
   }

   // ==================== ЖИЗНЕННЫЙ ЦИКЛ ====================

   public void loadConfig() {
      var cfg = this.plugin.getConfig();
      cfg.addDefault("darkness.enabled", true);
      cfg.addDefault("darkness.infectSpeed", 40);
      cfg.addDefault("darkness.growthSpeed", 20);
      cfg.addDefault("darkness.maxBlocksPerWorld", 20000000);
      cfg.addDefault("darkness.infectAll", false);
      cfg.options().copyDefaults(true);
      this.plugin.saveConfig();
      this.enabled = cfg.getBoolean("darkness.enabled", true);
      this.infectAll = cfg.getBoolean("darkness.infectAll", false);
      this.infectSpeed = MathUtil.clamp(cfg.getInt("darkness.infectSpeed", 40), 1, 1000);
      this.growthSpeed = MathUtil.clamp(cfg.getInt("darkness.growthSpeed", 20), 1, 1000);
      int maxCfg = cfg.getInt("darkness.maxBlocksPerWorld", 20000000);
      if (maxCfg <= 200000) maxCfg = 20000000; // миграция старого лимита 200000 -> 20 млн
      this.maxBlocksPerWorld = MathUtil.clamp(maxCfg, 1000, 200000000);
   }

   public void start() {
      this.loadConfig();
      this.loadFromDisk();
      this.spreadTask = Bukkit.getScheduler().runTaskTimer(this.plugin, this::tickSpread, 1L, 1L);
      this.effectsTask = Bukkit.getScheduler().runTaskTimer(this.plugin, this::tickStepEffects, 10L, 10L);
   }

   public void stop() {
      if (this.spreadTask != null) { this.spreadTask.cancel(); this.spreadTask = null; }
      if (this.effectsTask != null) { this.effectsTask.cancel(); this.effectsTask = null; }
      this.saveToDisk(false);
   }

   public int totalBlocks() {
      int n = 0;
      for (Set<Long> s : this.blocks.values()) n += s.size();
      return n;
   }

   // ==================== КЛЮЧИ КООРДИНАТ ====================

   private static long key(int x, int y, int z) {
      return ((long)x & 0x3FFFFFFL) << 38 | ((long)z & 0x3FFFFFFL) << 12 | ((long)y & 0xFFFL);
   }

   private static int keyX(long k) { int v = (int)(k >> 38 & 0x3FFFFFFL); return (v & 0x2000000) != 0 ? v | ~0x3FFFFFF : v; }
   private static int keyZ(long k) { int v = (int)(k >> 12 & 0x3FFFFFFL); return (v & 0x2000000) != 0 ? v | ~0x3FFFFFF : v; }
   private static int keyY(long k) { int v = (int)(k & 0xFFFL); return (v & 0x800) != 0 ? v | ~0xFFF : v; }

   private Set<Long> setOf(World w) {
      return this.blocks.computeIfAbsent(w.getUID(), u -> new HashSet<>());
   }

   private ArrayList<Long> listOf(World w) {
      return this.blockList.computeIfAbsent(w.getUID(), u -> new ArrayList<>());
   }

   // ==================== РЕГИСТРАЦИЯ БЛОКОВ ====================

   /** Пометить блок как тьму (сам блок уже должен быть чёрным бетоном). */
   public void register(World w, int x, int y, int z) {
      Set<Long> set = this.setOf(w);
      if (set.size() >= this.maxBlocksPerWorld) return;
      long k = key(x, y, z);
      if (set.add(k)) {
         this.listOf(w).add(k);
         this.dirty = true;
      }
   }

   public boolean isDarkness(World w, int x, int y, int z) {
      Set<Long> set = this.blocks.get(w.getUID());
      return set != null && set.contains(key(x, y, z));
   }

   private void unregister(World w, int x, int y, int z) {
      Set<Long> set = this.blocks.get(w.getUID());
      if (set != null && set.remove(key(x, y, z))) this.dirty = true;
      // из списка не удаляем — устаревшие записи отфильтруются лениво при выборе
   }

   /** Заразить блок тьмой: превратить в чёрный бетон и зарегистрировать. */
   public void infectBlock(Block b) {
      b.setType(DARK_BLOCK, false);
      this.register(b.getWorld(), b.getX(), b.getY(), b.getZ());
   }

   // ==================== РАСПРОСТРАНЕНИЕ ====================

   /** Множитель скорости (1..1000): до 100 — экспонента, до 1000 — квадрат (1000 = x100 от сотки). */
   private double speedMultiplier(int s) {
      double m = Math.pow(1.072, Math.min(s, 100));
      if (s > 100) {
         m *= Math.pow(Math.min(s, 1000) / 100.0, 2.0); // 200 -> x4, 500 -> x25, 1000 -> x100
      }
      return m;
   }

   /** Скорость 1..1000 -> попыток заражения в тик.
    *  База (как раньше) + бонус, растущий как ПЕРИМЕТР пятна (√блоков), а не площадь:
    *  так тьма не «задыхается» по мере разрастания, но и не взрывает тик на огромных картах. */
   private double infectRatePerTick() {
      int blocks = this.totalBlocks();
      double mult = this.speedMultiplier(this.infectSpeed);
      return 0.4 * mult / 20.0 + Math.sqrt(blocks) * mult / 40.0;
   }

   /** Скорость роста веток 1..1000 -> попыток роста в тик (ветки редкие — множитель меньше). */
   private double growthRatePerTick() {
      double mult = this.speedMultiplier(this.growthSpeed);
      return 0.08 * mult / 20.0 + Math.sqrt(this.totalBlocks()) * mult / 600.0;
   }

   private void tickSpread() {
      if (!this.enabled) return;
      this.infectAcc += this.infectRatePerTick();
      int attempts = (int)Math.min(20000.0, Math.floor(this.infectAcc));
      this.infectAcc -= attempts;
      if (this.infectAcc > 20000.0) this.infectAcc = 20000.0; // не копим бесконечный долг
      for (int i = 0; i < attempts; i++) this.trySpreadOnce();

      this.growthAcc += this.growthRatePerTick();
      int growth = (int)Math.min(2000.0, Math.floor(this.growthAcc));
      this.growthAcc -= growth;
      if (this.growthAcc > 2000.0) this.growthAcc = 2000.0;
      for (int i = 0; i < growth; i++) this.tryGrowBranchOnce();
   }

   /** Одна попытка заражения: случайный блок тьмы -> случайный сосед или разброс. */
   private void trySpreadOnce() {
      Block src = this.pickRandomDarkBlock();
      if (src == null) return;
      World w = src.getWorld();

      int dx;
      int dy;
      int dz;
      if (this.random.nextInt(10) < 3) {
         // РАЗБРОС: прыжок в радиусе до 3 блоков (тьма перескакивает)
         dx = this.random.nextInt(7) - 3;
         dz = this.random.nextInt(7) - 3;
         dy = this.random.nextInt(5) - 2;
      } else {
         // Обычный сосед (включая диагонали по горизонтали)
         dx = this.random.nextInt(3) - 1;
         dz = this.random.nextInt(3) - 1;
         dy = this.random.nextInt(3) - 1;
         if (dx == 0 && dy == 0 && dz == 0) dx = 1;
      }

      Block target = src.getRelative(dx, dy, dz);
      // Если попали в воздух — падаем на поверхность (ищем блок под ним, до 4 вниз)
      for (int i = 0; i < 4 && target.getType().isAir(); i++) target = target.getRelative(BlockFace.DOWN);

      // ТЬМА ПРОТИВ ВОДЫ: тьма «выпивает» воду — медленно осушает водоёмы
      if (target.getType() == Material.WATER || target.getType() == Material.KELP
            || target.getType() == Material.KELP_PLANT || target.getType() == Material.SEAGRASS
            || target.getType() == Material.TALL_SEAGRASS) {
         if (this.random.nextInt(4) == 0) { // медленно: пьёт лишь каждую 4-ю попытку
            target.setType(Material.AIR, false);
            if (this.random.nextInt(6) == 0) {
               try {
                  w.spawnParticle(Particle.SMOKE, target.getLocation().add(0.5, 0.5, 0.5), 4, 0.25, 0.25, 0.25, 0.01);
                  w.playSound(target.getLocation(), Sound.BLOCK_SPONGE_ABSORB, 0.4F, 0.6F);
               } catch (Throwable ignored) {}
            }
         }
         return;
      }

      // Оберег НЕ останавливает тьму — только тормозит в 10 раз
      if (this.plugin.engine.isWardProtected(w, target.getX(), target.getY(), target.getZ())
            && this.random.nextInt(10) != 0) {
         return;
      }

      this.tryInfectTarget(target);
   }

   /** Что тьма умеет заражать (всё то же, что бледный мох + сам бледный лес; в режиме infectAll — почти всё). */
   private boolean tryInfectTarget(Block b) {
      Material t = b.getType();
      if (t.isAir() || t == DARK_BLOCK) return false;
      if (this.uninfectable.contains(t)) return false; // стойкие блоки тьма не трогает никогда

      String n = t.name();
      boolean infectable;
      if (this.infectAll) {
         // Режим АПОКАЛИПСИС: заражается всё, что не в чёрном списке.
         // Жидкости (вода/лава) отдельно: вода выпивается, лаву трогаем без дропа.
         if (t == Material.WATER || t == Material.LAVA) return false;
         infectable = true;
      } else {
         infectable =
               t == Material.GRASS_BLOCK || t == Material.DIRT || t == Material.COARSE_DIRT
               || t == Material.ROOTED_DIRT || t == Material.PODZOL || t == Material.MYCELIUM
               || t == Material.MUD || t == Material.MOSS_BLOCK || t == Material.DIRT_PATH
               || n.contains("LOG") || n.contains("WOOD") || n.contains("LEAVES")
               || this.plugin.mats.getInfectedTypes().contains(t); // тьма поглощает и бледный лес
      }

      if (!infectable) return false;

      boolean isTree = n.contains("LOG") || n.contains("WOOD");
      this.infectBlock(b);
      if (isTree) this.darkenTreeUpwards(b);

      if (this.random.nextInt(20) == 0) {
         try {
            b.getWorld().spawnParticle(Particle.SCULK_SOUL, b.getLocation().add(0.5, 1.1, 0.5), 3, 0.3, 0.2, 0.3, 0.01);
            b.getWorld().playSound(b.getLocation(), Sound.BLOCK_SCULK_SPREAD, 0.4F, 0.7F);
         } catch (Throwable ignored) {}
      }
      return true;
   }

   /** Ствол дерева чернеет вверх целиком (как у бледного леса). */
   private void darkenTreeUpwards(Block base) {
      Block cur = base.getRelative(BlockFace.UP);
      for (int i = 0; i < 12; i++) {
         Material t = cur.getType();
         String n = t.name();
         if (t == DARK_BLOCK || (!n.contains("LOG") && !n.contains("WOOD") && !n.contains("LEAVES"))) break;
         this.infectBlock(cur);
         cur = cur.getRelative(BlockFace.UP);
      }
   }

   /** Редкие ветки тьмы: вертикальный столб до 10 блоков. Веткой может стать не каждый блок. */
   /** Редкие ветки тьмы: столб до 30 блоков; с 10-го блока раз в 2 блока пускает боковые отростки. */
   private void tryGrowBranchOnce() {
      Block base = this.pickRandomDarkBlock();
      if (base == null) return;
      // «Очень редкие ветки»: только ~1 из 25 позиций вообще может расти вверх (детерминированно по координатам)
      int h = base.getX() * 73856093 ^ base.getZ() * 19349663;
      if (Math.floorMod(h, 25) != 0) return;

      // Считаем высоту уже выросшей ветки над «землёй»
      World w = base.getWorld();
      int height = 0;
      Block below = base.getRelative(BlockFace.DOWN);
      while (height < BRANCH_MAX_HEIGHT + 2 && this.isDarkness(w, below.getX(), below.getY(), below.getZ())) {
         height++;
         below = below.getRelative(BlockFace.DOWN);
      }
      if (height >= BRANCH_MAX_HEIGHT) return;

      Block above = base.getRelative(BlockFace.UP);
      if (!above.getType().isAir()) return;
      this.infectBlock(above);

      // Высота нового блока в столбе (1 = у земли): блоки под ним + база + он сам
      int idx = height + 2;
      if (idx >= BRANCH_SIDE_START && (idx - BRANCH_SIDE_START) % 2 == 0) {
         // Боковой отросток: 1 блок в случайную сторону
         BlockFace side = switch (this.random.nextInt(4)) {
            case 0 -> BlockFace.NORTH;
            case 1 -> BlockFace.SOUTH;
            case 2 -> BlockFace.EAST;
            default -> BlockFace.WEST;
         };
         Block arm = above.getRelative(side);
         if (arm.getType().isAir()) {
            this.infectBlock(arm);
         }
      }

      try {
         w.spawnParticle(Particle.SCULK_CHARGE_POP, above.getLocation().add(0.5, 0.5, 0.5), 5, 0.2, 0.3, 0.2, 0.02);
         w.playSound(above.getLocation(), Sound.BLOCK_SCULK_CATALYST_BLOOM, 0.5F, 0.6F);
      } catch (Throwable ignored) {}
   }

   /** Случайный существующий блок тьмы (в загруженном чанке), с ленивой чисткой устаревших записей. */
   private Block pickRandomDarkBlock() {
      List<World> worlds = Bukkit.getWorlds();
      if (worlds.isEmpty()) return null;
      World w = worlds.get(this.random.nextInt(worlds.size()));
      ArrayList<Long> list = this.blockList.get(w.getUID());
      Set<Long> set = this.blocks.get(w.getUID());
      if (list == null || list.isEmpty() || set == null || set.isEmpty()) return null;

      for (int tries = 0; tries < 6; tries++) {
         int idx = this.random.nextInt(list.size());
         long k = list.get(idx);
         if (!set.contains(k)) {
            // устаревшая запись (очищено святой водой/сломано) — выкидываем свапом с концом
            int last = list.size() - 1;
            list.set(idx, list.get(last));
            list.remove(last);
            if (list.isEmpty()) return null;
            continue;
         }
         int x = keyX(k);
         int y = keyY(k);
         int z = keyZ(k);
         if (!w.isChunkLoaded(x >> 4, z >> 4)) continue;
         Block b = w.getBlockAt(x, y, z);
         if (b.getType() != DARK_BLOCK) {
            // блок заменили не мы — забываем
            set.remove(k);
            this.dirty = true;
            continue;
         }
         return b;
      }
      return null;
   }

   // ==================== ОЧИЩЕНИЕ (святая вода) ====================

   /** Очистить тьму в радиусе (для святой воды). Возвращает число очищенных блоков. */
   public int cleanse(Location center, int radius) {
      if (center == null || center.getWorld() == null) return 0;
      World w = center.getWorld();
      Set<Long> set = this.blocks.get(w.getUID());
      if (set == null || set.isEmpty()) return 0;
      int cx = center.getBlockX();
      int cy = center.getBlockY();
      int cz = center.getBlockZ();
      int r2 = radius * radius;
      int cleaned = 0;
      for (int x = cx - radius; x <= cx + radius; x++) {
         for (int y = Math.max(w.getMinHeight(), cy - radius); y <= Math.min(w.getMaxHeight() - 1, cy + radius); y++) {
            for (int z = cz - radius; z <= cz + radius; z++) {
               int ddx = x - cx;
               int ddy = y - cy;
               int ddz = z - cz;
               if (ddx * ddx + ddy * ddy + ddz * ddz > r2) continue;
               if (!set.contains(key(x, y, z))) continue;
               Block b = w.getBlockAt(x, y, z);
               if (b.getType() == DARK_BLOCK) {
                  // Верхний блок — в землю с травой, остальное — в воздух (ветки) или землю
                  boolean exposedTop = b.getRelative(BlockFace.UP).getType().isAir();
                  boolean hasSolidBelow = b.getRelative(BlockFace.DOWN).getType().isSolid();
                  if (!hasSolidBelow) b.setType(Material.AIR, false);         // висящая ветка — рассыпается
                  else b.setType(exposedTop ? Material.GRASS_BLOCK : Material.DIRT, false);
               }
               set.remove(key(x, y, z));
               cleaned++;
            }
         }
      }
      if (cleaned > 0) {
         this.dirty = true;
         try {
            w.spawnParticle(Particle.END_ROD, center.clone().add(0, 1, 0), 20, radius * 0.5, 1.0, radius * 0.5, 0.02);
         } catch (Throwable ignored) {}
      }
      return cleaned;
   }

   /** Очистить ВСЮ тьму в квадрате радиусом radiusChunks чанков вокруг чанка (ccx,ccz). Для админ-жезла. */
   public int cleanseChunks(World w, int ccx, int ccz, int radiusChunks) {
      Set<Long> set = this.blocks.get(w.getUID());
      if (set == null || set.isEmpty()) return 0;

      // Собираем цели
      List<Long> targets = new ArrayList<>();
      for (long k : set) {
         int x = keyX(k);
         int z = keyZ(k);
         if (Math.abs((x >> 4) - ccx) <= radiusChunks && Math.abs((z >> 4) - ccz) <= radiusChunks) {
            targets.add(k);
         }
      }
      if (targets.isEmpty()) return 0;

      Set<Long> targetSet = new HashSet<>(targets);
      // Сверху вниз: части веток над другими блоками тьмы рассыпаются в воздух, низ — в землю
      targets.sort((a, b) -> Integer.compare(keyY(b), keyY(a)));

      int cleaned = 0;
      for (long k : targets) {
         int x = keyX(k);
         int y = keyY(k);
         int z = keyZ(k);
         Block b = w.getBlockAt(x, y, z);
         if (b.getType() == DARK_BLOCK) {
            boolean belowIsDark = targetSet.contains(key(x, y - 1, z));
            boolean hasSolidBelow = b.getRelative(BlockFace.DOWN).getType().isSolid();
            if (belowIsDark || !hasSolidBelow) {
               b.setType(Material.AIR, false); // столб ветки / висящий блок — рассыпается
            } else {
               boolean exposedTop = b.getRelative(BlockFace.UP).getType().isAir();
               b.setType(exposedTop ? Material.GRASS_BLOCK : Material.DIRT, false);
            }
         }
         set.remove(k);
         cleaned++;
      }
      this.dirty = true;
      return cleaned;
   }

   // ==================== ЭФФЕКТЫ ОТ СТОЯНИЯ НА ТЬМЕ ====================

   /** Ключи LeperClass в PDC игрока (softdepend — читаем напрямую). */
   private static final org.bukkit.NamespacedKey LEPER_CLASS_KEY = org.bukkit.NamespacedKey.fromString("leperclass:class_leper");
   private static final org.bukkit.NamespacedKey LEPER_STAGE_KEY = org.bukkit.NamespacedKey.fromString("leperclass:infection_stage");

   /** Тьма — стихия прокажённых: полноценный прокажённый или заражённый 3-4 стадии. */
   public boolean isDarknessKin(Player p) {
      try {
         var pdc = p.getPersistentDataContainer();
         if (LEPER_CLASS_KEY != null) {
            Byte v = pdc.get(LEPER_CLASS_KEY, PersistentDataType.BYTE);
            if (v != null && v == 1) return true;
         }
         if (LEPER_STAGE_KEY != null) {
            Integer st = pdc.get(LEPER_STAGE_KEY, PersistentDataType.INTEGER);
            if (st != null && st >= 3) return true;
         }
      } catch (Throwable ignored) {}
      return false;
   }

   private void tickStepEffects() {
      if (!this.enabled) return;
      long now = System.currentTimeMillis();
      for (Player p : Bukkit.getOnlinePlayers()) {
         if (p.isDead()) continue;
         Location loc = p.getLocation();
         World w = p.getWorld();
         Block under = w.getBlockAt(loc.getBlockX(), loc.getBlockY() - 1, loc.getBlockZ());
         Block in = w.getBlockAt(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
         boolean standing = (under.getType() == DARK_BLOCK && this.isDarkness(w, under.getX(), under.getY(), under.getZ()))
               || (in.getType() == DARK_BLOCK && this.isDarkness(w, in.getX(), in.getY(), in.getZ()));
         if (!standing) continue;

         // Тьма — стихия прокажённых: 3-4 стадия и полноценные прокажённые не страдают, а РЕГЕНЕРИРУЮТ
         if (this.isDarknessKin(p)) {
            p.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 4 * 20, 0, true, false, true));
            if (this.random.nextInt(6) == 0) {
               try {
                  w.spawnParticle(Particle.SCULK_SOUL, loc.clone().add(0, 0.3, 0), 3, 0.25, 0.1, 0.25, 0.01);
               } catch (Throwable ignored) {}
            }
            continue;
         }

         if (p.hasPermission("pale.effect.immune")) continue;

         // 4 сердца (8 HP) за 5 секунд = 1.6 урона каждую секунду
         Long last = this.damageCooldown.get(p.getUniqueId());
         if (last == null || now - last >= 1000L) {
            this.damageCooldown.put(p.getUniqueId(), now);
            p.damage(1.6);
         }

         // Букет тьмы: слепота 10с, тьма 30с, замедление II, иссушение 2с, тошнота 15с
         p.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 10 * 20, 0, true, false, true));
         p.addPotionEffect(new PotionEffect(PotionEffectType.DARKNESS, 30 * 20, 0, true, false, true));
         p.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 10 * 20, 1, true, false, true));
         p.addPotionEffect(new PotionEffect(PotionEffectType.WITHER, 2 * 20, 0, true, false, true));
         p.addPotionEffect(new PotionEffect(PotionEffectType.NAUSEA, 15 * 20, 0, true, false, true));
         if (this.random.nextInt(4) == 0) {
            try {
               w.spawnParticle(Particle.SCULK_SOUL, loc.add(0, 0.3, 0), 4, 0.25, 0.1, 0.25, 0.02);
            } catch (Throwable ignored) {}
         }
      }
   }

   // ==================== СОБЫТИЯ ====================

   /** Установка «Блока Тьмы» из предмета — только владелец может разносить тьму. */
   @EventHandler
   public void onDarkBlockPlace(BlockPlaceEvent e) {
      var item = e.getItemInHand();
      if (item == null || !item.hasItemMeta()) return;
      var pdc = item.getItemMeta().getPersistentDataContainer();
      if (!pdc.has(this.plugin.keys.KEY_DARK_BLOCK, PersistentDataType.BYTE)) return;

      // Тьму может разносить только тот, кому она была выдана
      String owner = pdc.get(this.plugin.keys.KEY_DARK_BLOCK_OWNER, PersistentDataType.STRING);
      if (owner != null && !owner.isEmpty() && !owner.equals(e.getPlayer().getUniqueId().toString())) {
         e.setCancelled(true);
         e.getPlayer().sendMessage("§8☠ §7Тьма не признаёт тебя хозяином — блок отказывается подчиняться.");
         try {
            e.getPlayer().playSound(e.getPlayer().getLocation(), Sound.ENTITY_ELDER_GUARDIAN_CURSE, 0.5F, 1.6F);
         } catch (Throwable ignored) {}
         return;
      }

      Block b = e.getBlockPlaced();
      if (b.getType() != DARK_BLOCK) b.setType(DARK_BLOCK, false);
      this.register(b.getWorld(), b.getX(), b.getY(), b.getZ());
      try {
         b.getWorld().playSound(b.getLocation(), Sound.BLOCK_SCULK_CATALYST_BLOOM, 1.0F, 0.5F);
         b.getWorld().spawnParticle(Particle.SCULK_SOUL, b.getLocation().add(0.5, 1.0, 0.5), 15, 0.4, 0.4, 0.4, 0.05);
      } catch (Throwable ignored) {}
      e.getPlayer().sendMessage("§8☠ §7Тьма пробудилась и начинает расползаться...");
   }

   /** Поднял Блок Тьмы — получи: отравление, тьма, тошнота (владелец и прокажённые не страдают). */
   @EventHandler
   public void onDarkBlockPickup(org.bukkit.event.entity.EntityPickupItemEvent e) {
      if (!(e.getEntity() instanceof Player p)) return;
      var item = e.getItem().getItemStack();
      if (item == null || !item.hasItemMeta()) return;
      var pdc = item.getItemMeta().getPersistentDataContainer();
      if (!pdc.has(this.plugin.keys.KEY_DARK_BLOCK, PersistentDataType.BYTE)) return;

      String owner = pdc.get(this.plugin.keys.KEY_DARK_BLOCK_OWNER, PersistentDataType.STRING);
      boolean isOwner = owner != null && owner.equals(p.getUniqueId().toString());
      if (isOwner || this.isDarknessKin(p) || p.hasPermission("pale.effect.immune")) return;

      p.addPotionEffect(new PotionEffect(PotionEffectType.POISON, 8 * 20, 0, true, false, true));
      p.addPotionEffect(new PotionEffect(PotionEffectType.DARKNESS, 15 * 20, 0, true, false, true));
      p.addPotionEffect(new PotionEffect(PotionEffectType.NAUSEA, 15 * 20, 0, true, false, true));
      p.sendMessage("§8☠ §7Чужая тьма жжёт руки...");
      try {
         p.playSound(p.getLocation(), Sound.ENTITY_ELDER_GUARDIAN_CURSE, 0.4F, 1.8F);
      } catch (Throwable ignored) {}
   }

   /** Сломанный блок тьмы забываем (без дропа скалка). */
   @EventHandler
   public void onDarkBlockBreak(BlockBreakEvent e) {
      Block b = e.getBlock();
      if (b.getType() != DARK_BLOCK) return;
      if (this.isDarkness(b.getWorld(), b.getX(), b.getY(), b.getZ())) {
         this.unregister(b.getWorld(), b.getX(), b.getY(), b.getZ());
         e.setDropItems(false);
      }
   }

   // ==================== КАРТА ТЬМЫ ====================

   /** Чат-карта очагов тьмы по чанкам (аналог карты заражения). */
   public void sendDarkMap(Player p, int radiusChunks) {
      World w = p.getWorld();
      Set<Long> set = this.blocks.get(w.getUID());
      int cx = p.getLocation().getBlockX() >> 4;
      int cz = p.getLocation().getBlockZ() >> 4;

      // Подсчёт блоков тьмы по чанкам в квадрате радиуса
      Map<Long, Integer> counts = new HashMap<>();
      if (set != null) {
         for (long k : set) {
            int bx = keyX(k);
            int bz = keyZ(k);
            int chx = bx >> 4;
            int chz = bz >> 4;
            if (Math.abs(chx - cx) > radiusChunks || Math.abs(chz - cz) > radiusChunks) continue;
            counts.merge(((long)chx << 32) ^ (chz & 0xFFFFFFFFL), 1, Integer::sum);
         }
      }

      int max = 0;
      for (int v : counts.values()) max = Math.max(max, v);

      p.sendMessage(org.bukkit.ChatColor.DARK_GRAY + "☠ " + org.bukkit.ChatColor.GRAY + "Карта ТЬМЫ (0..9), r=" + radiusChunks + " чанков, max=" + max + " бл./чанк");

      for (int dz = -radiusChunks; dz <= radiusChunks; dz++) {
         StringBuilder line = new StringBuilder();
         for (int dx = -radiusChunks; dx <= radiusChunks; dx++) {
            if (dx == 0 && dz == 0) {
               line.append(org.bukkit.ChatColor.WHITE).append('X');
               continue;
            }
            int count = counts.getOrDefault(((long)(cx + dx) << 32) ^ ((cz + dz) & 0xFFFFFFFFL), 0);
            int level = max <= 0 ? 0 : (int)Math.round((double)count / (double)max * 9.0);
            level = MathUtil.clamp(level, 0, 9);
            org.bukkit.ChatColor c;
            if (level == 0) c = org.bukkit.ChatColor.DARK_GREEN;
            else if (level <= 3) c = org.bukkit.ChatColor.GRAY;
            else if (level <= 6) c = org.bukkit.ChatColor.DARK_GRAY;
            else c = org.bukkit.ChatColor.DARK_RED;
            line.append(c).append((char)(48 + level));
         }
         line.append(org.bukkit.ChatColor.RESET);
         p.sendMessage(line.toString());
      }

      p.sendMessage(org.bukkit.ChatColor.DARK_GRAY + "Легенда: 0=чисто, 9=сплошная тьма, X=ты. Всего в мире: " + (set == null ? 0 : set.size()) + " бл.");
   }

   // ==================== ПЕРСИСТЕНТНОСТЬ (darkness.yml) ====================

   private File file() {
      return new File(this.plugin.getDataFolder(), "darkness.yml");
   }

   public void saveToDisk(boolean async) {
      if (!this.dirty && this.file().exists()) return;
      this.dirty = false;
      YamlConfiguration yml = new YamlConfiguration();
      for (Map.Entry<UUID, Set<Long>> en : this.blocks.entrySet()) {
         List<String> lines = new ArrayList<>(en.getValue().size());
         for (Long k : en.getValue()) lines.add(Long.toString(k));
         yml.set(en.getKey().toString(), lines);
      }
      String data = yml.saveToString();
      Runnable write = () -> {
         try {
            this.plugin.getDataFolder().mkdirs();
            Files.writeString(this.file().toPath(), data);
         } catch (Exception ex) {
            this.plugin.getLogger().warning("[Darkness] Не удалось сохранить darkness.yml: " + ex.getMessage());
         }
      };
      if (async) Bukkit.getScheduler().runTaskAsynchronously(this.plugin, write);
      else write.run();
   }

   private void loadFromDisk() {
      File f = this.file();
      if (!f.exists()) return;
      try {
         YamlConfiguration yml = YamlConfiguration.loadConfiguration(f);
         int n = 0;
         for (String worldKey : yml.getKeys(false)) {
            try {
               UUID wid = UUID.fromString(worldKey);
               Set<Long> set = this.blocks.computeIfAbsent(wid, u -> new HashSet<>());
               ArrayList<Long> list = this.blockList.computeIfAbsent(wid, u -> new ArrayList<>());
               for (String s : yml.getStringList(worldKey)) {
                  try {
                     long k = Long.parseLong(s);
                     if (set.add(k)) list.add(k);
                     n++;
                  } catch (NumberFormatException ignored) {}
               }
            } catch (IllegalArgumentException ignored) {}
         }
         if (n > 0) this.plugin.getLogger().info("[Darkness] Загружено блоков тьмы: " + n);
      } catch (Exception ex) {
         this.plugin.getLogger().warning("[Darkness] Не удалось прочитать darkness.yml: " + ex.getMessage());
      }
   }
}
