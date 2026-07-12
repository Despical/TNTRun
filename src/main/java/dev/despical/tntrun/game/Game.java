/*
 * TNT Run - A fast-paced parkour minigame for Minecraft.
 * Copyright (C) 2026  Berke Akçen
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

package dev.despical.tntrun.game;

import dev.despical.commons.serializer.InventorySerializer;
import dev.despical.tntrun.TNTRun;
import dev.despical.tntrun.api.event.game.GameStateChangeEvent;
import dev.despical.tntrun.arena.Arena;
import dev.despical.tntrun.arena.options.ArenaKeys;
import dev.despical.tntrun.blocks.BlockRemovalManager;
import dev.despical.tntrun.bossbar.BossBarManager;
import dev.despical.tntrun.game.messages.MessageTicker;
import dev.despical.tntrun.game.messages.PlacementMessenger;
import dev.despical.tntrun.game.scores.ScoreRegistry;
import dev.despical.tntrun.game.spectator.SpectatorManager;
import dev.despical.tntrun.game.states.*;
import dev.despical.tntrun.game.visibility.VisibilityManager;
import dev.despical.tntrun.option.IntOption;
import dev.despical.tntrun.scoreboard.ScoreboardManager;
import dev.despical.tntrun.stats.Statistics;
import dev.despical.tntrun.user.User;
import dev.despical.tntrun.utils.ItemUtils;
import dev.despical.tntrun.utils.Var;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;
import java.util.function.Consumer;

/**
 * @author Despical
 * <p>
 * Created at 17.06.2026
 */
public class Game extends BukkitRunnable {

    private static final TNTRun plugin = TNTRun.getInstance();

    @Getter
    private int timer;
    private int tick;
    private int peakActivePlayers;
    private long survivalStartTimeMillis;

    private boolean tickImmediately;

    private GameState gameState = GameState.INACTIVE;
    private GameStateHandler stateHandler;

    private final int period;

    private final @Getter Arena arena;
    private final @Getter VisibilityManager visibilityManager;
    private final @Getter ScoreboardManager scoreboardManager;
    private final @Getter ScoreRegistry scores;
    private final @Getter SpectatorManager spectatorManager;
    private final @Getter MessageTicker messageTicker;
    private final @Getter PlacementMessenger placementMessenger;
    private final @Getter BossBarManager bossBarManager;
    private final @Getter BlockRemovalManager blockRemovalManager;

    private final @Getter List<User> users;
    private final Map<UUID, Long> survivalTimesMillis;
    private final Map<UUID, PlayerMetadata> playerMetadata;
    private final Map<GameState, GameStateHandler> states;

    public Game(Arena arena, int tickPeriod) {
        this.arena = arena;
        this.period = tickPeriod;
        this.visibilityManager = new VisibilityManager(this);
        this.scoreboardManager = new ScoreboardManager(this);
        this.spectatorManager = new SpectatorManager(this);
        this.messageTicker = plugin.getGameManager().getMessageTicker();
        this.placementMessenger = new PlacementMessenger(this);
        this.bossBarManager = new BossBarManager(this);
        this.blockRemovalManager = new BlockRemovalManager(this);
        this.users = new ArrayList<>();
        this.survivalTimesMillis = new HashMap<>();
        this.playerMetadata = new HashMap<>();
        this.scores = new ScoreRegistry(this);
        this.states = Map.of(
            GameState.WAITING, new WaitingState(this),
            GameState.STARTING, new StartingState(this),
            GameState.IN_GAME, new InGameState(this),
            GameState.ENDING, new EndingState(this),
            GameState.RESTARTING, new RestartingState(this)
        );
    }

    @Override
    public void run() {
        boolean shouldRun = (tick += period) >= 20 || shouldTickImmediately();

        if (shouldRun) {
            tick = 0;

            stateHandler.tick();
            messageTicker.tick(this);
        }
    }

    private boolean shouldTickImmediately() {
        if (tickImmediately) {
            tickImmediately = false;
            return true;
        }

        return false;
    }

    public void tickImmediately() {
        tickImmediately = true;
    }

    public void resetTickProgress() {
        tick = 0;
    }

    public void broadcastMessage(String msgPath) {
        users.forEach(user -> user.sendMessage(msgPath));
    }

    public void broadcastMessage(String msgPath, Var... variables) {
        users.forEach(user -> user.sendMessage(msgPath, variables));
    }

    public void broadcastRawMessage(String message, Var... variables) {
        users.forEach(user -> plugin.getChatManager().sendRawMessage(user.getPlayer(), message, variables));
    }

    public void broadcastRawComponent(Component message, Var... variables) {
        users.forEach(user -> plugin.getChatManager().sendRawComponent(user.getPlayer(), message, variables));
    }

    public final void setTimer(int timer) {
        this.timer = timer;

        scoreboardManager.updateAllScoreboards();
    }

    public final void setTimer(IntOption option) {
        setTimer(option.value());
    }

    public void joinAsPlayer(User user) {
        join(user, users);
    }

    public void joinAsSpectator(User user) {
        if (!isState(GameState.IN_GAME)) {
            user.sendMessage("cannot-join-now");
            return;
        }

        user.resetTemporaryStats();
        user.setSpectator(true);

        users.add(user);

        states.get(gameState).join(user);

        scoreboardManager.updateAllScoreboards();

        plugin.getSignManager().updateSigns(arena);
    }

    private void join(User user, List<User> list) {
        if (!isState(GameState.WAITING, GameState.STARTING)) {
            user.sendMessage("cannot-join-now");
            return;
        }

        user.resetTemporaryStats();
        user.setSpectator(false);

        list.add(user);
        states.get(gameState).join(user);

        scoreboardManager.updateAllScoreboards();

        plugin.getSignManager().updateSigns(arena);
    }

    public void leaveUser(User user) {
        this.leaveUser(user, player -> InventorySerializer.loadInventory(plugin, player));
    }

    public void quitUser(User user) {
        this.leaveUser(user, _ -> {});
    }

    private void leaveUser(User user, Consumer<Player> playerConsumer) {
        users.remove(user);

        states.get(gameState).leave(user);
        user.setSpectator(false);

        plugin.getSignManager().updateSigns(arena);
        plugin.getDatabase().saveData(user);

        Player player = user.getPlayer();
        if (player != null && player.isOnline()) {
            scoreboardManager.removeScoreboard(player);
            bossBarManager.removePlayer(player);

            player.teleport(arena.getOption(ArenaKeys.END_LOCATION));
            player.clearTitle();
            player.sendActionBar(Component.empty());
            player.closeInventory();
            player.setInvulnerable(false);
            player.setAllowFlight(false);
            player.setFlying(false);
            player.getActivePotionEffects().forEach(effect -> player.removePotionEffect(effect.getType()));
            player.setItemOnCursor(null);

            PlayerInventory inventory = player.getInventory();
            inventory.clear();
            inventory.setArmorContents(ItemUtils.EMPTY_ARMORS);

            playerConsumer.accept(player);

            visibilityManager.hidePlayerFromGame(player);
            visibilityManager.showPlayerOutsideTheGame(player);
        }

        scoreboardManager.updateAllScoreboards();
    }

    public boolean isPlaying(User user) {
        return users.contains(user);
    }

    public boolean isPlaying(Player player) {
        UUID uuid = player.getUniqueId();
        return users.stream().anyMatch(user -> user.getUUID().equals(uuid));
    }

    public void setGameState(GameState newState) {
        if (this.gameState == newState) return;

        GameStateChangeEvent event = plugin.getEventManager().gameStateChange(this, this.gameState, newState);
        if (event.isCancelled()) return;

        this.gameState = newState;
        this.bossBarManager.update();

        Optional.ofNullable(plugin.getSignManager())
            .ifPresent(manager -> manager.updateSigns(arena));

        stateHandler = states.get(newState);
        stateHandler.firstTick();
    }

    public GameState getState() {
        return gameState;
    }

    public PlayerMetadata getPlayerMetadata(UUID uuid) {
        return playerMetadata.get(uuid);
    }

    public void updatePlayerMetadata(User user) {
        int doubleJumps = user.getStatistic(Statistics.LOCAL_DOUBLE_JUMPS);
        int maxDoubleJumps = user.getStatistic(Statistics.LOCAL_MAX_DOUBLE_JUMPS);

        playerMetadata.put(user.getUUID(), new PlayerMetadata(user.getName(), doubleJumps, maxDoubleJumps));
    }

    public void clearPlayerMetadata() {
        playerMetadata.clear();
    }

    public List<Player> getPlayers() {
        return users.stream().map(User::getPlayer).filter(Objects::nonNull).toList();
    }

    public Location getStartLocation() {
        return arena.getOption(ArenaKeys.START_LOCATION).clone();
    }

    public Set<User> getPlayersLeft() {
        return users.stream().filter(user -> !user.isSpectator()).collect(java.util.stream.Collectors.toSet());
    }

    public void startSurvivalRound() {
        peakActivePlayers = getPlayersLeft().size();
        survivalStartTimeMillis = System.currentTimeMillis();
        survivalTimesMillis.clear();
    }

    public void recordSurvivalTime(User user) {
        survivalTimesMillis.putIfAbsent(user.getUUID(), getCurrentSurvivalTimeMillis());
    }

    public void captureSurvivalTimes() {
        users.forEach(this::recordSurvivalTime);
    }

    public long getSurvivalTimeMillis(User user) {
        return survivalTimesMillis.getOrDefault(user.getUUID(), getCurrentSurvivalTimeMillis());
    }

    private long getCurrentSurvivalTimeMillis() {
        if (survivalStartTimeMillis == 0L) {
            return 0L;
        }

        return Math.max(0L, System.currentTimeMillis() - survivalStartTimeMillis);
    }

    public void eliminate(User user) {
        if (!isState(GameState.IN_GAME) || user.isSpectator() || arena.isDeathPlayer(user)) {
            return;
        }

        recordSurvivalTime(user);
        scores.addScore(user, user.getStatistic(Statistics.LOCAL_SURVIVE_TIME));
        user.setSpectator(true);

        arena.addDeathPlayer(user);
        prepareSpectator(user, true, false);

        int playersLeft = getPlayersLeft().size();
        plugin.getEventManager().playerEliminate(user.getPlayer(), this, playersLeft);

        broadcastMessage("you-eliminated",
            Var.ofPlayer(user),
            Var.of("%players_left%", playersLeft)
        );

        finishIfLastSurvivor();
    }

    public void finishIfLastSurvivor() {
        if (!isState(GameState.IN_GAME) || peakActivePlayers <= 0) {
            return;
        }

        Set<User> playersLeft = getPlayersLeft();
        if (playersLeft.size() > 1) {
            return;
        }

        if (playersLeft.size() == 1) {
            User winner = playersLeft.iterator().next();
            arena.addWinner(winner);
            scores.addScore(winner, winner.getStatistic(Statistics.LOCAL_SURVIVE_TIME));
            scores.setWinner(winner);
        }

        setGameState(GameState.ENDING);
    }

    public void prepareSpectator(User user, boolean teleportToStart) {
        prepareSpectator(user, teleportToStart, true);
    }

    public void prepareSpectator(User user, boolean teleportToStart, boolean saveInventory) {
        Player player = user.getPlayer();
        if (player == null) {
            return;
        }

        if (saveInventory) {
            InventorySerializer.saveInventoryToFile(plugin, player);
        }

        PlayerInventory inventory = player.getInventory();
        inventory.clear();
        inventory.setArmorContents(ItemUtils.EMPTY_ARMORS);

        if (teleportToStart) {
            player.teleport(getStartLocation());
        }

        player.setGameMode(GameMode.ADVENTURE);
        player.setAllowFlight(true);
        player.setFlying(true);
        player.setInvulnerable(true);
        player.setFireTicks(0);
        player.sendActionBar(Component.empty());

        spectatorManager.giveItems(player);
        spectatorManager.applySettings(user);
        spectatorManager.updateVisibility();
        scoreboardManager.createScoreboard(player);
        bossBarManager.addPlayer(player);

        user.sendMessage("you-are-spectator-now");
    }

    public void applySpectatorSettings(User user) {
        spectatorManager.applySettings(user);
    }

    public void updateSpectatorVisibility() {
        spectatorManager.updateVisibility();
    }

    @SuppressWarnings("unchecked")
    public <T extends GameStateHandler> T getHandler(GameState gameState) {
        return (T) states.get(gameState);
    }

    public boolean isState(GameState gameState, GameState... states) {
        if (this.gameState == gameState) return true;

        for (GameState state : states) {
            if (this.gameState == state) return true;
        }

        return false;
    }

    @Override
    public String toString() {
        return "Game[state=%s, players=%d]".formatted(gameState, users.size());
    }

    public record PlayerMetadata(String name, int doubleJumps, int maxDoubleJumps) {
    }
}
