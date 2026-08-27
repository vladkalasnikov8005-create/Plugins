package org.examplee.leperClassPlugin.data;

import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataType;
import org.examplee.leperClassPlugin.core.LeperKeys;

public final class LeperData {
    private final LeperKeys keys;

    public LeperData(LeperKeys keys) {
        this.keys = keys;
    }

    public boolean isLeper(Player p) {
        Byte val = p.getPersistentDataContainer().get(keys.leperKey, PersistentDataType.BYTE);
        return val != null && val == 1;
    }

    public void setLeper(Player p, boolean val) {
        if (val) {
            p.getPersistentDataContainer().set(keys.leperKey, PersistentDataType.BYTE, (byte) 1);
        } else {
            p.getPersistentDataContainer().remove(keys.leperKey);
        }
    }

    public int getInfectionHits(Player p) {
        return p.getPersistentDataContainer().getOrDefault(keys.infectionHitsKey, PersistentDataType.INTEGER, 0);
    }

    public void setInfectionHits(Player p, int hits) {
        p.getPersistentDataContainer().set(keys.infectionHitsKey, PersistentDataType.INTEGER, hits);
    }

    public int getInfectionStage(Player p) {
        return p.getPersistentDataContainer().getOrDefault(keys.infectionStageKey, PersistentDataType.INTEGER, 0);
    }

    public void setInfectionStage(Player p, int stage) {
        p.getPersistentDataContainer().set(keys.infectionStageKey, PersistentDataType.INTEGER, stage);
    }

    public Long getInfectionNextPhaseMs(Player p) {
        return p.getPersistentDataContainer().get(keys.infectionNextPhaseKey, PersistentDataType.LONG);
    }

    public void setInfectionNextPhaseMs(Player p, long ms) {
        p.getPersistentDataContainer().set(keys.infectionNextPhaseKey, PersistentDataType.LONG, ms);
    }

    public void clearInfection(Player p) {
        p.getPersistentDataContainer().remove(keys.infectionHitsKey);
        p.getPersistentDataContainer().remove(keys.infectionStageKey);
        p.getPersistentDataContainer().remove(keys.infectionNextPhaseKey);
    }

    public boolean isDangerBlessed(Player p) {
        Byte val = p.getPersistentDataContainer().get(keys.dangerBlessKey, PersistentDataType.BYTE);
        return val != null && val == 1;
    }

    public void setDangerBlessed(Player p, boolean blessed) {
        if (blessed) {
            p.getPersistentDataContainer().set(keys.dangerBlessKey, PersistentDataType.BYTE, (byte) 1);
        } else {
            p.getPersistentDataContainer().remove(keys.dangerBlessKey);
        }
    }

    public long getRageUntil(Player p) {
        Long until = p.getPersistentDataContainer().get(keys.rageUntilKey, PersistentDataType.LONG);
        return until == null ? 0L : until;
    }

    public void setRageUntil(Player p, long ms) {
        p.getPersistentDataContainer().set(keys.rageUntilKey, PersistentDataType.LONG, ms);
    }
}
