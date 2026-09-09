package org.examplee.customdiscs;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.utils.IOUtils;
import org.tukaani.xz.XZInputStream;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Конвертация аудио в моно-OGG для Minecraft.
 * Порядок поиска ffmpeg/ffprobe: config ffmpeg-path -> PATH системы ->
 * папка bin/ внутри датапапки плагина -> автоскачивание.
 * Если ffmpeg найти не удалось, а файл уже .ogg — копируется как есть.
 */
public final class AudioConverter {

    /** Результат конвертации: ok + длительность в секундах (-1 если неизвестна). */
    public record Result(boolean ok, double durationSeconds, String message) {
        public static Result fail(String msg) { return new Result(false, -1, msg); }
    }

    private final CustomDiscsPlugin plugin;
    private Path ffmpeg;
    private Path ffprobe;
    private boolean resolved = false;

    public AudioConverter(CustomDiscsPlugin plugin) {
        this.plugin = plugin;
    }

    /* ============================ ПУБЛИЧНОЕ ============================ */

    /** Конвертировать любой входной файл в моно OGG. */
    public Result toMonoOgg(Path input, Path output) {
        if (!resolveTools()) {
            // Фолбэк: готовый .ogg копируем как есть
            if (input.toString().toLowerCase(Locale.ROOT).endsWith(".ogg")) {
                try {
                    Files.createDirectories(output.getParent());
                    Files.copy(input, output, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                    return new Result(true, -1,
                            "ffmpeg не найден: .ogg скопирован как есть (лучше поставить ffmpeg и переконвертировать в моно)");
                } catch (IOException e) {
                    return Result.fail("нет ffmpeg и не удалось скопировать .ogg: " + e.getMessage());
                }
            }
            return Result.fail("ffmpeg не найден. Установи ffmpeg в систему или укажи путь в config.yml (ffmpeg-path).");
        }
        try {
            Files.createDirectories(output.getParent());
            Process p = new ProcessBuilder(
                    ffmpeg.toString(), "-y", "-i", input.toString(), "-vn",
                    "-ac", "1", "-ar", "44100",
                    "-c:a", "libvorbis", "-q:a", "5",
                    output.toString()
            ).redirectErrorStream(true).start();
            boolean done = p.waitFor(180, java.util.concurrent.TimeUnit.SECONDS);
            if (!done) {
                p.destroyForcibly();
                return Result.fail("ffmpeg не ответил за 180 секунд.");
            }
            if (p.exitValue() != 0 || !Files.exists(output)) {
                return Result.fail("ffmpeg завершился с ошибкой (код " + p.exitValue() + ").");
            }
            return new Result(true, probeDuration(input), "ok");
        } catch (Exception e) {
            return Result.fail("ошибка конвертации: " + e.getMessage());
        }
    }

    /** Длительность файла в секундах (ffprobe), -1 при неудаче. */
    public double probeDuration(Path input) {
        try {
            if (ffprobe == null || !Files.exists(ffprobe)) return -1;
            Process p = new ProcessBuilder(
                    ffprobe.toString(), "-v", "error",
                    "-show_entries", "format=duration",
                    "-of", "csv=p=0", input.toString()
            ).redirectErrorStream(true).start();
            String out = new String(p.getInputStream().readAllBytes()).strip();
            p.waitFor(30, java.util.concurrent.TimeUnit.SECONDS);
            return Double.parseDouble(out.split("\\s+")[0]);
        } catch (Exception e) {
            return -1;
        }
    }

    /* ============================ ПОИСК ИНСТРУМЕНТОВ ============================ */

    private boolean resolveTools() {
        if (resolved && ffmpeg != null) return true;
        resolved = true;

        String override = plugin.getConfig().getString("ffmpeg-path", "").strip();
        Path dataDir = plugin.getDataFolder().toPath();

        // 1. Явный путь из конфига
        if (!override.isEmpty()) {
            Path p = Path.of(override);
            Path found = locateIn(p, "ffmpeg");
            Path probe = locateIn(p, "ffprobe");
            if (found != null) {
                ffmpeg = found;
                ffprobe = probe != null ? probe : found;
                plugin.getLogger().info("ffmpeg: " + ffmpeg + " (из config.yml)");
                return true;
            }
        }

        // 2. PATH системы
        Path sys = findOnPath("ffmpeg");
        Path sysProbe = findOnPath("ffprobe");
        if (sys != null) {
            ffmpeg = sys;
            ffprobe = sysProbe != null ? sysProbe : sys;
            plugin.getLogger().info("ffmpeg найден в системе: " + ffmpeg);
            return true;
        }

        // 3. bin/ внутри папки плагина (то, что мы скачали ранее)
        Path bin = dataDir.resolve("bin");
        Path local = locateIn(bin, "ffmpeg");
        Path localProbe = locateIn(bin, "ffprobe");
        if (local != null) {
            ffmpeg = local;
            ffprobe = localProbe != null ? localProbe : local;
            plugin.getLogger().info("ffmpeg: " + ffmpeg + " (bin/ плагина)");
            return true;
        }

        // 4. Автоскачивание
        plugin.getLogger().info("ffmpeg не найден — пробую скачать автоматически в bin/ ...");
        return downloadFfmpeg(bin);
    }

    private Path locateIn(Path dirOrFile, String tool) {
        if (dirOrFile == null || !Files.exists(dirOrFile)) return null;
        String exe = isWindows() ? tool + ".exe" : tool;
        if (Files.isRegularFile(dirOrFile) && dirOrFile.getFileName().toString().equalsIgnoreCase(exe)) {
            return dirOrFile;
        }
        Path direct = dirOrFile.resolve(exe);
        if (Files.isRegularFile(direct)) return direct;
        // поиск в подпапках (после распаковки архивов)
        if (Files.isDirectory(dirOrFile)) {
            try (var stream = Files.walk(dirOrFile, 4)) {
                return stream.filter(f -> f.getFileName().toString().equalsIgnoreCase(exe))
                        .findFirst().orElse(null);
            } catch (IOException ignored) {
            }
        }
        return null;
    }

    private Path findOnPath(String tool) {
        String[] dirs = System.getenv("PATH").split(File.pathSeparator);
        String exe = isWindows() ? tool + ".exe" : tool;
        for (String d : dirs) {
            Path p = Path.of(d.strip()).resolve(exe);
            if (Files.isRegularFile(p)) return p;
        }
        return null;
    }

    /* ============================ АВТОСКАЧИВАНИЕ ============================ */

    private boolean downloadFfmpeg(Path binDir) {
        String os = isWindows() ? "windows" : isMac() ? "macos" : "linux";
        String arch = isArm() ? "arm64" : "x64";
        plugin.getLogger().info("OS: " + os + "/" + arch);

        List<String[]> urls = new ArrayList<>(); // {url, тип}
        if (os.equals("linux")) {
            String a = arch.equals("arm64") ? "linuxarm64-gpl" : "linux64-gpl";
            urls.add(new String[]{
                    "https://github.com/BtbN/FFmpeg-Builds/releases/download/latest/ffmpeg-master-latest-" + a + ".tar.xz",
                    "tar.xz"});
        } else if (os.equals("windows")) {
            urls.add(new String[]{
                    "https://www.gyan.dev/ffmpeg/builds/ffmpeg-release-essentials.zip",
                    "zip"});
        } else {
            urls.add(new String[]{"https://evermeet.cx/ffmpeg/get/ffmpeg/zip", "zip-single"});
            urls.add(new String[]{"https://evermeet.cx/ffmpeg/get/ffprobe/zip", "zip-single-probe"});
        }

        try {
            Files.createDirectories(binDir);
            HttpClient http = HttpClient.newBuilder()
                    .followRedirects(HttpClient.Redirect.ALWAYS)
                    .connectTimeout(Duration.ofSeconds(30))
                    .build();

            for (String[] entry : urls) {
                String url = entry[0];
                String kind = entry[1];
                Path archive = binDir.resolve("download." + (kind.startsWith("zip") ? "zip" : "tar.xz"));
                plugin.getLogger().info("Скачиваю " + url + " ...");
                HttpRequest req = HttpRequest.newBuilder(URI.create(url))
                        .header("User-Agent", "CustomDiscs/2.0")
                        .timeout(Duration.ofMinutes(10))
                        .GET().build();
                HttpResponse<Path> resp = http.send(req, HttpResponse.BodyHandlers.ofFile(archive));
                if (resp.statusCode() != 200) {
                    plugin.getLogger().warning("HTTP " + resp.statusCode() + " — пропускаю " + url);
                    continue;
                }
                if (kind.startsWith("zip")) {
                    unzipTool(archive, binDir, kind.endsWith("probe") ? "ffprobe" : "ffmpeg");
                } else {
                    untarXzTool(archive, binDir);
                }
                Files.deleteIfExists(archive);
            }
        } catch (Exception e) {
            plugin.getLogger().warning("Автоскачивание ffmpeg не удалось: " + e.getMessage());
        }

        Path f = locateIn(binDir, "ffmpeg");
        if (f == null) {
            plugin.getLogger().warning("ffmpeg недоступен. Варианты: 1) установи в систему; "
                    + "2) скачай вручную и укажи путь в config.yml (ffmpeg-path); "
                    + "3) кидай в songs/ файлы уже в формате .ogg (моно).");
            return false;
        }
        ffmpeg = f;
        ffprobe = locateIn(binDir, "ffprobe");
        if (ffprobe == null) ffprobe = f;
        if (!isWindows()) makeExecutable(ffmpeg);
        if (ffprobe != null && !isWindows()) makeExecutable(ffprobe);
        plugin.getLogger().info("ffmpeg готов: " + ffmpeg);
        return true;
    }

    private void unzipTool(Path zip, Path dest, String wantTool) throws IOException {
        try (ZipInputStream zis = new ZipInputStream(new BufferedInputStream(Files.newInputStream(zip)))) {
            ZipEntry e;
            while ((e = zis.getNextEntry()) != null) {
                String name = e.getName();
                String base = Path.of(name).getFileName().toString();
                boolean wanted = base.equalsIgnoreCase(wantTool + (isWindows() ? ".exe" : ""))
                        || (wantTool.equals("ffmpeg") && base.equalsIgnoreCase("ffmpeg" + (isWindows() ? ".exe" : "")));
                if (!e.isDirectory() && wanted) {
                    Files.copy(zis, dest.resolve(base), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                }
            }
        }
    }

    private void untarXzTool(Path tarXz, Path dest) throws IOException {
        try (InputStream fin = new BufferedInputStream(Files.newInputStream(tarXz));
             InputStream xz = new XZInputStream(fin);
             TarArchiveInputStream tar = new TarArchiveInputStream(xz)) {
            TarArchiveEntry e;
            while ((e = tar.getNextTarEntry()) != null) {
                if (!e.isDirectory()) {
                    String base = Path.of(e.getName()).getFileName().toString();
                    if (base.equals("ffmpeg") || base.equals("ffprobe")) {
                        Path out = dest.resolve(base);
                        try (FileOutputStream ignored = new FileOutputStream(out.toFile())) {
                            IOUtils.copy(tar, ignored);
                        }
                        makeExecutable(out);
                    }
                }
            }
        }
    }

    private void makeExecutable(Path p) {
        try {
            Files.setPosixFilePermissions(p, java.nio.file.attribute.PosixFilePermissions.fromString("rwxr-xr-x"));
        } catch (Exception ignored) {
        }
    }

    private boolean isWindows() { return System.getProperty("os.name").toLowerCase(Locale.ROOT).contains("win"); }
    private boolean isMac() { return System.getProperty("os.name").toLowerCase(Locale.ROOT).contains("mac"); }
    private boolean isArm() {
        String arch = System.getProperty("os.arch").toLowerCase(Locale.ROOT);
        return arch.contains("aarch64") || arch.contains("arm");
    }

    /** SHA-1 файла в hex (для server.properties resource-pack-sha1). */
    public static String sha1Hex(Path file) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            try (InputStream in = Files.newInputStream(file)) {
                byte[] buf = new byte[8192];
                int n;
                while ((n = in.read(buf)) > 0) md.update(buf, 0, n);
            }
            StringBuilder sb = new StringBuilder();
            for (byte b : md.digest()) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (Exception e) {
            return "";
        }
    }
}
