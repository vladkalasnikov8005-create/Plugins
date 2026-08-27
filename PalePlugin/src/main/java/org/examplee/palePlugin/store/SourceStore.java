package org.examplee.palePlugin.store;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import org.bukkit.World;
import org.bukkit.block.Block;

public final class SourceStore {
    private java.util.ArrayList list;
    private final java.util.HashSet set;
    private final java.util.HashMap countByChunk;

    public SourceStore() {
        super();
        this.list = new java.util.ArrayList();
        this.set = new java.util.HashSet();
        this.countByChunk = new java.util.HashMap();
    }

    public int size() {
        return set.size();
    }

    public int getChunkCount(int chunkX, int chunkZ) {
        return ((java.lang.Integer) countByChunk.getOrDefault(java.lang.Long.valueOf(org.examplee.palePlugin.store.SourceStore.packChunk(chunkX, chunkZ)), java.lang.Integer.valueOf(0))).intValue();
    }

    public void add(int x, int y, int z) {
        long key = org.examplee.palePlugin.store.SourceStore.pack(x, y, z);
        if (set.add(java.lang.Long.valueOf(key))) {
            list.add(java.lang.Long.valueOf(key));
            incChunk(x >> 4, z >> 4, 1);
        }
    }

    public void remove(int x, int y, int z) {
        long key = org.examplee.palePlugin.store.SourceStore.pack(x, y, z);
        if (set.remove(java.lang.Long.valueOf(key))) {
            incChunk(x >> 4, z >> 4, -1);
        }
    }

    public void compactIfNeeded() {
        if (list.size() <= set.size() * 2 + 64) {
            return;
        }
        java.util.ArrayList nl = new java.util.ArrayList(set.size());
        nl.addAll(set);
        this.list = nl;
    }

    public org.bukkit.block.Block getRandomLiveSource(org.bukkit.World world, java.util.Random rnd, java.util.Set infectedTypes) {
        int tries = 0;
        if (tries < 250) {
            if (list.isEmpty()) {
                return null;
            }
            int idx = rnd.nextInt(list.size());
            long key = ((java.lang.Long) list.get(idx)).longValue();
            if (!(set.contains(java.lang.Long.valueOf(key)))) {
                org.examplee.palePlugin.store.SourceStore.swapRemove(list, idx);
            } else {
                int x = org.examplee.palePlugin.store.SourceStore.unpackX(key);
                int y = org.examplee.palePlugin.store.SourceStore.unpackY(key);
                int z = org.examplee.palePlugin.store.SourceStore.unpackZ(key);
                if (world.isChunkLoaded(x >> 4, z >> 4)) {
                    org.bukkit.block.Block b = world.getBlockAt(x, y, z);
                    if (infectedTypes.contains(b.getType())) {
                        return b;
                    }
                    set.remove(java.lang.Long.valueOf(key));
                    incChunk(x >> 4, z >> 4, -1);
                    org.examplee.palePlugin.store.SourceStore.swapRemove(list, idx);
                    return b;
                }
                b = world.getBlockAt(x, y, z);
                if (infectedTypes.contains(b.getType())) {
                    return b;
                }
                set.remove(java.lang.Long.valueOf(key));
                incChunk(x >> 4, z >> 4, -1);
                org.examplee.palePlugin.store.SourceStore.swapRemove(list, idx);
                return b;
            }
            tries++;
            /* continue */
        }
        return null;
    }

    private void incChunk(int cx, int cz, int delta) {
        long ck = org.examplee.palePlugin.store.SourceStore.packChunk(cx, cz);
        int v = ((java.lang.Integer) countByChunk.getOrDefault(java.lang.Long.valueOf(ck), java.lang.Integer.valueOf(0))).intValue() + delta;
        if (v <= 0) {
            countByChunk.remove(java.lang.Long.valueOf(ck));
        } else {
            countByChunk.put(java.lang.Long.valueOf(ck), java.lang.Integer.valueOf(v));
        }
    }

    private static void swapRemove(java.util.ArrayList list, int idx) {
        int last = list.size() - 1;
        if (idx != last) {
            list.set(idx, (java.lang.Long) list.get(last));
        }
        list.remove(last);
    }

    public static long packChunk(int cx, int cz) {
        return (long) cx << 32 | (long) cz & 4294967295L;
    }

    private static long pack(int x, int y, int z) {
        int yEnc = y + 2048;
        long xx = (long) x & 67108863L;
        long zz = (long) z & 67108863L;
        long yy = (long) yEnc & 4095L;
        return xx << 38 | zz << 12 | yy;
    }

    private static int unpackX(long key) {
        int x = (int) (key >> 38);
        if ((x & 33554432) != 0) {
            x = x | -67108864;
        }
        return x;
    }

    private static int unpackZ(long key) {
        int z = (int) (key >> 12 & 67108863L);
        if ((z & 33554432) != 0) {
            z = z | -67108864;
        }
        return z;
    }

    private static int unpackY(long key) {
        int yEnc = (int) (key & 4095L);
        return yEnc - 2048;
    }

}
