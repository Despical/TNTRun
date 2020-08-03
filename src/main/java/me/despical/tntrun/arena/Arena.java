package me.despical.tntrun.arena;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;

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

import me.despical.commonsbox.configuration.ConfigUtils;
import me.despical.commonsbox.serializer.InventorySerializer;
import me.despical.tntrun.ConfigPreferences;
import me.despical.tntrun.Main;
import me.despical.tntrun.api.StatsStorage;
import me.despical.tntrun.api.events.game.TRGameStartEvent;
import me.despical.tntrun.api.events.game.TRGameStateChangeEvent;
import me.despical.tntrun.arena.managers.ScoreboardManager;
import me.despical.tntrun.arena.options.ArenaOption;
import me.despical.tntrun.handlers.items.SpecialItemManager;
import me.despical.tntrun.handlers.rewards.Reward;
import me.despical.tntrun.user.User;
import me.despical.tntrun.utils.Debugger;

/**
 * @author Despical
 * <p>
 * Created at 10.07.2020
 */
public class Arena extends BukkitRunnable {

	private static final Main plugin = JavaPlugin.getPlugin(Main.class);
	private final String id;

	private final Set<Player> players = new HashSet<>();

	private final LinkedList<BlockState> destroyedBlocks = new LinkedList<>();
	
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
		if (plugin.getConfigPreferences().getOption(ConfigPreferences.Option.BOSSBAR_ENABLED)) {
			gameBar = Bukkit.createBossBar(plugin.getChatManager().colorMessage("Bossbar.Main-Title"), BarColor.BLUE, BarStyle.SOLID);
		}
		scoreboardManager = new ScoreboardManager(this);
	}

	public boolean isReady() {
		return ready;
	}

	public void setReady(boolean ready) {
		this.ready = ready;
	}

	@Override
	public void run() {
		if (getPlayers().isEmpty() && getArenaState() == ArenaState.WAITING_FOR_PLAYERS) {
			return;
		}
		Debugger.performance("ArenaTask", "[PerformanceMonitor] [{0}] Running game task", getId());
		long start = System.currentTimeMillis();

		switch (getArenaState()) {
		case WAITING_FOR_PLAYERS:
			if (plugin.getConfigPreferences().getOption(ConfigPreferences.Option.BUNGEE_ENABLED)) {
				plugin.getServer().setWhitelist(false);
			}
			if (getPlayers().size() < getMinimumPlayers()) {
				if (getTimer() <= 0) {
					setTimer(15);
					plugin.getChatManager().broadcast(this, plugin.getChatManager().formatMessage(this, plugin.getChatManager().colorMessage("In-Game.Messages.Lobby-Messages.Waiting-For-Players"), getMinimumPlayers()));
					break;
				}
			} else {
				if (plugin.getConfigPreferences().getOption(ConfigPreferences.Option.BOSSBAR_ENABLED)) {
					gameBar.setTitle(plugin.getChatManager().colorMessage("Bossbar.Waiting-For-Players"));
				}
				plugin.getChatManager().broadcast(this, plugin.getChatManager().colorMessage("In-Game.Messages.Lobby-Messages.Enough-Players-To-Start"));
				setArenaState(ArenaState.STARTING);
				setTimer(plugin.getConfig().getInt("Starting-Waiting-Time", 60));
				this.showPlayers();
			}
			setTimer(getTimer() - 1);
			break;
		case STARTING:
			if (getPlayers().size() == getMaximumPlayers() && getTimer() >= plugin.getConfig().getInt("Start-Time-On-Full-Lobby", 15) && !forceStart) {
				setTimer(plugin.getConfig().getInt("Start-Time-On-Full-Lobby", 15));
				plugin.getChatManager().broadcast(this, plugin.getChatManager().colorMessage("In-Game.Messages.Lobby-Messages.Start-In").replace("%time%", String.valueOf(getTimer())));
			}
			if (plugin.getConfigPreferences().getOption(ConfigPreferences.Option.BOSSBAR_ENABLED)) {
				gameBar.setTitle(plugin.getChatManager().colorMessage("Bossbar.Starting-In").replace("%time%", String.valueOf(getTimer())));
				gameBar.setProgress(getTimer() / plugin.getConfig().getDouble("Starting-Waiting-Time", 60));
			}
			for (Player player : getPlayers()) {
				player.setExp((float) (getTimer() / plugin.getConfig().getDouble("Starting-Waiting-Time", 60)));
				player.setLevel(getTimer());
			}
			if (getPlayers().size() < getMinimumPlayers() && !forceStart) {
				if (plugin.getConfigPreferences().getOption(ConfigPreferences.Option.BOSSBAR_ENABLED)) {
					gameBar.setTitle(plugin.getChatManager().colorMessage("Bossbar.Waiting-For-Players"));
					gameBar.setProgress(1.0);
				}
				plugin.getChatManager().broadcast(this, plugin.getChatManager().formatMessage(this, plugin.getChatManager().colorMessage("In-Game.Messages.Lobby-Messages.Waiting-For-Players"), getMinimumPlayers()));
				setArenaState(ArenaState.WAITING_FOR_PLAYERS);
				Bukkit.getPluginManager().callEvent(new TRGameStartEvent(this));
				setTimer(15);
				for (Player player : getPlayers()) {
					player.setExp(1);
					player.setLevel(0);
				}
				if (forceStart) {
					forceStart = false;
				}
				break;
			}
			if (getTimer() == 0 || forceStart) {
				TRGameStartEvent gameStartEvent = new TRGameStartEvent(this);
				Bukkit.getPluginManager().callEvent(gameStartEvent);
				setArenaState(ArenaState.IN_GAME);
				if (plugin.getConfigPreferences().getOption(ConfigPreferences.Option.BOSSBAR_ENABLED)) {
					gameBar.setProgress(1.0);
				}
				setTimer(5);
			 	if (players.isEmpty()) {
					break;
				}
				teleportAllToStartLocation();
				for (Player player : getPlayers()) {
					ArenaUtils.updateNameTagsVisibility(player);
					player.getInventory().clear();
					player.setGameMode(GameMode.ADVENTURE);
					ArenaUtils.hidePlayersOutsideTheGame(player, this);
					plugin.getUserManager().getUser(player).addStat(StatsStorage.StatisticType.GAMES_PLAYED, 1);
					plugin.getUserManager().getUser(player).setStat(StatsStorage.StatisticType.LOCAL_DOUBLE_JUMPS, 5);
					setTimer(2);
					player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 0, false, false), false);
					player.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, Integer.MAX_VALUE, 0, false, false), false);
					player.sendMessage(plugin.getChatManager().getPrefix() + plugin.getChatManager().colorMessage("In-Game.Messages.Lobby-Messages.Game-Started"));
					player.getInventory().setItem(SpecialItemManager.getSpecialItem("Double-Jump").getSlot(), SpecialItemManager.getSpecialItem("Double-Jump").getItemStack());
					player.updateInventory();
					player.setAllowFlight(true);
				}
			}
			if (plugin.getConfigPreferences().getOption(ConfigPreferences.Option.BOSSBAR_ENABLED)) {
				gameBar.setTitle(plugin.getChatManager().colorMessage("Bossbar.In-Game-Info"));
			}
			if (forceStart) {
				forceStart = false;
			}
			setTimer(getTimer() - 1);
			break;	
		case IN_GAME:
			if (plugin.getConfigPreferences().getOption(ConfigPreferences.Option.BUNGEE_ENABLED)) {
				if (getMaximumPlayers() <= getPlayers().size()) {
					plugin.getServer().setWhitelist(true);
				} else {
					plugin.getServer().setWhitelist(false);
				}
			}
			if (getPlayersLeft().size() < 2) {
				ArenaManager.stopGame(false, this);
				return;
			}
			for (Player player : getPlayersLeft()) {
				if (getTimer() % 30 == 0) {
					plugin.getUserManager().getUser(player).addStat(StatsStorage.StatisticType.LOCAL_COINS, 15);
					plugin.getUserManager().getUser(player).addStat(StatsStorage.StatisticType.COINS, 15);
					player.sendMessage(plugin.getChatManager().getPrefix() + plugin.getChatManager().colorMessage("In-Game.Messages.Earned-Coin"));
				}
			}
			for (Player player : getPlayersLeft()) {
				plugin.getUserManager().getUser(player).addStat(StatsStorage.StatisticType.LOCAL_SURVIVE, 1);
			}
			setTimer(getTimer() + 1);
			break;
		case ENDING:
			scoreboardManager.stopAllScoreboards();
			if (plugin.getConfigPreferences().getOption(ConfigPreferences.Option.BUNGEE_ENABLED)) {
				plugin.getServer().setWhitelist(false);
			}
			if (plugin.getConfigPreferences().getOption(ConfigPreferences.Option.BOSSBAR_ENABLED)) {
				gameBar.setTitle(plugin.getChatManager().colorMessage("Bossbar.Game-Ended"));
			}
			List<Player> playersToQuit = new ArrayList<>(getPlayers());
			for (Player player : playersToQuit) {
				plugin.getUserManager().getUser(player).removeScoreboard();
				player.setGameMode(GameMode.SURVIVAL);
				for (Player players : Bukkit.getOnlinePlayers()) {
					player.showPlayer(plugin, players);
					if (ArenaRegistry.getArena(players) == null) {
						players.showPlayer(plugin, player);
					}
				}
				for (PotionEffect effect : player.getActivePotionEffects()) {
					player.removePotionEffect(effect.getType());
				}
				player.setWalkSpeed(0.2f);
				player.setFlying(false);
				player.setAllowFlight(false);
				player.getInventory().clear();
				player.getInventory().setArmorContents(null);
				doBarAction(BarAction.REMOVE, player);
				player.setFireTicks(0);
				player.setFoodLevel(20);

			}
			teleportAllToEndLocation();
			if (plugin.getConfigPreferences().getOption(ConfigPreferences.Option.INVENTORY_MANAGER_ENABLED)) {
				for (Player player : getPlayers()) {
					InventorySerializer.loadInventory(plugin, player);
				}
			}
			plugin.getChatManager().broadcast(this, plugin.getChatManager().colorMessage("Commands.Teleported-To-The-Lobby"));
			for (User user : plugin.getUserManager().getUsers(this)) {
				user.setSpectator(false);
				user.getPlayer().setCollidable(true);
			}
			plugin.getRewardsFactory().performReward(this, Reward.RewardType.END_GAME);
			if (plugin.getConfigPreferences().getOption(ConfigPreferences.Option.BUNGEE_ENABLED)) {
				if (ConfigUtils.getConfig(plugin, "bungee").getBoolean("Shutdown-When-Game-Ends")) {
					plugin.getServer().shutdown();
				}
			}
			setArenaState(ArenaState.RESTARTING);
			break;
		case RESTARTING:
			getPlayers().clear();
			if (destroyedBlocks.size() > 0) {
			Iterator<BlockState> iterator = destroyedBlocks.iterator();
				while (iterator.hasNext()) {
					BlockState bs = iterator.next();
					bs.update(true);
					iterator.remove();
				}
			}
			setArenaState(ArenaState.WAITING_FOR_PLAYERS);
			if (plugin.getConfigPreferences().getOption(ConfigPreferences.Option.BUNGEE_ENABLED)) {
				ArenaRegistry.shuffleBungeeArena();
				for (Player player : Bukkit.getOnlinePlayers()) {
					ArenaManager.joinAttempt(player, ArenaRegistry.getArenas().get(ArenaRegistry.getBungeeArena()));
				}
			}
			if (plugin.getConfigPreferences().getOption(ConfigPreferences.Option.BOSSBAR_ENABLED)) {
				gameBar.setTitle(plugin.getChatManager().colorMessage("Bossbar.Waiting-For-Players"));
			}
			break;
		default:
			break;
		}
		Debugger.performance("ArenaTask", "[PerformanceMonitor] [{0}] Game task finished took {1} ms", getId(), System.currentTimeMillis() - start);
	}

	public void setForceStart(boolean forceStart) {
		this.forceStart = forceStart;
	}

	public ScoreboardManager getScoreboardManager() {
		return scoreboardManager;
	}

	/**
	 * Get arena identifier used to get arenas by string.
	 *
	 * @return arena name
	 * @see ArenaRegistry#getArena(String)
	 */
	public String getId() {
		return id;
	}

	/**
	 * Get minimum players needed.
	 *
	 * @return minimum players needed to start arena
	 */
	public int getMinimumPlayers() {
		return getOption(ArenaOption.MINIMUM_PLAYERS);
	}

	/**
	 * Set minimum players needed.
	 *
	 * @param minimumPlayers players needed to start arena
	 */
	public void setMinimumPlayers(int minimumPlayers) {
		if (minimumPlayers < 2) {
			Debugger.debug(Level.WARNING, "Minimum players amount for arena cannot be less than 2! Got {0}", minimumPlayers);
			setOptionValue(ArenaOption.MINIMUM_PLAYERS, 2);
			return;
		}
		setOptionValue(ArenaOption.MINIMUM_PLAYERS, minimumPlayers);
	}

	/**
	 * Get arena map name.
	 *
	 * @return arena map name, it's not arena id
	 * @see #getId()
	 */
	public String getMapName() {
		return mapName;
	}

	/**
	 * Set arena map name.
	 *
	 * @param mapname new map name, it's not arena id
	 */
	public void setMapName(String mapname) {
		this.mapName = mapname;
	}

	/**
	 * Get timer of arena.
	 *
	 * @return timer of lobby time / time to next wave
	 */
	public int getTimer() {
		return getOption(ArenaOption.TIMER);
	}

	/**
	 * Modify game timer.
	 *
	 * @param timer timer of lobby / time to next wave
	 */
	public void setTimer(int timer) {
		setOptionValue(ArenaOption.TIMER, timer);
	}

	/**
	 * Return maximum players arena can handle.
	 *
	 * @return maximum players arena can handle
	 */
	public int getMaximumPlayers() {
		return getOption(ArenaOption.MAXIMUM_PLAYERS);
	}

	/**
	 * Set maximum players arena can handle.
	 *
	 * @param maximumPlayers how many players arena can handle
	 */
	public void setMaximumPlayers(int maximumPlayers) {
		setOptionValue(ArenaOption.MAXIMUM_PLAYERS, maximumPlayers);
	}

	/**
	 * Return game state of arena.
	 *
	 * @return game state of arena
	 * @see ArenaState
	 */
	public ArenaState getArenaState() {
		return arenaState;
	}

	/**
	 * Set game state of arena.
	 *
	 * @param arenaState new game state of arena
	 * @see ArenaState
	 */
	public void setArenaState(ArenaState arenaState) {
		this.arenaState = arenaState;
		TRGameStateChangeEvent gameStateChangeEvent = new TRGameStateChangeEvent(this, getArenaState());
		Bukkit.getPluginManager().callEvent(gameStateChangeEvent);
	}

	/**
	 * Get all players in arena.
	 *
	 * @return set of players in arena
	 */
	public Set<Player> getPlayers() {
		return players;
	}

	public void teleportToLobby(Player player) {
		player.setFoodLevel(20);
		player.setFlying(false);
		player.setAllowFlight(false);
		for (PotionEffect effect : player.getActivePotionEffects()) {
			player.removePotionEffect(effect.getType());
		}
		player.setWalkSpeed(0.2f);
		Location location = getLobbyLocation();
		if (location == null) {
			System.out.print("Lobby location isn't intialized for arena " + getId());
		}
		player.teleport(location);
	}

	/**
	 * Executes boss bar action for arena
	 *
	 * @param action add or remove a player from boss bar
	 * @param p      player
	 */
	public void doBarAction(BarAction action, Player p) {
		if (!plugin.getConfigPreferences().getOption(ConfigPreferences.Option.BOSSBAR_ENABLED)) {
			return;
		}
		switch (action) {
		case ADD:
			gameBar.addPlayer(p);
			break;
		case REMOVE:
			gameBar.removePlayer(p);
			break;
		default:
			break;
		}
	}

	/**
	 * Get lobby location of arena.
	 *
	 * @return lobby location of arena
	 */
	public Location getLobbyLocation() {
		return gameLocations.get(GameLocation.LOBBY);
	}

	/**
	 * Set lobby location of arena.
	 *
	 * @param loc new lobby location of arena
	 */
	public void setLobbyLocation(Location loc) {
		gameLocations.put(GameLocation.LOBBY, loc);
	}
	
	/**
	 * Get destroyed blocks of arena.
	 * 
	 * @return destroyed blocks
	 */
	public LinkedList<BlockState> getDestroyedBlocks(){
		return destroyedBlocks;
	}
	
	public void teleportToStartLocation(Player player) {
		player.teleport(getLobbyLocation());
	}
	
	private void teleportAllToStartLocation() {
		for (Player player : players) {
			teleportToStartLocation(player);
		}
	}

	public void teleportAllToEndLocation() {
		if (plugin.getConfigPreferences().getOption(ConfigPreferences.Option.BUNGEE_ENABLED) && ConfigUtils.getConfig(plugin, "bungee").getBoolean("End-Location-Hub", true)) {
			for (Player player : getPlayers()) {
				plugin.getBungeeManager().connectToHub(player);
			}
			return;
		}
		Location location = getEndLocation();
		if (location == null) {
			location = getLobbyLocation();
			System.out.print("EndLocation for arena " + getId() + " isn't intialized!");
		}
		for (Player player : getPlayers()) {
			player.teleport(location);
		}
	}

	public void teleportToEndLocation(Player player) {
		if (plugin.getConfigPreferences().getOption(ConfigPreferences.Option.BUNGEE_ENABLED) && ConfigUtils.getConfig(plugin, "bungee").getBoolean("End-Location-Hub", true)) {
			plugin.getBungeeManager().connectToHub(player);
			return;
		}
		Location location = getEndLocation();
		if (location == null) {
			location = getLobbyLocation();
			System.out.print("EndLocation for arena " + getId() + " isn't intialized!");
		}
		player.teleport(location);
	}

	/**
	 * Get end location of arena.
	 *
	 * @return end location of arena
	 */
	public Location getEndLocation() {
		return gameLocations.get(GameLocation.END);
	}

	/**
	 * Set end location of arena.
	 *
	 * @param endLoc new end location of arena
	 */
	public void setEndLocation(Location endLoc) {
		gameLocations.put(GameLocation.END, endLoc);
	}

	public void start() {
		Debugger.debug(Level.INFO, "[{0}] Game instance started", getId());
		this.startRemovingBlock();
		this.runTaskTimer(plugin, 20L, 20L);
		this.setArenaState(ArenaState.RESTARTING);
	}

	void addPlayer(Player player) {
		players.add(player);
	}

	void removePlayer(Player player) {
		if (player == null) {
			return;
		}
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

	void showPlayers() {
		for (Player player : getPlayers()) {
			for (Player p : getPlayers()) {
				player.showPlayer(plugin, p);
				p.showPlayer(plugin, player);
			}
		}
	}
	
	public void startRemovingBlock() {
		Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, () -> {
			for (Player player : getPlayers()) {
				if (arenaState != ArenaState.IN_GAME) {
					return;
				}
				if (plugin.getUserManager().getUser(player).isSpectator()) {
					return;
				}
				if (getTimer() <= plugin.getConfig().getInt("Start-Block-Remove", 5)) {
					return;
				}
				Location location = player.getLocation().clone();
				for (int i = 0; i <= 4; i++) {
					Block block = location.add(0, -i, 0).getBlock().getRelative(BlockFace.DOWN);
					if (plugin.getConfig().getStringList("Whitelisted-Blocks").contains(block.getType().name())) {
						continue;
					}
					destroyedBlocks.add(block.getState());
					Bukkit.getScheduler().runTaskLater(plugin, () -> block.setType(Material.AIR), plugin.getConfig().getLong("Block-Remove-Delay", 8L));
				}
			}
		}, 0L, 1L);
	}

	public int getOption(ArenaOption option) {
		return arenaOptions.get(option);
	}

	public void setOptionValue(ArenaOption option, int value) {
		arenaOptions.put(option, value);
	}

	public void addOptionValue(ArenaOption option, int value) {
		arenaOptions.put(option, arenaOptions.get(option) + value);
	}

	public enum BarAction {
		ADD, REMOVE
	}

	public enum GameLocation {
		LOBBY, END
	}
}