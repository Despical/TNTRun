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

import me.despical.commons.compat.XSound;
import me.despical.commons.serializer.InventorySerializer;
import me.despical.tntrun.ConfigPreferences;
import me.despical.tntrun.Main;
import me.despical.tntrun.api.StatsStorage;
import me.despical.tntrun.api.events.game.TRGameStartEvent;
import me.despical.tntrun.api.events.game.TRGameStateChangeEvent;
import me.despical.tntrun.arena.managers.GameBarManager;
import me.despical.tntrun.arena.managers.ScoreboardManager;
import me.despical.tntrun.arena.options.ArenaOption;
import me.despical.tntrun.handlers.ChatManager;
import me.despical.tntrun.user.User;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.NumberConversions;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.logging.Level;
import java.util.stream.Collectors;

/**
 * @author Despical
 * <p>
 * Created at 10.07.2020
 */
public class Arena extends BukkitRunnable {

	private static final Main plugin = JavaPlugin.getPlugin(Main.class);
	private static final ChatManager chatManager = plugin.getChatManager();

	private final String id;

	private final Map<ArenaOption, Integer> arenaOptions;
	private final Map<GameLocation, Location> gameLocations;

	private final Set<BlockState> destroyedBlocks;
	private final List<User> players, spectators, deaths, winners;

	@NotNull
	private final GameBarManager gameBarManager;

	@NotNull
	private final ScoreboardManager scoreboardManager;

	private boolean ready, forceStart, stopped;
	private String mapName;
	private ArenaState arenaState = ArenaState.INACTIVE;

	public Arena(final @NotNull String id) {
		this.id = id;
		this.mapName = id;
		this.destroyedBlocks = new HashSet<>();
		this.players = new ArrayList<>();
		this.spectators = new ArrayList<>();
		this.deaths = new ArrayList<>();
		this.winners = new ArrayList<>();
		this.arenaOptions = new EnumMap<>(ArenaOption.class);
		this.gameLocations = new EnumMap<>(GameLocation.class);
		this.gameBarManager = new GameBarManager(this, plugin);
		this.scoreboardManager = new ScoreboardManager(this, plugin);

		for (final var option : ArenaOption.values()) {
			arenaOptions.put(option, option.getIntegerValue());
		}
	}

	public boolean isInArena(final User user) {
		return user != null && this.players.contains(user);
	}

	public boolean isArenaState(final ArenaState first, final ArenaState second) {
		return this.arenaState == first || this.arenaState == second;
	}

	private void teleportToGameLocation(final User user, final GameLocation gameLocation) {
		if (!validateLocation(gameLocation)) return;

		final var player = user.getPlayer();

		user.removePotionEffectsExcept(PotionEffectType.BLINDNESS);

		player.setFoodLevel(20);
		player.setFlying(false);
		player.setAllowFlight(false);
		player.setFlySpeed(.1F);
		player.setWalkSpeed(.2F);
		player.teleport(gameLocations.get(gameLocation));
	}

	public void teleportToLobby(final User user) {
		this.teleportToGameLocation(user, GameLocation.LOBBY);
	}

	public void teleportToEndLocation(final User user) {
		if (plugin.getOption(ConfigPreferences.Option.BUNGEE_ENABLED)) {
			plugin.getBungeeManager().connectToHub(user);
			return;
		}

		this.teleportToGameLocation(user, GameLocation.END);
	}

	public GameBarManager getGameBar() {
		return gameBarManager;
	}

	public ArenaState getArenaState() {
		return this.arenaState;
	}

	public void setArenaState(final ArenaState arenaState) {
		this.arenaState = arenaState;
		this.gameBarManager.handleGameBar();
		this.updateSigns();

		plugin.getServer().getPluginManager().callEvent(new TRGameStateChangeEvent(this, arenaState));
	}

	public boolean isReady() {
		return ready;
	}

	public void setReady(boolean ready) {
		this.ready = ready;
	}

	public int getSetupProgress() {
		return ready ? 100 : 0;
	}

	public String getMapName() {
		return mapName;
	}

	public void setMapName(String mapName) {
		this.mapName = mapName;
	}

	public int getTimer() {
		return getOption(ArenaOption.TIMER);
	}

	public void setTimer(int timer) {
		setOptionValue(ArenaOption.TIMER, timer);
	}

	public int getMaximumPlayers() {
		return getOption(ArenaOption.MAXIMUM_PLAYERS);
	}

	public void setMaximumPlayers(int maximumPlayers) {
		setOptionValue(ArenaOption.MAXIMUM_PLAYERS, maximumPlayers);
	}

	public int getMinimumPlayers() {
		return getOption(ArenaOption.MINIMUM_PLAYERS);
	}

	public void setMinimumPlayers(int minimumPlayers) {
		setOptionValue(ArenaOption.MINIMUM_PLAYERS, minimumPlayers);
	}

	public Location getLobbyLocation() {
		return gameLocations.get(GameLocation.LOBBY);
	}

	public void setLobbyLocation(Location lobbyLocation) {
		gameLocations.put(GameLocation.LOBBY, lobbyLocation);
	}

	public void setEndLocation(Location endLocation) {
		gameLocations.put(GameLocation.END, endLocation);
	}

	@NotNull
	public ScoreboardManager getScoreboardManager() {
		return scoreboardManager;
	}

	public List<User> getPlayers() {
		return players;
	}

	public void addUser(final User user) {
		this.players.add(user);
	}

	public void removeUser(final User user) {
		this.players.remove(user);
	}

	public boolean isForceStart() {
		return this.forceStart;
	}

	public void setForceStart(final boolean forceStart) {
		this.forceStart = forceStart;
	}

	public void addDeathPlayer(final User user) {
		deaths.add(user);

		if (this.getPlayersLeft().size() < 4) {
			winners.add(user);
		}

		this.hideSpectator(user);
		this.addSpectator(user);
	}

	public boolean isDeathPlayer(final User user) {
		return this.deaths.contains(user);
	}

	public List<User> getWinners() {
		return winners;
	}

	public void addSpectator(final User user) {
		this.spectators.add(user);

		final var nightVision = user.getStat(StatsStorage.StatisticType.SPECTATOR_NIGHT_VISION);
		final var player = user.getPlayer();

		if (nightVision == 1) {
			player.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, Integer.MAX_VALUE, 0, false, false, false));
		}

		final var level = user.getStat(StatsStorage.StatisticType.SPECTATOR_SPEED);

		player.setFlySpeed(.1F + (level + 1) * .05F);
		player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, level, false, false, false));
	}

	public void removeSpectator(final User user) {
		this.spectators.remove(user);
	}

	public boolean isSpectator(final User user) {
		return spectators.contains(user);
	}

	public void start() {
		this.startBlockRemoving();
		this.runTaskTimer(plugin, 20L, 20L);
		this.setArenaState(ArenaState.WAITING_FOR_PLAYERS);
	}

	public void stop() {
		this.stopped = true;

		if (arenaState != ArenaState.INACTIVE) this.cancel();

		this.cleanUpArena();
		this.players.forEach(user -> plugin.getArenaManager().leaveAttempt(user, this));
	}

	private void startBlockRemoving() {
		final var startBlockRemoving = ArenaOption.START_BLOCK_REMOVING.getIntegerValue();
		final var blockRemoveDelay = ArenaOption.BLOCK_REMOVE_DELAY.getIntegerValue();
		final var removableBlocks = plugin.getConfig().getStringList("Whitelisted-Blocks");

		new BukkitRunnable() {

			@Override
			public void run() {
				for (final var user : getPlayersLeft()) {
					if (stopped) cancel();
					if (arenaState != ArenaState.IN_GAME) return;
					if (getTimer() <= startBlockRemoving) return;

					for (final var block : getRemovableBlocks(user)) {
						if (!removableBlocks.contains(block.getType().name())) continue;

						destroyedBlocks.add(block.getState());

						if (plugin.isEnabled()) plugin.getServer().getScheduler().runTaskLater(plugin, () -> block.setType(Material.AIR), blockRemoveDelay);
					}
				}
			}
		}.runTaskTimerAsynchronously(plugin, 0, 1);

	}

	private List<Block> getRemovableBlocks(User user) {
		List<Block> removableBlocks = new ArrayList<>();
		Location loc = user.getLocation();
		int SCAN_DEPTH = getOption(user.getPlayer().isOnGround() ? ArenaOption.MIN_DEPTH : ArenaOption.MAX_DEPTH), y = loc.getBlockY();

		Block block;

		for (int i = 0; i <= SCAN_DEPTH; i++) {
			block = getBlockUnderPlayer(y--, loc);

			if (block != null) {
				removableBlocks.add(block);
			}
		}

		return removableBlocks;
	}

	private Block getBlockUnderPlayer(int y, Location location) {
		Position loc = new Position(location.getX(), y, location.getZ());
		Block b1 = loc.getBlock(location.getWorld(), 0.3, -0.3);

		if (b1.getType() != Material.AIR) {
			return b1;
		}

		Block b2 = loc.getBlock(location.getWorld(), -0.3, 0.3);

		if (b2.getType() != Material.AIR) {
			return b2;
		}

		Block b3 = loc.getBlock(location.getWorld(), 0.3, 0.3);

		if (b3.getType() != Material.AIR) {
			return b3;
		}

		Block b4 = loc.getBlock(location.getWorld(), -0.3, -0.3);

		if (b4.getType() != Material.AIR) {
			return b4;
		}

		return null;
	}

	public Set<User> getPlayersLeft() {
		return this.players.stream().filter(user -> !user.isSpectator()).collect(Collectors.toSet());
	}

	public void playSound(XSound sound) {
		this.players.forEach(user -> sound.play(user.getPlayer()));
	}

	public void broadcastFormattedMessage(final String path, final User user, boolean onlySpectators) {
		if (!onlySpectators) {
			this.broadcastFormattedMessage(path, user);
			return;
		}

		if (user.isSpectator()) {
			this.players.stream().filter(u -> isSpectator(u) && !user.equals(u)).forEach(u -> u.sendRawMessage(chatManager.message(path, this, user)));
		}
	}

	public void broadcastFormattedMessage(final String path, final User user) {
		this.players.forEach(u -> u.sendRawMessage(chatManager.message(path, this, user)));
	}

	public void broadcastMessage(final String path) {
		this.players.forEach(user -> user.sendRawMessage(chatManager.message(path, this, user)));
	}

	@Nullable
	public User getWinner() {
		for (final var user : this.getPlayersLeft()) return user;

		return null;
	}

	public void cleanUpArena() {
		players.clear();
		deaths.clear();
		spectators.clear();
		winners.clear();

		final var iterator = destroyedBlocks.iterator();

		while (iterator.hasNext()) {
			iterator.next().update(true);
			iterator.remove();
		}

		stopped = false;
		forceStart = false;
	}

	public void showPlayers() {
		for (final var user : this.players) {
			var player = user.getPlayer();

			user.removePotionEffectsExcept(PotionEffectType.BLINDNESS);

			for (final User u : this.players) {
				player.showPlayer(plugin, u.getPlayer());
				u.getPlayer().showPlayer(plugin, player);
			}
		}
	}

	public void showUserToArena(final User user) {
		final var player = user.getPlayer();

		for (final var targetUser : this.players) {
			final var targetPlayer = targetUser.getPlayer();

			targetPlayer.showPlayer(plugin, player);
			player.showPlayer(plugin, targetPlayer);
		}
	}

	public void hideSpectator(final User user) {
		if (!user.isSpectator()) return;

		final var player = user.getPlayer();

		for (final var targetUser : this.players) {
			final var targetPlayer = targetUser.getPlayer();

			player.showPlayer(plugin, targetPlayer);

			if (targetUser.isSpectator()) {
				targetPlayer.showPlayer(plugin, player);
			} else {
				targetPlayer.hidePlayer(plugin, player);
			}
		}
	}

	public void hideUserOutsideTheGame(final User user) {
		final var player = user.getPlayer();

		for (final var targetUser : plugin.getUserManager().getUsers()) {
			final var targetPlayer = targetUser.getPlayer();

			if (isInArena(targetUser)) {
				this.showUserToArena(targetUser);
			} else {
				targetPlayer.hidePlayer(plugin, player);
				player.hidePlayer(plugin, targetPlayer);
			}
		}
	}

	public void showUserOutsideTheGame(final User user) {
		final var player = user.getPlayer();

		for (final var targetUser : plugin.getUserManager().getUsers()) {
			final var targetPlayer = targetUser.getPlayer();

			if (!isInArena(targetUser)) {
				targetPlayer.showPlayer(plugin, player);
				player.showPlayer(plugin, targetPlayer);
			} else {
				targetPlayer.hidePlayer(plugin, player);
				player.hidePlayer(plugin, targetPlayer);
			}
		}
	}

	public void updateSigns() {
		final var signManager = plugin.getSignManager();

		if (signManager == null) return;

		signManager.updateSign(this);
	}

	private int getOption(ArenaOption option) {
		return arenaOptions.get(option);
	}

	private void setOptionValue(ArenaOption option, int value) {
		arenaOptions.put(option, value);
	}

	private boolean validateLocation(final GameLocation gameLocation) {
		final var location = this.gameLocations.get(gameLocation);

		if (location == null) {
			plugin.getLogger().log(Level.WARNING, "Lobby location isn't initialized for arena {0}!", id);
			return false;
		}

		return true;
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
						broadcastMessage("messages.arena.waiting-for-players");
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

					plugin.getServer().getPluginManager().callEvent(new TRGameStartEvent(this));

					this.playSound(XSound.ENTITY_ENDER_DRAGON_GROWL);

					for (final var user : this.players) {
						teleportToLobby(user);

						user.resetTemporaryStats();
						user.addGameItems("double-jump");
						user.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, Integer.MAX_VALUE, 0, false, false, false));

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

						user.removePotionEffectsExcept(PotionEffectType.BLINDNESS);
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

	@NotNull
	public String getId() {
		return this.id;
	}

	@Override
	public String toString() {
		return this.id;
	}

	public enum GameLocation {
		LOBBY, END
	}

	private record Position(double x, int y, double z) {

		public Block getBlock(World world, double addx, double addz) {
			return world.getBlockAt(NumberConversions.floor(x + addx), y, NumberConversions.floor(z + addz));
		}
	}
}