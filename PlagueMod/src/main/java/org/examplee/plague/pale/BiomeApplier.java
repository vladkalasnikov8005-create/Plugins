package org.examplee.plague.pale;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.chunk.LevelChunk;
import org.examplee.plague.PlagueConfig;
import org.examplee.plague.util.PosPack;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Rewrites chunk biomes to pale_garden on infection, restores the saved
 * original biome once a 4x4x4 cell is fully clean.
 */
public class BiomeApplier {
    private static Holder<Biome> infectedHolder(ServerLevel level) {
        Registry<Biome> reg = level.registryAccess().lookupOrThrow(Registries.BIOME);
        String[] parts = PlagueConfig.INSTANCE.pale.infectedBiome.split(":", 2);
        Identifier id = parts.length == 2
                ? Identifier.fromNamespaceAndPath(parts[0], parts[1])
                : Identifier.fromNamespaceAndPath("minecraft", parts[0]);
        Optional<Holder.Reference<Biome>> holder = reg.getHolder(ResourceKey.create(Registries.BIOME, id));
        return holder.orElse(null);
    }

    private static Holder<Biome> holderById(ServerLevel level, String idStr) {
        Registry<Biome> reg = level.registryAccess().lookupOrThrow(Registries.BIOME);
        String[] parts = idStr.split(":", 2);
        Identifier id = parts.length == 2
                ? Identifier.fromNamespaceAndPath(parts[0], parts[1])
                : Identifier.fromNamespaceAndPath("minecraft", parts[0]);
        return reg.getHolder(ResourceKey.create(Registries.BIOME, id)).orElse(null);
    }

    public static void applyInfected(ServerLevel level, PaleWorldData data, BlockPos pos) {
        if (!PlagueConfig.INSTANCE.pale.biomeEnabled) return;
        Holder<Biome> infected = infectedHolder(level);
        if (infected == null) return;
        Holder<Biome> cur = level.getBiome(pos);
        if (cur.value() == infected.value()) return;
        long cell = PaleWorldData.cellKey(pos);
        if (!data.hasCell(cell)) {
            String oldId = cur.unwrapKey().map(k -> k.toString().replace("[", "").replace("]", "")).orElse("minecraft:plains");
            if (oldId.contains("/")) oldId = oldId.substring(oldId.indexOf('/') + 1);
            if (!oldId.equals(PlagueConfig.INSTANCE.pale.infectedBiome)) data.putCell(cell, oldId);
        }
        LevelChunk chunk = level.getChunk(pos.getX() >> 4, pos.getZ() >> 4);
        chunk.setBiome(pos.getX() >> 2, pos.getY() >> 2, pos.getZ() >> 2, infected);
        chunk.setUnsaved(true);
        level.getChunkSource().chunkMap.resendBiomesForChunks(List.of(chunk));
    }

    public static void restoreCellsIfClean(ServerLevel level, PaleWorldData data, Set<Long> cells) {
        if (!PlagueConfig.INSTANCE.pale.biomeEnabled || cells.isEmpty()) return;
        Set<LevelChunk> touched = new HashSet<>();
        List<LevelChunk> toResend = new ArrayList<>();
        for (long cell : cells) {
            String oldId = data.getCell(cell);
            if (oldId == null) continue;
            int cx = PosPack.x(cell), cy = PosPack.y(cell), cz = PosPack.z(cell);
            if (cellHasInfected(level, cx, cy, cz)) continue;
            Holder<Biome> oldBiome = holderById(level, oldId);
            if (oldBiome == null) oldBiome = holderById(level, "minecraft:plains");
            if (oldBiome == null) continue;
            BlockPos p = new BlockPos((cx << 2) + 1,
                    Math.max(level.getMinY(), Math.min(level.getMaxY() - 1, (cy << 2) + 1)), (cz << 2) + 1);
            if (!level.hasChunk(p.getX() >> 4, p.getZ() >> 4)) continue;
            LevelChunk chunk = level.getChunk(p.getX() >> 4, p.getZ() >> 4);
            chunk.setBiome(cx, cy, cz, oldBiome);
            chunk.setUnsaved(true);
            if (touched.add(chunk)) toResend.add(chunk);
            data.removeCell(cell);
        }
        if (!toResend.isEmpty()) level.getChunkSource().chunkMap.resendBiomesForChunks(toResend);
    }

    private static boolean cellHasInfected(ServerLevel level, int cx, int cy, int cz) {
        int baseX = cx << 2, baseY = cy << 2, baseZ = cz << 2;
        for (int ox = 0; ox < 4; ox++) {
            for (int oz = 0; oz < 4; oz++) {
                for (int oy = 0; oy < 4; oy++) {
                    int y = baseY + oy;
                    if (y < level.getMinY() || y >= level.getMaxY()) continue;
                    if (PaleEngine.isInfectedBlock(level.getBlockState(new BlockPos(baseX + ox, y, baseZ + oz)))) return true;
                }
            }
        }
        return false;
    }
}
