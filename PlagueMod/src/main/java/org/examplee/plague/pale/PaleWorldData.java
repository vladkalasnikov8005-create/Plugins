package org.examplee.plague.pale;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.LongTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.saveddata.SavedData;
import org.examplee.plague.pale.PaleEngine;
import org.examplee.plague.util.PosPack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/** Per-world pale state: infection sources, wards, great wards + charge, saved biomes. */
public class PaleWorldData extends SavedData {
    private final Set<Long> sources = new HashSet<>();
    private final ArrayList<Long> sourceList = new ArrayList<>();
    private final Map<Long, Integer> chunkCounts = new HashMap<>();
    private final Set<Long> wards = new HashSet<>();
    private final Map<Long, Integer> greatWards = new HashMap<>();
    private final Map<Long, String> biomeCells = new HashMap<>();

    public PaleWorldData() {}

    private static SavedData.Factory<PaleWorldData> factory() {
        return new SavedData.Factory<>(PaleWorldData::new, PaleWorldData::load, DataFixTypes.LEVEL);
    }

    public static PaleWorldData get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(factory(), "plague_pale");
    }

    private static PaleWorldData load(CompoundTag tag) {
        PaleWorldData d = new PaleWorldData();
        ListTag src = tag.getList("sources", 4);
        for (int i = 0; i < src.size(); i++) d.addSourceRaw(src.getLong(i));
        ListTag w = tag.getList("wards", 4);
        for (int i = 0; i < w.size(); i++) d.wards.add(w.getLong(i));
        ListTag gw = tag.getList("great", 10);
        for (int i = 0; i < gw.size(); i++) {
            CompoundTag c = gw.getCompound(i);
            d.greatWards.put(c.getLong("p"), c.getInt("c"));
        }
        ListTag b = tag.getList("biomes", 10);
        for (int i = 0; i < b.size(); i++) {
            CompoundTag c = b.getCompound(i);
            d.biomeCells.put(c.getLong("k"), c.getString("v"));
        }
        return d;
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider provider) {
        ListTag src = new ListTag();
        for (long k : sources) src.add(LongTag.valueOf(k));
        tag.put("sources", src);
        ListTag w = new ListTag();
        for (long k : wards) w.add(LongTag.valueOf(k));
        tag.put("wards", w);
        ListTag gw = new ListTag();
        for (Map.Entry<Long, Integer> e : greatWards.entrySet()) {
            CompoundTag c = new CompoundTag();
            c.putLong("p", e.getKey());
            c.putInt("c", e.getValue());
            gw.add(c);
        }
        tag.put("great", gw);
        ListTag bi = new ListTag();
        for (Map.Entry<Long, String> e : biomeCells.entrySet()) {
            CompoundTag c = new CompoundTag();
            c.putLong("k", e.getKey());
            c.putString("v", e.getValue());
            bi.add(c);
        }
        tag.put("biomes", bi);
        return tag;
    }

    // ---- sources ----
    public int sourceCount() { return sources.size(); }
    public int chunkCount(int cx, int cz) { return chunkCounts.getOrDefault(PosPack.packChunk(cx, cz), 0); }

    private void addSourceRaw(long key) {
        if (sources.add(key)) {
            sourceList.add(key);
            BlockPos p = PosPack.unpack(key);
            long ck = PosPack.packChunk(p.getX() >> 4, p.getZ() >> 4);
            chunkCounts.put(ck, chunkCounts.getOrDefault(ck, 0) + 1);
        }
    }

    public void addSource(BlockPos pos) { addSourceRaw(PosPack.pack(pos)); setDirty(); }

    public void removeSource(BlockPos pos) {
        long key = PosPack.pack(pos);
        if (sources.remove(key)) {
            long ck = PosPack.packChunk(pos.getX() >> 4, pos.getZ() >> 4);
            int v = chunkCounts.getOrDefault(ck, 0) - 1;
            if (v <= 0) chunkCounts.remove(ck); else chunkCounts.put(ck, v);
            setDirty();
        }
    }

    public void compact() {
        if (sourceList.size() > sources.size() * 2 + 64) {
            sourceList.clear();
            sourceList.addAll(sources);
        }
    }

    /** Random live source (loaded chunk + still infected), cleans dead entries lazily. */
    public BlockPos randomLiveSource(ServerLevel level, RandomSource rnd) {
        for (int t = 0; t < 250 && !sourceList.isEmpty(); t++) {
            int idx = rnd.nextInt(sourceList.size());
            long key = sourceList.get(idx);
            if (!sources.contains(key)) {
                swapRemove(idx);
                continue;
            }
            BlockPos p = PosPack.unpack(key);
            if (!level.hasChunk(p.getX() >> 4, p.getZ() >> 4)) continue;
            if (PaleEngine.isInfectedBlock(level.getBlockState(p))) return p;
            sources.remove(key);
            long ck = PosPack.packChunk(p.getX() >> 4, p.getZ() >> 4);
            int v = chunkCounts.getOrDefault(ck, 0) - 1;
            if (v <= 0) chunkCounts.remove(ck); else chunkCounts.put(ck, v);
            swapRemove(idx);
            setDirty();
        }
        return null;
    }

    private void swapRemove(int idx) {
        int last = sourceList.size() - 1;
        if (idx != last) sourceList.set(idx, sourceList.get(last));
        sourceList.remove(last);
    }

    // ---- wards ----
    public int wardCount() { return wards.size(); }
    public void addWard(BlockPos pos) { wards.add(PosPack.pack(pos)); setDirty(); }
    public boolean removeWard(BlockPos pos) { boolean r = wards.remove(PosPack.pack(pos)); if (r) setDirty(); return r; }

    public void addGreatWard(BlockPos pos, int charge) { greatWards.put(PosPack.pack(pos), Math.max(0, charge)); setDirty(); }
    public int removeGreatWard(BlockPos pos) {
        Integer c = greatWards.remove(PosPack.pack(pos));
        if (c != null) setDirty();
        return c == null ? -1 : c;
    }
    public Integer greatWardCharge(BlockPos pos) { return greatWards.get(PosPack.pack(pos)); }
    public Map<Long, Integer> greatWards() { return greatWards; }

    public int addGreatCharge(BlockPos pos, int delta, int max) {
        long k = PosPack.pack(pos);
        Integer cur = greatWards.get(k);
        if (cur == null) return -1;
        int next = Math.max(0, Math.min(max, cur + delta));
        greatWards.put(k, next);
        setDirty();
        return next;
    }

    public boolean isProtected(int x, int y, int z, int wardR, int greatR) {
        return isProtectedBy(wards, x, y, z, wardR) || isProtectedBy(greatWards.keySet(), x, y, z, greatR);
    }

    private static boolean isProtectedBy(Set<Long> set, int x, int y, int z, int radius) {
        if (set.isEmpty()) return false;
        int rSq = radius * radius;
        int cRad = (radius >> 4) + 1;
        int cx = x >> 4, cz = z >> 4;
        for (long k : set) {
            int wx = PosPack.x(k), wy = PosPack.y(k), wz = PosPack.z(k);
            if (Math.abs((wx >> 4) - cx) > cRad || Math.abs((wz >> 4) - cz) > cRad) continue;
            int dx = wx - x, dy = wy - y, dz = wz - z;
            if (dx * dx + dy * dy + dz * dz <= rSq) return true;
        }
        return false;
    }

    // ---- biome cells ----
    public boolean hasCell(long cell) { return biomeCells.containsKey(cell); }
    public void putCell(long cell, String biomeId) { biomeCells.putIfAbsent(cell, biomeId); setDirty(); }
    public String getCell(long cell) { return biomeCells.get(cell); }
    public void removeCell(long cell) { if (biomeCells.remove(cell) != null) setDirty(); }
    public int biomeCellCount() { return biomeCells.size(); }

    public static long cellKey(BlockPos pos) {
        return PosPack.pack(pos.getX() >> 2, pos.getY() >> 2, pos.getZ() >> 2);
    }
}
