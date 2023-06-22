package me.despical.tntrun.arena.managers;

import me.despical.commons.miscellaneous.AttributeUtils;
import me.despical.commons.miscellaneous.MiscUtils;
import me.despical.commons.serializer.InventorySerializer;
import me.despical.commons.string.StringFormatUtils;
import me.despical.tntrun.ConfigPreferences;
import me.despical.tntrun.Main;
import me.despical.tntrun.api.StatsStorage;
import me.despical.tntrun.arena.Arena;
import me.despical.tntrun.arena.ArenaState;
import me.despical.tntrun.arena.ArenaUtils;
import me.despical.tntrun.arena.options.ArenaOption;
import me.despical.tntrun.handlers.rewards.Reward;
import me.despical.tntrun.user.User;
import org.bukkit.GameMode;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.Collections;

/**
 * @author Despical
 * <p>
 * Created at 4.02.2023
 */
public record ArenaManager(Main plugin) {

	public void joinAttempt(final User user, final Arena arena) {
		if (!arena.isReady()) {
			user.sendMessage("messages.arena.not-configured");
			return;
		}

		if (user.isInArena()) {
			user.sendMessage("messages.arena.already-playing");
			return;
		}

		if (arena.getArenaState() == ArenaState.RESTARTING) {
			user.sendMessage("messages.arena.restarting");
			return;
		}

		if (!plugin.getPermissionManager().hasPermission(user, arena)) {
			user.sendMessage("messages.arena.no-permission");
			return;
		}

		final var player = user.getPlayer();

		if (plugin.getConfigPreferences().getOption(ConfigPreferences.Option.INVENTORY_MANAGER_ENABLED)) {
			InventorySerializer.saveInventoryToFile(plugin, player);
		}

		ArenaUtils.updateNameTagsVisibility(user);

		arena.addUser(user);
		arena.teleportToLobby(user);
		arena.getScoreboardManager().createScoreboard(user);
		arena.getGameBar().doBarAction(user, 1);
		arena.hideUserOutsideTheGame(user);

		player.setLevel(0);
		player.setExp(0);
		player.setFoodLevel(20);
		player.getInventory().clear();
		player.getInventory().setHeldItemSlot(0);
		player.getInventory().setArmorContents(null);
		player.setGameMode(GameMode.ADVENTURE);
		player.setAllowFlight(false);
		player.setGlowing(false);

		AttributeUtils.healPlayer(player);

		user.removePotionEffectsExcept();
		user.resetTemporaryStats();
		user.addGameItem("leave-item");

		if (player.isOp()) user.addGameItem("force-start-item");

		if (arena.isArenaState(ArenaState.IN_GAME, ArenaState.ENDING)) {
			user.setSpectator(true);
			user.sendMessage("messages.in-game.you-are-spectator-now");
			user.addGameItems("settings-item", "teleporter-item", "leave-item");

			arena.hideSpectator(user);
			arena.addSpectator(user);
			arena.teleportToLobby(user);

			player.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, Integer.MAX_VALUE, 0, false, false, false));
			player.setAllowFlight(true);
			player.setFlying(true);
			return;
		}

		arena.showPlayers();
		arena.teleportToLobby(user);
		arena.broadcastFormattedMessage("messages.arena.join-arena", user);

		for (final var message : plugin.getChatManager().message("messages.arena.game-explanation").split("\n")) {
			MiscUtils.sendCenteredMessage(player, message);
		}
	}

	public void leaveAttempt(final User user, final Arena arena) {
		plugin.getUserManager().saveStatistics(user);

		final var localScore = user.getStat(StatsStorage.StatisticType.LOCAL_SURVIVE);

		if (localScore > user.getStat(StatsStorage.StatisticType.LONGEST_SURVIVE)) {
			user.setStat(StatsStorage.StatisticType.LONGEST_SURVIVE, localScore);
		}

		final var player = user.getPlayer();

		arena.broadcastFormattedMessage("messages.arena.quit-arena", user, true);
		arena.removeUser(user);
		arena.removeSpectator(user);
		arena.teleportToEndLocation(user);
		arena.getScoreboardManager().removeScoreboard(user);
		arena.getGameBar().doBarAction(user, 0);
		arena.showUserOutsideTheGame(user);

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

		AttributeUtils.healPlayer(player);

		if (plugin.getConfigPreferences().getOption(ConfigPreferences.Option.INVENTORY_MANAGER_ENABLED)) {
			InventorySerializer.loadInventory(plugin, player);
		}

		user.setSpectator(false);
		user.removePotionEffectsExcept();

		plugin.getUserManager().saveStatistics(user);

		if (arena.getArenaState() == ArenaState.IN_GAME) {
			if (arena.getPlayersLeft().size() == 1) {
				stopGame(false, arena);
			}
		}
	}

	public void stopGame(boolean quickStop, Arena arena) {
		arena.setArenaState(ArenaState.ENDING);
		arena.setTimer(quickStop ? 2 : ArenaOption.LOBBY_ENDING_TIME.getIntegerValue());
		arena.showPlayers();

		final var chatManager = plugin.getChatManager();
		final var winner = arena.getWinner();

		for (final var user : arena.getPlayers()) {
			final var localScore = user.getStat(StatsStorage.StatisticType.LOCAL_SURVIVE);

			if (localScore > user.getStat(StatsStorage.StatisticType.LONGEST_SURVIVE)) {
				user.setStat(StatsStorage.StatisticType.LONGEST_SURVIVE, localScore);
			}

			user.addStat(StatsStorage.StatisticType.COINS, user.getStat(StatsStorage.StatisticType.LOCAL_COINS));
			user.addStat(StatsStorage.StatisticType.GAMES_PLAYED, 1);
			user.addGameItems(true, "leave-item", "play-again");
			user.removePotionEffectsExcept(PotionEffectType.BLINDNESS);
			user.addStat(user.equals(winner) ? StatsStorage.StatisticType.WINS : StatsStorage.StatisticType.LOSES, 1);

			plugin.getUserManager().saveStatistics(user);
		}

		if (quickStop) return;

		final var summaryMessages = chatManager.getStringList("messages.summary-message");

		for (final var user : arena.getPlayers()) {
			user.performReward(Reward.RewardType.END_GAME);

			for (final var msg : summaryMessages) {
				final var message = formatSummaryPlaceholders(msg, arena, user);

				if (message.contains("%skip_line%")) continue;

				MiscUtils.sendCenteredMessage(user.getPlayer(), message);
			}
		}
	}

	private String formatSummaryPlaceholders(String msg, Arena arena, User user) {
		var formatted = msg;

		final var winners = new ArrayList<>(arena.getWinners());
		Collections.reverse(winners);

		for (int i = 0; i < 4; i++) {
			if (i >= winners.size()) {
				formatted = formatted.replace("%player_" + (i + 1) + '%', "%skip_line%");
				continue;
			}

			formatted = formatted.replace("%player_" + (i + 1) + '%', winners.get(i).getName());
		}

		formatted = formatted.replace("%winner%", arena.getWinner().getName());
		formatted = formatted.replace("%earned_coins%", Integer.toString(user.getStat(StatsStorage.StatisticType.LOCAL_COINS)));
		formatted = formatted.replace("%survive_time%", Integer.toString(user.getStat(StatsStorage.StatisticType.LOCAL_SURVIVE)));
		formatted = formatted.replace("%formatted_survive_time%", StringFormatUtils.formatIntoMMSS(user.getStat(StatsStorage.StatisticType.LOCAL_SURVIVE)));

		return formatted;
	}
}