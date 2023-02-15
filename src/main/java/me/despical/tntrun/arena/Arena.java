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

import me.despical.commons.serializer.InventorySerializer;
import me.despical.commons.util.LogUtils;
import me.despical.tntrun.ConfigPreferences;
import me.despical.tntrun.Main;
import me.despical.tntrun.api.StatsStorage;
import me.despical.tntrun.api.events.game.TRGameStartEvent;
import me.despical.tntrun.api.events.game.TRGameStateChangeEvent;
import me.despical.tntrun.arena.managers.ScoreboardManager;
import me.despical.tntrun.arena.options.ArenaOption;
import me.despical.tntrun.handlers.rewards.Reward;
import me.despical.tntrun.user.User;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

/**
 * @author Despical
 * <p>
 * Created at 10.07.2020
 */
public class Arena extends BukkitRunnable {

	private static final Main plugin = JavaPlugin.getPlugin(Main.class);
	private final String id;

	private final Set<Player> players = new HashSet<>();
	private final Set<BlockState> destroyedBlocks = new HashSet<>();

	private final Map<ArenaOption, Integer> arenaOptions = new EnumMap<>(ArenaOption.class);
	private final Map<GameLocation, Location> gameLocations = new EnumMap<>(GameLocation.class);

	private ArenaState arenaState = ArenaState.INACTIVE;
	private BossBar gameBar;
	private final ScoreboardManager scoreboardManager;

	private String mapName = "";
	private boolean ready;
	private boolean forceStart = false;

	public Arena(String id) {
		this.id = id;

		for (ArenaOption option : ArenaOption.values()) {
			arenaOptions.put(option, option.getDefaultValue());
		}

		if (plugin.getConfigPreferences().getOption(ConfigPreferences.Option.BOSS_BAR_ENABLED)) {
			gameBar = Bukkit.createBossBar(plugin.getChatManager().message("boss_bar.main_title"), BarColor.BLUE, BarStyle.SOLID);
		}

		scoreboardManager = new ScoreboardManager(plugin, this);
	}

	public boolean isReady() {
		return ready;
	}

	public void setReady(boolean ready) {
		this.ready = ready;
	}

	@Override
	public void run() {
		if (players.isEmpty() && arenaState == ArenaState.WAITING_FOR_PLAYERS) {
			return;
		}

		int size = players.size(), waitingTime = getWaitingTime(), minPlayers = getMinimumPlayers();

		switch (arenaState) {
			case WAITING_FOR_PLAYERS:

				if (size < minPlayers) {
					if (plugin.getConfigPreferences().getOption(ConfigPreferences.Option.BOSS_BAR_ENABLED)) {
						gameBar.setTitle(plugin.getChatManager().message("boss_bar.waiting_for_players"));
					}

					if (getTimer() <= 0) {
						setTimer(45);
						broadcastMessage(plugin.getChatManager().prefixedFormattedMessage(this, "in_game.messages.lobby_messages.waiting_for_players", minPlayers));
					}
				} else {
					showPlayers();
					setTimer(waitingTime);
					setArenaState(ArenaState.STARTING);
					broadcastMessage(plugin.getChatManager().prefixedMessage("in_game.messages.lobby_messages.enough_players_to_start"));
					break;
				}

				setTimer(getTimer() - 1);
				break;
			case STARTING:
				if (plugin.getConfigPreferences().getOption(ConfigPreferences.Option.BOSS_BAR_ENABLED)) {
					gameBar.setProgress((double) getTimer() / waitingTime);
					gameBar.setTitle(plugin.getChatManager().message("boss-bar.starting-in", getTimer()));
				}


				if (size < minPlayers) {
					if (plugin.getConfigPreferences().getOption(ConfigPreferences.Option.BOSS_BAR_ENABLED)) {
						gameBar.setProgress(1D);
						gameBar.setTitle(plugin.getChatManager().message("boss-bar.waiting-for-players"));
					}

					setTimer(waitingTime);
					setArenaState(ArenaState.WAITING_FOR_PLAYERS);
					broadcastMessage(plugin.getChatManager().prefixedFormattedMessage(this, "in-game.messages.lobby-messages.waiting-for-players", minPlayers));

					for (Player player : players) {
						player.setExp(1F);
						player.setLevel(0);
					}

					break;
				}

				if (size >= getMaximumPlayers() && getTimer() >= getStartTime() && !forceStart) {
					setTimer(getStartTime());
					broadcastMessage(plugin.getChatManager().prefixedMessage("in-game.messages.lobby-messages.start-in", getTimer()));
				}

				if (getTimer() == 0 || forceStart) {
					setArenaState(ArenaState.IN_GAME);

					plugin.getServer().getPluginManager().callEvent(new TRGameStartEvent(this));

					if (plugin.getConfigPreferences().getOption(ConfigPreferences.Option.BOSS_BAR_ENABLED)) {
						gameBar.setProgress(1D);
						gameBar.setTitle(plugin.getChatManager().message("boss-bar.in-game-info"));
					}

					setTimer(5);
					teleportAllToStartLocation();

					for (Player player : players) {
						ArenaUtils.updateNameTagsVisibility(player);
						ArenaUtils.hidePlayersOutsideTheGame(player, this);

						User user = plugin.getUserManager().getUser(player);
						user.addStat(StatsStorage.StatisticType.GAMES_PLAYED, 1);
						user.setStat(StatsStorage.StatisticType.LOCAL_DOUBLE_JUMPS, plugin.getPermissionManager().getDoubleJumps(player));
						user.addGameItem("double-jump");

						player.getInventory().clear();
						player.setGameMode(GameMode.ADVENTURE);
						player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 0, false, false));
						player.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, Integer.MAX_VALUE, 0, false, false));
						player.sendMessage(plugin.getChatManager().prefixedMessage("in-game.messages.lobby-messages.game-started"));
						player.updateInventory();
					}

					if (forceStart) {
						forceStart = false;
					}
				}

				setTimer(getTimer() - 1);
				break;
			case IN_GAME:
				int playerSize = getPlayersLeft().size();

				if (playerSize < 2) {
					ArenaManager.stopGame(false, this);
					return;
				}

				for (Player player : getPlayersLeft()) {
					User user = plugin.getUserManager().getUser(player);

					if (getTimer() % 30 == 0) {
						user.addStat(StatsStorage.StatisticType.COINS, 15);
						user.addStat(StatsStorage.StatisticType.LOCAL_COINS, 15);

						player.sendMessage(plugin.getChatManager().prefixedMessage("in-game.messages.earned-coin"));
					}

					if (user.getCooldown("double_jump") > 0) {
						player.setAllowFlight(false);
					} else if (user.getStat(StatsStorage.StatisticType.LOCAL_DOUBLE_JUMPS) > 0) {
						player.setAllowFlight(true);
					}

					user.addStat(StatsStorage.StatisticType.LOCAL_SURVIVE, 1);
				}

				setTimer(getTimer() + 1);
				break;
			case ENDING:
				if (getTimer() != 0) {
					setTimer(getTimer() - 1);
					return;
				}

				scoreboardManager.stopAllScoreboards();

				if (plugin.getConfigPreferences().getOption(ConfigPreferences.Option.BOSS_BAR_ENABLED)) {
					gameBar.setTitle(plugin.getChatManager().message("boss-bar.game-ended"));
				}

				for (Player player : players) {
					ArenaUtils.showPlayersOutsideTheGame(player, this);

					player.setGameMode(GameMode.SURVIVAL);
					player.setFlySpeed(.1f);
					player.setWalkSpeed(.2f);
					player.setFlying(false);
					player.setAllowFlight(false);
					player.setCollidable(true);
					player.getInventory().clear();
					player.getInventory().setArmorContents(null);
					player.getActivePotionEffects().forEach(effect -> player.removePotionEffect(effect.getType()));

					teleportToEndLocation(player);
					doBarAction(BarAction.REMOVE, player);
				}

				plugin.getUserManager().getUsers(this).forEach(user -> user.setSpectator(false));
				plugin.getRewardsFactory().performReward(this, Reward.RewardType.END_GAME);

				if (plugin.getConfigPreferences().getOption(ConfigPreferences.Option.INVENTORY_MANAGER_ENABLED)) {
					players.forEach(player -> InventorySerializer.loadInventory(plugin, player));
				}

				setArenaState(ArenaState.RESTARTING);
				broadcastMessage(plugin.getChatManager().prefixedMessage("commands.teleported-to-the-lobby"));
				break;
			case RESTARTING:
				players.clear();

				if (!destroyedBlocks.isEmpty()) {
					Iterator<BlockState> iterator = destroyedBlocks.iterator();

					while (iterator.hasNext()) {
						iterator.next().update(true);
						iterator.remove();
					}
				}

				setArenaState(ArenaState.WAITING_FOR_PLAYERS);
				break;
		}
	}

	public boolean isForceStart() {
		return forceStart;
	}

	public void setForceStart(boolean forceStart) {
		this.forceStart = forceStart;
	}

	public ScoreboardManager getScoreboardManager() {
		return scoreboardManager;
	}

	public String getId() {
		return id;
	}

	public int getMinimumPlayers() {
		return getOption(ArenaOption.MINIMUM_PLAYERS);
	}

	public void setMinimumPlayers(int minimumPlayers) {
		setOptionValue(ArenaOption.MINIMUM_PLAYERS, Math.min(2, minimumPlayers));
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

	public int getStartTime() {
		return getOption(ArenaOption.START_TIME);
	}

	public int getWaitingTime() {
		return getOption(ArenaOption.WAITING_TIME);
	}

	public ArenaState getArenaState() {
		return arenaState;
	}

	public void setArenaState(ArenaState arenaState) {
		this.arenaState = arenaState;

		plugin.getServer().getPluginManager().callEvent(new TRGameStateChangeEvent(this, arenaState));
		plugin.getSignManager().updateSigns();
	}

	public Set<Player> getPlayers() {
		return new HashSet<>(players);
	}

	public void teleportToLobby(Player player) {
		player.setFoodLevel(20);
		player.setFlySpeed(.1F);
		player.setWalkSpeed(.2F);
		player.setFlying(false);
		player.setAllowFlight(false);
		player.getActivePotionEffects().forEach(effect -> player.removePotionEffect(effect.getType()));

		Location location = getLobbyLocation();

		if (location == null) {
			LogUtils.sendConsoleMessage("&cLobby location isn't initialized for arena " + getId());
			return;
		}

		player.teleport(location);
	}

	public void doBarAction(BarAction action, Player player) {
		if (gameBar == null) return;

		if (action == BarAction.ADD) {
			gameBar.addPlayer(player);
		} else {
			gameBar.removePlayer(player);
		}
	}

	public Location getLobbyLocation() {
		return gameLocations.get(GameLocation.LOBBY);
	}

	public void setLobbyLocation(Location loc) {
		gameLocations.put(GameLocation.LOBBY, loc);
	}

	public Set<BlockState> getDestroyedBlocks() {
		return destroyedBlocks;
	}

	public void teleportToStartLocation(Player player) {
		player.teleport(getLobbyLocation());
	}

	private void teleportAllToStartLocation() {
		players.forEach(this::teleportToStartLocation);
	}

	public void teleportToEndLocation(Player player) {
		player.teleport(getEndLocation() == null ? getLobbyLocation() : getEndLocation());
	}

	public Location getEndLocation() {
		return gameLocations.get(GameLocation.END);
	}

	public void setEndLocation(Location endLoc) {
		gameLocations.put(GameLocation.END, endLoc);
	}

	public void start() {
		LogUtils.log("[{0}] Game instance started.", id);

		startRemovingBlock();
		runTaskTimer(plugin, 20L, 20L);
		setArenaState(ArenaState.RESTARTING);
	}

	public void addPlayer(Player player) {
		players.add(player);
	}

	public void removePlayer(Player player) {
		players.remove(player);
	}

	public List<Player> getPlayersLeft() {
		List<Player> players = new ArrayList<>();

		for (User user : plugin.getUserManager().getUsers(this)) {
			if (!user.isSpectator()) {
				players.add(user.getPlayer());
			}
		}

		return players;
	}

	public void showPlayers() {
		for (Player player : players) {
			for (Player p : players) {
				player.showPlayer(plugin, p);
				p.showPlayer(plugin, player);
			}
		}
	}

	public void broadcastMessage(String message) {
		getPlayers().forEach(player -> player.sendMessage(message));
	}

	public void startRemovingBlock() {
		plugin.getServer().getScheduler().runTaskTimerAsynchronously(plugin, () -> {
			for (Player player : getPlayersLeft()) {
				if (arenaState != ArenaState.IN_GAME) {
					return;
				}

				if (getTimer() <= plugin.getConfig().getInt("Start-Block-Remove", 5)) {
					return;
				}

				for (Block block : getRemovableBlocks(player)) {
					if (!plugin.getConfig().getStringList("Whitelisted-Blocks").contains(block.getType().name())) {
						continue;
					}

					destroyedBlocks.add(block.getState());
					plugin.getServer().getScheduler().runTaskLater(plugin, () -> block.setType(Material.AIR), plugin.getConfig().getLong("Block-Remove-Delay", 12L));
				}
			}
		}, 0L, 1L);
	}

	private List<Block> getRemovableBlocks(Player player) {
		List<Block> removableBlocks = new ArrayList<>();
		Location playerLocation = player.getLocation();

		for (double ox = -0.2; ox <= 0.2; ox += 0.2) {
			for (double oz = -0.2; oz <= 0.2; oz += 0.2) {
				Block block = playerLocation.add(ox, 0, oz).getBlock().getRelative(BlockFace.DOWN);

				removableBlocks.add(block);
				removableBlocks.add(block.getRelative(BlockFace.DOWN));
				removableBlocks.add(block.getRelative(BlockFace.DOWN).getRelative(BlockFace.DOWN));
			}
		}

		return removableBlocks;
	}

	public int getOption(ArenaOption option) {
		return arenaOptions.get(option);
	}

	public void setOptionValue(ArenaOption option, int value) {
		arenaOptions.put(option, value);
	}

	public boolean isArenaState(ArenaState first, ArenaState second) {
		return arenaState == first || arenaState == second;
	}

	public enum BarAction {
		ADD, REMOVE
	}

	public enum GameLocation {
		LOBBY, END
	}
}