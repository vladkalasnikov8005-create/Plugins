package org.examplee.plague.network;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import org.examplee.plague.PlagueConfig;
import org.examplee.plague.PlagueMod;
import org.examplee.plague.darkness.DarknessEngine;
import org.examplee.plague.leper.LanternData;
import org.examplee.plague.leper.LeperData;
import org.examplee.plague.pale.PaleEngine;
import org.examplee.plague.pale.PaleWorldData;

/** All S2C/C2S payloads + server handlers. Client receivers live in PlagueClient. */
public class ModNetworking {
    public record InfectionSync(boolean leper, int hits, int stage, boolean blessed, long rageLeftMs) implements CustomPacketPayload {
        public static final Type<InfectionSync> TYPE = new Type<>(PlagueMod.id("infection_sync"));
        public static final StreamCodec<RegistryFriendlyByteBuf, InfectionSync> CODEC = StreamCodec.composite(
                ByteBufCodecs.BOOL, InfectionSync::leper,
                ByteBufCodecs.INT, InfectionSync::hits,
                ByteBufCodecs.INT, InfectionSync::stage,
                ByteBufCodecs.BOOL, InfectionSync::blessed,
                ByteBufCodecs.VAR_LONG, InfectionSync::rageLeftMs,
                InfectionSync::new);
        @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
    }

    public record ZoneSync(int paleStage, boolean dark, boolean lantern) implements CustomPacketPayload {
        public static final Type<ZoneSync> TYPE = new Type<>(PlagueMod.id("zone_sync"));
        public static final StreamCodec<RegistryFriendlyByteBuf, ZoneSync> CODEC = StreamCodec.composite(
                ByteBufCodecs.INT, ZoneSync::paleStage,
                ByteBufCodecs.BOOL, ZoneSync::dark,
                ByteBufCodecs.BOOL, ZoneSync::lantern,
                ZoneSync::new);
        @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
    }

    public record MapRequest(boolean dark, int radius) implements CustomPacketPayload {
        public static final Type<MapRequest> TYPE = new Type<>(PlagueMod.id("map_request"));
        public static final StreamCodec<RegistryFriendlyByteBuf, MapRequest> CODEC = StreamCodec.composite(
                ByteBufCodecs.BOOL, MapRequest::dark,
                ByteBufCodecs.INT, MapRequest::radius,
                MapRequest::new);
        @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
    }

    public record MapData(boolean dark, int radius, int max, int extra, byte[] levels) implements CustomPacketPayload {
        public static final Type<MapData> TYPE = new Type<>(PlagueMod.id("map_data"));
        public static final StreamCodec<RegistryFriendlyByteBuf, MapData> CODEC = StreamCodec.composite(
                ByteBufCodecs.BOOL, MapData::dark,
                ByteBufCodecs.INT, MapData::radius,
                ByteBufCodecs.INT, MapData::max,
                ByteBufCodecs.INT, MapData::extra,
                ByteBufCodecs.BYTE_ARRAY, MapData::levels,
                MapData::new);
        @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
    }

    public record WardQuery(int x, int y, int z) implements CustomPacketPayload {
        public static final Type<WardQuery> TYPE = new Type<>(PlagueMod.id("ward_query"));
        public static final StreamCodec<RegistryFriendlyByteBuf, WardQuery> CODEC = StreamCodec.composite(
                ByteBufCodecs.INT, WardQuery::x,
                ByteBufCodecs.INT, WardQuery::y,
                ByteBufCodecs.INT, WardQuery::z,
                WardQuery::new);
        @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
    }

    public record WardInfo(int x, int y, int z, String kind, int charge, int max) implements CustomPacketPayload {
        public static final Type<WardInfo> TYPE = new Type<>(PlagueMod.id("ward_info"));
        public static final StreamCodec<RegistryFriendlyByteBuf, WardInfo> CODEC = StreamCodec.composite(
                ByteBufCodecs.INT, WardInfo::x,
                ByteBufCodecs.INT, WardInfo::y,
                ByteBufCodecs.INT, WardInfo::z,
                ByteBufCodecs.STRING_UTF8, WardInfo::kind,
                ByteBufCodecs.INT, WardInfo::charge,
                ByteBufCodecs.INT, WardInfo::max,
                WardInfo::new);
        @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
    }

    public record Cinematic(long startedAt) implements CustomPacketPayload {
        public static final Type<Cinematic> TYPE = new Type<>(PlagueMod.id("cinematic"));
        public static final StreamCodec<RegistryFriendlyByteBuf, Cinematic> CODEC = StreamCodec.composite(
                ByteBufCodecs.VAR_LONG, Cinematic::startedAt,
                Cinematic::new);
        @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
    }

    public static void register() {
        PayloadTypeRegistry.playS2C().register(InfectionSync.TYPE, InfectionSync.CODEC);
        PayloadTypeRegistry.playS2C().register(ZoneSync.TYPE, ZoneSync.CODEC);
        PayloadTypeRegistry.playS2C().register(MapData.TYPE, MapData.CODEC);
        PayloadTypeRegistry.playS2C().register(WardInfo.TYPE, WardInfo.CODEC);
        PayloadTypeRegistry.playS2C().register(Cinematic.TYPE, Cinematic.CODEC);
        PayloadTypeRegistry.playC2S().register(MapRequest.TYPE, MapRequest.CODEC);
        PayloadTypeRegistry.playC2S().register(WardQuery.TYPE, WardQuery.CODEC);

        ServerPlayNetworking.registerGlobalReceiver(MapRequest.TYPE, (payload, context) -> {
            ServerPlayer p = context.player();
            context.server().execute(() -> handleMapRequest(p, payload));
        });
        ServerPlayNetworking.registerGlobalReceiver(WardQuery.TYPE, (payload, context) -> {
            ServerPlayer p = context.player();
            context.server().execute(() -> handleWardQuery(p, payload));
        });
    }

    public static void sendInfection(ServerPlayer p) {
        LeperData d = LeperData.get(p);
        ServerPlayNetworking.send(p, new InfectionSync(d.leper(), d.hits(), d.stage(), d.blessed(),
                Math.max(0L, d.rageUntil() - System.currentTimeMillis())));
    }

    public static void sendZone(ServerPlayer p) {
        ServerLevel level = p.serverLevel();
        BlockPos pos = p.blockPosition();
        int stage = PaleEngine.get().getStage(level, pos.getX() >> 4, pos.getZ() >> 4);
        boolean dark = level.getBlockState(pos.below()).is(org.examplee.plague.registry.ModBlocks.DARKNESS_BLOCK)
                || level.getBlockState(pos).is(org.examplee.plague.registry.ModBlocks.DARKNESS_BLOCK);
        boolean lantern = LanternData.get(level).isInRange(pos);
        ServerPlayNetworking.send(p, new ZoneSync(stage, dark, lantern));
    }

    public static void sendCinematic(ServerPlayer p) {
        ServerPlayNetworking.send(p, new Cinematic(System.currentTimeMillis()));
    }

    private static void handleMapRequest(ServerPlayer p, MapRequest req) {
        int r = Math.max(1, Math.min(PlagueConfig.INSTANCE.pale.mapMaxRadiusChunks, req.radius()));
        ServerLevel level = p.serverLevel();
        int ccx = p.blockPosition().getX() >> 4, ccz = p.blockPosition().getZ() >> 4;
        if (req.dark()) {
            var m = DarknessEngine.get().mapLevels(level, ccx, ccz, r);
            ServerPlayNetworking.send(p, new MapData(true, r, m.max(), m.total(), m.levels()));
        } else {
            var m = PaleEngine.get().mapLevels(level, ccx, ccz, r);
            ServerPlayNetworking.send(p, new MapData(false, r, m.max(), m.stage(), m.levels()));
        }
    }

    private static void handleWardQuery(ServerPlayer p, WardQuery q) {
        ServerLevel level = p.serverLevel();
        BlockPos pos = new BlockPos(q.x(), q.y(), q.z());
        if (!level.hasChunk(pos.getX() >> 4, pos.getZ() >> 4)) return;
        if (p.blockPosition().distSqr(pos) > 36.0) return;
        PaleWorldData pale = PaleWorldData.get(level);
        Integer great = pale.greatWardCharge(pos);
        if (great != null) {
            ServerPlayNetworking.send(p, new WardInfo(q.x(), q.y(), q.z(), "great",
                    great, PlagueConfig.INSTANCE.pale.greatWardChargeMaxSec));
            return;
        }
        int fuel = LanternData.get(level).fuelAt(pos);
        if (fuel >= 0) {
            ServerPlayNetworking.send(p, new WardInfo(q.x(), q.y(), q.z(), "lantern",
                    fuel, PlagueConfig.INSTANCE.leper.lanternFuelMaxSec));
        }
    }
}
