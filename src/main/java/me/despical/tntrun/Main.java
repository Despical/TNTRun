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

package me.despical.tntrun;

import me.despical.commandframework.CommandFramework;
import me.despical.commons.scoreboard.ScoreboardLib;
import me.despical.commons.serializer.InventorySerializer;
import me.despical.commons.util.UpdateChecker;
import me.despical.fileitems.ItemManager;
import me.despical.fileitems.ItemOption;
import me.despical.tntrun.arena.Arena;
import me.despical.tntrun.arena.ArenaRegistry;
import me.despical.tntrun.arena.ArenaUtils;
import me.despical.tntrun.arena.managers.ArenaManager;
import me.despical.tntrun.command.AdminCommands;
import me.despical.tntrun.command.PlayerCommands;
import me.despical.tntrun.events.EventListener;
import me.despical.tntrun.handlers.ChatManager;
import me.despical.tntrun.handlers.PermissionsManager;
import me.despical.tntrun.handlers.PlaceholderHandler;
import me.despical.tntrun.handlers.bungee.BungeeManager;
import me.despical.tntrun.handlers.rewards.RewardsFactory;
import me.despical.tntrun.handlers.sign.SignManager;
import me.despical.tntrun.user.User;
import me.despical.tntrun.user.UserManager;
import org.bstats.bukkit.Metrics;
import org.bstats.charts.SimplePie;
import org.bukkit.entity.Player;
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
	private BungeeManager bungeeManager;
	private RewardsFactory rewardsFactory;
	private ConfigPreferences configPreferences;
	private ChatManager chatManager;
	private UserManager userManager;
	private ItemManager itemManager;
	private PermissionsManager permissionManager;
	private CommandFramework commandFramework;
	private SignManager signManager;

	@Override
	public void onEnable() {
		initializeClasses();
		checkUpdate();

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

				if (getOption(ConfigPreferences.Option.INVENTORY_MANAGER_ENABLED)) {
					InventorySerializer.loadInventory(this, player);
				}
			}

			arena.cleanUpArena();
		}

		userManager.getUserDatabase().shutdown();
	}

	private void initializeClasses() {
		this.setupConfigurationFiles();

		this.configPreferences = new ConfigPreferences(this);
		this.chatManager = new ChatManager(this);
		this.userManager = new UserManager(this);
		this.commandFramework = new CommandFramework(this);
		this.arenaRegistry = new ArenaRegistry(this);
		this.arenaManager = new ArenaManager(this);
		this.itemManager = new ItemManager(this, manager -> {
			ItemOption.enableOptions(ItemOption.GLOW);
			manager.addCustomKeys("slot");
			manager.editItemBuilder(builder -> builder.unbreakable(true).hideTooltip(true));
			manager.registerItems("items", "items");
		});
		this.permissionManager = new PermissionsManager(this);
		this.rewardsFactory = new RewardsFactory(this);
		this.signManager = new SignManager(this);

		ScoreboardLib.setPluginInstance(this);
		EventListener.registerEvents(this);
		User.cooldownHandlerTask();

		new AdminCommands();
		new PlayerCommands();

		if (getOption(ConfigPreferences.Option.BUNGEE_ENABLED)) {
			this.bungeeManager = new BungeeManager(this);
		}

		if (getServer().getPluginManager().isPluginEnabled("PlaceholderAPI")) {
			new PlaceholderHandler(this);
		}

		if (getOption(ConfigPreferences.Option.NAME_TAGS_HIDDEN)) {
			getServer().getScheduler().scheduleSyncRepeatingTask(this, () -> userManager.getUsers().forEach(ArenaUtils::updateNameTagsVisibility), 60, 140);
		}

		final var metrics = new Metrics(this, 8147);
		metrics.addCustomChart(new SimplePie("database_enabled", () -> getOption(ConfigPreferences.Option.DATABASE_ENABLED) ? "Enabled" : "Disabled"));
		metrics.addCustomChart(new SimplePie("update_notifier", () -> getOption(ConfigPreferences.Option.UPDATE_NOTIFIER_ENABLED) ? "Enabled" : "Disabled"));
	}

	private void checkUpdate() {
		if (!getOption(ConfigPreferences.Option.UPDATE_NOTIFIER_ENABLED)) return;

		UpdateChecker.init(this, 83196).requestUpdateCheck().whenComplete((result, exception) -> {
			if (!result.requiresUpdate()) return;

			var logger = getLogger();
			logger.info("Found a new version available: v" + result.getNewestVersion());
			logger.info("Download it on SpigotMC:");
			logger.info("https://spigotmc.org/resources/83196");
		});
	}

	private void setupConfigurationFiles() {
		Stream.of("config", "arena", "rewards", "stats", "items", "mysql", "messages", "bungee").filter(name -> !new File(getDataFolder(), name + ".yml").exists()).forEach(name -> saveResource(name + ".yml", false));
	}

	public boolean getOption(ConfigPreferences.Option option) {
		return configPreferences.getOption(option);
	}

	@NotNull
	public RewardsFactory getRewardsFactory() {
		return rewardsFactory;
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
	public ItemManager getItemManager() {
		return itemManager;
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

	@NotNull
	public BungeeManager getBungeeManager() {
		return bungeeManager;
	}

	public SignManager getSignManager() {
		return signManager;
	}
}