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

package me.despical.tntrun.arena;

import me.clip.placeholderapi.PlaceholderAPI;
import me.despical.commons.compat.XMaterial;
import me.despical.commons.item.ItemBuilder;
import me.despical.commons.miscellaneous.AttributeUtils;
import me.despical.commons.miscellaneous.MiscUtils;
import me.despical.commons.serializer.InventorySerializer;
import me.despical.commons.string.StringFormatUtils;
import me.despical.commons.util.LogUtils;
import me.despical.tntrun.ConfigPreferences;
import me.despical.tntrun.Main;
import me.despical.tntrun.api.StatsStorage;
import me.despical.tntrun.api.events.game.TRGameJoinAttemptEvent;
import me.despical.tntrun.api.events.game.TRGameLeaveAttemptEvent;
import me.despical.tntrun.api.events.game.TRGameStopEvent;
import me.despical.tntrun.handlers.ChatManager.ActionType;
import me.despical.tntrun.handlers.PermissionsManager;
import me.despical.tntrun.user.User;
import org.apache.commons.lang.StringUtils;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;


/**
 * @author Despical
 * <p>
 * Created at 02.07.2018
 */
public class ArenaManager {

	private static final Main plugin = JavaPlugin.getPlugin(Main.class);

	private ArenaManager() {
	}

	public static void joinAttempt(Player player, Arena arena) {
		LogUtils.log("[{0}] Initial join attempt for {1}", arena.getId(), player.getName());
		long start = System.currentTimeMillis();

		TRGameJoinAttemptEvent gameJoinAttemptEvent = new TRGameJoinAttemptEvent(player, arena);
		plugin.getServer().getPluginManager().callEvent(gameJoinAttemptEvent);

		if (!arena.isReady()) {
			player.sendMessage(plugin.getChatManager().prefixedMessage("In-Game.Arena-Not-Configured"));
			return;
		}

		if (gameJoinAttemptEvent.isCancelled()) {
			player.sendMessage(plugin.getChatManager().prefixedMessage("In-Game.Join-Cancelled-Via-API"));
			return;
		}

		if (ArenaRegistry.isInArena(player)) {
			player.sendMessage(plugin.getChatManager().prefixedMessage("In-Game.Already-Playing"));
			return;
		}

		PermissionsManager permManager = plugin.getPermissionManager();

		if (!permManager.hasJoinPerm(player, "*") || !permManager.hasJoinPerm(player, arena.getId())) {
			player.sendMessage(plugin.getChatManager().prefixedMessage("In-Game.Join-No-Permission").replace("%permission%", permManager.getJoinPerm().replace("<arena>", arena.getId())));
			return;
		}

		if (arena.getArenaState() == ArenaState.RESTARTING) {
			return;
		}

		if (arena.getPlayers().size() >= arena.getMaximumPlayers() && arena.getArenaState() == ArenaState.STARTING) {
			if (!permManager.hasFullPerm(player)) {
				player.sendMessage(plugin.getChatManager().prefixedMessage("In-Game.Full-Game-No-Permission"));
				return;
			}

			boolean foundSlot = false;

			for (Player loopPlayer : arena.getPlayers()) {
				if (permManager.hasFullPerm(loopPlayer)) {
					continue;
				}

				leaveAttempt(loopPlayer, arena);
				loopPlayer.sendMessage(plugin.getChatManager().prefixedMessage("In-Game.Messages.Lobby-Messages.You-Were-Kicked-For-Premium-Slot"));
				arena.broadcastMessage(plugin.getChatManager().formatMessage(arena, plugin.getChatManager().message("In-Game.Messages.Lobby-Messages.Kicked-For-Premium-Slot"), loopPlayer));

				foundSlot = true;
				break;
			}

			if (!foundSlot) {
				player.sendMessage(plugin.getChatManager().prefixedMessage("In-Game.No-Slots-For-Premium"));
				return;
			}
		}

		LogUtils.log("[{0}] Checked join attempt for {1} initialized.", arena.getId(), player.getName());
		arena.getScoreboardManager().createScoreboard(player);

		if (plugin.getConfigPreferences().getOption(ConfigPreferences.Option.INVENTORY_MANAGER_ENABLED)) {
			InventorySerializer.saveInventoryToFile(plugin, player);
		}

		arena.addPlayer(player);

		player.setLevel(0);
		player.setExp(1);
		player.setFoodLevel(20);
		player.getInventory().setArmorContents(null);
		player.getInventory().clear();
		player.setGameMode(GameMode.ADVENTURE);
		player.getInventory().setHeldItemSlot(0);
		AttributeUtils.healPlayer(player);

		User user = plugin.getUserManager().getUser(player);

		if (arena.getArenaState() == ArenaState.IN_GAME || arena.getArenaState() == ArenaState.ENDING) {
			arena.teleportToStartLocation(player);
			player.sendMessage(plugin.getChatManager().prefixedMessage("In-Game.You-Are-Spectator"));
			player.getInventory().clear();
			player.getInventory().setItem(0, new ItemBuilder(XMaterial.COMPASS.parseItem()).name(plugin.getChatManager().message("In-Game.Spectator.Spectator-Item-Name")).build());
			player.getInventory().setItem(4, new ItemBuilder(XMaterial.COMPARATOR.parseItem()).name(plugin.getChatManager().message("In-Game.Spectator.Settings-Menu.Item-Name")).build());
			player.getActivePotionEffects().forEach(effect -> player.removePotionEffect(effect.getType()));
			player.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, Integer.MAX_VALUE, 0, false, false, false));
			player.setCollidable(false);
			player.setAllowFlight(true);
			player.setFlying(true);

			ArenaUtils.hidePlayer(player, arena);

			user.setSpectator(true);
			user.addGameItem("leave-item");
			user.resetTemporaryStats();

			for (Player spectator : arena.getPlayers()) {
				if (plugin.getUserManager().getUser(spectator).isSpectator()) {
					player.hidePlayer(plugin, spectator);
				} else {
					player.showPlayer(plugin, spectator);
				}
			}

			ArenaUtils.hidePlayersOutsideTheGame(player, arena);
			LogUtils.log("[{0}] Join attempt as spectator finished for {1} took {2} ms", arena.getId(), player.getName(), System.currentTimeMillis() - start);
			return;
		}

		arena.teleportToLobby(player);
		arena.doBarAction(Arena.BarAction.ADD, player);

		player.setFlying(false);
		player.setAllowFlight(false);

		if (!plugin.getUserManager().getUser(player).isSpectator()) {
			plugin.getChatManager().broadcastAction(arena, player, ActionType.JOIN);
		}

		if (arena.getArenaState() == ArenaState.STARTING || arena.getArenaState() == ArenaState.WAITING_FOR_PLAYERS) {
			user.addGameItem("leave-item");

			if (player.isOp()) user.addGameItem("force-start-item");
		}

		player.updateInventory();

		arena.getPlayers().forEach(arenaPlayer -> ArenaUtils.showPlayer(arenaPlayer, arena));
		arena.showPlayers();

		ArenaUtils.updateNameTagsVisibility(player);
		LogUtils.log("[{0}] Join attempt as player for {1} took {2} ms", arena.getId(), player.getName(), System.currentTimeMillis() - start);
	}

	public static void leaveAttempt(Player player, Arena arena) {
		LogUtils.log("[{0}] Initial leave attempt for {1}", arena.getId(), player.getName());
		long start = System.currentTimeMillis();

		plugin.getServer().getPluginManager().callEvent(new TRGameLeaveAttemptEvent(player, arena));
		User user = plugin.getUserManager().getUser(player);

		if (user.getStat(StatsStorage.StatisticType.LOCAL_SURVIVE) > user.getStat(StatsStorage.StatisticType.LONGEST_SURVIVE)) {
			user.setStat(StatsStorage.StatisticType.LONGEST_SURVIVE, user.getStat(StatsStorage.StatisticType.LOCAL_SURVIVE));
		}

		arena.getScoreboardManager().removeScoreboard(player);

		if (arena.getArenaState() == ArenaState.IN_GAME && !user.isSpectator()) {
			if (arena.getPlayersLeft().size() - 1 == 1) {
				stopGame(false, arena);
				return;
			}
		}

		arena.removePlayer(player);

		if (!user.isSpectator()) {
			plugin.getChatManager().broadcastAction(arena, player, ActionType.LEAVE);
		}

		user.setSpectator(false);

		arena.doBarAction(Arena.BarAction.REMOVE, player);

		if (plugin.getConfigPreferences().getOption(ConfigPreferences.Option.INVENTORY_MANAGER_ENABLED)) {
			InventorySerializer.loadInventory(plugin, player);
		} else {
			player.getInventory().clear();
			player.getInventory().setArmorContents(null);
			player.setFoodLevel(20);
			player.setLevel(0);
			player.setExp(0);
			player.setFlying(false);
			player.setAllowFlight(false);
			player.getActivePotionEffects().forEach(effect -> player.removePotionEffect(effect.getType()));
			player.setFlySpeed(.1F);
			player.setWalkSpeed(.2F);
			player.setFireTicks(0);
			player.setGameMode(GameMode.SURVIVAL);

			AttributeUtils.healPlayer(player);
		}

		for (Player players : plugin.getServer().getOnlinePlayers()) {
			if (!ArenaRegistry.isInArena(players)) {
				players.showPlayer(plugin, player);
			}

			player.showPlayer(plugin, players);
		}

		arena.teleportToEndLocation(player);

		plugin.getUserManager().saveAllStatistic(user);
		LogUtils.log("[{0}] Game leave finished for {1} took {2} ms.", arena.getId(), player.getName(), System.currentTimeMillis() - start);
	}

	public static void stopGame(boolean quickStop, Arena arena) {
		LogUtils.log("[{0}] Stop game event initialized with quickStop {1}", arena.getId(), quickStop);
		long start = System.currentTimeMillis();

		plugin.getServer().getPluginManager().callEvent(new TRGameStopEvent(arena));

		arena.setArenaState(ArenaState.ENDING);

		if (quickStop) {
			arena.setTimer(2);
			arena.broadcastMessage(plugin.getChatManager().prefixedMessage("in_game.messages.admin_messages.stopped_game"));
			return;
		} else {
			arena.setTimer(6);
		}

		arena.getScoreboardManager().stopAllScoreboards();

		for (Player player : arena.getPlayers()) {
			User user = plugin.getUserManager().getUser(player);

			if (user.getStat(StatsStorage.StatisticType.LOCAL_SURVIVE) > user.getStat(StatsStorage.StatisticType.LONGEST_SURVIVE)) {
				user.setStat(StatsStorage.StatisticType.LONGEST_SURVIVE, user.getStat(StatsStorage.StatisticType.LOCAL_SURVIVE));
			}

			player.getInventory().clear();

			user.addGameItem("leave-item");

			for (String msg : plugin.getChatManager().getStringList("In-Game.Messages.Game-End-Messages.Summary-Message")) {
				MiscUtils.sendCenteredMessage(player, formatSummaryPlaceholders(msg, arena, player));
			}

			plugin.getUserManager().saveAllStatistic(user);
			
			if (plugin.getConfig().getBoolean("Firework-When-Game-Ends", true)) {
				new BukkitRunnable() {

					private int i = 0;

					public void run() {
						if (i == 4 || !arena.getPlayers().contains(player)) {
							cancel();
						}

						MiscUtils.spawnRandomFirework(player.getLocation());
						i++;
					}
				}.runTaskTimer(plugin, 30, 30);
			}
		}

		LogUtils.log("[{0}] Stop game event finished took {1} ms", arena.getId(), System.currentTimeMillis() - start);
	}

	private static String formatSummaryPlaceholders(String msg, Arena arena, Player player) {
		String formatted = msg;

		final User user = plugin.getUserManager().getUser(player);

		formatted = StringUtils.replace(formatted, "%winner%", arena.getPlayersLeft().get(0).getName());
		formatted = StringUtils.replace(formatted, "%earned_coins%", Integer.toString(user.getStat(StatsStorage.StatisticType.LOCAL_COINS)));
		formatted = StringUtils.replace(formatted, "%survive_time%", Integer.toString(user.getStat(StatsStorage.StatisticType.LOCAL_SURVIVE)));
		formatted = StringUtils.replace(formatted, "%formatted_survive_time%", StringFormatUtils.formatIntoMMSS(user.getStat(StatsStorage.StatisticType.LOCAL_SURVIVE)));

		if (plugin.getChatManager().isPapiEnabled()) {
			formatted = PlaceholderAPI.setPlaceholders(player, formatted);
		}

		return formatted;
	}
}