package org.examplee.plague.darkness;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.LongTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.saveddata.SavedData;
import org.examplee.plague.PlagueConfig;
import org.examplee.plague.registry.ModBlocks;
import org.examplee.plague.util.PosPack;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

/** Per-world set of darkness block positions. */
public class DarknessWorldData extends SavedData {
    private final Set<Long> blocks = new HashSet<>();
    private final ArrayList<Long> blockList = new ArrayList<>();

    public DarknessWorldData() {}

    private static SavedData.Factory<DarknessWorldData> factory() {
        return new SavedData.Factory<>(DarknessWorldData::new, DarknessWorldData::load, DataFixTypes.LEVEL);
    }

    public static DarknessWorldData get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(factory(), "plague_darkness");
    }

    private static DarknessWorldData load(CompoundTag tag) {
        DarknessWorldData d = new DarknessWorldData();
        ListTag list = tag.getList("blocks", 4);
        for (int i = 0; i < list.size(); i++) {
            long k = list.getLong(i);
            if (d.blocks.add(k)) d.blockList.add(k);
        }
        return d;
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider provider) {
        ListTag list = new ListTag();
        for (long k : blocks) list.add(LongTag.valueOf(k));
        tag.put("blocks", list);
        return tag;
    }

    public int size() { return blocks.size(); }
    public Set<Long> all() { return blocks; }

    public void register(BlockPos pos) {
        if (blocks.size() >= PlagueConfig.INSTANCE.dark.maxBlocksPerWorld) return;
        long k = PosPack.pack(pos);
        if (blocks.add(k)) {
            blockList.add(k);
            setDirty();
        }
    }

    public void unregister(BlockPos pos) {
        if (blocks.remove(PosPack.pack(pos))) setDirty();
    }

    public boolean isDarkness(BlockPos pos) {
        return blocks.contains(PosPack.pack(pos));
    }

    /** Random tracked block in a loaded chunk, lazily cleaning stale entries. */
    public BlockPos pickRandom(ServerLevel level, RandomSource rnd) {
        if (blockList.isEmpty() || blocks.isEmpty()) return null;
        for (int t = 0; t < 6; t++) {
            int idx = rnd.nextInt(blockList.size());
            long k = blockList.get(idx);
            if (!blocks.contains(k)) {
                int last = blockList.size() - 1;
                blockList.set(idx, blockList.get(last));
                blockList.remove(last);
                if (blockList.isEmpty()) return null;
                continue;
            }
            BlockPos p = PosPack.unpack(k);
            if (!level.hasChunk(p.getX() >> 4, p.getZ() >> 4)) continue;
            if (!level.getBlockState(p).is(ModBlocks.DARKNESS_BLOCK)) {
                blocks.remove(k);
                setDirty();
                continue;
            }
            return p;
        }
        return null;
    }
}
