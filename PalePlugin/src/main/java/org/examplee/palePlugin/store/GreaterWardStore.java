package org.examplee.palePlugin.store;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/** Великие обереги: позиция + заряд (секунды работы очищения). */
public final class GreaterWardStore {
   private final HashMap<Long, ArrayList<GreaterWardStore.Entry>> byChunk = new HashMap<>();
   private int size = 0;

   public int size() {
      return this.size;
   }

   public void add(int x, int y, int z, int charge) {
      long ck = SourceStore.packChunk(x >> 4, z >> 4);
      this.byChunk.computeIfAbsent(ck, k -> new ArrayList<>()).add(new GreaterWardStore.Entry(x, y, z, Math.max(0, charge)));
      this.size++;
   }

   /** @return заряд удалённого оберега или -1, если оберега тут нет */
   public int removeAt(int x, int y, int z) {
      long ck = SourceStore.packChunk(x >> 4, z >> 4);
      ArrayList<GreaterWardStore.Entry> list = this.byChunk.get(ck);
      if (list == null) {
         return -1;
      } else {
         for (int i = 0; i < list.size(); i++) {
            GreaterWardStore.Entry p = list.get(i);
            if (p.x == x && p.y == y && p.z == z) {
               int last = list.size() - 1;
               list.set(i, list.get(last));
               list.remove(last);
               this.size--;
               if (list.isEmpty()) {
                  this.byChunk.remove(ck);
               }

               return p.charge;
            }
         }

         return -1;
      }
   }

   public GreaterWardStore.Entry get(int x, int y, int z) {
      ArrayList<GreaterWardStore.Entry> list = this.byChunk.get(SourceStore.packChunk(x >> 4, z >> 4));
      if (list == null) {
         return null;
      } else {
         for (GreaterWardStore.Entry p : list) {
            if (p.x == x && p.y == y && p.z == z) {
               return p;
            }
         }

         return null;
      }
   }

   /** @return новый заряд или -1, если оберега тут нет */
   public int addCharge(int x, int y, int z, int delta, int max) {
      GreaterWardStore.Entry e = this.get(x, y, z);
      if (e == null) {
         return -1;
      } else {
         e.charge = Math.max(0, Math.min(max, e.charge + delta));
         return e.charge;
      }
   }

   public boolean isProtected(int x, int y, int z, int radius) {
      int rSq = radius * radius;
      int cRad = (radius >> 4) + 1;
      int cx = x >> 4;
      int cz = z >> 4;

      for (int dx = -cRad; dx <= cRad; dx++) {
         for (int dz = -cRad; dz <= cRad; dz++) {
            long ck = SourceStore.packChunk(cx + dx, cz + dz);
            ArrayList<GreaterWardStore.Entry> list = this.byChunk.get(ck);
            if (list != null) {
               for (GreaterWardStore.Entry p : list) {
                  int ox = p.x - x;
                  int oy = p.y - y;
                  int oz = p.z - z;
                  if (ox * ox + oy * oy + oz * oz <= rSq) {
                     return true;
                  }
               }
            }
         }
      }

      return false;
   }

   /** Живые ссылки на все обереги (изменение charge допустимо). */
   public List<GreaterWardStore.Entry> all() {
      ArrayList<GreaterWardStore.Entry> out = new ArrayList<>(this.size);

      for (ArrayList<GreaterWardStore.Entry> list : this.byChunk.values()) {
         out.addAll(list);
      }

      return out;
   }

   public List<String> serialize() {
      ArrayList<String> out = new ArrayList<>(this.size);

      for (ArrayList<GreaterWardStore.Entry> list : this.byChunk.values()) {
         for (GreaterWardStore.Entry p : list) {
            out.add(p.x + "," + p.y + "," + p.z + "," + p.charge);
         }
      }

      return out;
   }

   public static final class Entry {
      public final int x;
      public final int y;
      public final int z;
      public int charge;

      Entry(int x, int y, int z, int charge) {
         this.x = x;
         this.y = y;
         this.z = z;
         this.charge = charge;
      }
   }
}
