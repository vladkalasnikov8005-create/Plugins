package org.examplee.palePlugin.store;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;

public final class SourceStore {
   private ArrayList<Long> list = new ArrayList<>();
   private final HashSet<Long> set = new HashSet<>();
   private final HashMap<Long, Integer> countByChunk = new HashMap<>();

   public int size() {
      return this.set.size();
   }

   public int getChunkCount(int chunkX, int chunkZ) {
      return this.countByChunk.getOrDefault(packChunk(chunkX, chunkZ), 0);
   }

   public void add(int x, int y, int z) {
      long key = pack(x, y, z);
      if (this.set.add(key)) {
         this.list.add(key);
         this.incChunk(x >> 4, z >> 4, 1);
      }
   }

   public void remove(int x, int y, int z) {
      long key = pack(x, y, z);
      if (this.set.remove(key)) {
         this.incChunk(x >> 4, z >> 4, -1);
      }
   }

   public void compactIfNeeded() {
      if (this.list.size() > this.set.size() * 2 + 64) {
         ArrayList<Long> nl = new ArrayList<>(this.set.size());
         nl.addAll(this.set);
         this.list = nl;
      }
   }

   public Block getRandomLiveSource(World world, Random rnd, Set<Material> infectedTypes) {
      for (int tries = 0; tries < 250; tries++) {
         if (this.list.isEmpty()) {
            return null;
         }

         int idx = rnd.nextInt(this.list.size());
         long key = this.list.get(idx);
         if (!this.set.contains(key)) {
            swapRemove(this.list, idx);
         } else {
            int x = unpackX(key);
            int y = unpackY(key);
            int z = unpackZ(key);
            if (world.isChunkLoaded(x >> 4, z >> 4)) {
               Block b = world.getBlockAt(x, y, z);
               if (infectedTypes.contains(b.getType())) {
                  return b;
               }

               this.set.remove(key);
               this.incChunk(x >> 4, z >> 4, -1);
               swapRemove(this.list, idx);
            }
         }
      }

      return null;
   }

   private void incChunk(int cx, int cz, int delta) {
      long ck = packChunk(cx, cz);
      int v = this.countByChunk.getOrDefault(ck, 0) + delta;
      if (v <= 0) {
         this.countByChunk.remove(ck);
      } else {
         this.countByChunk.put(ck, v);
      }
   }

   private static void swapRemove(ArrayList<Long> list, int idx) {
      int last = list.size() - 1;
      if (idx != last) {
         list.set(idx, list.get(last));
      }

      list.remove(last);
   }

   public static long packChunk(int cx, int cz) {
      return (long)cx << 32 | (long)cz & 4294967295L;
   }

   private static long pack(int x, int y, int z) {
      int yEnc = y + 2048;
      long xx = (long)x & 67108863L;
      long zz = (long)z & 67108863L;
      long yy = (long)yEnc & 4095L;
      return xx << 38 | zz << 12 | yy;
   }

   private static int unpackX(long key) {
      int x = (int)(key >> 38);
      if ((x & 33554432) != 0) {
         x |= -67108864;
      }

      return x;
   }

   private static int unpackZ(long key) {
      int z = (int)(key >> 12 & 67108863L);
      if ((z & 33554432) != 0) {
         z |= -67108864;
      }

      return z;
   }

   private static int unpackY(long key) {
      int yEnc = (int)(key & 4095L);
      return yEnc - 2048;
   }
}
