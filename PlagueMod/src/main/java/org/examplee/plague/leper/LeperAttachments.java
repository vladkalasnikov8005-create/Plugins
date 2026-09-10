package org.examplee.plague.leper;

import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import org.examplee.plague.PlagueMod;

public class LeperAttachments {
    public static AttachmentType<LeperData> LEPER;

    public static void register() {
        LEPER = AttachmentRegistry.createPersistent(PlagueMod.id("leper"), LeperData.CODEC);
    }
}
