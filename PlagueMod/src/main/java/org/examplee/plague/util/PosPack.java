package org.examplee.plague.util;

import net.minecraft.core.BlockPos;

/** Compact long packing for block positions and chunks. */
public class PosPack {
    public static long pack(int x, int y, int z) {
        long xx = (long) x & 0x3FFFFFFL;
        long zz = (long) z & 0x3FFFFFFL;
        long yy = (long) (y + 2048) & 0xFFFL;
        return xx << 38 | zz << 12 | yy;
    }

    public static long pack(BlockPos p) { return pack(p.getX(), p.getY(), p.getZ()); }
    public static long packChunk(int cx, int cz) { return ((long) cx << 32) | (cz & 0xFFFFFFFFL); }
    public static int chunkX(long chunkKey) { return (int) (chunkKey >> 32); }
    public static int chunkZ(long chunkKey) { return (int) chunkKey; }

    public static int x(long k) { int v = (int) (k >> 38 & 0x3FFFFFFL); return (v & 0x2000000) != 0 ? v | ~0x3FFFFFF : v; }
    public static int z(long k) { int v = (int) (k >> 12 & 0x3FFFFFFL); return (v & 0x2000000) != 0 ? v | ~0x3FFFFFF : v; }
    public static int y(long k) { return (int) (k & 0xFFFL) - 2048; }

    public static BlockPos unpack(long k) { return new BlockPos(x(k), y(k), z(k)); }
}
