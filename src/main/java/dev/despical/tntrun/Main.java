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

import dev.despical.commandframework.CommandFramework;
import dev.despical.commons.scoreboard.ScoreboardLib;
import dev.despical.commons.serializer.InventorySerializer;
import dev.despical.commons.util.UpdateChecker;
import dev.despical.fileitems.ItemManager;
import dev.despical.fileitems.ItemOption;
import dev.despical.tntrun.arena.Arena;
import dev.despical.tntrun.arena.ArenaRegistry;
import dev.despical.tntrun.arena.ArenaUtils;
import dev.despical.tntrun.arena.managers.ArenaManager;
import dev.despical.tntrun.chat.ChatManager;
import dev.despical.tntrun.command.AdminCommands;
import dev.despical.tntrun.command.PlayerCommands;
import dev.despical.tntrun.database.Database;
import dev.despical.tntrun.database.DatabaseType;
import dev.despical.tntrun.database.FlatFileStorage;
import dev.despical.tntrun.database.MySQLStorage;
import dev.despical.tntrun.events.EventListener;
import dev.despical.tntrun.handlers.PermissionsManager;
import dev.despical.tntrun.handlers.PlaceholderHandler;
import dev.despical.tntrun.handlers.sign.SignManager;
import dev.despical.tntrun.leaderboard.LeaderboardManager;
import dev.despical.tntrun.option.BooleanOption;
import dev.despical.tntrun.option.ConfigOptions;
import dev.despical.tntrun.user.User;
import dev.despical.tntrun.user.UserManager;
import lombok.Getter;
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
	private ArenaRegistry arenaRegistry;
    private Database database;
    private ArenaManager arenaManager;
	private ChatManager chatManager;
	private UserManager userManager;
	private ItemManager itemManager;
	private PermissionsManager permissionManager;
	private CommandFramework commandFramework;
	private SignManager signManager;
	private LeaderboardManager leaderboardManager;

	@Override
	public void onEnable() {
		instance = this;
		initializeClasses();
		checkUpdates();

		getLogger().info("Initialization finished.");
		getLogger().info("Join our Discord server: https://discord.gg/uXVU8jmtpU");
	}

	@Override
	public void onDisable() {
		for (Arena arena : arenaRegistry.getArenas()) {
			arena.getScoreboardManager().stopAllScoreboards();
			arena.getGameBar().removeAll();

			for (User user : arena.getPlayers()) {
				Player player = user.getPlayer();

				arena.teleportToEndLocation(user);

				player.getInventory().clear();
				player.getInventory().setArmorContents(null);

        		InventorySerializer.loadInventory(this, player);
			}

			arena.cleanUpArena();
		}

        database.shutdown();
	}

	private void initializeClasses() {
        Locale.setDefault(Locale.ENGLISH);

		setupConfigurationFiles();

		options = new ConfigOptions(this);
		chatManager = new ChatManager(this);
		userManager = new UserManager(this);
		commandFramework = new CommandFramework(this);
		arenaRegistry = new ArenaRegistry(this);
        database = createDatabase();
		arenaManager = new ArenaManager(this);
		itemManager = new ItemManager(this, manager -> {
			ItemOption.enableOptions(ItemOption.GLOW);

			manager.editItemBuilder(builder -> builder.unbreakable(true).hideTooltip(true));
			manager.registerItems("items", "items");
		});

		permissionManager = new PermissionsManager(this);
		signManager = new SignManager(this);

		ScoreboardLib.setPluginInstance(this);
		EventListener.registerEvents(this);

		new AdminCommands();
		new PlayerCommands();

		if (getServer().getPluginManager().isPluginEnabled("PlaceholderAPI")) {
			leaderboardManager = new LeaderboardManager(this);

			new PlaceholderHandler(this);
		}

		if (BooleanOption.NAME_TAGS_HIDDEN.value()) {
			getServer().getScheduler().scheduleSyncRepeatingTask(this, () -> userManager.getUsers().forEach(ArenaUtils::updateNameTagsVisibility), 60, 140);
		}

		handleAutoDataSaving();
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

    private void handleAutoDataSaving() {

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

	private void setupConfigurationFiles() {
		saveDefaultConfig();

		Stream.of("arena", "stats", "items", "mysql", "messages", "bungee").filter(name -> !new File(getDataFolder(), name + ".yml").exists()).forEach(name -> saveResource(name + ".yml", false));
	}

}
