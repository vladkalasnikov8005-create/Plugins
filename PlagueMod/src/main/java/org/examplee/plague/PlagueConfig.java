package org.examplee.plague;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/** All balance numbers, ported 1:1 from LeperClass/PalePlugin configs. Stored as config/plague.json. */
public class PlagueConfig {
    public static PlagueConfig INSTANCE = new PlagueConfig();

    public Leper leper = new Leper();
    public Pale pale = new Pale();
    public Dark dark = new Dark();

    public static class Leper {
        public long infectionPhaseMinutes = 20;
        public double contactInfectPerHp = 0.015;
        public double contactFakeScareChance = 0.20;
        public long contactResolveMinutes = 20;
        public long knifeCooldownMinutes = 60;
        public long knifeFatigueMinutes = 5;
        public long knifeWeakMinutes = 10;
        public long knifeSlowMinutes = 10;
        public long sneezeCooldownSeconds = 10;
        public double sneezeVelocity = 1.15;
        public int sneezeMaxDistance = 12;
        public long sneezePoisonSeconds = 10;
        public long sneezeSlowSeconds = 10;
        public long sneezeWeakSeconds = 5;
        public long sneezeBlindSeconds = 10;
        public double healFromPoison = 6.0;
        public double healFromHarm = 8.0;
        public double damageFromHeal = 6.0;
        public boolean paleHomeEnabled = true;
        public int paleHomeMinStage = 2;
        public int paleHomeVerticalRange = 5;
        public int paleHomeRegenAmplifier = 0;
        public boolean deathInfectEnabled = true;
        public int deathInfectRadius = 4;
        public int deathInfectMaxBlocks = 150;
        public int lanternRadius = 16;
        public int lanternFuelPerDustSec = 300;
        public int lanternFuelMaxSec = 3600;
        public int lanternStartFuelSec = 300;
    }

    public static class Pale {
        public int speedPerChunk = 40;
        public int maxAttemptsPerTickGlobal = 3000;
        public int maxAttemptsPerTickPerWorld = 1800;
        public int turboMaxGlobal = 3000;
        public int turboMaxPerWorld = 1800;
        public int maxSourcesPerWorld = 250000;
        public int sourceChanceDivider = 6;
        public int logSourceChanceDivider = 4;
        public double rateMin = 0.0002;
        public double rateMax = 0.25;
        public double rateCurvePower = 2.0;
        public int spreadTriesPerAttempt = 3;
        public int indexChunksPerTickPerWorld = 1;
        public int indexDepth = 28;
        public int saltRadius = 8;
        public long saltCooldownMs = 1500;
        public int holyWaterRadius = 10;
        public int flintRadius = 10;
        public int flintUses = 2;
        public int wardRadius = 24;
        public int greatWardRadius = 48;
        public int greatWardCleanIntervalSec = 10;
        public int greatWardChargePerBottleSec = 300;
        public int greatWardChargeMaxSec = 3600;
        public int mapMaxRadiusChunks = 8;
        public int mapDefaultRadiusChunks = 6;
        public int wandRadius = 6;
        public long wandCooldownMs = 800;
        public int wandUses = 16;
        public int wandMaxBlocks = 900;
        public int wandBonusSourceDivider = 2;
        public boolean biomeEnabled = true;
        public String infectedBiome = "minecraft:pale_garden";
        public boolean stagesEnabled = true;
        public int stage1 = 5;
        public int stage2 = 15;
        public int stage3 = 40;
        public int stage4 = 90;
        public int stage5 = 180;
        public boolean effectsEnabled = true;
        public int effectsPeriodTicks = 20;
        public int effectsMinStage = 2;
        public int slowAmp2 = 0;
        public int weakAmp3 = 0;
        public int fatigueAmp4 = 0;
        public boolean darknessStage5 = true;
        public int stepPeriodTicks = 5;
        public int stepDurationTicks = 60;
        public int purgeRadiusChunks = 8;
        public int purgeDepth = 64;
        public int purgeChunksPerTick = 2;
        public boolean purgeOnlyLoaded = true;
        public boolean running = false;
    }

    public static class Dark {
        public boolean enabled = true;
        public boolean infectAll = false;
        public int infectSpeed = 40;
        public int growthSpeed = 20;
        public int maxBlocksPerWorld = 20000000;
    }

    /** Attempts per second per loaded chunk, same exponential curve as the plugin. */
    public static double effectiveRatePerSecPerChunk() {
        Pale p = INSTANCE.pale;
        double s = Math.max(1, Math.min(5000, p.speedPerChunk));
        double t = (s - 1.0) / 4999.0;
        double shaped = Math.pow(t, Math.max(1.0, p.rateCurvePower));
        double min = Math.max(1e-12, p.rateMin);
        double max = Math.max(min, p.rateMax);
        return min * Math.pow(max / min, shaped);
    }

    private static Path file() {
        return FabricLoader.getInstance().getConfigDir().resolve("plague.json");
    }

    public static void load() {
        Gson gson = new Gson();
        Path f = file();
        if (Files.exists(f)) {
            try {
                String json = Files.readString(f);
                PlagueConfig loaded = gson.fromJson(json, PlagueConfig.class);
                if (loaded != null) {
                    if (loaded.leper != null) INSTANCE.leper = loaded.leper;
                    if (loaded.pale != null) INSTANCE.pale = loaded.pale;
                    if (loaded.dark != null) INSTANCE.dark = loaded.dark;
                }
            } catch (Exception e) {
                PlagueMod.LOGGER.warn("[PlagueMod] bad config, using defaults: {}", e.toString());
            }
        }
        save();
    }

    public static void save() {
        try {
            Path f = file();
            Files.createDirectories(f.getParent());
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            Files.writeString(f, gson.toJson(INSTANCE));
        } catch (IOException e) {
            PlagueMod.LOGGER.warn("[PlagueMod] cannot save config: {}", e.toString());
        }
    }
}
