package org.examplee.customdiscs;

import io.papermc.paper.plugin.bootstrap.BootstrapContext;
import io.papermc.paper.plugin.bootstrap.PluginBootstrap;
import io.papermc.paper.registry.RegistryKey;
import io.papermc.paper.registry.TypedKey;
import io.papermc.paper.registry.data.JukeboxSongRegistryEntry;
import io.papermc.paper.registry.event.RegistryEvents;
import io.papermc.paper.registry.event.WritableRegistry;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.JukeboxSong;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * Bootstrap-фаза Paper: выполняется ДО загрузки миров и датапаков.
 * Регистрирует ВСЕ песни из jukebox-songs.list в динамический реестр
 * jukebox_song — поэтому отдельный датапак больше не нужен.
 * Файл заполняется автоматически при /disc import (или авто-импорте на старте).
 */
public final class CustomDiscsBootstrap implements PluginBootstrap {

    private static final LegacyComponentSerializer LEGACY_AMP = LegacyComponentSerializer.legacyAmpersand();

    @Override
    public void bootstrap(BootstrapContext context) {
        List<String> lines = null;
        Path listFile = context.getDataDirectory().resolve("jukebox-songs.list");

        // 1. Файл в dataDir (обновляется /disc import)
        if (Files.exists(listFile)) {
            try {
                lines = Files.readAllLines(listFile);
            } catch (Exception e) {
                context.getLogger().warn("CustomDiscs: не удалось прочитать jukebox-songs.list: " + e.getMessage());
            }
        } else {
            // 2. Первая загрузка — читаем список, вшитый в jar
            try (var zip = new java.util.zip.ZipInputStream(
                    Files.newInputStream(context.getPluginSource()))) {
                var entry = zip.getNextEntry();
                while (entry != null) {
                    if (entry.getName().equals("jukebox-songs.list")) {
                        lines = new String(zip.readAllBytes(), java.nio.charset.StandardCharsets.UTF_8)
                                .lines().toList();
                        break;
                    }
                    entry = zip.getNextEntry();
                }
            } catch (Exception e) {
                context.getLogger().warn("CustomDiscs: не удалось прочитать вшитый реестр песен: " + e.getMessage());
            }
        }

        if (lines == null || lines.isEmpty()) {
            context.getLogger().info("CustomDiscs: реестр песен пуст — ничего не добавлено.");
            return;
        }
        List<String> finalLines = lines;

        context.getLifecycleManager().registerEventHandler(
                RegistryEvents.JUKEBOX_SONG.compose(),
                event -> {
                    WritableRegistry<JukeboxSong, JukeboxSongRegistryEntry.Builder> registry = event.registry();
                    int count = 0;
                    try {
                        for (String line : finalLines) {
                            String trimmed = line.strip();
                            if (trimmed.isEmpty() || trimmed.startsWith("#")) continue;

                            String[] parts = trimmed.split(" ", 4);
                            if (parts.length < 3) continue;

                            try {
                                Key songKey = Key.key(parts[0].toLowerCase());
                                float length = Float.parseFloat(parts[1]);
                                Key soundKey = Key.key(parts[2].toLowerCase());
                                String description = parts.length >= 4 ? parts[3] : parts[0];

                                registry.register(
                                        TypedKey.create(RegistryKey.JUKEBOX_SONG, songKey),
                                        builder -> builder
                                                .soundEvent(factory -> factory.empty().location(soundKey))
                                                .description(LEGACY_AMP.deserialize(description))
                                                .lengthInSeconds(Math.max(1f, length))
                                                .comparatorOutput(15)
                                );
                                count++;
                            } catch (Exception bad) {
                                context.getLogger().warn("CustomDiscs: строка реестра пропущена: '" + trimmed + "' (" + bad.getMessage() + ")");
                            }
                        }
                    } catch (Exception e) {
                        context.getLogger().warn("CustomDiscs: не удалось прочитать jukebox-songs.list: " + e.getMessage());
                    }
                    if (count > 0) {
                        context.getLogger().info("CustomDiscs: зарегистрировано песен в реестре проигрывателя: " + count);
                    }
                }
        );
    }
}
