package org.examplee.customdiscs;

import org.bukkit.Material;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/**
 * Сборщик пака SmakcraftMusic.
 * Рабочая папка: plugins/CustomDiscs/pack/
 * Если рядом лежит zip исходного пака (например KSEPSP) — он берётся за основу,
 * и все новые диски вшиваются прямо в него. Итог: plugins/CustomDiscs/SmakcraftMusic.zip
 */
public final class PackBuilder {

    private final CustomDiscsPlugin plugin;
    private final Path work;
    private boolean announcedSource;

    public PackBuilder(CustomDiscsPlugin plugin) {
        this.plugin = plugin;
        this.work = plugin.getDataFolder().toPath().resolve("pack");
    }

    public Path workDir() {
        return work;
    }

    public Path packZip() {
        return plugin.getDataFolder().toPath().resolve("SmakcraftMusic.zip");
    }

    /* ========================= РАБОЧЕЕ ПРОСТРАНСТВО ========================= */

    /** Готовит рабочую папку: из исходного пака (если найден) или с нуля. */
    public void ensureWorkspace() throws IOException {
        Path marker = work.resolve(".cd-ready");
        String sourceDesc = describeSource();
        if (Files.exists(marker)) {
            String prev = Files.readString(marker, StandardCharsets.UTF_8).strip();
            if (prev.equals(sourceDesc)) return; // рабочая папка актуальна
            plugin.getLogger().info("PackBuilder: исходный пак изменился — пересоздаю рабочую папку.");
            deleteRecursively(work);
        }

        Files.createDirectories(work);
        Path source = resolveSourcePath(plugin.getConfig().getString("resourcepack-source", "").strip());

        if (source != null) {
            plugin.getLogger().info("PackBuilder: беру за основу " + source.getFileName());
            unzip(source, work);
        } else {
            plugin.getLogger().info("PackBuilder: исходный пак не найден — собираю самостоятельный "
                    + "(положи zip исходного пака в папку плагина для слияния).");
            createStandaloneSkeleton();
        }
        Files.writeString(work.resolve(".cd-ready"), sourceDesc, StandardCharsets.UTF_8);
    }

    private String describeSource() {
        Path p = resolveSourcePathQuiet(plugin.getConfig().getString("resourcepack-source", "").strip());
        return (p == null ? "<standalone>" : p + ":" + AudioConverter.sha1Hex(p)) + ";v=smakcraft2";
    }

    /** Путь к исходному паку: из конфига или автоопределение (единственный zip в папке плагина). */
    private Path resolveSourcePath(String sourceCfg) {
        Path p = resolveSourcePathQuiet(sourceCfg);
        if (p != null && !announcedSource) {
            plugin.getLogger().info("PackBuilder: автоопределён исходный пак: " + p.getFileName());
            announcedSource = true;
        }
        return p;
    }

    private Path resolveSourcePathQuiet(String sourceCfg) {
        if (!sourceCfg.isEmpty()) {
            Path p = Path.of(sourceCfg);
            if (!p.isAbsolute()) {
                Path inData = plugin.getDataFolder().toPath().resolve(sourceCfg);
                p = Files.exists(inData) ? inData : p;
            }
            return Files.isRegularFile(p) ? p : null;
        }
        try (var s = Files.list(plugin.getDataFolder().toPath())) {
            List<Path> zips = s.filter(f -> f.getFileName().toString().toLowerCase(Locale.ROOT).endsWith(".zip"))
                    .filter(Files::isRegularFile).toList();
            if (zips.size() == 1) return zips.get(0);
        } catch (IOException ignored) {
        }
        return null;
    }

    private void createStandaloneSkeleton() throws IOException {
        String mcmeta = "{\n  \"pack\": {\n\t\"pack_format\": 88,\n"
                + "\t\"supported_formats\": {\"min_inclusive\": 55, \"max_inclusive\": 999},\n"
                + "\t\"description\": \"§bSmakcraftMusic§r\"\n  }\n}";
        Files.writeString(work.resolve("pack.mcmeta"), mcmeta, StandardCharsets.UTF_8);

        Path itemsDir = work.resolve("assets/minecraft/items");
        Files.createDirectories(itemsDir);
        for (Material m : Material.values()) {
            if (!m.name().startsWith("MUSIC_DISC")) continue;
            String itemId = "music_disc_" + m.name().substring("MUSIC_DISC_".length()).toLowerCase(Locale.ROOT);
            String json = "{\n\"model\": {\n\t\"type\": \"minecraft:select\",\n"
                    + "\t\"property\": \"minecraft:component\",\n"
                    + "\t\"component\": \"minecraft:custom_name\",\n"
                    + "\"cases\": [\n    ],\n"
                    + "\t\"fallback\": {\n\t\t\"type\": \"minecraft:model\",\n"
                    + "\t\t\"model\": \"minecraft:item/" + itemId + "\"\n\t}\n}\n}";
            Files.writeString(itemsDir.resolve(itemId + ".json"), json, StandardCharsets.UTF_8);
        }
    }

    private void unzip(Path zip, Path dest) throws IOException {
        try (ZipInputStream zis = new ZipInputStream(Files.newInputStream(zip))) {
            ZipEntry e;
            while ((e = zis.getNextEntry()) != null) {
                Path out = dest.resolve(e.getName()).normalize();
                if (!out.startsWith(dest)) continue;
                if (e.isDirectory()) {
                    Files.createDirectories(out);
                } else {
                    Files.createDirectories(out.getParent());
                    Files.copy(zis, out, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                }
            }
        }
    }

    /* ========================= ДОБАВЛЕНИЕ ПЛАСТИНКИ ========================= */

    /** Текстура + модель + кейсы в item-файлах + sounds.json. */
    public void registerDiscAssets(String id, String displayName) throws IOException {
        ensureWorkspace();

        // 1. Текстура: случайный дизайн, кэшируется — после импорта не меняется
        Path cachePng = plugin.getDataFolder().toPath().resolve("cache/" + id + ".png");
        Path tex = work.resolve("assets/smakcraft/textures/item/" + id + ".png");
        Files.createDirectories(tex.getParent());
        if (Files.exists(cachePng)) {
            Files.copy(cachePng, tex, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
        } else {
            ImageIO.write(randomDiscTexture(id.hashCode() ^ System.nanoTime()), "png", tex.toFile());
            Files.createDirectories(cachePng.getParent());
            Files.copy(tex, cachePng, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
        }

        // 2. Модель
        Path modelPath = work.resolve("assets/smakcraft/models/item/" + id + ".json");
        Files.createDirectories(modelPath.getParent());
        String model = "{\n\t\"parent\": \"item/generated\",\n\t\"textures\": {\n"
                + "\t\t\"layer0\": \"smakcraft:item/" + id + "\"\n\t}\n}";
        Files.writeString(modelPath, model, StandardCharsets.UTF_8);

        // 3. Кейсы во все music_disc_*.json
        addCaseToAllDiscItems(displayName, "smakcraft:item/" + id);

        // 4. sounds.json по фактическим ogg-файлам
        rebuildSoundsJson();
    }

    /** Случайный винил 16x16: свой цвет и стиль у каждой песни. */
    private BufferedImage randomDiscTexture(long seed) {
        Random r = new Random(seed);
        float hue = r.nextFloat();
        float accent = (hue + 0.2f + r.nextFloat() * 0.6f) % 1f;
        int style = 1 + r.nextInt(3);

        BufferedImage img = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
        for (int y = 0; y < 16; y++) {
            for (int x = 0; x < 16; x++) {
                double d = Math.sqrt((x - 7.5) * (x - 7.5) + (y - 7.5) * (y - 7.5));
                int argb = pickPixel(style, d, hue, accent, r);
                img.setRGB(x, y, argb);
            }
        }
        // пара бликов на виниле
        for (int i = 0; i < 2; i++) {
            int x = 2 + r.nextInt(5), y = 2 + r.nextInt(5);
            if ((img.getRGB(x, y) & 0xFF000000) != 0) img.setRGB(x, y, hsl(hue, 0.05f, 0.55f));
        }
        return img;
    }

    private int pickPixel(int style, double d, float hue, float accent, Random r) {
        if (d > 7.6) return 0;
        switch (style) {
            case 2: // светлый «сиди»
                if (d > 7.0) return hsl(hue, 0.10f, 0.30f);
                if (d > 4.6) return hsl(hue, 0.35f, 0.74f);
                if (d > 3.4) return hsl(accent, 0.60f, 0.55f);
                if (d > 2.3) return hsl(hue, 0.30f, 0.82f);
                if (d > 1.0) return hsl(accent, 0.55f, 0.68f);
                return 0xFFFFFFFF;
            case 3: // ретро с широким кольцом
                if (d > 6.9) return hsl(hue, 0.08f, 0.16f);
                if (d > 5.6) return hsl(hue, 0.12f, 0.10f);
                if (d > 4.4) return hsl(hue, 0.80f, 0.52f);
                if (d > 3.2) return hsl(hue, 0.12f, 0.13f);
                if (d > 2.2) return hsl(accent, 0.65f, 0.58f);
                if (d > 0.9) return hsl(hue, 0.15f, 0.22f);
                return 0xFFF2F2F2;
            default: // классический винил
                if (d > 7.0) return hsl(hue, 0.06f, 0.18f);
                if (d > 5.8) return hsl(hue, 0.08f, 0.07f);
                if (d > 4.8) return hsl(hue, 0.35f, 0.16f);
                if (d > 3.8) return hsl(hue, 0.08f, 0.10f);
                if (d > 2.8) return hsl(hue, 0.75f, 0.60f);
                if (d > 2.1) return hsl(hue, 0.70f, 0.42f);
                if (d > 1.3) return hsl(hue, 0.60f, 0.78f);
                if (d > 0.7) return hsl(accent, 0.70f, 0.62f);
                return 0xFFFFFFFF;
        }
    }

    private int hsl(float h, float s, float l) {
        if (s == 0) {
            int v = Math.round(l * 255f);
            return (0xFF << 24) | (v << 16) | (v << 8) | v;
        }
        float q = l < 0.5f ? l * (1 + s) : l + s - l * s;
        float p = 2 * l - q;
        int rr = Math.round(hue2rgb(p, q, h + 1f / 3) * 255);
        int gg = Math.round(hue2rgb(p, q, h) * 255);
        int bb = Math.round(hue2rgb(p, q, h - 1f / 3) * 255);
        return (0xFF << 24) | (rr << 16) | (gg << 8) | bb;
    }

    private float hue2rgb(float p, float q, float t) {
        if (t < 0) t += 1;
        if (t > 1) t -= 1;
        if (t < 1f / 6) return p + (q - p) * 6f * t;
        if (t < 1f / 2) return q;
        if (t < 2f / 3) return p + (q - p) * (2f / 3 - t) * 6f;
        return p;
    }

    /** Вставить кейс с именем пластинки первым в массив "cases" каждого item-файла. */
    private void addCaseToAllDiscItems(String displayName, String modelRef) throws IOException {
        Path itemsDir = work.resolve("assets/minecraft/items");
        if (!Files.isDirectory(itemsDir)) return;
        String caseJson = "\n\t{\n\t\t\"when\": [\"" + esc(displayName) + "\"],\n"
                + "\t\t\"model\": {\n\t\t\t\"type\": \"minecraft:model\",\n"
                + "\t\t\t\"model\": \"" + modelRef + "\"\n\t\t}\n\t},";
        try (var files = Files.list(itemsDir)) {
            for (Path f : files.filter(p -> {
                String n = p.getFileName().toString();
                return n.startsWith("music_disc_") && n.endsWith(".json");
            }).toList()) {
                String text = Files.readString(f, StandardCharsets.UTF_8);
                if (text.contains("\"" + esc(displayName) + "\"")) continue;
                int casesIdx = text.indexOf("\"cases\"");
                if (casesIdx < 0) continue;
                int arr = text.indexOf('[', casesIdx);
                if (arr < 0) continue;
                String updated = text.substring(0, arr + 1) + caseJson + text.substring(arr + 1);
                Files.writeString(f, updated, StandardCharsets.UTF_8);
            }
        }
    }

    private String esc(String s) {
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    /** sounds.json пересобирается по фактическим ogg-файлам. */
    private void rebuildSoundsJson() throws IOException {
        Path recDir = work.resolve("assets/smakcraft/sounds/records");
        List<String> ids = new ArrayList<>();
        if (Files.isDirectory(recDir)) {
            try (var s = Files.list(recDir)) {
                s.filter(f -> f.getFileName().toString().toLowerCase(Locale.ROOT).endsWith(".ogg"))
                        .map(f -> f.getFileName().toString().substring(0, f.getFileName().toString().length() - 4))
                        .sorted().forEach(ids::add);
            }
        }
        StringBuilder sb = new StringBuilder("{");
        for (String id : ids) {
            sb.append("\n\t\"records.").append(id).append("\": {\n")
                    .append("\t\t\"category\": \"record\",\n")
                    .append("\t\t\"sounds\": [\n\t\t\t{\n")
                    .append("\t\t\t\t\"name\": \"smakcraft:records/").append(id).append("\",\n")
                    .append("\t\t\t\t\"stream\": true\n")
                    .append("\t\t\t}\n\t\t]\n\t},");
        }
        if (!ids.isEmpty()) sb.setLength(sb.length() - 1);
        sb.append("\n}");
        Files.createDirectories(recDir.getParent());
        Files.writeString(work.resolve("assets/smakcraft/sounds.json"), sb.toString(), StandardCharsets.UTF_8);
        java.nio.file.Path stray = recDir.resolve("sounds.json");
        if (java.nio.file.Files.exists(stray)) {
            java.nio.file.Files.delete(stray);
        }
    }

    /* ========================= СБОРКА ZIP ========================= */

    /** Запаковать рабочую папку в SmakcraftMusic.zip (+ .sha1). */
    public Path rebuildZip() throws IOException {
        Path out = packZip();
        try (OutputStream fos = Files.newOutputStream(out);
             ZipOutputStream zos = new ZipOutputStream(fos)) {
            Files.walk(work).filter(Files::isRegularFile)
                    .filter(p -> !".cd-ready".equals(p.getFileName().toString()))
                    .sorted()
                    .forEach(p -> {
                        try {
                            String rel = work.relativize(p).toString().replace('\\', '/');
                            zos.putNextEntry(new ZipEntry(rel));
                            Files.copy(p, zos);
                            zos.closeEntry();
                        } catch (IOException ignored) {
                        }
                    });
        }
        String sha1 = AudioConverter.sha1Hex(out);
        if (!sha1.isEmpty()) {
            Files.writeString(plugin.getDataFolder().toPath().resolve("SmakcraftMusic.zip.sha1"),
                    sha1, StandardCharsets.UTF_8);
        }
        return out;
    }

    private void deleteRecursively(Path dir) throws IOException {
        if (!Files.exists(dir)) return;
        try (var walk = Files.walk(dir)) {
            walk.sorted(java.util.Comparator.reverseOrder()).forEach(p -> {
                try {
                    Files.delete(p);
                } catch (IOException ignored) {
                }
            });
        }
    }
}
