package org.examplee.plague.client;

import net.minecraft.core.BlockPos;
import org.examplee.plague.network.ModNetworking;

/** Latest server-synced values, written by packet handlers, read by HUD/screens. */
public class ClientState {
    public static volatile boolean leper;
    public static volatile int hits;
    public static volatile int stage;
    public static volatile boolean blessed;
    public static volatile long rageLeftMs;

    public static volatile int paleStage;
    public static volatile boolean dark;
    public static volatile boolean lantern;

    public static volatile long cinematicUntilMs;

    public static volatile ModNetworking.WardInfo ward;
    public static volatile long wardAtMs;
    public static volatile BlockPos wardPos;

    public static boolean cinematicActive() {
        return System.currentTimeMillis() < cinematicUntilMs;
    }

    public static boolean wardFresh() {
        return ward != null && System.currentTimeMillis() - wardAtMs < 3000L;
    }
}
