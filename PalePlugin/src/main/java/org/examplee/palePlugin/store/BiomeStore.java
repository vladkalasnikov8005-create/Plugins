package org.examplee.palePlugin.store;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map.Entry;
import java.util.Set;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.World;
import org.bukkit.block.Biome;

public final class BiomeStore {
    private final java.util.HashMap oldByCell;

    public BiomeStore() {
        super();
        this.oldByCell = new java.util.HashMap();
    }

    public int size() {
        return oldByCell.size();
    }

    public boolean contains(long key) {
        return oldByCell.containsKey(java.lang.Long.valueOf(key));
    }

    public void put(long key, org.bukkit.NamespacedKey biomeKey) {
        if (biomeKey != null) {
            oldByCell.putIfAbsent(java.lang.Long.valueOf(key), biomeKey);
        }
    }

    public long cellKeyFromBlock(int x, int y, int z) {
        return cellKey(x >> 2, y >> 2, z >> 2);
    }

    public long cellKey(int xCell, int yCell, int zCell) {
        int yEnc = yCell + 2048;
        long xx = (long) xCell & 67108863L;
        long zz = (long) zCell & 67108863L;
        long yy = (long) yEnc & 4095L;
        return xx << 38 | zz << 12 | yy;
    }

    private int unpackX(long key) {
        int x = (int) (key >> 38);
        if ((x & 33554432) != 0) {
            x = x | -67108864;
        }
        return x;
    }

    private int unpackZ(long key) {
        int z = (int) (key >> 12 & 67108863L);
        if ((z & 33554432) != 0) {
            z = z | -67108864;
        }
        return z;
    }

    private int unpackY(long key) {
        int yEnc = (int) (key & 4095L);
        return yEnc - 2048;
    }

    public void restoreCellsIfClean(org.bukkit.World world, java.util.Set cellKeys, java.util.Set infectedTypes, org.bukkit.block.Biome infectedBiome) {
        java.util.Iterator local5 = cellKeys.iterator();
        if (local5.hasNext()) {
            long key = ((java.lang.Long) local5.next()).longValue();
            org.bukkit.NamespacedKey oldKey = (org.bukkit.NamespacedKey) oldByCell.get(java.lang.Long.valueOf(key));
            if (oldKey == null) {
                /* continue */
            }
            int xCell = unpackX(key);
            int yCell = unpackY(key);
            int zCell = unpackZ(key);
            if (cellHasInfected(world, xCell, yCell, zCell, infectedTypes)) {
                /* continue */
            }
            org.bukkit.block.Biome oldBiome = (org.bukkit.block.Biome) org.bukkit.Registry.BIOME.get(oldKey);
            if (oldBiome != null) {
                if (infectedBiome != null) {
                    if (oldBiome.equals(infectedBiome)) {
                        oldBiome = (org.bukkit.block.Biome) org.bukkit.Registry.BIOME.get(org.bukkit.NamespacedKey.fromString("minecraft:plains"));
                    }
                }
            }
            oldBiome = (org.bukkit.block.Biome) org.bukkit.Registry.BIOME.get(org.bukkit.NamespacedKey.fromString("minecraft:plains"));
            if (oldBiome == null) {
                /* continue */
            }
            int bx = (xCell << 2) + 1;
            int by = (yCell << 2) + 1;
            int bz = (zCell << 2) + 1;
            by = java.lang.Math.max(world.getMinHeight(), java.lang.Math.min(world.getMaxHeight() - 1, by));
            try {
                world.getBlockAt(bx, by, bz).setBiome(oldBiome);
                oldByCell.remove(java.lang.Long.valueOf(key));
            }
            catch (java.lang.Exception ex) {
                /* continue */
            }
        }
    }

    private boolean cellHasInfected(org.bukkit.World world, int xCell, int yCell, int zCell, java.util.Set infectedTypes) {
        int baseX = xCell << 2;
        int baseY = yCell << 2;
        int baseZ = zCell << 2;
        int minY = world.getMinHeight();
        int maxY = world.getMaxHeight() - 1;
        int ox = 0;
        if (ox < 4) {
            int x = baseX + ox;
            int oz = 0;
            if (oz < 4) {
                int z = baseZ + oz;
                int oy = 0;
                if (oy < 4) {
                    int y = baseY + oy;
                    if (y >= minY) {
                        if (y <= maxY) {
                            if (infectedTypes.contains(world.getBlockAt(x, y, z).getType())) {
                                return true;
                            }
                        }
                        if (infectedTypes.contains(world.getBlockAt(x, y, z).getType())) {
                            return true;
                        }
                    }
                    oy++;
                    /* continue */
                }
                oz++;
                /* continue */
            }
            ox++;
            /* continue */
        }
        return false;
    }

    public java.util.List serialize() {
        java.util.ArrayList out = new java.util.ArrayList(oldByCell.size());
        java.util.Iterator local2 = oldByCell.entrySet().iterator();
        if (local2.hasNext()) {
            java.util.Map.Entry e = (java.util.Map.Entry) local2.next();
            long key = ((java.lang.Long) e.getKey()).longValue();
            org.bukkit.NamespacedKey biomeKey = (org.bukkit.NamespacedKey) e.getValue();
            int xCell = unpackX(key);
            int yCell = unpackY(key);
            int zCell = unpackZ(key);
            out.add(xCell + "," + yCell + "," + zCell + "," + biomeKey.toString());
            /* continue */
        }
        return out;
    }

    public void loadSerializedLine(java.lang.String s) {
        java.lang.String[] p = s.split(",");
        if (p.length == 4) {
            try {
                int xCell = java.lang.Integer.parseInt(p[0]);
                int yCell = java.lang.Integer.parseInt(p[1]);
                int zCell = java.lang.Integer.parseInt(p[2]);
                org.bukkit.NamespacedKey key = org.bukkit.NamespacedKey.fromString(p[3].toLowerCase(java.util.Locale.ROOT));
                if (key == null) {
                    return;
                }
            }
            catch (java.lang.Exception ex) {
            }
            return;
        }
        return;
    }

}
