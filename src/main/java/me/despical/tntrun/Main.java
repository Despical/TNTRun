/*
 * TNT Run - Don't stop running to win!
 * Copyright (C) 2023 Despical
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

package me.despical.tntrun;

import me.despical.commandframework.CommandFramework;
import me.despical.commons.database.MysqlDatabase;
import me.despical.commons.scoreboard.ScoreboardLib;
import me.despical.commons.serializer.InventorySerializer;
import me.despical.commons.util.UpdateChecker;
import me.despical.tntrun.api.StatsStorage;
import me.despical.tntrun.arena.ArenaRegistry;
import me.despical.tntrun.arena.ArenaUtils;
import me.despical.tntrun.arena.managers.ArenaManager;
import me.despical.tntrun.commands.AbstractCommand;
import me.despical.tntrun.events.EventListener;
import me.despical.tntrun.handlers.ChatManager;
import me.despical.tntrun.handlers.PermissionsManager;
import me.despical.tntrun.handlers.PlaceholderHandler;
import me.despical.tntrun.handlers.items.GameItemManager;
import me.despical.tntrun.handlers.rewards.RewardsFactory;
import me.despical.tntrun.user.User;
import me.despical.tntrun.user.UserManager;
import me.despical.tntrun.user.data.MysqlManager;
import org.bstats.bukkit.Metrics;
import org.bstats.charts.SimplePie;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.stream.Stream;

/**
 * @author Despical
 * <p>
 * Created at 10.07.2020
 */
public class Main extends JavaPlugin {

	private ArenaRegistry arenaRegistry;
	private ArenaManager arenaManager;
	private RewardsFactory rewardsFactory;
	private MysqlDatabase database;
	private ConfigPreferences configPreferences;
	private ChatManager chatManager;
	private UserManager userManager;
	private GameItemManager gameItemManager;
	private PermissionsManager permissionManager;
	private CommandFramework commandFramework;

	@Override
	public void onEnable() {
		initializeClasses();
		checkUpdate();

		getLogger().info("Initialization finished. Join our Discord server if you need any help. (https://discord.gg/rVkaGmyszE)");
	}

	@Override
	public void onDisable() {
		saveAllUserStatistics();

		if (database != null) database.shutdownConnPool();

		for (final var arena : arenaRegistry.getArenas()) {
			arena.getScoreboardManager().stopAllScoreboards();
			arena.getGameBar().removeAll();

			for (final var user : arena.getPlayers()) {
				final var player = user.getPlayer();

				arena.teleportToEndLocation(user);

				if (configPreferences.getOption(ConfigPreferences.Option.INVENTORY_MANAGER_ENABLED)) {
					InventorySerializer.loadInventory(this, player);
				} else {

					player.getInventory().clear();
					player.getInventory().setArmorContents(null);
					player.getActivePotionEffects().forEach(effect -> player.removePotionEffect(effect.getType()));
				}
			}

			arena.cleanUpArena();
		}
	}

	private void initializeClasses() {
		this.setupConfigurationFiles();

		this.configPreferences = new ConfigPreferences(this);
		this.chatManager = new ChatManager(this);
		this.userManager = new UserManager(this);
		this.commandFramework = new CommandFramework(this);
		this.arenaRegistry = new ArenaRegistry(this);
		this.arenaManager = new ArenaManager(this);
		this.gameItemManager = new GameItemManager(this);
		this.permissionManager = new PermissionsManager(this);
		this.rewardsFactory = new RewardsFactory(this);

		ScoreboardLib.setPluginInstance(this);
		AbstractCommand.registerCommands(this);
		EventListener.registerEvents(this);
		User.cooldownHandlerTask();

		if (configPreferences.getOption(ConfigPreferences.Option.DATABASE_ENABLED)) {
			this.database = new MysqlDatabase(this, "mysql");
		}

		if (getServer().getPluginManager().isPluginEnabled("PlaceholderAPI")) {
			new PlaceholderHandler(this);
		}

		if (configPreferences.getOption(ConfigPreferences.Option.NAME_TAGS_HIDDEN)) {
			getServer().getScheduler().scheduleSyncRepeatingTask(this, () -> userManager.getUsers().forEach(ArenaUtils::updateNameTagsVisibility), 60, 140);
		}

		final var metrics = new Metrics(this, 8147);
		metrics.addCustomChart(new SimplePie("database_enabled", () -> configPreferences.getOption(ConfigPreferences.Option.DATABASE_ENABLED) ? "Enabled" : "Disabled"));
		metrics.addCustomChart(new SimplePie("update_notifier", () -> configPreferences.getOption(ConfigPreferences.Option.UPDATE_NOTIFIER_ENABLED) ? "Enabled" : "Disabled"));
	}

	private void checkUpdate() {
		if (!configPreferences.getOption(ConfigPreferences.Option.UPDATE_NOTIFIER_ENABLED)) return;

		UpdateChecker.init(this, 83196).requestUpdateCheck().whenComplete((result, exception) -> {
			if (result.requiresUpdate()) {
				var logger = getLogger();

				logger.info("Found a new version available: v" + result.getNewestVersion());
				logger.info("Download it on SpigotMC:");
				logger.info("https://www.spigotmc.org/resources/tnt-run.83196/");
			}
		});
	}

	private void setupConfigurationFiles() {
		Stream.of("arenas", "rewards", "stats", "items", "mysql", "messages").filter(name -> !new File(getDataFolder(),name + ".yml").exists()).forEach(name -> saveResource(name + ".yml", false));
	}

	@NotNull
	public RewardsFactory getRewardsFactory() {
		return rewardsFactory;
	}

	@NotNull
	public ConfigPreferences getConfigPreferences() {
		return configPreferences;
	}

	@NotNull
	public MysqlDatabase getMysqlDatabase() {
		return database;
	}

	@NotNull
	public ChatManager getChatManager() {
		return chatManager;
	}

	@NotNull
	public UserManager getUserManager() {
		return userManager;
	}

	@NotNull
	public GameItemManager getGameItemManager() {
		return gameItemManager;
	}

	@NotNull
	public PermissionsManager getPermissionManager() {
		return permissionManager;
	}

	@NotNull
	public CommandFramework getCommandFramework() {
		return commandFramework;
	}

	@NotNull
	public ArenaRegistry getArenaRegistry() {
		return arenaRegistry;
	}

	@NotNull
	public ArenaManager getArenaManager() {
		return arenaManager;
	}

	private void saveAllUserStatistics() {
		for (final var player : getServer().getOnlinePlayers()) {
			final var user = userManager.getUser(player);

			if (userManager.getUserDatabase() instanceof MysqlManager mysqlManager) {
				final var builder = new StringBuilder(" SET ");

				for (final var stat : StatsStorage.StatisticType.values()) {
					if (!stat.isPersistent()) continue;

					final var value = user.getStat(stat);
					final var name = stat.getName();

					if (builder.toString().equalsIgnoreCase(" SET ")) {
						builder.append(name).append("'='").append(value);
					}

					builder.append(", ").append(name).append("'='").append(value);
				}

				final var update = builder.toString();
				mysqlManager.getDatabase().executeUpdate("UPDATE playerstats" + update + " WHERE UUID='" + user.getUniqueId().toString() + "';");
				continue;
			}

			userManager.getUserDatabase().saveStatistics(user);
		}
	}
}