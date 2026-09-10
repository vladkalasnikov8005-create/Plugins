package org.examplee.plague.leper;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.server.level.ServerPlayer;

/** Persistent per-player infection state (Fabric attachment, saved in player NBT). */
public record LeperData(boolean leper, int hits, int stage, long nextPhaseMs,
                        boolean blessed, long rageUntil, long knifeLastMs,
                        boolean hadPoison, boolean hadRegen) {
    public static final LeperData DEFAULT = new LeperData(false, 0, 0, 0L, false, 0L, 0L, false, false);

    public static final Codec<LeperData> CODEC = RecordCodecBuilder.create(i -> i.group(
            Codec.BOOL.fieldOf("leper").forGetter(LeperData::leper),
            Codec.INT.fieldOf("hits").forGetter(LeperData::hits),
            Codec.INT.fieldOf("stage").forGetter(LeperData::stage),
            Codec.LONG.fieldOf("nextPhaseMs").forGetter(LeperData::nextPhaseMs),
            Codec.BOOL.fieldOf("blessed").forGetter(LeperData::blessed),
            Codec.LONG.fieldOf("rageUntil").forGetter(LeperData::rageUntil),
            Codec.LONG.fieldOf("knifeLastMs").forGetter(LeperData::knifeLastMs),
            Codec.BOOL.fieldOf("hadPoison").forGetter(LeperData::hadPoison),
            Codec.BOOL.fieldOf("hadRegen").forGetter(LeperData::hadRegen)
    ).apply(i, LeperData::new));

    public static LeperData get(ServerPlayer p) {
        LeperData d = p.getAttached(LeperAttachments.LEPER);
        if (d == null) {
            d = DEFAULT;
            p.setAttached(LeperAttachments.LEPER, d);
        }
        return d;
    }

    public static void set(ServerPlayer p, LeperData d) {
        p.setAttached(LeperAttachments.LEPER, d);
    }

    public LeperData withLeper(boolean v) { return new LeperData(v, hits, stage, nextPhaseMs, blessed, rageUntil, knifeLastMs, hadPoison, hadRegen); }
    public LeperData withHits(int v) { return new LeperData(leper, v, stage, nextPhaseMs, blessed, rageUntil, knifeLastMs, hadPoison, hadRegen); }
    public LeperData withStage(int v) { return new LeperData(leper, hits, v, nextPhaseMs, blessed, rageUntil, knifeLastMs, hadPoison, hadRegen); }
    public LeperData withNextPhaseMs(long v) { return new LeperData(leper, hits, stage, v, blessed, rageUntil, knifeLastMs, hadPoison, hadRegen); }
    public LeperData withBlessed(boolean v) { return new LeperData(leper, hits, stage, nextPhaseMs, v, rageUntil, knifeLastMs, hadPoison, hadRegen); }
    public LeperData withRageUntil(long v) { return new LeperData(leper, hits, stage, nextPhaseMs, blessed, v, knifeLastMs, hadPoison, hadRegen); }
    public LeperData withKnifeLastMs(long v) { return new LeperData(leper, hits, stage, nextPhaseMs, blessed, rageUntil, v, hadPoison, hadRegen); }
    public LeperData withEdges(boolean poison, boolean regen) { return new LeperData(leper, hits, stage, nextPhaseMs, blessed, rageUntil, knifeLastMs, poison, regen); }

    public LeperData cured() { return new LeperData(leper, 0, 0, 0L, blessed, rageUntil, knifeLastMs, hadPoison, hadRegen); }
}
