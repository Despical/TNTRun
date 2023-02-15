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

import me.despical.commons.compat.VersionResolver;
import me.despical.commons.database.MysqlDatabase;
import me.despical.commons.scoreboard.ScoreboardLib;
import me.despical.commons.serializer.InventorySerializer;
import me.despical.commons.util.Collections;
import me.despical.commons.util.LogUtils;
import me.despical.commons.util.UpdateChecker;
import me.despical.tntrun.api.StatsStorage;
import me.despical.tntrun.arena.Arena;
import me.despical.tntrun.arena.ArenaRegistry;
import me.despical.tntrun.arena.ArenaUtils;
import me.despical.tntrun.commands.CommandHandler;
import me.despical.tntrun.events.*;
import me.despical.tntrun.handlers.ChatManager;
import me.despical.tntrun.handlers.PermissionsManager;
import me.despical.tntrun.handlers.PlaceholderManager;
import me.despical.tntrun.handlers.items.GameItemManager;
import me.despical.tntrun.handlers.rewards.RewardsFactory;
import me.despical.tntrun.handlers.sign.SignManager;
import me.despical.tntrun.user.User;
import me.despical.tntrun.user.UserManager;
import me.despical.tntrun.user.data.MysqlManager;
import org.bstats.bukkit.Metrics;
import org.bstats.charts.SimplePie;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.Iterator;

/**
 * @author Despical
 * <p>
 * Created at 10.07.2020
 */
public class Main extends JavaPlugin {

	private boolean forceDisable;

	private RewardsFactory rewardsFactory;
	private MysqlDatabase database;
	private SignManager signManager;
	private ConfigPreferences configPreferences;
	private ChatManager chatManager;
	private UserManager userManager;
	private GameItemManager gameItemManager;
	private PermissionsManager permissionManager;
	private CommandHandler commandHandler;

	@Override
	public void onEnable() {
		if (forceDisable = !validateIfPluginShouldStart()) {
			getServer().getPluginManager().disablePlugin(this);
			return;
		}

		this.configPreferences = new ConfigPreferences(this);

		if (configPreferences.getOption(ConfigPreferences.Option.DEBUG_MESSAGES)) {
			LogUtils.enableLogging("TNTRun");
			LogUtils.log("Initialization started.");
		}

		long start = System.currentTimeMillis();

		setupFiles();
		initializeClasses();
		checkUpdate();

		LogUtils.sendConsoleMessage("[TNTRun] &aInitialization finished. Join our Discord server if you need any help. (https://discord.gg/rVkaGmyszE)");
		LogUtils.log("Initialization finished took {0} ms.", System.currentTimeMillis() - start);

		if (configPreferences.getOption(ConfigPreferences.Option.NAME_TAGS_HIDDEN)) {
			getServer().getScheduler().scheduleSyncRepeatingTask(this, () -> getServer().getOnlinePlayers().forEach(ArenaUtils::updateNameTagsVisibility), 60, 140);
		}
	}

	private boolean validateIfPluginShouldStart() {
		if (!VersionResolver.isCurrentBetween(VersionResolver.ServerVersion.v1_9_R1, VersionResolver.ServerVersion.v1_19_R2)) {
			LogUtils.sendConsoleMessage("[TNTRun] &cYour server version is not supported by TNT Run!");
			LogUtils.sendConsoleMessage("[TNTRun] &cMaybe you consider changing your server version?");
			return false;
		}

		try {
			Class.forName("org.spigotmc.SpigotConfig");
		} catch (Exception exception) {
			LogUtils.sendConsoleMessage("[TNTRun] &cYour server software is not supported by TNT Run!");
			LogUtils.sendConsoleMessage("[TNTRun] &cWe support only Spigot and Spigot forks! Shutting off...");
			return false;
		}

		return true;
	}

	@Override
	public void onDisable() {
		if (forceDisable) return;

		LogUtils.log("System disable initialized.");
		long start = System.currentTimeMillis();

		saveAllUserStatistics();

		if (database != null) {
			database.shutdownConnPool();
		}

		for (Arena arena : ArenaRegistry.getArenas()) {
			arena.getScoreboardManager().stopAllScoreboards();

			for (Player player : arena.getPlayers()) {
				arena.doBarAction(Arena.BarAction.REMOVE, player);
				arena.teleportToEndLocation(player);
				player.setFlySpeed(.1F);
				player.setWalkSpeed(.2F);

				if (configPreferences.getOption(ConfigPreferences.Option.INVENTORY_MANAGER_ENABLED)) {
					InventorySerializer.loadInventory(this, player);
				} else {
					player.getInventory().clear();
					player.getInventory().setArmorContents(null);
					player.getActivePotionEffects().forEach(effect -> player.removePotionEffect(effect.getType()));
				}
			}

			Iterator<BlockState> iterator = arena.getDestroyedBlocks().iterator();

			while (iterator.hasNext()) {
				iterator.next().update(true);
				iterator.remove();
			}
		}

		LogUtils.log("System disable finished took {0} ms.", System.currentTimeMillis() - start);
		LogUtils.disableLogging();
	}

	private void initializeClasses() {
		ScoreboardLib.setPluginInstance(this);

		configPreferences = new ConfigPreferences(this);
		chatManager = new ChatManager(this);
		permissionManager = new PermissionsManager(this);

		if (configPreferences.getOption(ConfigPreferences.Option.DATABASE_ENABLED)) {
			database = new MysqlDatabase(this, "mysql");
		}

		userManager = new UserManager(this);
		gameItemManager = new GameItemManager(this);

		User.cooldownHandlerTask();
		ListenerAdapter.registerEvents(this);

		signManager = new SignManager(this);
		ArenaRegistry.registerArenas();
		signManager.loadSigns();

		rewardsFactory = new RewardsFactory(this);
		commandHandler = new CommandHandler(this);

		registerSoftDependencies();
	}

	private void checkUpdate() {
		if (!configPreferences.getOption(ConfigPreferences.Option.UPDATE_NOTIFIER_ENABLED)) return;

		UpdateChecker.init(this, 83196).requestUpdateCheck().whenComplete((result, exception) -> {
			if (!result.requiresUpdate()) return;

			LogUtils.sendConsoleMessage("[TNTRun] Found a new version available: v" + result.getNewestVersion());
			LogUtils.sendConsoleMessage("[TNTRun] Download it on SpigotMC:");
			LogUtils.sendConsoleMessage("[TNTRun] https://wwwspigotmc.org/resources/tnt-run.83196/");
		});
	}

	private void registerSoftDependencies() {
		LogUtils.log("Hooking into soft dependencies.");

		startPluginMetrics();

		if (chatManager.isPapiEnabled()) {
			LogUtils.log("Hooking into PlaceholderAPI.");
			new PlaceholderManager(this);
		}

		LogUtils.log("Hooked into soft dependencies.");
	}

	private void startPluginMetrics() {
		Metrics metrics = new Metrics(this, 8147);

		metrics.addCustomChart(new SimplePie("database_enabled", () -> configPreferences.getOption(ConfigPreferences.Option.DATABASE_ENABLED) ? "Enabled" : "Disabled"));
		metrics.addCustomChart(new SimplePie("update_notifier", () -> configPreferences.getOption(ConfigPreferences.Option.UPDATE_NOTIFIER_ENABLED) ? "Enabled" : "Disabled"));
	}

	private void setupFiles() {
		Collections.streamOf("arenas", "rewards", "stats", "items", "mysql", "messages").filter(name -> !new File(getDataFolder(),name + ".yml").exists()).forEach(name -> saveResource(name + ".yml", false));
	}

	public RewardsFactory getRewardsFactory() {
		return rewardsFactory;
	}

	public ConfigPreferences getConfigPreferences() {
		return configPreferences;
	}

	public MysqlDatabase getMysqlDatabase() {
		return database;
	}

	public SignManager getSignManager() {
		return signManager;
	}

	public ChatManager getChatManager() {
		return chatManager;
	}

	public UserManager getUserManager() {
		return userManager;
	}

	public GameItemManager getGameItemManager() {
		return gameItemManager;
	}

	public PermissionsManager getPermissionManager() {
		return permissionManager;
	}

	public CommandHandler getCommandHandler() {
		return commandHandler;
	}

	private void saveAllUserStatistics() {
		for (Player player : getServer().getOnlinePlayers()) {
			final User user = userManager.getUser(player);

			if (userManager.getDatabase() instanceof MysqlManager) {
				final StringBuilder builder = new StringBuilder(" SET ");
				final MysqlDatabase mysqlDatabase = ((MysqlManager) userManager.getDatabase()).getDatabase();

				for (StatsStorage.StatisticType stat : StatsStorage.StatisticType.values()) {
					if (!stat.isPersistent()) continue;

					final int value = user.getStat(stat);

					if (builder.toString().equalsIgnoreCase(" SET ")) {
						builder.append(stat.getName()).append("'='").append(value);
					}

					builder.append(", ").append(stat.getName()).append("'='").append(value);
				}

				final String update = builder.toString();
				mysqlDatabase.executeUpdate("UPDATE " + mysqlDatabase + update + " WHERE UUID='" + user.getUniqueId().toString() + "';");
				continue;
			}

			userManager.saveAllStatistic(user);
		}
	}
}