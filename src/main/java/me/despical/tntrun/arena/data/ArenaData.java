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

package me.despical.tntrun.arena.data;

import me.despical.commons.compat.XPotion;
import me.despical.commons.compat.XSound;
import me.despical.commons.miscellaneous.PlayerUtils;
import me.despical.tntrun.ConfigPreferences;
import me.despical.tntrun.Main;
import me.despical.tntrun.api.StatsStorage;
import me.despical.tntrun.arena.ArenaState;
import me.despical.tntrun.arena.managers.GameBarManager;
import me.despical.tntrun.arena.managers.ScoreboardManager;
import me.despical.tntrun.arena.options.ArenaOption;
import me.despical.tntrun.handlers.ChatManager;
import me.despical.tntrun.user.User;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
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
 * Created at 6.10.2024
 */
public abstract class ArenaData extends BukkitRunnable {

	protected static final Main plugin = JavaPlugin.getPlugin(Main.class);
	protected static final ChatManager chatManager = plugin.getChatManager();

	protected final Map<ArenaOption, Object> arenaOptions;

	protected final Set<BlockState> destroyedBlocks;
	protected final List<User> players, spectators, deaths, winners;

	protected GameBarManager gameBarManager;
	protected ScoreboardManager scoreboardManager;

	protected ArenaState arenaState = ArenaState.INACTIVE;

	public ArenaData(String id) {
		this.arenaOptions = new EnumMap<>(ArenaOption.class);
		this.arenaOptions.put(ArenaOption.ID, id);
		this.destroyedBlocks = new HashSet<>();
		this.players = new ArrayList<>();
		this.spectators = new ArrayList<>();
		this.deaths = new ArrayList<>();
		this.winners = new ArrayList<>();
		this.gameBarManager = new GameBarManager(this, plugin);
		this.scoreboardManager = new ScoreboardManager(this, plugin);

		for (final var option : ArenaOption.values()) {
			arenaOptions.put(option, option.getOption());
		}
	}

	public boolean isInArena(final User user) {
		return user != null && this.players.contains(user);
	}

	private void teleportToGameLocation(final User user, ArenaOption gameLocation) {
		if (!validateLocation(gameLocation)) return;

		final var player = user.getPlayer();

		user.removePotionEffectsExcept(XPotion.BLINDNESS);

		player.setFoodLevel(20);
		player.setFlying(false);
		player.setAllowFlight(false);
		player.setFlySpeed(.1F);
		player.setWalkSpeed(.2F);
		player.teleport(this.<Location>getOption(gameLocation));
	}

	public void teleportToLobby(final User user) {
		this.teleportToGameLocation(user, ArenaOption.LOBBY_LOCATION);
	}

	public void teleportToEndLocation(final User user) {
		if (plugin.getOption(ConfigPreferences.Option.BUNGEE_ENABLED)) {
			plugin.getBungeeManager().connectToHub(user);
			return;
		}

		this.teleportToGameLocation(user, ArenaOption.END_LOCATION);
	}

	public GameBarManager getGameBar() {
		return gameBarManager;
	}

	@NotNull
	public ScoreboardManager getScoreboardManager() {
		return scoreboardManager;
	}

	public List<User> getPlayers() {
		return this.players.stream().filter(user -> {
			Player player = user.getPlayer();

			return player != null && player.isOnline();
		}).collect(Collectors.toList());
	}

	public void addUser(final User user) {
		this.players.add(user);
	}

	public void removeUser(final User user) {
		this.players.remove(user);
	}

	public boolean isForceStart() {
		return this.getOption(ArenaOption.FORCE_START);
	}

	public void setForceStart(final boolean forceStart) {
		this.setOption(ArenaOption.FORCE_START, forceStart);
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
			player.addPotionEffect(XPotion.NIGHT_VISION.buildInvisible(Integer.MAX_VALUE, 1));
		}

		final var level = user.getStat(StatsStorage.StatisticType.SPECTATOR_SPEED) + 1;

		player.setFlySpeed(.1F + level * .05F);
		player.addPotionEffect(XPotion.SPEED.buildInvisible(Integer.MAX_VALUE, level));
	}

	public void removeSpectator(final User user) {
		this.spectators.remove(user);
	}

	public boolean isSpectator(final User user) {
		return spectators.contains(user);
	}

	public <T> T getOption(ArenaOption option) {
		return (T) this.arenaOptions.get(option);
	}

	public void setOption(ArenaOption option, Object value) {
		this.arenaOptions.put(option, value);
	}

	public ArenaState getArenaState() {
		return this.arenaState;
	}

	public abstract void setArenaState(ArenaState arenaState);

	public boolean isArenaState(ArenaState arenaState, ArenaState... states) {
		if (this.arenaState == arenaState) {
			return true;
		}

		for (ArenaState state : states) {
			if (this.arenaState == state) {
				return true;
			}
		}

		return false;
	}

	public boolean isReady() {
		return this.<Boolean>getOption(ArenaOption.READY);
	}

	public void setReady(boolean ready) {
		this.setOption(ArenaOption.READY, ready);
	}

	public int getSetupProgress() {
		return this.isReady() ? 100 : 0;
	}

	public String getMapName() {
		return this.getOption(ArenaOption.MAP_NAME);
	}

	public void setMapName(String mapName) {
		this.setOption(ArenaOption.MAP_NAME, mapName);
	}

	public int getTimer() {
		return this.<Integer>getOption(ArenaOption.TIMER);
	}

	public void setTimer(int timer) {
		this.setOption(ArenaOption.TIMER, timer);
	}

	public int getMaximumPlayers() {
		return this.<Integer>getOption(ArenaOption.MAXIMUM_PLAYERS);
	}

	public void setMaximumPlayers(int maximumPlayers) {
		this.setOption(ArenaOption.MAXIMUM_PLAYERS, maximumPlayers);
	}

	public int getMinimumPlayers() {
		return this.<Integer>getOption(ArenaOption.MINIMUM_PLAYERS);
	}

	public void setMinimumPlayers(int minimumPlayers) {
		this.setOption(ArenaOption.MINIMUM_PLAYERS, minimumPlayers);
	}

	public Location getLobbyLocation() {
		return this.getOption(ArenaOption.LOBBY_LOCATION);
	}

	public void setLobbyLocation(Location location) {
		this.setOption(ArenaOption.LOBBY_LOCATION, location);
	}

	public Location getEndLocation() {
		return this.getOption(ArenaOption.END_LOCATION);
	}

	public void setEndLocation(Location location) {
		this.setOption(ArenaOption.END_LOCATION, location);
	}

	private boolean isStopped() {
		return this.getOption(ArenaOption.STOPPED);
	}

	protected void startBlockRemoving() {
		final int startBlockRemoving = ArenaOption.START_BLOCK_REMOVING.getOption();
		final int blockRemoveDelay = ArenaOption.BLOCK_REMOVE_DELAY.getOption();
		final var removableBlocks = plugin.getConfig().getStringList("Whitelisted-Blocks");

		new BukkitRunnable() {

			@Override
			public void run() {
				for (final var user : getPlayersLeft()) {
					if (isStopped()) cancel();
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
		return this.getPlayers().stream().filter(user -> !user.isSpectator()).collect(Collectors.toSet());
	}

	public void playSound(XSound sound) {
		this.getPlayers().forEach(user -> sound.play(user.getPlayer()));
	}

	public abstract void broadcastFormattedMessage(final String path, final User user, boolean onlySpectators);

	public abstract void broadcastFormattedMessage(final String path, final User user);

	public abstract void broadcastMessage(final String path, Object... params);

	@Nullable
	public User getWinner() {
		for (final var user : this.getPlayersLeft()) return user;
		return null;
	}

	public String getId() {
		return this.getOption(ArenaOption.ID);
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

		setOption(ArenaOption.STOPPED, false);
		setOption(ArenaOption.FORCE_START, false);
	}

	// TODO - Move visibility changer methods to another class, eg. VisibilityManager.
	public void showPlayers() {
		final var players = this.getPlayers();

		for (final var user : players) {
			var player = user.getPlayer();

			user.removePotionEffectsExcept(XPotion.BLINDNESS);

			for (final User other : players) {
				final var otherPlayer = other.getPlayer();

				PlayerUtils.showPlayer(player, otherPlayer, plugin);
				PlayerUtils.showPlayer(otherPlayer, player, plugin);
			}
		}
	}

	public void showUserToArena(final User user) {
		final var player = user.getPlayer();

		for (final var otherUser : this.getPlayers()) {
			final var otherPlayer = otherUser.getPlayer();

			PlayerUtils.showPlayer(player, otherPlayer, plugin);
			PlayerUtils.showPlayer(otherPlayer, player, plugin);
		}
	}

	public void hideSpectator(final User user) {
		if (!user.isSpectator()) return;

		final var player = user.getPlayer();

		for (final var otherUser : this.getPlayers()) {
			final var otherPlayer = otherUser.getPlayer();

			PlayerUtils.showPlayer(player, otherPlayer, plugin);

			if (otherUser.isSpectator()) {
				PlayerUtils.showPlayer(otherPlayer, player, plugin);
			} else {
				PlayerUtils.hidePlayer(otherPlayer, player, plugin);
			}
		}
	}

	public void hideUserOutsideTheGame(final User user) {
		final var player = user.getPlayer();

		for (final var otherUser : plugin.getUserManager().getUsers()) {
			final var otherPlayer = otherUser.getPlayer();

			if (isInArena(otherUser)) {
				this.showUserToArena(otherUser);
			} else {
				PlayerUtils.hidePlayer(player, otherPlayer, plugin);
				PlayerUtils.hidePlayer(otherPlayer, player, plugin);
			}
		}
	}

	public void showUserOutsideTheGame(final User user) {
		final var player = user.getPlayer();

		for (final var otherUser : plugin.getUserManager().getUsers()) {
			final var otherPlayer = otherUser.getPlayer();

			if (!this.isInArena(otherUser)) {
				PlayerUtils.showPlayer(player, otherPlayer, plugin);
				PlayerUtils.showPlayer(otherPlayer, player, plugin);
			} else {
				PlayerUtils.hidePlayer(player, otherPlayer, plugin);
				PlayerUtils.hidePlayer(otherPlayer, player, plugin);
			}
		}
	}

	public abstract void updateSigns();

	private boolean validateLocation(final ArenaOption gameLocation) {
		final var location = this.<Location>getOption(gameLocation);

		if (location == null) {
			plugin.getLogger().log(Level.WARNING, "Lobby location isn't initialized for arena {0}!", this.getId());
			return false;
		}

		return true;
	}

	public void broadcastWaitingForPlayers() {
		int neededPlayers = this.getMinimumPlayers() - players.size();

		this.broadcastMessage("messages.arena.waiting-for-players", neededPlayers, neededPlayers > 1 ? "s are" : " is");
	}

	private record Position(double x, int y, double z) {

		public Block getBlock(World world, double offsetX, double offsetZ) {
			return world.getBlockAt(NumberConversions.floor(x + offsetX), y, NumberConversions.floor(z + offsetZ));
		}
	}
}
