package org.examplee.customdiscs;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Авто-импорт песен: кидаешь файл в plugins/CustomDiscs/songs/ →
 * /disc import (или перезапуск) → плагин делает всё сам:
 * конвертация → текстура → модель → кейсы в паке → sounds.json →
 * конфиг → реестр песен → сборка SmakcraftMusic.zip.
 */
public final class AutoImportManager {

    private static final Set<String> AUDIO_EXT = Set.of("mp3", "wav", "ogg", "flac", "m4a", "aac", "opus", "wma");
    private static final NamedTextColor OK = NamedTextColor.GREEN;
    private static final NamedTextColor BAD = NamedTextColor.RED;
    private static final NamedTextColor NOTE = NamedTextColor.YELLOW;

    private final CustomDiscsPlugin plugin;
    private final DiscManager discs;
    private final PackBuilder pack;
    private final AudioConverter audio;

    public AutoImportManager(CustomDiscsPlugin plugin, DiscManager discs) {
        this.plugin = plugin;
        this.discs = discs;
        this.pack = new PackBuilder(plugin);
        this.audio = new AudioConverter(plugin);
    }

    public PackBuilder packBuilder() {
        return pack;
    }

    /** (Пере)создать ассеты (текстура/модель/кейсы/звук) для ВСЕХ пластинок из конфига. */
    public void syncAllDiscs() throws IOException {
        pack.ensureWorkspace();
        for (Disc d : discs.all()) {
            try {
                // Диски без нашего звука (например «Дора» из KSEPSP) не трогаем
                if (!d.sound().startsWith("smakcraft:records.")) continue;
                // ogg из кэша, если рабочая папка пересоздавалась
                if (d.sound().startsWith("smakcraft:records.")) {
                    String sid = d.sound().substring("smakcraft:records.".length());
                    Path oggWork = pack.workDir().resolve("assets/smakcraft/sounds/records/" + sid + ".ogg");
                    if (!Files.exists(oggWork)) {
                        Path cached = plugin.getDataFolder().toPath().resolve("cache/" + sid + ".ogg");
                        if (Files.exists(cached)) {
                            Files.createDirectories(oggWork.getParent());
                            Files.copy(cached, oggWork, StandardCopyOption.REPLACE_EXISTING);
                        }
                    }
                }
                pack.registerDiscAssets(d.id(), d.displayName());
            } catch (Exception e) {
                plugin.getLogger().warning("Не удалось добавить ассеты '" + d.id() + "' в пак: " + e.getMessage());
            }
        }
    }

    /** Гарантированно собрать полный пак: рабочая папка + ассеты всех дисков + zip. */
    public Path rebuildPack(CommandSender reporter) {
        try {
            syncAllDiscs();
            Path zip = pack.rebuildZip();
            say(reporter, net.kyori.adventure.text.Component.text(
                    "Текстурпак пересобран: " + zip + " (+ .sha1)", OK));
            return zip;
        } catch (IOException e) {
            say(reporter, net.kyori.adventure.text.Component.text(
                    "Пак не собрался: " + e.getMessage(), BAD));
            return null;
        }
    }

    /** Импортировать всё из папки songs/. reporter == null → только лог. */
    public void importAll(CommandSender reporter) {
        File songsDir = plugin.getDataFolder().toPath().resolve("songs").toFile();
        if (!songsDir.exists() && songsDir.mkdirs()) {
            writeHint(songsDir);
        }
        File[] files = songsDir.listFiles((dir, name) -> {
            int dot = name.lastIndexOf('.');
            return dot >= 0 && AUDIO_EXT.contains(name.substring(dot + 1).toLowerCase(Locale.ROOT));
        });

        if (files == null || files.length == 0) {
            say(reporter, Component.text("В папке plugins/CustomDiscs/songs/ нет аудиофайлов (mp3, ogg, wav...). Кидаешь файлы — и всё импортируется!", NOTE));
            if (plugin.getConfig().getBoolean("auto-rebuild-pack", true)) {
                rebuildPack(null); // держать пак в синхроне с конфигом
            }
            return;
        }

        try {
            pack.ensureWorkspace();
        } catch (IOException e) {
            say(reporter, Component.text("Не удалось подготовить рабочую папку пака: " + e.getMessage(), BAD));
            return;
        }

        List<String> jukeboxLines = readJukeboxList();
        int added = 0, skipped = 0, failed = 0, n = 2;

        for (File file : files) {
            String baseName = file.getName().substring(0, file.getName().lastIndexOf('.'));
            String id = Translit.toId(baseName);
            if (id.isBlank()) id = "song" + (added + skipped + failed + 1);
            String unique = id;
            while (taken(unique, jukeboxLines)) {
                unique = id + "_" + (n++);
            }
            id = unique;

            say(reporter, Component.text("Импорт: " + file.getName() + " ...", NOTE));
            try {
                if (importOne(file, baseName, id, jukeboxLines, reporter)) {
                    added++;
                } else {
                    failed++;
                }
            } catch (Exception e) {
                failed++;
                say(reporter, Component.text("  ОШИБКА: " + e.getMessage(), BAD));
                plugin.getLogger().warning("Импорт '" + file.getName() + "' упал: " + e);
            }
        }

        if (added > 0) {
            writeJukeboxList(jukeboxLines);
            rebuildPack(reporter);
            say(reporter, Component.text("Импортировано песен: " + added
                    + (failed > 0 ? " (ошибок: " + failed + ")" : ""), OK));
            say(reporter, Component.text("ПЕРЕЗАПУСТИ СЕРВЕР, чтобы песни заработали в проигрывателе (jukebox).", NOTE));
            say(reporter, Component.text("Раздай игрокам новый SmakcraftMusic.zip — он лежит в plugins/CustomDiscs/", NOTE));
        } else {
            if (added == 0 && failed == 0) {
                say(reporter, Component.text("Новых песен не найдено.", NOTE));
            }
            if (plugin.getConfig().getBoolean("auto-rebuild-pack", true)) {
                rebuildPack(null);
            }
        }
    }

    private boolean taken(String id, List<String> jukeboxLines) {
        if (discs.get(id) != null) return true;
        for (String line : jukeboxLines) {
            if (line.startsWith("smakcraft:" + id + " ")) return true;
        }
        return false;
    }

    private boolean importOne(File file, String baseName, String id, List<String> jukeboxLines, CommandSender reporter) throws IOException {
        // 1. Аудио → моно ogg
        Path oggTarget = pack.workDir().resolve("assets/smakcraft/sounds/records/" + id + ".ogg");
        AudioConverter.Result r = audio.toMonoOgg(file.toPath(), oggTarget);
        if (!r.ok()) {
            say(reporter, Component.text("  ОШИБКА: " + r.message(), BAD));
            return false;
        }
        double duration = r.durationSeconds() > 0 ? r.durationSeconds() : audio.probeDuration(oggTarget);
        if (duration <= 0) duration = 150.0;
        say(reporter, Component.text("  Аудио готово (" + fmtTime(duration) + ")" + (r.message().isEmpty() ? "" : " — " + r.message()), OK));
        // Кэш конвертированного ogg — чтобы пак можно было пересобрать в любой момент
        Files.createDirectories(plugin.getDataFolder().toPath().resolve("cache"));
        Files.copy(oggTarget, plugin.getDataFolder().toPath().resolve("cache/" + id + ".ogg"),
                StandardCopyOption.REPLACE_EXISTING);

        // 2. Текстура/модель/кейсы/sounds.json
        String displayName = baseName;
        pack.registerDiscAssets(id, displayName);
        say(reporter, Component.text("  Текстура: " + id + ".png, модель и кейсы добавлены в пак", OK));

        // 3. Конфиг
        Material material = Material.matchMaterial(plugin.getConfig().getString("auto-material", "MUSIC_DISC_13"));
        if (material == null) material = Material.MUSIC_DISC_13;
        List<String> lore = List.of(
                "&8SmakcraftMusic",
                "&dТрек: &f" + displayName,
                "&7Длительность: &f" + fmtTime(duration));
        discs.saveToConfig(new Disc(id, displayName, material, lore,
                "smakcraft:records." + id, "smakcraft:" + id));

        // 4. Реестр песен (bootstrap подхватит при старте)
        jukeboxLines.removeIf(l -> l.startsWith("smakcraft:" + id + " "));
        jukeboxLines.add("smakcraft:" + id + " " + String.format(Locale.ROOT, "%.1f", duration)
                + " smakcraft:records." + id + " " + displayName);

        // 5. Исходник — в songs/imported/
        Path importedDir = file.toPath().getParent().resolve("imported");
        Files.createDirectories(importedDir);
        Files.move(file.toPath(), importedDir.resolve(file.getName()), StandardCopyOption.REPLACE_EXISTING);
        return true;
    }

    /* ==================== jukebox-songs.list ==================== */

    private List<String> readJukeboxList() {
        Path list = plugin.getDataFolder().toPath().resolve("jukebox-songs.list");
        try {
            if (Files.exists(list)) {
                List<String> lines = new ArrayList<>(Files.readAllLines(list, StandardCharsets.UTF_8));
                boolean migrated = false;
                for (int i = 0; i < lines.size(); i++) {
                    if (lines.get(i).contains("customdiscs:")) {
                        lines.set(i, lines.get(i).replace("customdiscs:", "smakcraft:"));
                        migrated = true;
                    }
                }
                if (migrated) Files.write(list, lines, StandardCharsets.UTF_8);
                return lines;
            }
        } catch (IOException ignored) {
        }
        return new ArrayList<>(List.of(
                "# SmakcraftMusic: реестр песен для проигрывателя (обновляется автоматически).",
                "# Формат: <ключ_песни> <длительность> <ключ_звука> <описание...>"
        ));
    }

    private void writeJukeboxList(List<String> lines) {
        try {
            Files.write(plugin.getDataFolder().toPath().resolve("jukebox-songs.list"),
                    lines, StandardCharsets.UTF_8);
        } catch (IOException e) {
            plugin.getLogger().warning("Не удалось записать jukebox-songs.list: " + e.getMessage());
        }
    }

    private void writeHint(File songsDir) {
        try {
            Files.writeString(new File(songsDir, "КИДАЙ_ПЕСНИ_СЮДА.txt").toPath(),
                    "Кидай сюда аудиофайлы (mp3, ogg, wav, flac, m4a...), затем:\n"
                    + "  - перезапусти сервер, ИЛИ\n"
                    + "  - напиши /disc import\n"
                    + "Название файла станет названием пластинки.\n",
                    StandardCharsets.UTF_8);
        } catch (IOException ignored) {
        }
    }

    private String fmtTime(double sec) {
        int total = (int) Math.round(sec);
        return total / 60 + ":" + String.format(Locale.ROOT, "%02d", total % 60);
    }

    private void say(CommandSender to, Component msg) {
        if (to != null) to.sendMessage(msg);
        plugin.getLogger().info(net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer.plainText().serialize(msg));
    }
}
