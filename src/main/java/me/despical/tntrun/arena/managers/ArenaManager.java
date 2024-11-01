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

package me.despical.tntrun.arena.managers;

import me.despical.commons.compat.XPotion;
import me.despical.commons.miscellaneous.MiscUtils;
import me.despical.commons.serializer.InventorySerializer;
import me.despical.commons.string.StringFormatUtils;
import me.despical.commons.util.Strings;
import me.despical.tntrun.ConfigPreferences;
import me.despical.tntrun.Main;
import me.despical.tntrun.api.StatsStorage;
import me.despical.tntrun.api.events.game.GameJoinAttemptEvent;
import me.despical.tntrun.api.events.game.GameLeaveEvent;
import me.despical.tntrun.api.events.game.GameEndEvent;
import me.despical.tntrun.arena.Arena;
import me.despical.tntrun.arena.ArenaState;
import me.despical.tntrun.arena.ArenaUtils;
import me.despical.tntrun.arena.options.ArenaOption;
import me.despical.tntrun.handlers.rewards.Reward;
import me.despical.tntrun.user.User;
import me.despical.tntrun.utils.Utils;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * @author Despical
 * <p>
 * Created at 4.02.2023
 */
public record ArenaManager(Main plugin) {

	public void joinAttempt(final User user, final Arena arena) {
		final var gameJoinEvent = new GameJoinAttemptEvent(user, arena);

		plugin.getServer().getPluginManager().callEvent(gameJoinEvent);

		if (gameJoinEvent.isCancelled()) {
			return;
		}

		if (!arena.isReady()) {
			user.sendMessage("messages.arena.not-configured");
			return;
		}

		if (user.isInArena()) {
			user.sendMessage("messages.arena.already-playing");
			return;
		}

		if (arena.isArenaState(ArenaState.RESTARTING)) {
			user.sendMessage("messages.arena.restarting");
			return;
		}

		if (!plugin.getOption(ConfigPreferences.Option.BUNGEE_ENABLED) && !plugin.getPermissionManager().hasPermission(user, arena)) {
			user.sendMessage("messages.arena.no-permission");
			return;
		}

		final var player = user.getPlayer();

		if (arena.getPlayers().size() >= arena.getMaximumPlayers() && arena.isArenaState(ArenaState.STARTING)) {
			if (!plugin.getPermissionManager().hasFullGamePerm(player)) {
				user.sendMessage("messages.premium.full-game-no-permission");
				return;
			}

			boolean foundSlot = false;

			for (User loopedUser : arena.getPlayers()) {
				if (plugin.getPermissionManager().hasFullGamePerm(loopedUser.getPlayer())) {
					continue;
				}

				this.leaveAttempt(loopedUser, arena);

				loopedUser.sendMessage("messages.premium.you-were-kicked-for-premium-slot");
				arena.broadcastFormattedMessage("messages.premium.kicked-for-premium-slot", loopedUser);

				foundSlot = true;
				break;
			}

			if (!foundSlot) {
				user.sendMessage("messages.premium.no-slots-for-premium");
				return;
			}
		}

		if (plugin.getOption(ConfigPreferences.Option.INVENTORY_MANAGER_ENABLED)) {
			InventorySerializer.saveInventoryToFile(plugin, player);
		}

		ArenaUtils.updateNameTagsVisibility(user);

		arena.addUser(user);
		arena.teleportToLobby(user);
		arena.getScoreboardManager().createScoreboard(user);
		arena.getGameBar().doBarAction(user, 1);
		arena.hideUserOutsideTheGame(user);
		arena.updateSigns();

		player.setLevel(0);
		player.setExp(0);
		player.setFoodLevel(20);
		player.getInventory().clear();
		player.getInventory().setHeldItemSlot(0);
		player.getInventory().setArmorContents(null);
		player.setGameMode(GameMode.ADVENTURE);
		player.setAllowFlight(false);
		player.setGlowing(false);

		user.heal();
		user.removePotionEffectsExcept();
		user.resetTemporaryStats();
		user.addGameItem("leave-item");

		if (player.isOp()) user.addGameItem("force-start-item");

		if (arena.isArenaState(ArenaState.IN_GAME, ArenaState.ENDING)) {
			user.setSpectator(true);
			user.sendMessage("messages.in-game.you-are-spectator-now");
			user.addGameItems(false,"settings-item", "teleporter-item");

			arena.hideSpectator(user);
			arena.teleportToLobby(user);
			arena.addSpectator(user);

			player.setAllowFlight(true);
			player.setFlying(true);
			return;
		}

		arena.showPlayers();
		arena.teleportToLobby(user);
		arena.broadcastFormattedMessage("messages.arena.join-arena", user);

		plugin.getChatManager().getStringList("messages.arena.game-explanation").forEach(message -> MiscUtils.sendCenteredMessage(player, message));
	}

	public void leaveAttempt(final User user, final Arena arena) {
		plugin.getServer().getPluginManager().callEvent(new GameLeaveEvent(user, arena));

		int localScore = user.getStat(StatsStorage.StatisticType.LOCAL_SURVIVE);

		if (localScore > user.getStat(StatsStorage.StatisticType.LONGEST_SURVIVE) && !plugin.getOption(ConfigPreferences.Option.LONGEST_SURVIVE_ON_WINS)) {
			user.setStat(StatsStorage.StatisticType.LONGEST_SURVIVE, localScore);
		}

		arena.broadcastFormattedMessage("messages.arena.quit-arena", user, true);
		arena.removeUser(user);
		arena.removeSpectator(user);
		arena.teleportToEndLocation(user);
		arena.getScoreboardManager().removeScoreboard(user);
		arena.getGameBar().doBarAction(user, 0);
		arena.showUserOutsideTheGame(user);
		arena.updateSigns();

		Player player = user.getPlayer();
		player.getInventory().clear();
		player.getInventory().setArmorContents(null);
		player.setFoodLevel(20);
		player.setLevel(0);
		player.setExp(0);
		player.setFlying(false);
		player.setAllowFlight(false);
		player.setFlySpeed(.1F);
		player.setWalkSpeed(.2F);
		player.setFireTicks(0);
		player.setGameMode(GameMode.SURVIVAL);
		player.getInventory().setHeldItemSlot(0);

		user.heal();
		user.setSpectator(false);
		user.removePotionEffectsExcept();

		boolean bungeeEnabled = plugin.getOption(ConfigPreferences.Option.BUNGEE_ENABLED);

		if (!bungeeEnabled&& plugin.getOption(ConfigPreferences.Option.INVENTORY_MANAGER_ENABLED)) {
			InventorySerializer.loadInventory(plugin, player);
		}

		plugin.getUserManager().saveStatistics(user);

		if (bungeeEnabled) {
			plugin.getBungeeManager().connectToHub(user);
		}

		if (arena.getArenaState() == ArenaState.IN_GAME) {
			var playersLeft = List.copyOf(arena.getPlayersLeft());

			if (playersLeft.size() == 1) {
				arena.addWinner(playersLeft.get(0));

				stopGame(false, arena);
			}
		}
	}

	public void stopGame(boolean quickStop, Arena arena) {
		plugin.getServer().getPluginManager().callEvent(new GameEndEvent(arena, quickStop));

		arena.setArenaState(ArenaState.ENDING);
		arena.setTimer(quickStop ? 2 : ArenaOption.LOBBY_ENDING_TIME.getIntegerValue());
		arena.showPlayers();

		final var chatManager = plugin.getChatManager();
		final var winner = arena.getWinner();
		final var updateOnWins = plugin.getOption(ConfigPreferences.Option.LONGEST_SURVIVE_ON_WINS);

		for (final var user : arena.getPlayers()) {
			final var localScore = user.getStat(StatsStorage.StatisticType.LOCAL_SURVIVE);

			boolean isWinner = user.equals(winner);

			if (localScore > user.getStat(StatsStorage.StatisticType.LONGEST_SURVIVE)) {
				if (!updateOnWins || isWinner) {
					user.setStat(StatsStorage.StatisticType.LONGEST_SURVIVE, localScore);
				}
			}

			user.addStat(StatsStorage.StatisticType.COINS, user.getStat(StatsStorage.StatisticType.LOCAL_COINS));
			user.addStat(StatsStorage.StatisticType.GAMES_PLAYED, 1);
			user.addGameItems("leave-item", "play-again");
			user.removePotionEffectsExcept(XPotion.BLINDNESS);
			user.addStat(isWinner ? StatsStorage.StatisticType.WINS : StatsStorage.StatisticType.LOSES, 1);
			user.performReward(isWinner ? Reward.RewardType.WIN : Reward.RewardType.LOSE);

			plugin.getUserManager().saveStatistics(user);
		}

		if (quickStop) return;

		List<String> summaryMessages = chatManager.getStringList("messages.summary-message");

		for (User user : arena.getPlayers()) {
			user.performReward(Reward.RewardType.END_GAME);

			for (String msg : summaryMessages) {
				try {
					var message = formatSummaryMessage(msg, arena, user);

					if (Arrays.stream(message).anyMatch(component -> component.toLegacyText().contains("%skip_line%"))) {
						continue;
					}

					Utils.sendCenteredMessage(user.getPlayer(), message);
				} catch (NoSuchMethodError bungeeAPINotFound) {
					String message = legacyFormatSummaryMessage(msg, arena, user);

					if (message.contains("%skip_line%")) continue;

					MiscUtils.sendCenteredMessage(user.getPlayer(), message);
				}
 			}
		}
	}

	private String legacyFormatSummaryMessage(String msg, Arena arena, User user) {
		List<User> winners = new ArrayList<>(arena.getWinners());
		Collections.reverse(winners);

		for (int i = 0; i < 4; i++) {
			if (i >= winners.size()) {
				msg = msg.replace("%player_" + (i + 1) + '%', "%skip_line%");
				continue;
			}

			msg = msg.replace("%player_" + (i + 1) + '%', winners.get(i).getName());
		}

		msg = msg.replace("%winner%", arena.getWinner().getName());
		msg = msg.replace("%earned_coins%", Integer.toString(user.getStat(StatsStorage.StatisticType.LOCAL_COINS)));
		msg = msg.replace("%survive_time%", Integer.toString(user.getStat(StatsStorage.StatisticType.LOCAL_SURVIVE)));
		msg = msg.replace("%formatted_survive_time%", StringFormatUtils.formatIntoMMSS(user.getStat(StatsStorage.StatisticType.LOCAL_SURVIVE)));
		return msg;
	}

	private BaseComponent[] formatSummaryMessage(String msg, Arena arena, User user) {
		var formatted = msg;

		final var winners = new ArrayList<>(arena.getWinners());
		Collections.reverse(winners);

		formatted = formatted.replace("%winner%", arena.getWinner().getName());
		formatted = formatted.replace("%earned_coins%", Integer.toString(user.getStat(StatsStorage.StatisticType.LOCAL_COINS)));
		formatted = formatted.replace("%survive_time%", Integer.toString(user.getStat(StatsStorage.StatisticType.LOCAL_SURVIVE)));
		formatted = formatted.replace("%formatted_survive_time%", StringFormatUtils.formatIntoMMSS(user.getStat(StatsStorage.StatisticType.LOCAL_SURVIVE)));
		formatted = formatted.replace("%no_center%", "");

		var builder = new ComponentBuilder();

		for (int i = 0; i < 4; i++) {
			if (i >= winners.size()) {
				formatted = formatted.replace("%player_" + (i + 1) + '%', "%skip_line%");
				continue;
			}

			var winner = winners.get(i);

			if (msg.contains("%player_" + (i + 1))) {
				int jumps = winner.getStat(StatsStorage.StatisticType.LOCAL_DOUBLE_JUMPS), max = plugin.getPermissionManager().getDoubleJumps(winner.getPlayer());

				var hover = plugin.getChatManager().message("messages.summary-message-hover")
					.replace("%double_jumps%", arena.getScoreboardManager().getDoubleJumpColor(jumps, max) + jumps)
					.replace("%max_double_jumps%", Integer.toString(max));

				builder.event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText(hover)));
			}

			formatted = formatted.replace("%player_" + (i + 1) + '%', winner.getName());
		}

		return builder.append(TextComponent.fromLegacy(Strings.format(formatted))).create();
	}
}