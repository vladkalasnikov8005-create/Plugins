package org.examplee.customdiscs;

import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import java.nio.file.Path;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.SoundCategory;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/** Команда /disc — импорт, выдача и проигрывание кастомных пластинок. */
public final class DiscCommand implements BasicCommand {

    private static final TextColor ACCENT = TextColor.color(0xFF4F9E); // розовый «дора»-стиль
    private static final Component PREFIX = Component.text("[Диски] ", ACCENT);

    private final CustomDiscsPlugin plugin;
    private final DiscManager discs;
    private final AutoImportManager importer;

    public DiscCommand(CustomDiscsPlugin plugin, DiscManager discs, AutoImportManager importer) {
        this.plugin = plugin;
        this.discs = discs;
        this.importer = importer;
    }

    @Override
    public String permission() {
        return "customdiscs.admin";
    }

    /* ============================ EXECUTE ============================ */

    @Override
    public void execute(CommandSourceStack stack, String[] args) {
        CommandSender sender = stack.getSender();
        if (!sender.hasPermission("customdiscs.admin")) {
            sender.sendMessage(PREFIX.append(Component.text("Нет прав. Нужно customdiscs.admin", NamedTextColor.RED)));
            return;
        }

        String sub = args.length == 0 ? "help" : args[0].toLowerCase(Locale.ROOT);
        switch (sub) {
            case "list"    -> list(sender);
            case "give"    -> give(sender, args);
            case "giveall" -> giveAll(sender, args);
            case "add"     -> add(sender, args);
            case "remove"  -> remove(sender, args);
            case "play"    -> play(sender, args);
            case "stop"    -> stop(sender, args);
            case "debug"   -> debug(sender, args);
            case "import"  -> importer.importAll(sender);
            case "pack"    -> pack(sender);
            case "reload"  -> {
                discs.reload();
                sender.sendMessage(PREFIX.append(Component.text(
                        "Конфиг перезагружен. Пластинок: " + discs.ids().size(), NamedTextColor.GREEN)));
            }
            case "help"    -> help(sender);
            default        -> sender.sendMessage(PREFIX.append(
                    Component.text("Неизвестная подкоманда. См. /disc help", NamedTextColor.RED)));
        }
    }

    private void help(CommandSender sender) {
        sender.sendMessage(Component.text("───── CustomDiscs v2 — пластинки ─────", ACCENT));
        sender.sendMessage(cmd("/disc import", "импорт всех песен из plugins/CustomDiscs/songs/"));
        sender.sendMessage(cmd("/disc pack", "пересобрать текстурпак SmakcraftMusic.zip"));
        sender.sendMessage(cmd("/disc list", "список пластинок"));
        sender.sendMessage(cmd("/disc give <id> [ник] [кол-во]", "выдать пластинку"));
        sender.sendMessage(cmd("/disc giveall <id> [кол-во]", "выдать всем онлайн"));
        sender.sendMessage(cmd("/disc add <id> <material> <название...>", "добавить пластинку без песни"));
        sender.sendMessage(cmd("/disc remove <id>", "удалить пластинку"));
        sender.sendMessage(cmd("/disc play <id> [ник]", "проиграть звук пластинки"));
        sender.sendMessage(cmd("/disc stop [ник]", "остановить звук пластинок"));
        sender.sendMessage(cmd("/disc debug <id>", "проверка: песня в реестре?"));
        sender.sendMessage(cmd("/disc reload", "перечитать config.yml"));
        sender.sendMessage(Component.text("Новая песня = кинуть файл в songs/ + /disc import", NamedTextColor.GRAY));
    }

    private Component cmd(String usage, String desc) {
        return Component.text()
                .append(Component.text(usage + " ", NamedTextColor.YELLOW))
                .append(Component.text("— " + desc, NamedTextColor.GRAY))
                .build();
    }

    private void list(CommandSender sender) {
        sender.sendMessage(Component.text("── Пластинки (" + discs.ids().size() + ") ──", ACCENT));
        for (Disc d : discs.all()) {
            sender.sendMessage(Component.text()
                    .append(Component.text(" • " + d.id(), NamedTextColor.YELLOW))
                    .append(Component.text(" → \"" + d.displayName() + "\" ", NamedTextColor.WHITE))
                    .append(Component.text("(" + d.material().name()
                            + (d.jukeboxSong().isBlank() ? "" : ", песня ✔") + ")", NamedTextColor.GRAY))
                    .build());
        }
    }

    private void give(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(PREFIX.append(Component.text("Использование: /disc give <id> [ник] [кол-во]", NamedTextColor.RED)));
            return;
        }
        Disc disc = discs.get(args[1]);
        if (disc == null) {
            sender.sendMessage(PREFIX.append(Component.text("Пластинка '" + args[1] + "' не найдена. См. /disc list", NamedTextColor.RED)));
            return;
        }

        Player target = resolvePlayer(sender, args.length >= 3 ? args[2] : null, "/disc give <id> <ник> [кол-во]");
        if (target == null) return;

        int count = parseCount(sender, args.length >= 4 ? args[3] : "1");
        if (count <= 0) return;

        giveItem(target, discs.buildItem(disc), count);
        warnIfSongMissing(sender, disc);
        sender.sendMessage(PREFIX.append(Component.text()
                .append(Component.text("Выдано ", NamedTextColor.GREEN))
                .append(Component.text(count + " × ", NamedTextColor.WHITE))
                .append(Component.text("\"" + disc.displayName() + "\" ", ACCENT))
                .append(Component.text("игроку " + target.getName(), NamedTextColor.GREEN))
                .build()));
    }

    private void giveAll(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(PREFIX.append(Component.text("Использование: /disc giveall <id> [кол-во]", NamedTextColor.RED)));
            return;
        }
        Disc disc = discs.get(args[1]);
        if (disc == null) {
            sender.sendMessage(PREFIX.append(Component.text("Пластинка '" + args[1] + "' не найдена. См. /disc list", NamedTextColor.RED)));
            return;
        }
        int count = parseCount(sender, args.length >= 3 ? args[2] : "1");
        if (count <= 0) return;

        int sent = 0;
        for (Player p : Bukkit.getOnlinePlayers()) {
            giveItem(p, discs.buildItem(disc), count);
            sent++;
        }
        warnIfSongMissing(sender, disc);
        sender.sendMessage(PREFIX.append(Component.text(
                "Пластинка \"" + disc.displayName() + "\" выдана " + sent + " игрокам.", NamedTextColor.GREEN)));
    }

    private void add(CommandSender sender, String[] args) {
        if (args.length < 4) {
            sender.sendMessage(PREFIX.append(Component.text(
                    "Использование: /disc add <id> <material> <название...>", NamedTextColor.RED)));
            sender.sendMessage(PREFIX.append(Component.text(
                    "Пример: /disc add rick MUSIC_DISC_CAT Рик Ролл", NamedTextColor.GRAY)));
            return;
        }
        String id = args[1].toLowerCase(Locale.ROOT);
        if (!id.matches("[a-z0-9_-]+")) {
            sender.sendMessage(PREFIX.append(Component.text("id: только a-z, 0-9, _ и -", NamedTextColor.RED)));
            return;
        }
        if (discs.get(id) != null) {
            sender.sendMessage(PREFIX.append(Component.text("Пластинка '" + id + "' уже есть. Удали: /disc remove " + id, NamedTextColor.RED)));
            return;
        }
        Material material = Material.matchMaterial(args[2]);
        if (material == null || !material.isItem()) {
            sender.sendMessage(PREFIX.append(Component.text("Неизвестный материал '" + args[2] + "' (напр. MUSIC_DISC_CAT)", NamedTextColor.RED)));
            return;
        }
        String name = join(args, 3);
        discs.saveToConfig(new Disc(id, name, material, List.of("&8CustomDiscs"), "", ""));
        sender.sendMessage(PREFIX.append(Component.text()
                .append(Component.text("Добавлена пластинка ", NamedTextColor.GREEN))
                .append(Component.text(id + " ", NamedTextColor.YELLOW))
                .append(Component.text("→ \"" + name + "\"", ACCENT))
                .build()));
        sender.sendMessage(PREFIX.append(Component.text(
                "Для песни/текстуры кидай файл в plugins/CustomDiscs/songs/ и пиши /disc import", NamedTextColor.GRAY)));
    }

    private void remove(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(PREFIX.append(Component.text("Использование: /disc remove <id>", NamedTextColor.RED)));
            return;
        }
        if (discs.removeFromConfig(args[1])) {
            sender.sendMessage(PREFIX.append(Component.text("Пластинка '" + args[1].toLowerCase(Locale.ROOT) + "' удалена из конфига.", NamedTextColor.GREEN)));
        } else {
            sender.sendMessage(PREFIX.append(Component.text("Пластинка '" + args[1] + "' не найдена.", NamedTextColor.RED)));
        }
    }

    private void play(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(PREFIX.append(Component.text("Использование: /disc play <id> [ник]", NamedTextColor.RED)));
            return;
        }
        Disc disc = discs.get(args[1]);
        if (disc == null) {
            sender.sendMessage(PREFIX.append(Component.text("Пластинка '" + args[1] + "' не найдена.", NamedTextColor.RED)));
            return;
        }
        Player target = resolvePlayer(sender, args.length >= 3 ? args[2] : null, "/disc play <id> <ник>");
        if (target == null) return;

        if (disc.sound() == null || disc.sound().isBlank()) {
            sender.sendMessage(PREFIX.append(Component.text(
                    "У пластинки '" + disc.id() + "' не задан sound в config.yml.", NamedTextColor.YELLOW)));
            return;
        }
        Location loc = target.getLocation();
        target.getWorld().playSound(loc, disc.sound(), SoundCategory.RECORDS, 4.0f, 1.0f);
        sender.sendMessage(PREFIX.append(Component.text("Играет \"" + disc.displayName() + "\" для " + target.getName()
                + ". Остановить: /disc stop " + target.getName(), NamedTextColor.GREEN)));
    }

    private void stop(CommandSender sender, String[] args) {
        Player target = resolvePlayer(sender, args.length >= 2 ? args[1] : null, "/disc stop <ник>");
        if (target == null) return;
        // Категория "record" — остановит и /disc play, и музыку из проигрывателя.
        target.stopSound(SoundCategory.RECORDS);
        sender.sendMessage(PREFIX.append(Component.text()
                .append(Component.text("Звук пластинок остановлен для ", NamedTextColor.GREEN))
                .append(Component.text(target.getName(), NamedTextColor.WHITE))
                .build()));
    }

    private void debug(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(PREFIX.append(Component.text("Использование: /disc debug <id>", NamedTextColor.RED)));
            return;
        }
        Disc disc = discs.get(args[1]);
        if (disc == null) {
            sender.sendMessage(PREFIX.append(Component.text("Пластинка '" + args[1] + "' не найдена.", NamedTextColor.RED)));
            return;
        }
        sender.sendMessage(PREFIX.append(Component.text("Пластинка: " + disc.id()
                + " | material: " + disc.material().name()
                + " | sound: " + (disc.sound().isBlank() ? "—" : disc.sound())
                + " | jukebox_song: " + (disc.jukeboxSong().isBlank() ? "—" : disc.jukeboxSong()),
                NamedTextColor.GRAY)));
        if (disc.jukeboxSong().isBlank()) {
            sender.sendMessage(PREFIX.append(Component.text("jukebox_song не задан — в проигрывателе будет ванильный трек.", NamedTextColor.YELLOW)));
        } else if (discs.jukeboxSongExists(disc.jukeboxSong())) {
            sender.sendMessage(PREFIX.append(Component.text("OK: песня '" + disc.jukeboxSong()
                    + "' есть в реестре — диск играет её в проигрывателе.", NamedTextColor.GREEN)));
        } else {
            sender.sendMessage(PREFIX.append(Component.text("ОШИБКА: песни '" + disc.jukeboxSong() + "' НЕТ в реестре!", NamedTextColor.RED)));
            sender.sendMessage(PREFIX.append(Component.text(
                    "Кинь файл в plugins/CustomDiscs/songs/ и выполни /disc import, затем перезапусти сервер.", NamedTextColor.YELLOW)));
        }
    }

    private void pack(CommandSender sender) {
        Path zip = importer.rebuildPack(sender);
        if (zip == null) {
            sender.sendMessage(PREFIX.append(Component.text(
                    "Подсказка: положи исходный пак (например KSEPSP) рядом и укажи его в resourcepack-source.", NamedTextColor.GRAY)));
        }
    }

    /* ============================ HELPERS ============================ */

    private Player resolvePlayer(CommandSender sender, String name, String consoleUsage) {
        if (name != null) {
            Player t = Bukkit.getPlayerExact(name);
            if (t == null) {
                sender.sendMessage(PREFIX.append(Component.text("Игрок '" + name + "' не в сети.", NamedTextColor.RED)));
            }
            return t;
        }
        if (sender instanceof Player p) return p;
        sender.sendMessage(PREFIX.append(Component.text("Из консоли укажи ник: " + consoleUsage, NamedTextColor.RED)));
        return null;
    }

    private void warnIfSongMissing(CommandSender sender, Disc disc) {
        if (!disc.jukeboxSong().isBlank() && !discs.jukeboxSongExists(disc.jukeboxSong())) {
            sender.sendMessage(PREFIX.append(Component.text(
                    "Внимание: песня ещё не в реестре — перезапусти сервер после импорта. См. /disc debug " + disc.id(),
                    NamedTextColor.YELLOW)));
        }
    }

    private void giveItem(Player target, ItemStack item, int count) {
        Map<Integer, ItemStack> leftover = new HashMap<>();
        while (count > 0) {
            int stack = Math.min(count, item.getMaxStackSize());
            ItemStack copy = item.asQuantity(stack);
            leftover.putAll(target.getInventory().addItem(copy));
            count -= stack;
        }
        for (ItemStack rest : leftover.values()) {
            target.getWorld().dropItemNaturally(target.getLocation(), rest);
        }
        target.playSound(target.getLocation(), "minecraft:entity.item.pickup", 1.0f, 1.0f);
    }

    private int parseCount(CommandSender sender, String raw) {
        try {
            int n = Integer.parseInt(raw);
            if (n <= 0 || n > 2304) {
                sender.sendMessage(PREFIX.append(Component.text("Количество: 1..2304", NamedTextColor.RED)));
                return -1;
            }
            return n;
        } catch (NumberFormatException e) {
            sender.sendMessage(PREFIX.append(Component.text("Не число: " + raw, NamedTextColor.RED)));
            return -1;
        }
    }

    private String join(String[] args, int from) {
        StringBuilder sb = new StringBuilder();
        for (int i = from; i < args.length; i++) {
            if (i > from) sb.append(' ');
            sb.append(args[i]);
        }
        return sb.toString();
    }

    /* ========================= TAB COMPLETE ========================= */

    @Override
    public java.util.Collection<String> suggest(CommandSourceStack stack, String[] args) {
        List<String> out = new ArrayList<>();
        if (!stack.getSender().hasPermission("customdiscs.admin")) return out;

        if (args.length == 1) {
            String p = args[0].toLowerCase(Locale.ROOT);
            for (String s : List.of("import", "pack", "list", "give", "giveall", "add", "remove", "play", "stop", "debug", "reload", "help")) {
                if (s.startsWith(p)) out.add(s);
            }
        } else if (args.length == 2 && List.of("give", "giveall", "play", "remove", "debug").contains(args[0].toLowerCase(Locale.ROOT))) {
            for (String id : discs.ids()) {
                if (id.startsWith(args[1].toLowerCase(Locale.ROOT))) out.add(id);
            }
        } else if (args.length == 3 && List.of("give", "play", "stop").contains(args[0].toLowerCase(Locale.ROOT))) {
            for (Player p : Bukkit.getOnlinePlayers()) {
                if (p.getName().toLowerCase(Locale.ROOT).startsWith(args[2].toLowerCase(Locale.ROOT))) out.add(p.getName());
            }
        } else if (args.length == 3 && args[0].equalsIgnoreCase("add")) {
            String p = args[2].toUpperCase(Locale.ROOT);
            for (Material m : Material.values()) {
                if (m.name().startsWith("MUSIC_DISC") && m.name().startsWith(p)) out.add(m.name());
            }
        }
        return out;
    }
}
