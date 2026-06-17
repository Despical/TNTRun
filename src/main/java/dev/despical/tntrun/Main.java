/*
 * TNT Run - A Minecraft parkour minigame
 * Copyright (C) 2024  Berke Akçen
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package dev.despical.tntrun;

import dev.despical.commandframework.CommandArguments;
import dev.despical.commandframework.CommandFramework;
import dev.despical.commandframework.Message;
import dev.despical.commandframework.options.FrameworkOption;
import dev.despical.commons.util.Strings;
import dev.despical.commons.util.UpdateChecker;
import dev.despical.fileitems.ItemManager;
import dev.despical.fileitems.ItemOption;
import dev.despical.tntrun.arena.Arena;
import dev.despical.tntrun.arena.ArenaRegistry;
import dev.despical.tntrun.arena.managers.ArenaManager;
import dev.despical.tntrun.chat.ChatManager;
import dev.despical.tntrun.database.Database;
import dev.despical.tntrun.database.DatabaseType;
import dev.despical.tntrun.database.FlatFileStorage;
import dev.despical.tntrun.database.MySQLStorage;
import dev.despical.tntrun.handlers.sign.SignManager;
import dev.despical.tntrun.leaderboard.LeaderboardManager;
import dev.despical.tntrun.option.BooleanOption;
import dev.despical.tntrun.option.ConfigOptions;
import dev.despical.tntrun.stats.offline.StatsCacheManager;
import dev.despical.tntrun.user.User;
import dev.despical.tntrun.user.UserManager;
import dev.despical.tntrun.utils.AutoSaveHandler;
import dev.despical.tntrun.utils.ShutdownDetector;
import lombok.Getter;
import org.bstats.bukkit.Metrics;
import org.bstats.charts.SimplePie;
import org.bstats.charts.SingleLineChart;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;

/**
 * @author Despical
 * <p>
 * Created at 10.07.2020
 */
@Getter
public class Main extends JavaPlugin {

	@Getter
	private static Main instance;

    private ConfigOptions options;
    private ItemManager itemManager;
    private ChatManager chatManager;
//    private GameManager gameManager;
//    private EventManager eventManager;
    private ArenaRegistry arenaRegistry;
    private Database database;
    private StatsCacheManager statsCacheManager;
    private UserManager userManager;
    private ArenaManager arenaManager;
    private SignManager signManager;
    private LeaderboardManager leaderboardManager;
    private CommandFramework commandFramework;
//    private PlayingCommandPolicy playingCommandPolicy;

    @Override
    public void onEnable() {
        ShutdownDetector.init();

        instance = this;

        createConfigFiles();
        initializeClasses();
    }

    @Override
    public void onDisable() {
//        new ArenaDataSaver(this).saveAllArenas();

//        arenaManager.handleDisable();
        database.shutdown();
    }

    private void createConfigFiles() {
        saveDefaultConfig();
        saveResourceIfMissing("mysql.yml");
    }

    private void initializeClasses() {
        this.loadItemManager();

        options = new ConfigOptions(this);
        chatManager = new ChatManager(this);
//        gameManager = new GameManager(this);
//        eventManager = new EventManager(this);
        arenaRegistry = new ArenaRegistry(this);
        database = createDatabase();
        statsCacheManager = new StatsCacheManager(this);
        userManager = new UserManager(this);
        arenaManager = new ArenaManager(this);
        signManager = new SignManager(this);
        leaderboardManager = new LeaderboardManager(this);
//        playingCommandPolicy = new PlayingCommandPolicy(this);

        registerCommands();
        registerEvents();
        registerPlaceholderManager();
        runAutoSave();
        initializeMetrics();
        checkUpdates();

//        getServer().getOnlinePlayers().forEach(ScoreboardManager::resetPlayerScoreboard);
    }

    private void runAutoSave() {
        new AutoSaveHandler(this).runTaskTimerAsynchronously(this, 20, 20 * 60 * 5);
    }

    public void loadItemManager() {
        itemManager = new ItemManager(this, manager -> ItemOption.enableOptions(ItemOption.GLOW, ItemOption.AMOUNT));

        registerItems();
    }

    public void registerItems() {
        itemManager.registerItems("game-items", "game-items", "items");
        itemManager.registerItems("menu/setup-menu", "items");
        itemManager.registerItems("stats-menu-items", "items", "menu/stats-menu");
    }

    private Database createDatabase() {
        String databaseType = getConfig().getString("database");

        return switch (DatabaseType.getByName(databaseType)) {
            case FLAT_FILE -> new FlatFileStorage();
            case MYSQL -> new MySQLStorage();
            case null -> {
                getLogger().warning("Invalid database type. Using flat file storage.");
                yield new FlatFileStorage();
            }
        };
    }

    private void registerCommands() {
        commandFramework = new CommandFramework(this);

        if (BooleanOption.DEBUG.value()) {
            commandFramework.options().enableOptions(FrameworkOption.DEBUG);
        }

        commandFramework.addCustomParameter(Player.class, CommandArguments::getSender);
        commandFramework.addCustomParameter(User.class, args -> userManager.getUser(args.<Player>getSender()));
        commandFramework.addCustomParameter(Arena.class, args -> arenaRegistry.getArena(args.getFirst()));
        commandFramework.registerAllInPackage("dev.despical.tntrun.command");

        Message.setColorFormatter(Strings::format);

        var messages = Stream.of(Message.SHORT_ARG_SIZE, Message.LONG_ARG_SIZE);
        messages.forEach(message -> message.setMessage((cmd, args) -> {
            args.sendMessage("&cCorrect usage: " + cmd.usage().replace("%label%", args.getLabel()));
            return true;
        }));
    }

    private void registerEvents() {
//        new GeneralEvents();
//        new CommandBlockEvents();
//        new GameEvents();
//        new GameItemEvents();
    }

    private void registerPlaceholderManager() {
        if (!getServer().getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            return;
        }

//        PlaceholderManager manager = new PlaceholderManager(this);
//        manager.register();
    }

    private void initializeMetrics() {
        Metrics metrics = new Metrics(this, 30500);

        metrics.addCustomChart(new SimplePie("database_type", this::resolveMetricsDatabaseType));
        metrics.addCustomChart(new SimplePie("placeholderapi_enabled", () -> isPluginEnabled("PlaceholderAPI") ? "yes" : "no"));
        metrics.addCustomChart(new SingleLineChart("arenas_total", () -> arenaRegistry.getArenas().size()));
//        metrics.addCustomChart(new SingleLineChart("arenas_ready", () -> (int) arenaRegistry.getArenas().stream().filter(arena -> arena.getOption(ArenaKeys.READY)).count()));
    }

    private void checkUpdates() {
        if (!BooleanOption.UPDATE_NOTIFIER.value()) {
            return;
        }

        UpdateChecker.init(this, 83196).onNewUpdate(_ -> {
            Logger logger = getLogger();
            logger.log(Level.INFO, "An update for TNT Run ({0}) is available at:", getDescription().getVersion());
            logger.log(Level.INFO, "https://www.spigotmc.org/resources/tnt-run.83196/");
        });
    }

    private String resolveMetricsDatabaseType() {
        String configured = getConfig().getString("database", "flat");
        DatabaseType type = DatabaseType.getByName(configured);
        return type != null ? type.name().toLowerCase(Locale.ENGLISH) : configured.toLowerCase(Locale.ENGLISH);
    }

    private boolean isPluginEnabled(String pluginName) {
        return getServer().getPluginManager().isPluginEnabled(pluginName);
    }

    private void saveResourceIfMissing(String resourcePath) {
        File targetFile = new File(getDataFolder(), resourcePath);

        if (targetFile.exists()) {
            return;
        }

        saveResource(resourcePath, false);
    }
}
