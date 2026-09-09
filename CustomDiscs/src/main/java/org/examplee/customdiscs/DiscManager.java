package org.examplee.customdiscs;

import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.JukeboxPlayable;
import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.JukeboxSong;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/** Загружает пластинки из config.yml и собирает из них предметы. */
public final class DiscManager {

    private static final LegacyComponentSerializer LEGACY_AMP = LegacyComponentSerializer.legacyAmpersand();

    private final CustomDiscsPlugin plugin;
    private final NamespacedKey discKey;
    private final Map<String, Disc> discs = new LinkedHashMap<>();

    public DiscManager(CustomDiscsPlugin plugin) {
        this.plugin = plugin;
        this.discKey = new NamespacedKey(plugin, "disc_id");
    }

    /** Перечитать config.yml. */
    public void reload() {
        plugin.reloadConfig();
        discs.clear();
        ConfigurationSection root = plugin.getConfig().getConfigurationSection("discs");
        if (root == null) return;

        for (String id : root.getKeys(false)) {
            ConfigurationSection s = root.getConfigurationSection(id);
            if (s == null) continue;

            String name = s.getString("display_name", "");
            if (name.isBlank()) {
                plugin.getLogger().warning("Пластинка '" + id + "': display_name пуст — пропущена.");
                continue;
            }
            Material material = Material.matchMaterial(
                    s.getString("material", "MUSIC_DISC_13"));
            if (material == null) {
                plugin.getLogger().warning("Пластинка '" + id + "': неизвестный material — ставлю MUSIC_DISC_13.");
                material = Material.MUSIC_DISC_13;
            }

            discs.put(id.toLowerCase(Locale.ROOT), new Disc(
                    id.toLowerCase(Locale.ROOT),
                    name,
                    material,
                    s.getStringList("lore"),
                    s.getString("sound", "").replace("customdiscs:", "smakcraft:"),
                    s.getString("jukebox_song", "").replace("customdiscs:", "smakcraft:")));
        }
    }

    /** Все зарегистрированные пластинки. */
    public Collection<Disc> all() {
        return discs.values();
    }

    public java.util.Set<String> ids() {
        return discs.keySet();
    }

    /** Поиск без учёта регистра. */
    public Disc get(String id) {
        return discs.get(id.toLowerCase(Locale.ROOT));
    }

    /** Собрать предмет-пластинку. */
    public ItemStack buildItem(Disc disc) {
        ItemStack item = new ItemStack(disc.material());
        ItemMeta meta = item.getItemMeta();

        // Имя — чистый текст без стилей: именно его сравнивает
        // minecraft:select в текстурпаке (custom_name).
        meta.displayName(Component.text(disc.displayName()));

        if (disc.lore() != null && !disc.lore().isEmpty()) {
            List<Component> lore = new ArrayList<>(disc.lore().size());
            for (String line : disc.lore()) {
                lore.add(LEGACY_AMP.deserialize(line));
            }
            meta.lore(lore);
        }

        meta.getPersistentDataContainer().set(discKey, PersistentDataType.STRING, disc.id());
        item.setItemMeta(meta);

        applyJukeboxSong(item, disc, false);
        return item;
    }

    /**
     * Повесить компонент jukebox_playable (своя песня в проигрывателе).
     * @return true, если компонент установлен или не требуется
     */
    public boolean applyJukeboxSong(ItemStack item, Disc disc, boolean quiet) {
        if (disc.jukeboxSong() == null || disc.jukeboxSong().isBlank()) return true;
        try {
            JukeboxSong song = jukeboxSongByKey(disc.jukeboxSong());
            if (song == null) {
                if (!quiet) plugin.getLogger().warning("Пластинка '" + disc.id()
                        + "': песня '" + disc.jukeboxSong()
                        + "' ещё не в реестре (перезапусти сервер после импорта).");
                return false;
            }
            item.setData(DataComponentTypes.JUKEBOX_PLAYABLE,
                    JukeboxPlayable.jukeboxPlayable(song).build());
            return true;
        } catch (Exception e) {
            if (!quiet) plugin.getLogger().warning("Пластинка '" + disc.id()
                    + "': не удалось задать jukebox_song: " + e.getMessage());
            return false;
        }
    }

    /** Есть ли запись в реестре jukebox_song (зарегистрирована ли песня). */
    public boolean jukeboxSongExists(String songKey) {
        try {
            return jukeboxSongByKey(songKey) != null;
        } catch (Exception e) {
            return false;
        }
    }

    private JukeboxSong jukeboxSongByKey(String key) {
        return RegistryAccess.registryAccess()
                .getRegistry(RegistryKey.JUKEBOX_SONG)
                .get(Key.key(key));
    }

    /** Пластинка, созданная этим плагином? (по тегу в PDC) */
    public String idOf(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return null;
        return item.getItemMeta().getPersistentDataContainer().get(discKey, PersistentDataType.STRING);
    }

    /** Сохранить новую пластинку в config.yml. */
    public void saveToConfig(Disc disc) {
        String base = "discs." + disc.id() + ".";
        plugin.getConfig().set(base + "display_name", disc.displayName());
        plugin.getConfig().set(base + "material", disc.material().name());
        plugin.getConfig().set(base + "lore", disc.lore());
        plugin.getConfig().set(base + "sound", disc.sound());
        plugin.getConfig().set(base + "jukebox_song", disc.jukeboxSong());
        plugin.saveConfig();
        discs.put(disc.id(), disc);
    }

    /** Удалить пластинку из config.yml. */
    public boolean removeFromConfig(String id) {
        String key = id.toLowerCase(Locale.ROOT);
        if (!discs.containsKey(key)) return false;
        plugin.getConfig().set("discs." + key, null);
        plugin.saveConfig();
        discs.remove(key);
        return true;
    }
}
