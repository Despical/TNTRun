/*
 * TNT Run - Don't stop running to win!
 * Copyright (C) 2020 Despical
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
import me.despical.commons.compat.VersionResolver;
import me.despical.commons.configuration.ConfigUtils;
import me.despical.commons.database.MysqlDatabase;
import me.despical.commons.exception.ExceptionLogHandler;
import me.despical.commons.scoreboard.ScoreboardLib;
import me.despical.commons.serializer.InventorySerializer;
import me.despical.commons.util.LogUtils;
import me.despical.commons.util.UpdateChecker;
import me.despical.tntrun.api.StatsStorage;
import me.despical.tntrun.arena.Arena;
import me.despical.tntrun.arena.ArenaEvents;
import me.despical.tntrun.arena.ArenaRegistry;
import me.despical.tntrun.arena.ArenaUtils;
import me.despical.tntrun.commands.AdminCommands;
import me.despical.tntrun.commands.PlayerCommands;
import me.despical.tntrun.commands.TabCompletion;
import me.despical.tntrun.events.*;
import me.despical.tntrun.events.spectator.SpectatorEvents;
import me.despical.tntrun.events.spectator.SpectatorItemEvents;
import me.despical.tntrun.handlers.ChatManager;
import me.despical.tntrun.handlers.PermissionsManager;
import me.despical.tntrun.handlers.PlaceholderManager;
import me.despical.tntrun.handlers.items.SpecialItemManager;
import me.despical.tntrun.handlers.language.LanguageManager;
import me.despical.tntrun.handlers.rewards.RewardsFactory;
import me.despical.tntrun.handlers.sign.SignManager;
import me.despical.tntrun.user.User;
import me.despical.tntrun.user.UserManager;
import me.despical.tntrun.user.data.MysqlManager;
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

	private ExceptionLogHandler exceptionLogHandler;
	private RewardsFactory rewardsFactory;
	private MysqlDatabase database;
	private SignManager signManager;
	private ConfigPreferences configPreferences;
	private ChatManager chatManager;
	private LanguageManager languageManager;
	private UserManager userManager;
	private SpecialItemManager itemManager;
	private PermissionsManager permissionManager;
	private CommandFramework commandFramework;

	@Override
	public void onEnable() {
		LogUtils.setLoggerName("TNT Run");
		LogUtils.enableLogging();
		getServer().getLogger().setParent(LogUtils.getLogger());

//		if (getDescription().getVersion().contains("debug") || getConfig().getBoolean("Debug-Messages")) {
//			LogUtils.setLoggerName("TNT Run");
//			LogUtils.enableLogging();
//		}

		if (forceDisable = !validateIfPluginShouldStart()) {
			getServer().getPluginManager().disablePlugin(this);
			return;
		}

		exceptionLogHandler = new ExceptionLogHandler(this);
		exceptionLogHandler.setMainPackage("me.despical.tntrun");
		exceptionLogHandler.setRecordMessage("[TNT Run] We have found a bug in the code. Contact us at our official repo on GitHub with the following error above.");
		exceptionLogHandler.addBlacklistedClass("me.despical.tntrun.user.data.MysqlManager", "me.despical.commons.database.MysqlDatabase");

		saveDefaultConfig();

		LogUtils.log("Initialization started!");
		long start = System.currentTimeMillis();

		setupFiles();
		initializeClasses();
		checkUpdate();

		LogUtils.log("Initialization finished took {0} ms.", System.currentTimeMillis() - start);

		if (configPreferences.getOption(ConfigPreferences.Option.NAME_TAGS_HIDDEN)) {
			getServer().getScheduler().scheduleSyncRepeatingTask(this, () ->
				getServer().getOnlinePlayers().forEach(ArenaUtils::updateNameTagsVisibility), 60, 140);
		}
	}

	private boolean validateIfPluginShouldStart() {
		if (VersionResolver.isCurrentLower(VersionResolver.ServerVersion.v1_11_R1)) {
			LogUtils.sendConsoleMessage("&cYour server version is not supported by TNT Run!");
			LogUtils.sendConsoleMessage("&cSadly, we must shut off. Maybe you consider changing your server version?");
			return false;
		}

		try {
			Class.forName("org.spigotmc.SpigotConfig");
		} catch (Exception e) {
			LogUtils.sendConsoleMessage("&cYour server software is not supported by TNT Run!");
			LogUtils.sendConsoleMessage("&cWe support Spigot and forks only! Shutting off...");
			return false;
		}

		return true;
	}

	@Override
	public void onDisable() {
		if (forceDisable) return;

		LogUtils.log("System disable initialized.");
		long start = System.currentTimeMillis();

		getServer().getLogger().removeHandler(exceptionLogHandler);
		saveAllUserStatistics();

		if (database != null) {
			database.shutdownConnPool();
		}

		for (Arena arena : ArenaRegistry.getArenas()) {
			arena.getScoreboardManager().stopAllScoreboards();

			for (Player player : arena.getPlayers()) {
				arena.doBarAction(Arena.BarAction.REMOVE, player);
				arena.teleportToEndLocation(player);
				player.setFlySpeed(0.1f);
				player.setWalkSpeed(0.2f);

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
			database = new MysqlDatabase(ConfigUtils.getConfig(this, "mysql"));
		}

		languageManager = new LanguageManager(this);
		userManager = new UserManager(this);
		itemManager = new SpecialItemManager();

		User.cooldownHandlerTask();

		new SpectatorEvents(this);
		new QuitEvent(this);
		new JoinEvent(this);
		new ChatEvents(this);
		new Events(this);
		new LobbyEvent(this);
		new SpectatorItemEvents(this);
		new ArenaEvents(this);

		signManager = new SignManager(this);
		ArenaRegistry.registerArenas();
		signManager.loadSigns();

		rewardsFactory = new RewardsFactory(this);
		commandFramework = new CommandFramework(this);

		new PlayerCommands(this);
		new AdminCommands(this);
		new TabCompletion(this);

		if (configPreferences.isPapiEnabled()) {
			new PlaceholderManager(this);
		}
	}

	private void checkUpdate() {
		if (!getConfig().getBoolean("Update-Notifier.Enabled", true)) {
			return;
		}

		UpdateChecker.init(this, 83196).requestUpdateCheck().whenComplete((result, exception) -> {
			if (!result.requiresUpdate()) {
				return;
			}

			LogUtils.sendConsoleMessage("[TNTRun] Found a new version available: v" + result.getNewestVersion());
			LogUtils.sendConsoleMessage("[TNTRun] Download it SpigotMC:");
			LogUtils.sendConsoleMessage("[TNTRun] spigotmc.org/resources/tnt-run.83196/");
		});
	}

	private void setupFiles() {
		String[] files = {"arenas", "rewards", "stats", "items", "mysql", "messages"};

		for (String name : files) {
			File file = new File(getDataFolder(), name + ".yml");

			if (file.exists()) continue;

			saveResource(name + ".yml", false);
		}
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

	public SpecialItemManager getItemManager() {
		return itemManager;
	}

	public PermissionsManager getPermissionManager() {
		return permissionManager;
	}

	public CommandFramework getCommandFramework() {
		return commandFramework;
	}

	private void saveAllUserStatistics() {
		for (Player player : getServer().getOnlinePlayers()) {
			User user = userManager.getUser(player);

			if (userManager.getDatabase() instanceof MysqlManager) {
				StringBuilder update = new StringBuilder(" SET ");

				for (StatsStorage.StatisticType stat : StatsStorage.StatisticType.values()) {
					if (!stat.isPersistent()) continue;
					if (update.toString().equalsIgnoreCase(" SET ")) {
						update.append(stat.getName()).append("'='").append(user.getStat(stat));
					}

					update.append(", ").append(stat.getName()).append("'='").append(user.getStat(stat));
				}

				String finalUpdate = update.toString();
				MysqlDatabase mysqlDatabase = ((MysqlManager) userManager.getDatabase()).getDatabase();
				mysqlDatabase.executeUpdate("UPDATE " + mysqlDatabase + finalUpdate + " WHERE UUID='" + user.getUniqueId().toString() + "';");
				continue;
			}

			userManager.saveAllStatistic(user);
		}
	}
}