package org.examplee.customdiscs;

import org.bukkit.Material;

import java.util.List;

/** Описание одной пластинки из config.yml. */
public record Disc(
        String id,
        String displayName,     // точное имя предмета = триггер текстурпака (без цветов!)
        Material material,      // базовый ванильный диск, напр. MUSIC_DISC_13
        List<String> lore,      // строки с &-кодами (можно пусто)
        String sound,           // звук для /disc play, напр. "customdiscs:records.xxx"
        String jukeboxSong      // ключ песни в реестре (регистрирует bootstrap), напр. "customdiscs:xxx"
) {
}
