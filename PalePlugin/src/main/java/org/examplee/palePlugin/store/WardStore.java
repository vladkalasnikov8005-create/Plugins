package org.examplee.palePlugin.store;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

public final class WardStore {
    private final java.util.HashMap byChunk;
    private int size;

    public WardStore() {
        super();
        this.byChunk = new java.util.HashMap();
        this.size = 0;
    }

    public int size() {
        return size;
    }

    public void add(int x, int y, int z) {
        long ck = org.examplee.palePlugin.store.SourceStore.packChunk(x >> 4, z >> 4);
        ((java.util.ArrayList) byChunk.computeIfAbsent(java.lang.Long.valueOf(ck), (java.lang.Long p0) -> org.examplee.palePlugin.store.WardStore.lambda$add$0(p0))).add(new org.examplee.palePlugin.store.WardStore$Int3(x, y, z));
        this.size = size + 1;
    }

    public boolean remove(int x, int y, int z) {
        long ck = org.examplee.palePlugin.store.SourceStore.packChunk(x >> 4, z >> 4);
        java.util.ArrayList list = (java.util.ArrayList) byChunk.get(java.lang.Long.valueOf(ck));
        if (list == null) {
            return false;
        }
        int i = 0;
        if (i < list.size()) {
            org.examplee.palePlugin.store.WardStore$Int3 p = (org.examplee.palePlugin.store.WardStore$Int3) list.get(i);
            if (p.x == x) {
                if (p.y == y) {
                    if (p.z == z) {
                        int last = list.size() - 1;
                        list.set(i, (org.examplee.palePlugin.store.WardStore$Int3) list.get(last));
                        list.remove(last);
                        this.size = size - 1;
                        if (!(list.isEmpty())) {
                            return true;
                        }
                        byChunk.remove(java.lang.Long.valueOf(ck));
                        return true;
                    }
                }
            }
            i++;
            /* continue */
        }
        return false;
    }

    public boolean isProtected(int x, int y, int z, int radius) {
        int rSq = radius * radius;
        int cRad = (radius >> 4) + 1;
        int cx = x >> 4;
        int cz = z >> 4;
        int dx = -cRad;
        if (dx <= cRad) {
            int dz = -cRad;
            if (dz <= cRad) {
                long ck = org.examplee.palePlugin.store.SourceStore.packChunk(cx + dx, cz + dz);
                java.util.ArrayList list = (java.util.ArrayList) byChunk.get(java.lang.Long.valueOf(ck));
                if (list == null) {
                } else {
                    java.util.Iterator local14 = list.iterator();
                    if (local14.hasNext()) {
                        org.examplee.palePlugin.store.WardStore$Int3 p = (org.examplee.palePlugin.store.WardStore$Int3) local14.next();
                        int ox = p.x - x;
                        int oy = p.y - y;
                        int oz = p.z - z;
                        if (ox * ox + oy * oy + oz * oz <= rSq) {
                            return true;
                        }
                        /* continue */
                    }
                }
                dz++;
                /* continue */
            }
            dx++;
            /* continue */
        }
        return false;
    }

    public java.util.List serialize() {
        java.util.ArrayList out = new java.util.ArrayList(size);
        java.util.Iterator local2 = byChunk.values().iterator();
        if (local2.hasNext()) {
            java.util.ArrayList list = (java.util.ArrayList) local2.next();
            java.util.Iterator local4 = list.iterator();
            if (local4.hasNext()) {
                org.examplee.palePlugin.store.WardStore$Int3 p = (org.examplee.palePlugin.store.WardStore$Int3) local4.next();
                out.add(p.x + "," + p.y + "," + p.z);
                /* continue */
            }
            /* continue */
        }
        return out;
    }

    private static java.util.ArrayList lambda$add$0(java.lang.Long k) {
        return new java.util.ArrayList();
    }

}
