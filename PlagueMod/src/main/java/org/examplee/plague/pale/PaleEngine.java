package org.examplee.plague.pale;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Heightmap;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.examplee.plague.PlagueConfig;
import org.examplee.plague.util.PosPack;

import java.util.HashSet;
import java.util.Set;

/** Pale forest spread/cleanse engine. Singleton via {@link #get()}. */
public class PaleEngine {
    private static final PaleEngine INSTANCE = new PaleEngine();
    public static PaleEngine get() { return INSTANCE; }

    private static final Set<Block> INFECTED = Set.of(
            Blocks.PALE_OAK_LOG, Blocks.PALE_OAK_WOOD, Blocks.PALE_OAK_LEAVES,
            Blocks.PALE_MOSS_BLOCK, Blocks.PALE_MOSS_CARPET);

    public static boolean isInfectedBlock(BlockState state) {
        return INFECTED.contains(state.getBlock());
    }

    public boolean isWardProtected(ServerLevel level, int x, int y, int z) {
        var c = PlagueConfig.INSTANCE.pale;
        return PaleWorldData.get(level).isProtected(x, y, z, c.wardRadius, c.greatWardRadius);
    }

    // ---- infection ----
    public boolean tryInfect(ServerLevel level, BlockPos pos, PaleWorldData data) {
        BlockState state = level.getBlockState(pos);
        if (state.isAir() || isInfectedBlock(state)) return false;
        String name = BuiltInRegistries.BLOCK.getKey(state.getBlock()).getPath();
        Block into = null;
        if (name.contains("log")) into = Blocks.PALE_OAK_LOG;
        else if (name.contains("wood")) into = Blocks.PALE_OAK_WOOD;
        else if (name.contains("leaves")) into = Blocks.PALE_OAK_LEAVES;
        else if ((state.is(Blocks.GRASS_BLOCK) || state.is(Blocks.DIRT) || state.is(Blocks.MOSS_BLOCK))
                && level.random.nextInt(10) == 0) into = Blocks.PALE_MOSS_BLOCK;
        if (into == null) return false;

        level.setBlock(pos, into.defaultBlockState(), Block.UPDATE_CLIENTS);
        BiomeApplier.applyInfected(level, data, pos);
        if (into == Blocks.PALE_OAK_LOG) {
            BlockPos cur = pos.above();
            for (int i = 0; i < 12; i++) {
                BlockState s = level.getBlockState(cur);
                String n = BuiltInRegistries.BLOCK.getKey(s.getBlock()).getPath();
                if (s.isAir() || isInfectedBlock(s) || !n.contains("log")) break;
                level.setBlock(cur, Blocks.PALE_OAK_LOG.defaultBlockState(), Block.UPDATE_CLIENTS);
                BiomeApplier.applyInfected(level, data, cur);
                cur = cur.above();
            }
        }
        var c = PlagueConfig.INSTANCE.pale;
        if (data.sourceCount() < c.maxSourcesPerWorld) {
            boolean wood = into == Blocks.PALE_OAK_LOG || into == Blocks.PALE_OAK_WOOD;
            boolean make = wood ? level.random.nextInt(Math.max(1, c.logSourceChanceDivider)) == 0
                    : level.random.nextInt(Math.max(1, c.sourceChanceDivider)) == 0;
            if (make) data.addSource(pos);
        }
        return true;
    }

    public int infectArea(ServerLevel level, BlockPos center, int radius, int maxBlocks) {
        radius = Math.max(1, Math.min(64, radius));
        maxBlocks = Math.max(10, Math.min(200000, maxBlocks));
        PaleWorldData data = PaleWorldData.get(level);
        int infected = 0, processed = 0;
        int rSq = radius * radius;
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dy = -radius; dy <= radius; dy++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    if (dx * dx + dy * dy + dz * dz > rSq) continue;
                    if (processed++ >= maxBlocks) return infected;
                    BlockPos p = center.offset(dx, dy, dz);
                    if (!level.hasChunk(p.getX() >> 4, p.getZ() >> 4)) continue;
                    if (!isWardProtected(level, p.getX(), p.getY(), p.getZ()) && tryInfect(level, p, data)) infected++;
                }
            }
        }
        return infected;
    }

    public int infectAreaWand(ServerLevel level, BlockPos center) {
        var c = PlagueConfig.INSTANCE.pale;
        PaleWorldData data = PaleWorldData.get(level);
        int infected = 0, processed = 0;
        int rSq = c.wandRadius * c.wandRadius;
        for (int dx = -c.wandRadius; dx <= c.wandRadius; dx++) {
            for (int dy = -c.wandRadius; dy <= c.wandRadius; dy++) {
                for (int dz = -c.wandRadius; dz <= c.wandRadius; dz++) {
                    if (dx * dx + dy * dy + dz * dz > rSq) continue;
                    if (processed++ >= c.wandMaxBlocks) return infected;
                    BlockPos p = center.offset(dx, dy, dz);
                    if (!level.hasChunk(p.getX() >> 4, p.getZ() >> 4)) continue;
                    if (!isWardProtected(level, p.getX(), p.getY(), p.getZ()) && tryInfect(level, p, data)) {
                        infected++;
                        if (data.sourceCount() < c.maxSourcesPerWorld
                                && level.random.nextInt(Math.max(1, c.wandBonusSourceDivider)) == 0) data.addSource(p);
                    }
                }
            }
        }
        return infected;
    }

    // ---- cleanse ----
    public int cleanseSingle(ServerLevel level, BlockPos pos, PaleWorldData data) {
        BlockState state = level.getBlockState(pos);
        if (!isInfectedBlock(state)) return 0;
        String name = BuiltInRegistries.BLOCK.getKey(state.getBlock()).getPath();
        if (name.contains("log") || name.contains("wood") || name.contains("leaves") || name.contains("carpet")) {
            level.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_CLIENTS);
        } else {
            level.setBlock(pos, Blocks.DIRT.defaultBlockState(), Block.UPDATE_CLIENTS);
        }
        data.removeSource(pos);
        return 1;
    }

    public int cleanse(ServerLevel level, BlockPos center, int radius) {
        PaleWorldData data = PaleWorldData.get(level);
        Set<Long> cells = new HashSet<>();
        int cleaned = 0;
        int rSq = radius * radius;
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dy = -radius; dy <= radius; dy++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    if (dx * dx + dy * dy + dz * dz > rSq) continue;
                    BlockPos p = center.offset(dx, dy, dz);
                    if (!level.hasChunk(p.getX() >> 4, p.getZ() >> 4)) continue;
                    if (isInfectedBlock(level.getBlockState(p))) {
                        cells.add(PaleWorldData.cellKey(p));
                        cleaned += cleanseSingle(level, p, data);
                    }
                }
            }
        }
        BiomeApplier.restoreCellsIfClean(level, data, cells);
        return cleaned;
    }

    public int purgeChunk(ServerLevel level, int cx, int cz, int depth) {
        PaleWorldData data = PaleWorldData.get(level);
        Set<Long> cells = new HashSet<>();
        int cleaned = 0;
        for (int lx = 0; lx < 16; lx++) {
            for (int lz = 0; lz < 16; lz++) {
                int wx = (cx << 4) + lx, wz = (cz << 4) + lz;
                int topY = level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, new BlockPos(wx, 0, wz)).getY();
                for (int d = 0; d < depth; d++) {
                    int y = topY - d;
                    if (y < level.getMinY()) break;
                    BlockPos p = new BlockPos(wx, y, wz);
                    if (isInfectedBlock(level.getBlockState(p))) {
                        cells.add(PaleWorldData.cellKey(p));
                        cleaned += cleanseSingle(level, p, data);
                    }
                }
            }
        }
        BiomeApplier.restoreCellsIfClean(level, data, cells);
        return cleaned;
    }

    // ---- spread ----
    public void indexChunk(ServerLevel level, int cx, int cz) {
        var c = PlagueConfig.INSTANCE.pale;
        PaleWorldData data = PaleWorldData.get(level);
        for (int lx = 0; lx < 16; lx++) {
            for (int lz = 0; lz < 16; lz++) {
                int wx = (cx << 4) + lx, wz = (cz << 4) + lz;
                int topY = level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, new BlockPos(wx, 0, wz)).getY();
                for (int d = 0; d < c.indexDepth; d++) {
                    int y = topY - d;
                    if (y < level.getMinY()) break;
                    BlockPos p = new BlockPos(wx, y, wz);
                    if (isInfectedBlock(level.getBlockState(p))) data.addSource(p);
                }
            }
        }
    }

    public boolean trySpreadFromSource(ServerLevel level, BlockPos source) {
        var c = PlagueConfig.INSTANCE.pale;
        PaleWorldData data = PaleWorldData.get(level);
        RandomSource rnd = level.random;
        for (int attempt = 0; attempt < c.spreadTriesPerAttempt; attempt++) {
            int dx = rnd.nextInt(9) - 4, dz = rnd.nextInt(9) - 4, dy = rnd.nextInt(5) - 2;
            if (dx == 0 && dy == 0 && dz == 0) continue;
            BlockPos target = source.offset(dx, dy, dz);
            if (level.getBlockState(target).isAir()) {
                for (int i = 0; i < 6 && level.getBlockState(target).isAir() && target.getY() > level.getMinY(); i++) {
                    target = target.below();
                }
            }
            if (!level.hasChunk(target.getX() >> 4, target.getZ() >> 4)) continue;
            if (isWardProtected(level, target.getX(), target.getY(), target.getZ())) continue;
            int bonus = hasInfectedNear(level, target) ? 2 : 0;
            for (int i = 0; i < 1 + bonus; i++) {
                if (tryInfect(level, target, data)) {
                    if (rnd.nextInt(3) == 0) {
                        level.sendParticles(ParticleTypes.SPORE_BLOSSOM_AIR,
                                target.getX() + 0.5, target.getY() + 0.5, target.getZ() + 0.5, 4, 0.3, 0.3, 0.3, 0.01);
                    }
                    return true;
                }
            }
        }
        return false;
    }

    public boolean hasInfectedNear(ServerLevel level, BlockPos pos) {
        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                for (int dz = -1; dz <= 1; dz++) {
                    if (dx == 0 && dy == 0 && dz == 0) continue;
                    if (isInfectedBlock(level.getBlockState(pos.offset(dx, dy, dz)))) return true;
                }
            }
        }
        return false;
    }

    public int getStage(ServerLevel level, int cx, int cz) {
        var c = PlagueConfig.INSTANCE.pale;
        if (!c.stagesEnabled) return 0;
        int n = PaleWorldData.get(level).chunkCount(cx, cz);
        int stage = 0;
        if (n >= c.stage1) stage = 1;
        if (n >= c.stage2) stage = 2;
        if (n >= c.stage3) stage = 3;
        if (n >= c.stage4) stage = 4;
        if (n >= c.stage5) stage = 5;
        return stage;
    }

    public record MapResult(byte[] levels, int max, int stage) {}

    public MapResult mapLevels(ServerLevel level, int ccx, int ccz, int radius) {
        PaleWorldData data = PaleWorldData.get(level);
        int size = radius * 2 + 1;
        int[] counts = new int[size * size];
        int max = 0;
        for (int dz = -radius; dz <= radius; dz++) {
            for (int dx = -radius; dx <= radius; dx++) {
                int n = data.chunkCount(ccx + dx, ccz + dz);
                counts[(dz + radius) * size + (dx + radius)] = n;
                if (n > max) max = n;
            }
        }
        byte[] levels = new byte[size * size];
        for (int i = 0; i < counts.length; i++) {
            levels[i] = (byte) (max <= 0 ? 0 : Math.max(0, Math.min(9, Math.round(counts[i] / (double) max * 9.0))));
        }
        return new MapResult(levels, max, getStage(level, ccx, ccz));
    }
}
