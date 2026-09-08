package org.examplee.palePlugin.engine;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.Registry;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.examplee.palePlugin.core.PaleConfig;
import org.examplee.palePlugin.core.PaleMaterials;
import org.examplee.palePlugin.store.BiomeStore;
import org.examplee.palePlugin.store.GreaterWardStore;
import org.examplee.palePlugin.store.SourceStore;
import org.examplee.palePlugin.store.WardStore;
import org.examplee.palePlugin.util.BiomeUtil;
import org.examplee.palePlugin.util.MathUtil;

public final class PaleEngine {
   private final JavaPlugin plugin;
   public final PaleConfig cfg;
   public final PaleMaterials mats;
   private final Random random = new Random();
   private final Map<UUID, SourceStore> sourcesByWorld = new HashMap<>();
   private final Map<UUID, WardStore> wardsByWorld = new HashMap<>();
   private final Map<UUID, GreaterWardStore> greaterWardsByWorld = new HashMap<>();
   private final Map<UUID, BiomeStore> biomeStoreByWorld = new HashMap<>();
   private NamespacedKey infectedBiomeKey;
   private Biome infectedBiome;

   public PaleEngine(JavaPlugin plugin, PaleConfig cfg, PaleMaterials mats) {
      this.plugin = plugin;
      this.cfg = cfg;
      this.mats = mats;
   }

   public Set<Material> infectedTypes() {
      return this.mats.getInfectedTypes();
   }

   public SourceStore sources(World w) {
      return this.sourcesByWorld.computeIfAbsent(w.getUID(), k -> new SourceStore());
   }

   public WardStore wards(World w) {
      return this.wardsByWorld.computeIfAbsent(w.getUID(), k -> new WardStore());
   }

   public GreaterWardStore greaterWards(World w) {
      return this.greaterWardsByWorld.computeIfAbsent(w.getUID(), k -> new GreaterWardStore());
   }

   public Map<UUID, GreaterWardStore> greaterWardsByWorld() {
      return this.greaterWardsByWorld;
   }

   public boolean isWardProtected(World w, int x, int y, int z) {
      return this.wards(w).isProtected(x, y, z, this.cfg.wardRadius)
         || this.greaterWards(w).isProtected(x, y, z, this.cfg.greaterWardRadius);
   }

   public BiomeStore biomes(World w) {
      return this.biomeStoreByWorld.computeIfAbsent(w.getUID(), k -> new BiomeStore());
   }

   public Map<UUID, WardStore> wardsByWorld() {
      return this.wardsByWorld;
   }

   public Map<UUID, BiomeStore> biomesByWorld() {
      return this.biomeStoreByWorld;
   }

   public void resolveInfectedBiomeOrDisableBiome() {
      this.infectedBiome = null;
      this.infectedBiomeKey = null;
      if (this.cfg.biomeEnabled) {
         this.infectedBiomeKey = BiomeUtil.parseBiomeKey(this.cfg.infectedBiomeName);
         if (this.infectedBiomeKey == null) {
            this.cfg.biomeEnabled = false;
            this.plugin.getLogger().warning("[PalePlugin] biome.infected неверный: " + this.cfg.infectedBiomeName + ". Биомы отключены.");
         } else {
            try {
               this.infectedBiome = (Biome)Registry.BIOME.get(this.infectedBiomeKey);
            } catch (Throwable var2) {
               this.infectedBiome = null;
            }

            if (this.infectedBiome == null) {
               this.cfg.biomeEnabled = false;
               this.plugin.getLogger().warning("[PalePlugin] Биом не найден: " + this.infectedBiomeKey + ". Биомы отключены.");
            }
         }
      }
   }

   public int apiInfect(Location center, int radius, int maxBlocks) {
      return this.infectAreaExternal(center, radius, maxBlocks);
   }

   public int infectAreaExternal(Location center, int radius, int maxBlocks) {
      World world = center == null ? null : center.getWorld();
      if (world == null) {
         return 0;
      } else {
         radius = MathUtil.clamp(radius, 1, 64);
         maxBlocks = MathUtil.clamp(maxBlocks, 10, 200000);
         int cx = center.getBlockX();
         int cy = center.getBlockY();
         int cz = center.getBlockZ();
         int rSq = radius * radius;
         SourceStore store = this.sources(world);
         WardStore wards = this.wards(world);
         BiomeStore bs = this.biomes(world);
         int infected = 0;
         int processed = 0;

         for (int x = -radius; x <= radius; x++) {
            int xx = x * x;

            for (int y = -radius; y <= radius; y++) {
               int xxyy = xx + y * y;

               for (int z = -radius; z <= radius; z++) {
                  if (xxyy + z * z <= rSq) {
                     if (processed++ >= maxBlocks) {
                        return infected;
                     }

                     Block b = world.getBlockAt(cx + x, cy + y, cz + z);
                     if (!this.isWardProtected(world, b.getX(), b.getY(), b.getZ()) && this.tryInfect(b, store, bs)) {
                        infected++;
                     }
                  }
               }
            }
         }

         return infected;
      }
   }

   public int infectAreaWand(Location center) {
      World world = center.getWorld();
      if (world == null) {
         return 0;
      } else {
         int radius = this.cfg.infectWandRadius;
         int maxBlocks = this.cfg.infectWandMaxBlocksPerUse;
         int cx = center.getBlockX();
         int cy = center.getBlockY();
         int cz = center.getBlockZ();
         int rSq = radius * radius;
         SourceStore store = this.sources(world);
         WardStore wards = this.wards(world);
         BiomeStore bs = this.biomes(world);
         int infected = 0;
         int processed = 0;

         for (int x = -radius; x <= radius; x++) {
            int xx = x * x;

            for (int y = -radius; y <= radius; y++) {
               int xxyy = xx + y * y;

               for (int z = -radius; z <= radius; z++) {
                  if (xxyy + z * z <= rSq) {
                     if (processed++ >= maxBlocks) {
                        return infected;
                     }

                     Block b = world.getBlockAt(cx + x, cy + y, cz + z);
                     if (!this.isWardProtected(world, b.getX(), b.getY(), b.getZ()) && this.tryInfect(b, store, bs)) {
                        infected++;
                        if (store.size() < this.cfg.maxSourcesPerWorld
                           && this.cfg.infectWandBonusSourceChanceDivider > 0
                           && this.random.nextInt(this.cfg.infectWandBonusSourceChanceDivider) == 0) {
                           store.add(b.getX(), b.getY(), b.getZ());
                        }
                     }
                  }
               }
            }
         }

         return infected;
      }
   }

   public int cleanse(Location center, int radius) {
      World world = center.getWorld();
      if (world == null) {
         return 0;
      } else {
         int cx = center.getBlockX();
         int cy = center.getBlockY();
         int cz = center.getBlockZ();
         int rSq = radius * radius;
         SourceStore store = this.sources(world);
         BiomeStore bs = this.biomeStoreByWorld.get(world.getUID());
         int cleaned = 0;
         HashSet<Long> touchedCells = new HashSet<>();
         HashSet<Long> touchedChunks = new HashSet<>();

         for (int x = -radius; x <= radius; x++) {
            int xx = x * x;

            for (int y = -radius; y <= radius; y++) {
               int xxyy = xx + y * y;

               for (int z = -radius; z <= radius; z++) {
                  if (xxyy + z * z <= rSq) {
                     Block b = world.getBlockAt(cx + x, cy + y, cz + z);
                     Material t = b.getType();
                     if (this.infectedTypes().contains(t)) {
                        touchedChunks.add(SourceStore.packChunk(b.getX() >> 4, b.getZ() >> 4));
                        if (bs != null) {
                           touchedCells.add(bs.cellKeyFromBlock(b.getX(), b.getY(), b.getZ()));
                        }

                        cleaned += this.cleanseSingleBlock(b, store);
                     }
                  }
               }
            }
         }

         if (this.cfg.biomeEnabled && this.infectedBiome != null && bs != null && !touchedCells.isEmpty()) {
            bs.restoreCellsIfClean(world, touchedCells, this.infectedTypes(), this.infectedBiome);
         }

         for (long ck : touchedChunks) {
            int ccx = (int)(ck >> 32);
            int ccz = (int)ck;

            try {
               world.refreshChunk(ccx, ccz);
            } catch (Exception var20) {
            }
         }

         return cleaned;
      }
   }

   public int cleanseSingleBlock(Block b, SourceStore store) {
      Material t = b.getType();
      if (!this.infectedTypes().contains(t)) {
         return 0;
      } else {
         String n = t.name();
         if (n.contains("LOG") || n.contains("WOOD") || n.contains("LEAVES")) {
            b.setType(Material.AIR, false);
         } else if (n.contains("MOSS")) {
            if (n.contains("CARPET")) {
               b.setType(Material.AIR, false);
            } else {
               b.setType(Material.DIRT, false);
            }
         } else {
            b.setType(Material.AIR, false);
         }

         store.remove(b.getX(), b.getY(), b.getZ());
         return 1;
      }
   }

   public boolean tryInfect(Block b, SourceStore store, BiomeStore bs) {
      Material t = b.getType();
      if (t.isAir()) {
         return false;
      } else if (this.infectedTypes().contains(t)) {
         return false;
      } else {
         Material newType = null;
         String n = t.name();
         if (n.contains("LOG")) {
            newType = this.mats.PALE_LOG;
         } else if (n.contains("WOOD")) {
            newType = this.mats.PALE_WOOD;
         } else if (n.contains("LEAVES")) {
            newType = this.mats.PALE_LEAVES;
         } else if ((t == Material.GRASS_BLOCK || t == Material.DIRT || t == Material.MOSS_BLOCK) && this.random.nextInt(10) == 0) {
            newType = this.mats.PALE_MOSS_BLOCK;
         }

         if (newType == null) {
            return false;
         } else {
            b.setType(newType, false);
            this.applyInfectedBiome(b, bs);
            if (newType == this.mats.PALE_LOG) {
               this.infectLogUpwards(b, bs);
            }

            if (store.size() < this.cfg.maxSourcesPerWorld) {
               boolean isWood = newType == this.mats.PALE_LOG || newType == this.mats.PALE_WOOD;
               boolean makeSource = isWood && this.random.nextInt(this.cfg.logSourceChanceDivider) == 0
                  || !isWood && this.random.nextInt(this.cfg.sourceChanceDivider) == 0;
               if (makeSource) {
                  store.add(b.getX(), b.getY(), b.getZ());
               }
            }

            return true;
         }
      }
   }

   private void infectLogUpwards(Block base, BiomeStore bs) {
      Block cur = base.getRelative(BlockFace.UP);

      for (int i = 0; i < 12; i++) {
         Material t = cur.getType();
         if (t.isAir()) {
            break;
         }

         String n = t.name();
         if (!n.contains("LOG") || this.infectedTypes().contains(t)) {
            break;
         }

         cur.setType(this.mats.PALE_LOG, false);
         this.applyInfectedBiome(cur, bs);
         cur = cur.getRelative(BlockFace.UP);
      }
   }

   private void applyInfectedBiome(Block b, BiomeStore bs) {
      if (this.cfg.biomeEnabled && this.infectedBiome != null) {
         try {
            Biome cur = b.getBiome();
            if (!cur.equals(this.infectedBiome)) {
               long cellKey = bs.cellKeyFromBlock(b.getX(), b.getY(), b.getZ());
               if (!bs.contains(cellKey)) {
                  NamespacedKey oldKey = BiomeUtil.biomeKeyOf(cur);
                  if (oldKey != null && !oldKey.equals(this.infectedBiomeKey)) {
                     bs.put(cellKey, oldKey);
                  }
               }

               b.setBiome(this.infectedBiome);
            }
         } catch (Exception var7) {
         }
      }
   }

   public void indexChunkSurface(World world, int chunkX, int chunkZ) {
      int baseX = chunkX << 4;
      int baseZ = chunkZ << 4;
      SourceStore store = this.sources(world);

      for (int lx = 0; lx < 16; lx++) {
         for (int lz = 0; lz < 16; lz++) {
            int wx = baseX + lx;
            int wz = baseZ + lz;
            int topY = world.getHighestBlockYAt(wx, wz);

            for (int dy = 0; dy < this.cfg.indexDepth; dy++) {
               int y = topY - dy;
               if (y < world.getMinHeight()) {
                  break;
               }

               Block b = world.getBlockAt(wx, y, wz);
               if (this.infectedTypes().contains(b.getType())) {
                  store.add(wx, y, wz);
               }
            }
         }
      }
   }

   public boolean trySpreadFromSource(Block source) {
      SourceStore store = this.sources(source.getWorld());
      WardStore wards = this.wards(source.getWorld());
      BiomeStore bs = this.biomes(source.getWorld());
      int jumpRadius = 4;
      int jumpUpDown = 2;
      int probeDown = 6;
      int tries = this.cfg.spreadTriesPerAttempt;

      for (int attempt = 0; attempt < tries; attempt++) {
         int dx = this.random.nextInt(9) - 4;
         int dz = this.random.nextInt(9) - 4;
         int dy = this.random.nextInt(5) - 2;
         if (dx != 0 || dy != 0 || dz != 0) {
            Block target = source.getRelative(dx, dy, dz);
            if (target.getType().isAir()) {
               Block t = target;

               for (int i = 0; i < 6; i++) {
                  Block down = t.getRelative(BlockFace.DOWN);
                  if (down.getY() <= down.getWorld().getMinHeight()) {
                     break;
                  }

                  t = down;
                  if (!down.getType().isAir()) {
                     break;
                  }
               }

               target = t;
            }

            if (!this.isWardProtected(source.getWorld(), target.getX(), target.getY(), target.getZ())) {
               int bonus = this.hasInfectedNear(target) ? 2 : 0;

               for (int i = 0; i < 1 + bonus; i++) {
                  if (this.tryInfect(target, store, bs)) {
                     if (this.random.nextInt(3) == 0) {
                        source.getWorld().spawnParticle(Particle.SPORE_BLOSSOM_AIR, target.getLocation().add(0.5, 0.5, 0.5), 4, 0.3, 0.3, 0.3, 0.01);
                     }

                     return true;
                  }
               }
            }
         }
      }

      return false;
   }

   public int getChunkStage(World w, int chunkX, int chunkZ) {
      if (!this.cfg.stagesEnabled) {
         return 0;
      } else {
         int c = this.sources(w).getChunkCount(chunkX, chunkZ);
         int stage = 0;
         if (c >= this.cfg.stage1Sources) {
            stage = 1;
         }

         if (c >= this.cfg.stage2Sources) {
            stage = 2;
         }

         if (c >= this.cfg.stage3Sources) {
            stage = 3;
         }

         if (c >= this.cfg.stage4Sources) {
            stage = 4;
         }

         if (c >= this.cfg.stage5Sources) {
            stage = 5;
         }

         return stage;
      }
   }

   public void sendMap(Player p, int radiusChunks) {
      World w = p.getWorld();
      SourceStore store = this.sources(w);
      int cx = p.getLocation().getBlockX() >> 4;
      int cz = p.getLocation().getBlockZ() >> 4;
      int stageHere = this.getChunkStage(w, cx, cz);
      int max = 0;

      for (int dz = -radiusChunks; dz <= radiusChunks; dz++) {
         for (int dx = -radiusChunks; dx <= radiusChunks; dx++) {
            max = Math.max(max, store.getChunkCount(cx + dx, cz + dz));
         }
      }

      p.sendMessage(ChatColor.GRAY + "[Pale] Карта заражения (0..9), r=" + radiusChunks + " чанков, max=" + max + ", stage=" + stageHere);

      for (int dz = -radiusChunks; dz <= radiusChunks; dz++) {
         StringBuilder line = new StringBuilder();

         for (int dx = -radiusChunks; dx <= radiusChunks; dx++) {
            int count = store.getChunkCount(cx + dx, cz + dz);
            if (dx == 0 && dz == 0) {
               line.append(ChatColor.WHITE).append('X');
            } else {
               int level = max <= 0 ? 0 : (int)Math.round((double)count / (double)max * 9.0);
               level = MathUtil.clamp(level, 0, 9);
               line.append(this.mapColorForLevel(level)).append((char)(48 + level));
            }
         }

         line.append(ChatColor.RESET);
         p.sendMessage(line.toString());
      }

      p.sendMessage(ChatColor.DARK_GRAY + "Легенда: 0=нет, 9=максимум, X=ты.");
   }

   private String rgb(String hex) {
      try {
         return net.md_5.bungee.api.ChatColor.of(hex).toString();
      } catch (Throwable var3) {
         return ChatColor.GREEN.toString();
      }
   }

   private String mapColorForLevel(int level) {
      if (level <= 0) {
         return this.rgb("#1a1a1a");
      } else if (level <= 2) {
         return this.rgb("#145214");
      } else if (level <= 4) {
         return this.rgb("#1aff1a");
      } else if (level <= 6) {
         return this.rgb("#39FF14");
      } else {
         return level <= 8 ? this.rgb("#66ff00") : this.rgb("#ccff00");
      }
   }

   public boolean hasInfectedNear(Block b) {
      for (int dx = -1; dx <= 1; dx++) {
         for (int dy = -1; dy <= 1; dy++) {
            for (int dz = -1; dz <= 1; dz++) {
               if ((dx != 0 || dy != 0 || dz != 0) && this.infectedTypes().contains(b.getRelative(dx, dy, dz).getType())) {
                  return true;
               }
            }
         }
      }

      return false;
   }

   public int purgeChunkSurface(World world, int chunkX, int chunkZ, int depth) {
      int baseX = chunkX << 4;
      int baseZ = chunkZ << 4;
      int minY = world.getMinHeight();
      int cleaned = 0;
      SourceStore store = this.sources(world);
      BiomeStore bs = this.biomeStoreByWorld.get(world.getUID());
      HashSet<Long> touchedCells = new HashSet<>();

      for (int lx = 0; lx < 16; lx++) {
         for (int lz = 0; lz < 16; lz++) {
            int wx = baseX + lx;
            int wz = baseZ + lz;
            int topY = world.getHighestBlockYAt(wx, wz);

            for (int dy = 0; dy < depth; dy++) {
               int y = topY - dy;
               if (y < minY) {
                  break;
               }

               Block b = world.getBlockAt(wx, y, wz);
               if (this.infectedTypes().contains(b.getType())) {
                  if (bs != null) {
                     touchedCells.add(bs.cellKeyFromBlock(b.getX(), b.getY(), b.getZ()));
                  }

                  cleaned += this.cleanseSingleBlock(b, store);
               }
            }
         }
      }

      if (this.cfg.biomeEnabled && this.infectedBiome != null && bs != null && !touchedCells.isEmpty()) {
         bs.restoreCellsIfClean(world, touchedCells, this.infectedTypes(), this.infectedBiome);
      }

      try {
         world.refreshChunk(chunkX, chunkZ);
      } catch (Exception var20) {
      }

      return cleaned;
   }
}
