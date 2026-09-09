package org.examplee.customdiscs;

import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.JukeboxPlayable;
import net.kyori.adventure.key.Key;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

/**
 * Авто-лечение пластинок при вставке в проигрыватель:
 * если у диска в компоненте jukebox_playable не та песня, что в конфиге
 * (например, диск выдан до импорта/переименования, или это старый диск
 * ещё со времён датапака) — компонент тихо перезаписывается актуальным.
 */
public final class JukeboxListener implements Listener {

    private final CustomDiscsPlugin plugin;
    private final DiscManager discs;

    public JukeboxListener(CustomDiscsPlugin plugin, DiscManager discs) {
        this.plugin = plugin;
        this.discs = discs;
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onJukeboxUse(PlayerInteractEvent e) {
        if (e.getAction() != Action.RIGHT_CLICK_BLOCK || e.getHand() == null) return;
        Block block = e.getClickedBlock();
        if (block == null || block.getType() != Material.JUKEBOX) return;

        ItemStack item = e.getItem();
        if (item == null || !item.hasItemMeta()) return;

        String id = discs.idOf(item);
        if (id == null) return; // не наша пластинка
        Disc disc = discs.get(id);
        if (disc == null || disc.jukeboxSong() == null || disc.jukeboxSong().isBlank()) return;
        if (!discs.jukeboxSongExists(disc.jukeboxSong())) return; // чинить нечем (перезапусти сервер)

        JukeboxPlayable comp = item.getData(DataComponentTypes.JUKEBOX_PLAYABLE);
        if (comp != null && comp.jukeboxSong().getKey().equals(Key.key(disc.jukeboxSong()))) {
            return; // уже актуально
        }

        ItemStack fixed = item.clone();
        if (discs.applyJukeboxSong(fixed, disc, true)) {
            // У PlayerInteractEvent нет setItem — обновляем слот в руке напрямую.
            e.getPlayer().getInventory().setItem(e.getHand(), fixed);
            plugin.getLogger().info("Пластинка '" + id + "' (" + e.getPlayer().getName()
                    + "): обновлена песня в компоненте диска (" + disc.jukeboxSong() + ").");
        }
    }
}
