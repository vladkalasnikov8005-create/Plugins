package org.examplee.plague.leper;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.saveddata.SavedData;
import org.examplee.plague.PlagueConfig;

import java.util.HashMap;
import java.util.Map;

/** Quarantine lantern positions + fuel seconds, per world. Replaces the plugin's chunk-PDC storage. */
public class LanternData extends SavedData {
    private final Map<Long, Integer> fuel = new HashMap<>();

    public LanternData() {}

    private static SavedData.Factory<LanternData> factory() {
        return new SavedData.Factory<>(LanternData::new, LanternData::load, DataFixTypes.LEVEL);
    }

    public static LanternData get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(factory(), "plague_lanterns");
    }

    private static LanternData load(CompoundTag tag) {
        LanternData d = new LanternData();
        ListTag list = tag.getList("lanterns", 10);
        for (int i = 0; i < list.size(); i++) {
            CompoundTag c = list.getCompound(i);
            long key = pack(c.getInt("x"), c.getInt("y"), c.getInt("z"));
            d.fuel.put(key, c.getInt("fuel"));
        }
        return d;
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider provider) {
        ListTag list = new ListTag();
        for (Map.Entry<Long, Integer> e : fuel.entrySet()) {
            CompoundTag c = new CompoundTag();
            c.putInt("x", unpackX(e.getKey()));
            c.putInt("y", unpackY(e.getKey()));
            c.putInt("z", unpackZ(e.getKey()));
            c.putInt("fuel", e.getValue());
            list.add(c);
        }
        tag.put("lanterns", list);
        return tag;
    }

    public static long pack(int x, int y, int z) {
        long xx = (long) x & 0x3FFFFFFL;
        long zz = (long) z & 0x3FFFFFFL;
        long yy = (long) (y + 2048) & 0xFFFL;
        return xx << 38 | zz << 12 | yy;
    }

    public static int unpackX(long k) { int v = (int) (k >> 38 & 0x3FFFFFFL); return (v & 0x2000000) != 0 ? v | ~0x3FFFFFF : v; }
    public static int unpackZ(long k) { int v = (int) (k >> 12 & 0x3FFFFFFL); return (v & 0x2000000) != 0 ? v | ~0x3FFFFFF : v; }
    public static int unpackY(long k) { return (int) (k & 0xFFFL) - 2048; }

    public void place(BlockPos pos, int startFuel) {
        fuel.put(pack(pos.getX(), pos.getY(), pos.getZ()), Math.max(0, startFuel));
        setDirty();
    }

    public boolean remove(BlockPos pos) {
        boolean r = fuel.remove(pack(pos.getX(), pos.getY(), pos.getZ())) != null;
        if (r) setDirty();
        return r;
    }

    public boolean isLantern(BlockPos pos) {
        return fuel.containsKey(pack(pos.getX(), pos.getY(), pos.getZ()));
    }

    public int fuelAt(BlockPos pos) {
        return fuel.getOrDefault(pack(pos.getX(), pos.getY(), pos.getZ()), -1);
    }

    public int refuel(BlockPos pos) {
        long k = pack(pos.getX(), pos.getY(), pos.getZ());
        Integer cur = fuel.get(k);
        if (cur == null) return -1;
        int next = Math.min(PlagueConfig.INSTANCE.leper.lanternFuelMaxSec, cur + PlagueConfig.INSTANCE.leper.lanternFuelPerDustSec);
        fuel.put(k, next);
        setDirty();
        return next;
    }

    /** True if any fueled lantern covers this position (sphere). */
    public boolean isInRange(BlockPos pos) {
        if (fuel.isEmpty()) return false;
        int r = PlagueConfig.INSTANCE.leper.lanternRadius;
        int rSq = r * r;
        for (Map.Entry<Long, Integer> e : fuel.entrySet()) {
            if (e.getValue() <= 0) continue;
            int dx = unpackX(e.getKey()) - pos.getX();
            int dy = unpackY(e.getKey()) - pos.getY();
            int dz = unpackZ(e.getKey()) - pos.getZ();
            if (dx * dx + dy * dy + dz * dz <= rSq) return true;
        }
        return false;
    }

    /** Drain 1 second of fuel from every lantern. Call once per second. */
    public void tickSecond(ServerLevel level) {
        if (fuel.isEmpty()) return;
        boolean changed = false;
        for (Map.Entry<Long, Integer> e : fuel.entrySet()) {
            int f = e.getValue();
            if (f > 0) {
                e.setValue(f - 1);
                changed = true;
                if ((level.getGameTime() & 31) == 0) {
                    BlockPos p = new BlockPos(unpackX(e.getKey()), unpackY(e.getKey()), unpackZ(e.getKey()));
                    if (level.hasChunk(p.getX() >> 4, p.getZ() >> 4)) {
                        level.sendParticles(net.minecraft.core.particles.ParticleTypes.SOUL_FIRE_FLAME,
                                p.getX() + 0.5, p.getY() + 0.6, p.getZ() + 0.5, 3, 0.15, 0.2, 0.15, 0.005);
                    }
                }
            }
        }
        if (changed) setDirty();
    }
}
