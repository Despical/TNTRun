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
import dev.despical.tntrun.command.AdminCommands;
import dev.despical.tntrun.command.PlayerCommands;
import dev.despical.tntrun.events.EventListener;
import dev.despical.tntrun.handlers.ChatManager;
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
import java.util.Optional;
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

		userManager.getUserDatabase().shutdown();
	}

	private void initializeClasses() {
        Locale.setDefault(Locale.ENGLISH);

		this.setupConfigurationFiles();

		this.options = new ConfigOptions(this);
		this.chatManager = new ChatManager(this);
		this.userManager = new UserManager(this);
		this.commandFramework = new CommandFramework(this);
		this.arenaRegistry = new ArenaRegistry(this);
		this.arenaManager = new ArenaManager(this);
		this.itemManager = new ItemManager(this, manager -> {
			ItemOption.enableOptions(ItemOption.GLOW);

			manager.editItemBuilder(builder -> builder.unbreakable(true).hideTooltip(true));
			manager.registerItems("items", "items");
		});

		this.permissionManager = new PermissionsManager(this);
		this.signManager = new SignManager(this);

		ScoreboardLib.setPluginInstance(this);
		EventListener.registerEvents(this);
		User.cooldownHandlerTask();

		new AdminCommands();
		new PlayerCommands();

		if (getServer().getPluginManager().isPluginEnabled("PlaceholderAPI")) {
			this.leaderboardManager = new LeaderboardManager(this);

			new PlaceholderHandler(this);
		}

		if (BooleanOption.NAME_TAGS_HIDDEN.value()) {
			getServer().getScheduler().scheduleSyncRepeatingTask(this, () -> userManager.getUsers().forEach(ArenaUtils::updateNameTagsVisibility), 60, 140);
		}

		this.handleAutoDataSaving();
	}

	private void handleAutoDataSaving() {
		long period = getConfig().getLong("Statistic-Saving-Period", 300) * 20;

		if (period > 0) {
			getServer().getScheduler().runTaskTimerAsynchronously(this, () -> {
				userManager.getUserDatabase().saveAllStatistics();

				Optional.ofNullable(leaderboardManager).ifPresent(LeaderboardManager::updateLeaderboards);
			}, period, period);
		}
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
