package org.examplee.customdiscs;

import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import org.bukkit.block.Jukebox;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * CustomDiscs v2 — «свои пластинки под ключ»:
 *  - кидаешь аудиофайлы в plugins/CustomDiscs/songs/
 *  - перезапускаешь сервер (или /disc import)
 *  - плагин сам конвертирует, рисует текстуру, собирает текстурпак
 *    и регистрирует песни в реестре проигрывателя (без датапаков!).
 */
public final class CustomDiscsPlugin extends JavaPlugin {

    private DiscManager discs;
    private AutoImportManager importer;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        if (!getDataFolder().toPath().resolve("jukebox-songs.list").toFile().exists()) {
            saveResource("jukebox-songs.list", false);
        }

        discs = new DiscManager(this);
        discs.reload();

        importer = new AutoImportManager(this, discs);

        // Авто-импорт песен из папки songs/ при старте
        if (getConfig().getBoolean("auto-import", true)) {
            importer.importAll(null);
        }

        // Команда /disc (Brigadier для paper-плагинов)
        DiscCommand command = new DiscCommand(this, discs, importer);
        getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, event ->
                event.registrar().register(
                        "disc",
                        "Управление кастомными пластинками",
                        java.util.List.of("plastinka", "mdisc"),
                        (BasicCommand) command));

        // Авто-лечение старых/выданных-до-датапака дисков при вставке в jukebox
        getServer().getPluginManager().registerEvents(new JukeboxListener(this, discs), this);

        getLogger().info("CustomDiscs включён. Пластинок: " + discs.ids().size()
                + ". Папка для новых песен: plugins/CustomDiscs/songs/");
    }

    @Override
    public void onDisable() {
        getLogger().info("CustomDiscs выключен.");
    }

    public DiscManager discs() {
        return discs;
    }

    public AutoImportManager importer() {
        return importer;
    }
}
