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

package me.despical.tntrun.arena;

import me.despical.commons.compat.XPotion;
import me.despical.commons.compat.XSound;
import me.despical.commons.serializer.InventorySerializer;
import me.despical.tntrun.ConfigPreferences;
import me.despical.tntrun.api.StatsStorage;
import me.despical.tntrun.api.events.game.GameStartEvent;
import me.despical.tntrun.arena.data.ArenaData;
import me.despical.tntrun.arena.options.ArenaOption;
import org.bukkit.GameMode;

/**
 * @author Despical
 * <p>
 * Created at 6.10.2024
 */
public abstract class ArenaHandler extends ArenaData {

	public ArenaHandler(String id) {
		super(id);
	}

	@Override
	public void run() {
		if (players.isEmpty() && arenaState == ArenaState.WAITING_FOR_PLAYERS) {
			return;
		}

		final int minPlayers = getMinimumPlayers(), waitingTime = getOption(ArenaOption.LOBBY_WAITING_TIME), startingTime = getOption(ArenaOption.LOBBY_STARTING_TIME);

		switch (arenaState) {
			case WAITING_FOR_PLAYERS -> {
				if (players.size() < minPlayers) {
					if (getTimer() <= 0) {
						setTimer(waitingTime);
						broadcastWaitingForPlayers();
						break;
					}
				} else {
					setArenaState(ArenaState.STARTING);
					showPlayers();
					setTimer(startingTime);
				}

				setTimer(getTimer() - 1);
			}

			case STARTING -> {
				if (players.size() < minPlayers) {
					setArenaState(ArenaState.WAITING_FOR_PLAYERS);
					setTimer(waitingTime);
					setForceStart(false);
					broadcastMessage("messages.arena.countdown-cancelled");
					break;
				}

				if (getTimer() == 20) {
					broadcastMessage("messages.arena.starts-in-20s");

					this.playSound(XSound.UI_BUTTON_CLICK);
				}

				if (getTimer() == 10) {
					broadcastMessage("messages.arena.starts-in-10s");

					this.playSound(XSound.UI_BUTTON_CLICK);
				}

				if (getTimer() <= 5 && getTimer() != 0) {
					broadcastMessage("messages.arena.starts-in-5s-and-less");

					this.playSound(XSound.UI_BUTTON_CLICK);
				}

				if (getTimer() == 0) {
					setArenaState(ArenaState.IN_GAME);
					broadcastMessage("messages.in-game.game-started");

					plugin.getServer().getPluginManager().callEvent(new GameStartEvent(plugin.getArenaRegistry().getArena(this.getId())));

					this.playSound(XSound.ENTITY_ENDER_DRAGON_GROWL);

					for (final var user : this.players) {
						teleportToLobby(user);

						user.resetTemporaryStats();
						user.addGameItems("double-jump");
						user.getPlayer().addPotionEffect(XPotion.NIGHT_VISION.buildInvisible(Integer.MAX_VALUE, 1));

						ArenaUtils.updateNameTagsVisibility(user);
					}

					break;
				}

				setTimer(getTimer() - 1);
			}

			case IN_GAME -> {
				int timer = getTimer();

				for (final var user : this.players) {
					if (user.isSpectator() || isDeathPlayer(user)) continue;

					final var player = user.getPlayer();

					if (user.getCooldown("double_jump") > 0) {
						player.setAllowFlight(false);
					} else if (user.getStat(StatsStorage.StatisticType.LOCAL_DOUBLE_JUMPS) > 0) {
						player.setAllowFlight(true);
					}

					user.addStat(StatsStorage.StatisticType.LOCAL_SURVIVE, 1);

					if (timer > 0 && timer % 30 == 0) {
						ArenaUtils.addScore(user, ArenaUtils.ScoreAction.SURVIVE_TIME);
					}
				}

				setTimer(getTimer() + 1);
			}

			case ENDING -> {
				if (getTimer() <= 0) {
					scoreboardManager.stopAllScoreboards();
					gameBarManager.removeAll();

					for (final var user : this.players) {
						plugin.getUserManager().saveStatistics(user);

						final var player = user.getPlayer();

						for (final var users : plugin.getUserManager().getUsers()) {
							player.showPlayer(plugin, users.getPlayer());

							if (!users.isInArena()) {
								users.getPlayer().showPlayer(plugin, player);
							}
						}

						user.removePotionEffectsExcept(XPotion.BLINDNESS);
						user.setSpectator(false);

						player.getInventory().clear();
						player.getInventory().setArmorContents(null);

						if (plugin.getOption(ConfigPreferences.Option.INVENTORY_MANAGER_ENABLED)) {
							InventorySerializer.loadInventory(plugin, player);
						} else {
							player.setGameMode(GameMode.SURVIVAL);
							player.setWalkSpeed(.2F);
							player.setFlying(false);
							player.setAllowFlight(false);
							player.setFireTicks(0);
							player.setFoodLevel(20);
						}

						teleportToEndLocation(user);
					}

					if (plugin.getOption(ConfigPreferences.Option.BUNGEE_ENABLED) && plugin.getBungeeManager().isShutdownWhenGameEnds()) {
						plugin.getServer().shutdown();
					}

					setArenaState(ArenaState.RESTARTING);
				}

				setTimer(getTimer() - 1);
			}

			case RESTARTING -> {
				cleanUpArena();

				setArenaState(ArenaState.WAITING_FOR_PLAYERS);

				if (plugin.getOption(ConfigPreferences.Option.BUNGEE_ENABLED)) {
					final var arenaRegistry = plugin.getArenaRegistry();
					final var arenaManager = plugin.getArenaManager();
					final var userManager = plugin.getUserManager();

					arenaRegistry.shuffleBungeeArena();

					for (final var player : plugin.getServer().getOnlinePlayers()) {
						arenaManager.joinAttempt(userManager.getUser(player), arenaRegistry.getBungeeArena());
					}
				}
			}
		}
	}
}
