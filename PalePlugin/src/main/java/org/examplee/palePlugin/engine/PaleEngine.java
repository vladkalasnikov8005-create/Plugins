package org.examplee.palePlugin.engine;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import net.md_5.bungee.api.ChatColor;
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
import org.examplee.palePlugin.store.SourceStore;
import org.examplee.palePlugin.store.WardStore;
import org.examplee.palePlugin.util.BiomeUtil;
import org.examplee.palePlugin.util.MathUtil;

public final class PaleEngine {
    private final org.bukkit.plugin.java.JavaPlugin plugin;
    public final org.examplee.palePlugin.core.PaleConfig cfg;
    public final org.examplee.palePlugin.core.PaleMaterials mats;
    private final java.util.Random random;
    private final java.util.Map sourcesByWorld;
    private final java.util.Map wardsByWorld;
    private final java.util.Map biomeStoreByWorld;
    private org.bukkit.NamespacedKey infectedBiomeKey;
    private org.bukkit.block.Biome infectedBiome;

    public PaleEngine(org.bukkit.plugin.java.JavaPlugin plugin, org.examplee.palePlugin.core.PaleConfig cfg, org.examplee.palePlugin.core.PaleMaterials mats) {
        super();
        this.random = new java.util.Random();
        this.sourcesByWorld = new java.util.HashMap();
        this.wardsByWorld = new java.util.HashMap();
        this.biomeStoreByWorld = new java.util.HashMap();
        this.plugin = plugin;
        this.cfg = cfg;
        this.mats = mats;
    }

    public java.util.Set infectedTypes() {
        return mats.getInfectedTypes();
    }

    public org.examplee.palePlugin.store.SourceStore sources(org.bukkit.World w) {
        return (org.examplee.palePlugin.store.SourceStore) sourcesByWorld.computeIfAbsent(w.getUID(), (java.util.UUID p0) -> org.examplee.palePlugin.engine.PaleEngine.lambda_sources_0(p0));
    }

    public org.examplee.palePlugin.store.WardStore wards(org.bukkit.World w) {
        return (org.examplee.palePlugin.store.WardStore) wardsByWorld.computeIfAbsent(w.getUID(), (java.util.UUID p0) -> org.examplee.palePlugin.engine.PaleEngine.lambda_wards_1(p0));
    }

    public org.examplee.palePlugin.store.BiomeStore biomes(org.bukkit.World w) {
        return (org.examplee.palePlugin.store.BiomeStore) biomeStoreByWorld.computeIfAbsent(w.getUID(), (java.util.UUID p0) -> org.examplee.palePlugin.engine.PaleEngine.lambda_biomes_2(p0));
    }

    public java.util.Map wardsByWorld() {
        return wardsByWorld;
    }

    public java.util.Map biomesByWorld() {
        return biomeStoreByWorld;
    }

    public void resolveInfectedBiomeOrDisableBiome() {
        this.infectedBiome = null;
        this.infectedBiomeKey = null;
        if (cfg.biomeEnabled) {
            this.infectedBiomeKey = org.examplee.palePlugin.util.BiomeUtil.parseBiomeKey(cfg.infectedBiomeName);
            if (infectedBiomeKey == null) {
                cfg.biomeEnabled = 0;
                plugin.getLogger().warning("[PalePlugin] biome.infected неверный: " + cfg.infectedBiomeName + ". Биомы отключены.");
                return;
            }
        }
        return;
    }

    public int apiInfect(org.bukkit.Location center, int radius, int maxBlocks) {
        return infectAreaExternal(center, radius, maxBlocks);
    }

    public int infectAreaExternal(org.bukkit.Location center, int radius, int maxBlocks) {
        if (center != null) {
            org.bukkit.World world = center.getWorld();
            if (world == null) {
                return 0;
            }
        }
        world = center.getWorld();
        if (world == null) {
            return 0;
        }
        int radius = org.examplee.palePlugin.util.MathUtil.clamp(radius, 1, 64);
        int maxBlocks = org.examplee.palePlugin.util.MathUtil.clamp(maxBlocks, 10, 200000);
        int cx = center.getBlockX();
        int cy = center.getBlockY();
        int cz = center.getBlockZ();
        int rSq = radius * radius;
        org.examplee.palePlugin.store.SourceStore store = sources(world);
        org.examplee.palePlugin.store.WardStore wards = wards(world);
        org.examplee.palePlugin.store.BiomeStore bs = biomes(world);
        int infected = 0;
        int processed = 0;
        int x = -radius;
        if (x <= radius) {
            int xx = x * x;
            int y = -radius;
            if (y <= radius) {
                int xxyy = xx + y * y;
                int z = -radius;
                if (z <= radius) {
                    if (xxyy + z * z > rSq) {
                    } else {
                        processed++;
                        if (processed >= maxBlocks) {
                        } else {
                            org.bukkit.block.Block b = world.getBlockAt(cx + x, cy + y, cz + z);
                            if (wards.isProtected(b.getX(), b.getY(), b.getZ(), cfg.wardRadius)) {
                            } else {
                                if (tryInfect(b, store, bs)) {
                                    infected++;
                                }
                            }
                            z++;
                            /* continue */
                            y++;
                            /* continue */
                            x++;
                            /* continue */
                        }
                    }
                    z++;
                    /* continue */
                }
                y++;
                /* continue */
            }
            x++;
            /* continue */
        }
        return infected;
    }

    public int infectAreaWand(org.bukkit.Location center) {
        org.bukkit.World world = center.getWorld();
        if (world == null) {
            return 0;
        }
        int radius = cfg.infectWandRadius;
        int maxBlocks = cfg.infectWandMaxBlocksPerUse;
        int cx = center.getBlockX();
        int cy = center.getBlockY();
        int cz = center.getBlockZ();
        int rSq = radius * radius;
        org.examplee.palePlugin.store.SourceStore store = sources(world);
        org.examplee.palePlugin.store.WardStore wards = wards(world);
        org.examplee.palePlugin.store.BiomeStore bs = biomes(world);
        int infected = 0;
        int processed = 0;
        int x = -radius;
        if (x <= radius) {
            int xx = x * x;
            int y = -radius;
            if (y <= radius) {
                int xxyy = xx + y * y;
                int z = -radius;
                if (z <= radius) {
                    if (xxyy + z * z > rSq) {
                    } else {
                        processed++;
                        if (processed >= maxBlocks) {
                        } else {
                            org.bukkit.block.Block b = world.getBlockAt(cx + x, cy + y, cz + z);
                            if (wards.isProtected(b.getX(), b.getY(), b.getZ(), cfg.wardRadius)) {
                            } else {
                                if (tryInfect(b, store, bs)) {
                                    infected++;
                                    if (store.size() < cfg.maxSourcesPerWorld) {
                                        if (cfg.infectWandBonusSourceChanceDivider > 0) {
                                            if (random.nextInt(cfg.infectWandBonusSourceChanceDivider) == 0) {
                                                store.add(b.getX(), b.getY(), b.getZ());
                                            }
                                        }
                                    }
                                }
                            }
                            z++;
                            /* continue */
                            y++;
                            /* continue */
                            x++;
                            /* continue */
                        }
                    }
                    z++;
                    /* continue */
                }
                y++;
                /* continue */
            }
            x++;
            /* continue */
        }
        return infected;
    }

    public int cleanse(org.bukkit.Location center, int radius) {
        org.bukkit.World world = center.getWorld();
        if (world == null) {
            return 0;
        }
        int cx = center.getBlockX();
        int cy = center.getBlockY();
        int cz = center.getBlockZ();
        int rSq = radius * radius;
        org.examplee.palePlugin.store.SourceStore store = sources(world);
        org.examplee.palePlugin.store.BiomeStore bs = (org.examplee.palePlugin.store.BiomeStore) biomeStoreByWorld.get(world.getUID());
        int cleaned = 0;
        java.util.HashSet touchedCells = new java.util.HashSet();
        java.util.HashSet touchedChunks = new java.util.HashSet();
        int x = -radius;
        if (x <= radius) {
            int xx = x * x;
            int y = -radius;
            if (y <= radius) {
                int xxyy = xx + y * y;
                int z = -radius;
                if (z <= radius) {
                    if (xxyy + z * z > rSq) {
                    } else {
                        org.bukkit.block.Block b = world.getBlockAt(cx + x, cy + y, cz + z);
                        org.bukkit.Material t = b.getType();
                        if (!(infectedTypes().contains(t))) {
                        } else {
                            touchedChunks.add(java.lang.Long.valueOf(org.examplee.palePlugin.store.SourceStore.packChunk(b.getX() >> 4, b.getZ() >> 4)));
                            if (bs != null) {
                                touchedCells.add(java.lang.Long.valueOf(bs.cellKeyFromBlock(b.getX(), b.getY(), b.getZ())));
                            }
                            cleaned = cleaned + cleanseSingleBlock(b, store);
                        }
                    }
                    z++;
                    /* continue */
                }
                y++;
                /* continue */
            }
            x++;
            /* continue */
        }
        if (cfg.biomeEnabled) {
            if (infectedBiome != null) {
                if (bs != null) {
                    if (!(touchedCells.isEmpty())) {
                        bs.restoreCellsIfClean(world, touchedCells, infectedTypes(), infectedBiome);
                    }
                }
            }
        }
        x = touchedChunks.iterator();
        if (x.hasNext()) {
            xx = ((java.lang.Long) x.next()).longValue();
            xxyy = (int) (ck >> 32);
            z = (int) ck;
            try {
                world.refreshChunk(ccx, ccz);
            }
            catch (java.lang.Exception b) {
                /* continue */
            }
        }
        return cleaned;
    }

    public int cleanseSingleBlock(org.bukkit.block.Block b, org.examplee.palePlugin.store.SourceStore store) {
        org.bukkit.Material t = b.getType();
        if (!(infectedTypes().contains(t))) {
            return 0;
        }
        java.lang.String n = t.name();
        if (!(n.contains("LOG"))) {
            if (!(n.contains("WOOD"))) {
                if (n.contains("LEAVES")) {
                    b.setType(org.bukkit.Material.AIR, false);
                } else {
                    if (n.contains("MOSS")) {
                        if (n.contains("CARPET")) {
                            b.setType(org.bukkit.Material.AIR, false);
                        } else {
                            b.setType(org.bukkit.Material.DIRT, false);
                            b.setType(org.bukkit.Material.AIR, false);
                        }
                    } else {
                        b.setType(org.bukkit.Material.AIR, false);
                    }
                }
            }
        }
        b.setType(org.bukkit.Material.AIR, false);
        if (n.contains("MOSS")) {
            if (n.contains("CARPET")) {
                b.setType(org.bukkit.Material.AIR, false);
            } else {
                b.setType(org.bukkit.Material.DIRT, false);
                b.setType(org.bukkit.Material.AIR, false);
            }
        } else {
            b.setType(org.bukkit.Material.AIR, false);
        }
        store.remove(b.getX(), b.getY(), b.getZ());
        return 1;
    }

    public boolean tryInfect(org.bukkit.block.Block b, org.examplee.palePlugin.store.SourceStore store, org.examplee.palePlugin.store.BiomeStore bs) {
        org.bukkit.Material t = b.getType();
        if (!(t.isAir())) {
            if (infectedTypes().contains(t)) {
                return false;
            }
        }
        return false;
    }

    private void infectLogUpwards(org.bukkit.block.Block base, org.examplee.palePlugin.store.BiomeStore bs) {
        org.bukkit.block.Block cur = base.getRelative(org.bukkit.block.BlockFace.UP);
        int i = 0;
        if (i < 12) {
            org.bukkit.Material t = cur.getType();
            if (t.isAir()) {
            } else {
                java.lang.String n = t.name();
                if (!(n.contains("LOG"))) {
                } else {
                    if (infectedTypes().contains(t)) {
                    } else {
                        cur.setType(mats.PALE_LOG, false);
                        applyInfectedBiome(cur, bs);
                        cur = cur.getRelative(org.bukkit.block.BlockFace.UP);
                        i++;
                        /* continue */
                    }
                }
            }
        }
    }

    private void applyInfectedBiome(org.bukkit.block.Block b, org.examplee.palePlugin.store.BiomeStore bs) {
        if (!(cfg.biomeEnabled)) {
            return;
        }
        if (infectedBiome == null) {
            return;
        }
        try {
            org.bukkit.block.Biome cur = b.getBiome();
            if (!(cur.equals(infectedBiome))) {
                long cellKey = bs.cellKeyFromBlock(b.getX(), b.getY(), b.getZ());
                if (!(bs.contains(cellKey))) {
                    org.bukkit.NamespacedKey oldKey = org.examplee.palePlugin.util.BiomeUtil.biomeKeyOf(cur);
                    if (oldKey != null) {
                        if (!(oldKey.equals(infectedBiomeKey))) {
                            bs.put(cellKey, oldKey);
                        }
                    }
                }
                b.setBiome(infectedBiome);
            }
        }
        catch (java.lang.Exception cur) {
            return;
        }
    }

    public void indexChunkSurface(org.bukkit.World world, int chunkX, int chunkZ) {
        int baseX = chunkX << 4;
        int baseZ = chunkZ << 4;
        org.examplee.palePlugin.store.SourceStore store = sources(world);
        int lx = 0;
        if (lx < 16) {
            int lz = 0;
            if (lz < 16) {
                int wx = baseX + lx;
                int wz = baseZ + lz;
                int topY = world.getHighestBlockYAt(wx, wz);
                int dy = 0;
                if (dy < cfg.indexDepth) {
                    int y = topY - dy;
                    if (y < world.getMinHeight()) {
                    } else {
                        org.bukkit.block.Block b = world.getBlockAt(wx, y, wz);
                        if (infectedTypes().contains(b.getType())) {
                            store.add(wx, y, wz);
                        }
                        dy++;
                        /* continue */
                    }
                }
                lz++;
                /* continue */
            }
            lx++;
            /* continue */
        }
    }

    public boolean trySpreadFromSource(org.bukkit.block.Block source) {
        org.examplee.palePlugin.store.SourceStore store = sources(source.getWorld());
        org.examplee.palePlugin.store.WardStore wards = wards(source.getWorld());
        org.examplee.palePlugin.store.BiomeStore bs = biomes(source.getWorld());
        int jumpRadius = 4;
        int jumpUpDown = 2;
        int probeDown = 6;
        int tries = cfg.spreadTriesPerAttempt;
        int attempt = 0;
        if (attempt < tries) {
            int dx = random.nextInt(9) - 4;
            int dz = random.nextInt(9) - 4;
            int dy = random.nextInt(5) - 2;
            if (dx == 0) {
                if (dy == 0) {
                    if (dz == 0) {
                    } else {
                        org.bukkit.block.Block target = source.getRelative(dx, dy, dz);
                        if (target.getType().isAir()) {
                            org.bukkit.block.Block t = target;
                            int i = 0;
                            if (i < 6) {
                                org.bukkit.block.Block down = t.getRelative(org.bukkit.block.BlockFace.DOWN);
                                if (down.getY() <= down.getWorld().getMinHeight()) {
                                } else {
                                    t = down;
                                    if (!(t.getType().isAir())) {
                                    } else {
                                        i++;
                                        /* continue */
                                    }
                                }
                            }
                            target = t;
                        }
                        if (wards.isProtected(target.getX(), target.getY(), target.getZ(), cfg.wardRadius)) {
                        } else {
                            if (hasInfectedNear(target)) {
                            } else {
                            }
                            t = 0;
                            i = 0;
                            if (i < 1 + bonus) {
                                if (tryInfect(target, store, bs)) {
                                    if (random.nextInt(3) != 0) {
                                        return true;
                                    }
                                    source.getWorld().spawnParticle(org.bukkit.Particle.SPORE_BLOSSOM_AIR, target.getLocation().add(0.5, 0.5, 0.5), 4, 0.3, 0.3, 0.3, 0.01);
                                    return true;
                                }
                                i++;
                                /* continue */
                            }
                        }
                    }
                } else {
                    target = source.getRelative(dx, dy, dz);
                    if (target.getType().isAir()) {
                        t = target;
                        i = 0;
                        if (i < 6) {
                            down = t.getRelative(org.bukkit.block.BlockFace.DOWN);
                            if (down.getY() <= down.getWorld().getMinHeight()) {
                            } else {
                                t = down;
                                if (!(t.getType().isAir())) {
                                } else {
                                    i++;
                                    /* continue */
                                }
                            }
                        }
                        target = t;
                    }
                    if (wards.isProtected(target.getX(), target.getY(), target.getZ(), cfg.wardRadius)) {
                    } else {
                        if (hasInfectedNear(target)) {
                        } else {
                        }
                        t = 0;
                        i = 0;
                        if (i < 1 + bonus) {
                            if (tryInfect(target, store, bs)) {
                                if (random.nextInt(3) != 0) {
                                    return true;
                                }
                                source.getWorld().spawnParticle(org.bukkit.Particle.SPORE_BLOSSOM_AIR, target.getLocation().add(0.5, 0.5, 0.5), 4, 0.3, 0.3, 0.3, 0.01);
                                return true;
                            }
                            i++;
                            /* continue */
                        }
                    }
                }
            } else {
                target = source.getRelative(dx, dy, dz);
                if (target.getType().isAir()) {
                    t = target;
                    i = 0;
                    if (i < 6) {
                        down = t.getRelative(org.bukkit.block.BlockFace.DOWN);
                        if (down.getY() <= down.getWorld().getMinHeight()) {
                        } else {
                            t = down;
                            if (!(t.getType().isAir())) {
                            } else {
                                i++;
                                /* continue */
                            }
                        }
                    }
                    target = t;
                }
                if (wards.isProtected(target.getX(), target.getY(), target.getZ(), cfg.wardRadius)) {
                } else {
                    if (hasInfectedNear(target)) {
                    } else {
                    }
                    t = 0;
                    i = 0;
                    if (i < 1 + bonus) {
                        if (tryInfect(target, store, bs)) {
                            if (random.nextInt(3) != 0) {
                                return true;
                            }
                            source.getWorld().spawnParticle(org.bukkit.Particle.SPORE_BLOSSOM_AIR, target.getLocation().add(0.5, 0.5, 0.5), 4, 0.3, 0.3, 0.3, 0.01);
                            return true;
                        }
                        i++;
                        /* continue */
                    }
                }
            }
            attempt++;
            /* continue */
        }
        return false;
    }

    public int getChunkStage(org.bukkit.World w, int chunkX, int chunkZ) {
        if (!(cfg.stagesEnabled)) {
            return 0;
        }
        int c = sources(w).getChunkCount(chunkX, chunkZ);
        int stage = 0;
        if (c >= cfg.stage1Sources) {
            stage = 1;
        }
        if (c >= cfg.stage2Sources) {
            stage = 2;
        }
        if (c >= cfg.stage3Sources) {
            stage = 3;
        }
        if (c >= cfg.stage4Sources) {
            stage = 4;
        }
        if (c >= cfg.stage5Sources) {
            stage = 5;
        }
        return stage;
    }

    public void sendMap(org.bukkit.entity.Player p, int radiusChunks) {
        org.bukkit.World w = p.getWorld();
        org.examplee.palePlugin.store.SourceStore store = sources(w);
        int cx = p.getLocation().getBlockX() >> 4;
        int cz = p.getLocation().getBlockZ() >> 4;
        int stageHere = getChunkStage(w, cx, cz);
        int max = 0;
        int dz = -radiusChunks;
        if (dz <= radiusChunks) {
            int dx = -radiusChunks;
            if (dx <= radiusChunks) {
                max = java.lang.Math.max(max, store.getChunkCount(cx + dx, cz + dz));
                dx++;
                /* continue */
            }
            dz++;
            /* continue */
        }
        p.sendMessage(java.lang.String.valueOf(org.bukkit.ChatColor.GRAY) + "[Pale] Карта заражения (0..9), r=" + radiusChunks + " чанков, max=" + max + ", stage=" + stageHere);
        dz = -radiusChunks;
        if (dz <= radiusChunks) {
            dx = new java.lang.StringBuilder();
            dx = -radiusChunks;
            if (dx <= radiusChunks) {
                int count = store.getChunkCount(cx + dx, cz + dz);
                if (dx == 0) {
                    if (dz == 0) {
                        line.append(org.bukkit.ChatColor.WHITE).append(88);
                    } else {
                        if (max <= 0) {
                        } else {
                        }
                        int level = 0;
                        level = org.examplee.palePlugin.util.MathUtil.clamp(level, 0, 9);
                        line.append(mapColorForLevel(level)).append((char) (48 + level));
                    }
                } else {
                    if (max <= 0) {
                    } else {
                    }
                    level = 0;
                    level = org.examplee.palePlugin.util.MathUtil.clamp(level, 0, 9);
                    line.append(mapColorForLevel(level)).append((char) (48 + level));
                }
                dx++;
                /* continue */
            }
            line.append(org.bukkit.ChatColor.RESET);
            p.sendMessage(line.toString());
            dz++;
            /* continue */
        }
        p.sendMessage(java.lang.String.valueOf(org.bukkit.ChatColor.DARK_GRAY) + "Легенда: 0=нет, 9=максимум, X=ты.");
    }

    private java.lang.String rgb(java.lang.String hex) {
        try {
        }
        catch (java.lang.Throwable ex) {
            return org.bukkit.ChatColor.GREEN.toString();
        }
        return null;
    }

    private java.lang.String mapColorForLevel(int level) {
        if (level > 0) {
            if (level > 2) {
                if (level > 4) {
                    if (level > 6) {
                        if (level <= 8) {
                            return rgb("#66ff00");
                        }
                    }
                    return rgb("#39FF14");
                }
                return rgb("#1aff1a");
            }
            return rgb("#145214");
        }
        return rgb("#1a1a1a");
    }

    public boolean hasInfectedNear(org.bukkit.block.Block b) {
        int dx = -1;
        if (dx <= 1) {
            int dy = -1;
            if (dy <= 1) {
                int dz = -1;
                if (dz <= 1) {
                    if (dx != 0) {
                        if (infectedTypes().contains(b.getRelative(dx, dy, dz).getType())) {
                            return true;
                        }
                    }
                    if (dy != 0) {
                        if (infectedTypes().contains(b.getRelative(dx, dy, dz).getType())) {
                            return true;
                        }
                    }
                    if (dz != 0) {
                        if (infectedTypes().contains(b.getRelative(dx, dy, dz).getType())) {
                            return true;
                        }
                    }
                    if (infectedTypes().contains(b.getRelative(dx, dy, dz).getType())) {
                        return true;
                    }
                    dz++;
                    /* continue */
                }
                dy++;
                /* continue */
            }
            dx++;
            /* continue */
        }
        return false;
    }

    public int purgeChunkSurface(org.bukkit.World world, int chunkX, int chunkZ, int depth) {
        int baseX = chunkX << 4;
        int baseZ = chunkZ << 4;
        int minY = world.getMinHeight();
        int cleaned = 0;
        org.examplee.palePlugin.store.SourceStore store = sources(world);
        org.examplee.palePlugin.store.BiomeStore bs = (org.examplee.palePlugin.store.BiomeStore) biomeStoreByWorld.get(world.getUID());
        java.util.HashSet touchedCells = new java.util.HashSet();
        int lx = 0;
        if (lx < 16) {
            int lz = 0;
            if (lz < 16) {
                int wx = baseX + lx;
                int wz = baseZ + lz;
                int topY = world.getHighestBlockYAt(wx, wz);
                int dy = 0;
                if (dy < depth) {
                    int y = topY - dy;
                    if (y < minY) {
                    } else {
                        org.bukkit.block.Block b = world.getBlockAt(wx, y, wz);
                        if (!(infectedTypes().contains(b.getType()))) {
                        } else {
                            if (bs != null) {
                                touchedCells.add(java.lang.Long.valueOf(bs.cellKeyFromBlock(b.getX(), b.getY(), b.getZ())));
                            }
                            cleaned = cleaned + cleanseSingleBlock(b, store);
                        }
                        dy++;
                        /* continue */
                    }
                }
                lz++;
                /* continue */
            }
            lx++;
            /* continue */
        }
        if (cfg.biomeEnabled) {
            if (infectedBiome != null) {
                if (bs != null) {
                    if (!(touchedCells.isEmpty())) {
                        bs.restoreCellsIfClean(world, touchedCells, infectedTypes(), infectedBiome);
                    }
                }
            }
        }
        try {
            world.refreshChunk(chunkX, chunkZ);
        }
        catch (java.lang.Exception lx) {
            return cleaned;
        }
    }

    private static org.examplee.palePlugin.store.BiomeStore lambda_biomes_2(java.util.UUID k) {
        return new org.examplee.palePlugin.store.BiomeStore();
    }

    private static org.examplee.palePlugin.store.WardStore lambda_wards_1(java.util.UUID k) {
        return new org.examplee.palePlugin.store.WardStore();
    }

    private static org.examplee.palePlugin.store.SourceStore lambda_sources_0(java.util.UUID k) {
        return new org.examplee.palePlugin.store.SourceStore();
    }

}
