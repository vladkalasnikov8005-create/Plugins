package com.example.petmonsters.model;

import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import java.util.UUID;

/**
 * A mutable holder binding a player to their tamed mob.
 */
public class Pet {

    private final UUID ownerId;
    private String petName;
    private PetMode mode = PetMode.FOLLOWING;
    private EntityType type;
    private double baseScale = 1.0;
    private double scaleMultiplier = 1.0;

    public Pet(UUID ownerId) {
        this.ownerId = ownerId;
    }

    public UUID getOwnerId() {
        return ownerId;
    }

    public String getPetName() {
        return petName;
    }

    public void setPetName(String petName) {
        this.petName = petName;
    }

    public PetMode getMode() {
        return mode;
    }

    public void setMode(PetMode mode) {
        this.mode = mode;
    }

    public EntityType getType() {
        return type;
    }

    public void setType(EntityType type) {
        this.type = type;
    }

    public double getBaseScale() {
        return baseScale;
    }

    public void setBaseScale(double baseScale) {
        this.baseScale = baseScale;
    }

    public double getScaleMultiplier() {
        return scaleMultiplier;
    }

    public void setScaleMultiplier(double scaleMultiplier) {
        this.scaleMultiplier = scaleMultiplier;
    }

    public double getEffectiveScale() {
        return baseScale * scaleMultiplier;
    }
}
