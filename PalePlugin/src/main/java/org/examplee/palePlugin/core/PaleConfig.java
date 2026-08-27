package org.examplee.palePlugin.core;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.examplee.palePlugin.util.MathUtil;

public final class PaleConfig {
    private final org.bukkit.plugin.java.JavaPlugin plugin;
    public int speedPerChunk;
    public int maxAttemptsPerTickGlobal;
    public int maxAttemptsPerTickPerWorld;
    public int turboMaxAttemptsPerTickGlobal;
    public int turboMaxAttemptsPerTickPerWorld;
    public int maxSourcesPerWorld;
    public int sourceChanceDivider;
    public double rateMinPerSecPerChunk;
    public double rateMaxPerSecPerChunk;
    public double rateCurvePower;
    public int spreadTriesPerAttempt;
    public int logSourceChanceDivider;
    public int indexChunksPerTickPerWorld;
    public int indexDepth;
    public int saltRadius;
    public long saltCooldownMs;
    public int holyWaterRadius;
    public int purifierFlintRadius;
    public int purifierFlintUsesDefault;
    public int wardRadius;
    public int mapMaxRadiusChunks;
    public int mapItemDefaultRadiusChunks;
    public int infectWandRadius;
    public long infectWandCooldownMs;
    public int infectWandUsesDefault;
    public int infectWandMaxBlocksPerUse;
    public int infectWandBonusSourceChanceDivider;
    public boolean biomeEnabled;
    public java.lang.String infectedBiomeName;
    public boolean stagesEnabled;
    public int stage1Sources;
    public int stage2Sources;
    public int stage3Sources;
    public int stage4Sources;
    public int stage5Sources;
    public boolean effectsEnabled;
    public int effectsCheckPeriodTicks;
    public int effectsMinStage;
    public int effectsSlownessAmpStage2;
    public int effectsWeaknessAmpStage3;
    public int effectsMiningFatigueAmpStage4;
    public boolean effectsDarknessStage5;
    public int stepEffectsCheckPeriodTicks;
    public int stepEffectsDurationTicks;
    public int adminPurgeRadiusChunks;
    public int adminPurgeDepth;
    public int adminPurgeChunksPerTick;
    public boolean adminPurgeOnlyLoadedChunks;

    public PaleConfig(org.bukkit.plugin.java.JavaPlugin plugin) {
        super();
        this.speedPerChunk = 40;
        this.maxAttemptsPerTickGlobal = 3000;
        this.maxAttemptsPerTickPerWorld = 1800;
        this.turboMaxAttemptsPerTickGlobal = 3000;
        this.turboMaxAttemptsPerTickPerWorld = 1800;
        this.maxSourcesPerWorld = 250000;
        this.sourceChanceDivider = 6;
        this.rateMinPerSecPerChunk = 0.0002;
        this.rateMaxPerSecPerChunk = 0.25;
        this.rateCurvePower = 2.0;
        this.spreadTriesPerAttempt = 3;
        this.logSourceChanceDivider = 4;
        this.indexChunksPerTickPerWorld = 1;
        this.indexDepth = 28;
        this.saltRadius = 8;
        this.saltCooldownMs = 1500L;
        this.holyWaterRadius = 10;
        this.purifierFlintRadius = 10;
        this.purifierFlintUsesDefault = 2;
        this.wardRadius = 24;
        this.mapMaxRadiusChunks = 8;
        this.mapItemDefaultRadiusChunks = 6;
        this.infectWandRadius = 6;
        this.infectWandCooldownMs = 800L;
        this.infectWandUsesDefault = 16;
        this.infectWandMaxBlocksPerUse = 900;
        this.infectWandBonusSourceChanceDivider = 2;
        this.biomeEnabled = 1;
        this.infectedBiomeName = "minecraft:pale_garden";
        this.stagesEnabled = 1;
        this.stage1Sources = 5;
        this.stage2Sources = 15;
        this.stage3Sources = 40;
        this.stage4Sources = 90;
        this.stage5Sources = 180;
        this.effectsEnabled = 1;
        this.effectsCheckPeriodTicks = 20;
        this.effectsMinStage = 2;
        this.effectsSlownessAmpStage2 = 0;
        this.effectsWeaknessAmpStage3 = 0;
        this.effectsMiningFatigueAmpStage4 = 0;
        this.effectsDarknessStage5 = 1;
        this.stepEffectsCheckPeriodTicks = 5;
        this.stepEffectsDurationTicks = 60;
        this.adminPurgeRadiusChunks = 8;
        this.adminPurgeDepth = 64;
        this.adminPurgeChunksPerTick = 2;
        this.adminPurgeOnlyLoadedChunks = 1;
        this.plugin = plugin;
    }

    public void setupDefaults() {
        org.bukkit.configuration.file.FileConfiguration cfg = plugin.getConfig();
        cfg.addDefault("spread.speedPerChunk", java.lang.Integer.valueOf(40));
        cfg.addDefault("spread.maxAttemptsPerTickGlobal", java.lang.Integer.valueOf(3000));
        cfg.addDefault("spread.maxAttemptsPerTickPerWorld", java.lang.Integer.valueOf(1800));
        cfg.addDefault("spread.turboMaxAttemptsPerTickGlobal", java.lang.Integer.valueOf(3000));
        cfg.addDefault("spread.turboMaxAttemptsPerTickPerWorld", java.lang.Integer.valueOf(1800));
        cfg.addDefault("spread.maxSourcesPerWorld", java.lang.Integer.valueOf(250000));
        cfg.addDefault("spread.sourceChanceDivider", java.lang.Integer.valueOf(6));
        cfg.addDefault("spread.rateMinPerSecPerChunk", java.lang.Double.valueOf(0.0002));
        cfg.addDefault("spread.rateMaxPerSecPerChunk", java.lang.Double.valueOf(0.25));
        cfg.addDefault("spread.rateCurvePower", java.lang.Double.valueOf(2.0));
        cfg.addDefault("spread.spreadTriesPerAttempt", java.lang.Integer.valueOf(3));
        cfg.addDefault("spread.logSourceChanceDivider", java.lang.Integer.valueOf(4));
        cfg.addDefault("index.indexChunksPerTickPerWorld", java.lang.Integer.valueOf(1));
        cfg.addDefault("index.indexDepth", java.lang.Integer.valueOf(28));
        cfg.addDefault("cleanse.saltRadius", java.lang.Integer.valueOf(8));
        cfg.addDefault("cleanse.saltCooldownMs", java.lang.Integer.valueOf(1500));
        cfg.addDefault("cleanse.holyWaterRadius", java.lang.Integer.valueOf(10));
        cfg.addDefault("cleanse.purifierFlintRadius", java.lang.Integer.valueOf(10));
        cfg.addDefault("cleanse.purifierFlintUses", java.lang.Integer.valueOf(2));
        cfg.addDefault("ward.radius", java.lang.Integer.valueOf(24));
        cfg.addDefault("map.maxRadiusChunks", java.lang.Integer.valueOf(8));
        cfg.addDefault("map.itemDefaultRadiusChunks", java.lang.Integer.valueOf(6));
        cfg.addDefault("wand.radius", java.lang.Integer.valueOf(6));
        cfg.addDefault("wand.cooldownMs", java.lang.Integer.valueOf(800));
        cfg.addDefault("wand.uses", java.lang.Integer.valueOf(16));
        cfg.addDefault("wand.maxBlocksPerUse", java.lang.Integer.valueOf(900));
        cfg.addDefault("wand.bonusSourceChanceDivider", java.lang.Integer.valueOf(2));
        cfg.addDefault("biome.enabled", java.lang.Boolean.valueOf(1));
        cfg.addDefault("biome.infected", "minecraft:pale_garden");
        cfg.addDefault("stages.enabled", java.lang.Boolean.valueOf(1));
        cfg.addDefault("stages.stage1Sources", java.lang.Integer.valueOf(5));
        cfg.addDefault("stages.stage2Sources", java.lang.Integer.valueOf(15));
        cfg.addDefault("stages.stage3Sources", java.lang.Integer.valueOf(40));
        cfg.addDefault("stages.stage4Sources", java.lang.Integer.valueOf(90));
        cfg.addDefault("stages.stage5Sources", java.lang.Integer.valueOf(180));
        cfg.addDefault("effects.enabled", java.lang.Boolean.valueOf(1));
        cfg.addDefault("effects.checkPeriodTicks", java.lang.Integer.valueOf(20));
        cfg.addDefault("effects.minStage", java.lang.Integer.valueOf(2));
        cfg.addDefault("effects.slownessAmpStage2", java.lang.Integer.valueOf(0));
        cfg.addDefault("effects.weaknessAmpStage3", java.lang.Integer.valueOf(0));
        cfg.addDefault("effects.miningFatigueAmpStage4", java.lang.Integer.valueOf(0));
        cfg.addDefault("effects.darknessStage5", java.lang.Boolean.valueOf(1));
        cfg.addDefault("stepEffects.checkPeriodTicks", java.lang.Integer.valueOf(5));
        cfg.addDefault("stepEffects.durationTicks", java.lang.Integer.valueOf(60));
        cfg.addDefault("adminPurge.radiusChunks", java.lang.Integer.valueOf(8));
        cfg.addDefault("adminPurge.depth", java.lang.Integer.valueOf(64));
        cfg.addDefault("adminPurge.chunksPerTick", java.lang.Integer.valueOf(2));
        cfg.addDefault("adminPurge.onlyLoadedChunks", java.lang.Boolean.valueOf(1));
        cfg.options().copyDefaults(1);
        plugin.saveConfig();
    }

    public void load() {
        org.bukkit.configuration.file.FileConfiguration cfg = plugin.getConfig();
        this.speedPerChunk = org.examplee.palePlugin.util.MathUtil.clamp(cfg.getInt("spread.speedPerChunk", 40), 1, 5000);
        this.maxAttemptsPerTickGlobal = org.examplee.palePlugin.util.MathUtil.clamp(cfg.getInt("spread.maxAttemptsPerTickGlobal", 3000), 50, 500000);
        this.maxAttemptsPerTickPerWorld = org.examplee.palePlugin.util.MathUtil.clamp(cfg.getInt("spread.maxAttemptsPerTickPerWorld", 1800), 50, 500000);
        this.turboMaxAttemptsPerTickGlobal = org.examplee.palePlugin.util.MathUtil.clamp(cfg.getInt("spread.turboMaxAttemptsPerTickGlobal", 3000), 1000, 500000);
        this.turboMaxAttemptsPerTickPerWorld = org.examplee.palePlugin.util.MathUtil.clamp(cfg.getInt("spread.turboMaxAttemptsPerTickPerWorld", 1800), 1000, 500000);
        this.maxSourcesPerWorld = org.examplee.palePlugin.util.MathUtil.clamp(cfg.getInt("spread.maxSourcesPerWorld", 250000), 1000, 5000000);
        this.sourceChanceDivider = org.examplee.palePlugin.util.MathUtil.clamp(cfg.getInt("spread.sourceChanceDivider", 6), 1, 50);
        this.rateMinPerSecPerChunk = java.lang.Math.max(1e-12, cfg.getDouble("spread.rateMinPerSecPerChunk", 0.0002));
        this.rateMaxPerSecPerChunk = java.lang.Math.max(rateMinPerSecPerChunk, cfg.getDouble("spread.rateMaxPerSecPerChunk", 0.25));
        this.rateCurvePower = java.lang.Math.max(1.0, cfg.getDouble("spread.rateCurvePower", 2.0));
        this.spreadTriesPerAttempt = org.examplee.palePlugin.util.MathUtil.clamp(cfg.getInt("spread.spreadTriesPerAttempt", 3), 1, 50);
        this.logSourceChanceDivider = org.examplee.palePlugin.util.MathUtil.clamp(cfg.getInt("spread.logSourceChanceDivider", 4), 1, 50);
        this.indexChunksPerTickPerWorld = org.examplee.palePlugin.util.MathUtil.clamp(cfg.getInt("index.indexChunksPerTickPerWorld", 1), 0, 200);
        this.indexDepth = org.examplee.palePlugin.util.MathUtil.clamp(cfg.getInt("index.indexDepth", 28), 1, 128);
        this.saltRadius = org.examplee.palePlugin.util.MathUtil.clamp(cfg.getInt("cleanse.saltRadius", 8), 1, 64);
        this.saltCooldownMs = java.lang.Math.max(0L, cfg.getLong("cleanse.saltCooldownMs", 1500L));
        this.holyWaterRadius = org.examplee.palePlugin.util.MathUtil.clamp(cfg.getInt("cleanse.holyWaterRadius", 10), 1, 64);
        this.purifierFlintRadius = org.examplee.palePlugin.util.MathUtil.clamp(cfg.getInt("cleanse.purifierFlintRadius", 10), 1, 64);
        this.purifierFlintUsesDefault = org.examplee.palePlugin.util.MathUtil.clamp(cfg.getInt("cleanse.purifierFlintUses", 2), 1, 64);
        this.wardRadius = org.examplee.palePlugin.util.MathUtil.clamp(cfg.getInt("ward.radius", 24), 4, 128);
        this.mapMaxRadiusChunks = org.examplee.palePlugin.util.MathUtil.clamp(cfg.getInt("map.maxRadiusChunks", 8), 1, 32);
        this.mapItemDefaultRadiusChunks = org.examplee.palePlugin.util.MathUtil.clamp(cfg.getInt("map.itemDefaultRadiusChunks", 6), 1, mapMaxRadiusChunks);
        this.infectWandRadius = org.examplee.palePlugin.util.MathUtil.clamp(cfg.getInt("wand.radius", 6), 1, 64);
        this.infectWandCooldownMs = java.lang.Math.max(0L, cfg.getLong("wand.cooldownMs", 800L));
        this.infectWandUsesDefault = org.examplee.palePlugin.util.MathUtil.clamp(cfg.getInt("wand.uses", 16), 1, 10000);
        this.infectWandMaxBlocksPerUse = org.examplee.palePlugin.util.MathUtil.clamp(cfg.getInt("wand.maxBlocksPerUse", 900), 10, 50000);
        this.infectWandBonusSourceChanceDivider = org.examplee.palePlugin.util.MathUtil.clamp(cfg.getInt("wand.bonusSourceChanceDivider", 2), 1, 50);
        this.biomeEnabled = cfg.getBoolean("biome.enabled", 1);
        this.infectedBiomeName = cfg.getString("biome.infected", "minecraft:pale_garden");
        if (infectedBiomeName != null) {
            if (infectedBiomeName.isBlank()) {
                this.infectedBiomeName = "minecraft:pale_garden";
            }
        }
        this.infectedBiomeName = "minecraft:pale_garden";
        this.stagesEnabled = cfg.getBoolean("stages.enabled", 1);
        this.stage1Sources = org.examplee.palePlugin.util.MathUtil.clamp(cfg.getInt("stages.stage1Sources", 5), 1, 1000000);
        this.stage2Sources = org.examplee.palePlugin.util.MathUtil.clamp(cfg.getInt("stages.stage2Sources", 15), stage1Sources + 1, 1000000);
        this.stage3Sources = org.examplee.palePlugin.util.MathUtil.clamp(cfg.getInt("stages.stage3Sources", 40), stage2Sources + 1, 1000000);
        this.stage4Sources = org.examplee.palePlugin.util.MathUtil.clamp(cfg.getInt("stages.stage4Sources", 90), stage3Sources + 1, 1000000);
        this.stage5Sources = org.examplee.palePlugin.util.MathUtil.clamp(cfg.getInt("stages.stage5Sources", 180), stage4Sources + 1, 1000000);
        this.effectsEnabled = cfg.getBoolean("effects.enabled", 1);
        this.effectsCheckPeriodTicks = org.examplee.palePlugin.util.MathUtil.clamp(cfg.getInt("effects.checkPeriodTicks", 20), 5, 200);
        this.effectsMinStage = org.examplee.palePlugin.util.MathUtil.clamp(cfg.getInt("effects.minStage", 2), 0, 5);
        this.effectsSlownessAmpStage2 = org.examplee.palePlugin.util.MathUtil.clamp(cfg.getInt("effects.slownessAmpStage2", 0), 0, 5);
        this.effectsWeaknessAmpStage3 = org.examplee.palePlugin.util.MathUtil.clamp(cfg.getInt("effects.weaknessAmpStage3", 0), 0, 5);
        this.effectsMiningFatigueAmpStage4 = org.examplee.palePlugin.util.MathUtil.clamp(cfg.getInt("effects.miningFatigueAmpStage4", 0), 0, 5);
        this.effectsDarknessStage5 = cfg.getBoolean("effects.darknessStage5", 1);
        this.stepEffectsCheckPeriodTicks = org.examplee.palePlugin.util.MathUtil.clamp(cfg.getInt("stepEffects.checkPeriodTicks", 5), 1, 200);
        this.stepEffectsDurationTicks = org.examplee.palePlugin.util.MathUtil.clamp(cfg.getInt("stepEffects.durationTicks", 60), 20, 1200);
        this.adminPurgeRadiusChunks = org.examplee.palePlugin.util.MathUtil.clamp(cfg.getInt("adminPurge.radiusChunks", 8), 1, 32);
        this.adminPurgeDepth = org.examplee.palePlugin.util.MathUtil.clamp(cfg.getInt("adminPurge.depth", 64), 1, 256);
        this.adminPurgeChunksPerTick = org.examplee.palePlugin.util.MathUtil.clamp(cfg.getInt("adminPurge.chunksPerTick", 2), 1, 50);
        this.adminPurgeOnlyLoadedChunks = cfg.getBoolean("adminPurge.onlyLoadedChunks", 1);
    }

    public double effectiveAttemptsPerSecondPerChunk() {
        double s = (double) java.lang.Math.max(1, java.lang.Math.min(5000, speedPerChunk));
        double t = (s - 1.0) / 4999.0;
        double shaped = java.lang.Math.pow(t, rateCurvePower);
        double min = java.lang.Math.max(1e-12, rateMinPerSecPerChunk);
        double max = java.lang.Math.max(min, rateMaxPerSecPerChunk);
        return min * java.lang.Math.pow(max / min, shaped);
    }

    public static double lerp(double a, double b, double t) {
        if (Double.compare(t, 0.0) > 0) {
            if (Double.compare(t, 1.0) >= 0) {
                return b;
            }
        }
        return a;
    }

}
