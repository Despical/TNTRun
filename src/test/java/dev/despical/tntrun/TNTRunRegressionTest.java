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

package dev.despical.tntrun;

import dev.despical.tntrun.api.EventManager;
import dev.despical.tntrun.api.event.player.PlayerJoinAttemptEvent;
import dev.despical.tntrun.api.event.player.PlayerDoubleJumpEvent;
import dev.despical.tntrun.arena.Arena;
import dev.despical.tntrun.arena.ArenaManager;
import dev.despical.tntrun.arena.ArenaRegistry;
import dev.despical.tntrun.arena.options.ArenaKeys;
import dev.despical.tntrun.blocks.BlockRemovalManager;
import dev.despical.tntrun.event.GameEvents;
import dev.despical.tntrun.event.GameItemEvents;
import dev.despical.tntrun.event.GeneralEvents;
import dev.despical.tntrun.game.Game;
import dev.despical.tntrun.game.GameState;
import dev.despical.tntrun.game.messages.MessageTicker;
import dev.despical.tntrun.game.scores.ScoreRegistry;
import dev.despical.tntrun.game.states.InGameState;
import dev.despical.tntrun.game.states.GameStateHandler;
import dev.despical.tntrun.game.states.StartingState;
import dev.despical.tntrun.game.states.WaitingState;
import dev.despical.tntrun.option.BooleanOption;
import dev.despical.tntrun.option.ConfigOptions;
import dev.despical.tntrun.option.IntOption;
import dev.despical.tntrun.rewards.RoundRewards;
import dev.despical.tntrun.scoreboard.ScoreboardManager;
import dev.despical.tntrun.sound.SoundManager;
import dev.despical.tntrun.stats.Statistics;
import dev.despical.tntrun.user.User;
import dev.despical.tntrun.user.UserManager;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerToggleFlightEvent;
import org.bukkit.util.Vector;
import org.bukkit.plugin.PluginManager;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.MockedStatic;

import java.lang.reflect.Field;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

/**
 * @author Despical
 * <p>
 * Created at 13.10.2026
 */
class TNTRunRegressionTest {

    private static final TNTRun plugin = mock(TNTRun.class);
    private static final EventManager eventManager = mock(EventManager.class);
    private static final UserManager userManager = mock(UserManager.class);
    private static final ArenaRegistry arenaRegistry = mock(ArenaRegistry.class);
    private static final ConfigOptions options = mock(ConfigOptions.class);

    private static MockedStatic<TNTRun> pluginAccess;
    private static MockedStatic<Bukkit> bukkitAccess;

    private Arena arena;
    private Game game;
    private User user;
    private Player player;
    private RoundRewards roundRewards;

    @BeforeAll
    static void initializePluginAccess() {
        pluginAccess = mockStatic(TNTRun.class);
        pluginAccess.when(TNTRun::getInstance).thenReturn(plugin);

        bukkitAccess = mockStatic(Bukkit.class);
        bukkitAccess.when(Bukkit::getPluginManager).thenReturn(mock(PluginManager.class));
    }

    @AfterAll
    static void closePluginAccess() {
        bukkitAccess.close();
        pluginAccess.close();
    }

    @BeforeEach
    void initializeGame() {
        reset(plugin, eventManager, userManager, arenaRegistry, options);
        when(plugin.getEventManager()).thenReturn(eventManager);
        when(plugin.getUserManager()).thenReturn(userManager);
        when(plugin.getArenaRegistry()).thenReturn(arenaRegistry);
        when(plugin.getOptions()).thenReturn(options);
        when(options.get(BooleanOption.LEVEL_BAR_TIMER)).thenReturn(false);

        arena = mock(Arena.class);
        game = mock(Game.class);
        user = mock(User.class);
        player = mock(Player.class);
        roundRewards = mock(RoundRewards.class);

        when(arena.getGame()).thenReturn(game);
        when(game.getArena()).thenReturn(arena);
        when(user.getPlayer()).thenReturn(player);
        when(userManager.getUser(player)).thenReturn(user);
        when(game.getRoundRewards()).thenReturn(roundRewards);
    }

    @Test
    void rejectsJoiningAnotherArenaWhileAlreadyPlaying() {
        when(user.isInArena()).thenReturn(true);

        new ArenaManager(plugin).joinAttempt(user, arena);

        verify(user).sendMessage("already-playing");
        verify(game, never()).joinAsPlayer(any());
        verify(game, never()).joinAsSpectator(any());
        verifyNoInteractions(eventManager);
    }

    @Test
    void allowsJoiningAfterLeavingThePreviousArena() {
        when(game.isState(GameState.WAITING, GameState.STARTING)).thenReturn(true);
        when(eventManager.playerJoinAttempt(player, game, false)).thenReturn(mock(PlayerJoinAttemptEvent.class));

        new ArenaManager(plugin).joinAttempt(user, arena);

        verify(game).joinAsPlayer(user);
    }

    @Test
    void allowsJoiningAsSpectatorWhenOutsideAnyArena() {
        when(game.isState(GameState.IN_GAME)).thenReturn(true);
        when(arena.getOption(ArenaKeys.ARENA_SPECTATOR_JOIN_ENABLED)).thenReturn(true);
        when(eventManager.playerJoinAttempt(player, game, true)).thenReturn(mock(PlayerJoinAttemptEvent.class));

        new ArenaManager(plugin).joinAttempt(user, arena);

        verify(game).joinAsSpectator(user);
    }

    @Test
    void preventsInventoryDraggingInsideAnArena() {
        InventoryDragEvent event = mock(InventoryDragEvent.class);
        when(event.getWhoClicked()).thenReturn(player);
        when(arenaRegistry.isInArena(player)).thenReturn(true);

        new GeneralEvents().onInventoryDrag(event);

        verify(event).setCancelled(true);
    }

    @Test
    void preservesInventoryDraggingOutsideAnArena() {
        InventoryDragEvent event = mock(InventoryDragEvent.class);
        when(event.getWhoClicked()).thenReturn(player);

        new GeneralEvents().onInventoryDrag(event);

        verify(event, never()).setCancelled(anyBoolean());
    }

    @Test
    void convertsLethalPvpDamageToArenaElimination() {
        EntityDamageByEntityEvent event = createPvpDamage(20D);

        new GameEvents().onPlayerDamage(event);

        verify(event).setCancelled(true);
        verify(game).eliminate(user, userManager.getUser((Player) event.getDamager()));
    }

    @Test
    void preservesNonLethalPvpDamage() {
        EntityDamageByEntityEvent event = createPvpDamage(19D);

        new GameEvents().onPlayerDamage(event);

        verify(event, never()).setCancelled(anyBoolean());
        verify(game, never()).eliminate(any());
        verify(game, never()).eliminate(any(), any());
    }

    @Test
    void preventsDamageWithoutEliminatingWhenPvpIsDisabled() {
        EntityDamageByEntityEvent event = createPvpDamage(20D);
        when(arena.getOption(ArenaKeys.ARENA_PVP_ENABLED)).thenReturn(false);

        new GameEvents().onPlayerDamage(event);

        verify(event).setCancelled(true);
        verify(game, never()).eliminate(any());
        verify(game, never()).eliminate(any(), any());
    }

    @Test
    void convertsLethalProjectileDamageToArenaElimination() {
        EntityDamageByEntityEvent event = createPvpDamage(20D);
        Projectile projectile = mock(Projectile.class);
        Player attacker = (Player) event.getDamager();
        when(projectile.getShooter()).thenReturn(attacker);
        when(event.getDamager()).thenReturn(projectile);

        new GameEvents().onPlayerDamage(event);

        verify(event).setCancelled(true);
        verify(game).eliminate(user, userManager.getUser((Player) projectile.getShooter()));
    }

    @Test
    void preventsSpectatorsFromEliminatingPlayers() {
        EntityDamageByEntityEvent event = createPvpDamage(20D);
        User attacker = userManager.getUser((Player) event.getDamager());
        when(attacker.isSpectator()).thenReturn(true);

        new GameEvents().onPlayerDamage(event);

        verify(event).setCancelled(true);
        verify(game, never()).eliminate(any());
        verify(game, never()).eliminate(any(), any());
    }

    @Test
    void startsWhenCountdownIsAlreadyZero() {
        new StartingState(game).tick();

        verify(game).setGameState(GameState.IN_GAME);
    }

    @Test
    void startsWhenTheLastCountdownSecondExpires() {
        when(game.getTimer()).thenReturn(1);

        new StartingState(game).tick();

        verify(game).setTimer(0);
        verify(game).setGameState(GameState.IN_GAME);
    }

    @Test
    void continuesCountingDownWhileTimeRemains() {
        when(game.getTimer()).thenReturn(2);

        new StartingState(game).tick();

        verify(game).setTimer(1);
        verify(game, never()).setGameState(any());
    }

    @Test
    void requestsAnImmediateTickForZeroPreGameWaitingTime() {
        initializeTimer(120);
        when(options.get(IntOption.PRE_GAME_WAITING_TIME)).thenReturn(0);

        new StartingState(game).firstTick();

        verify(game).setTimer(0);
        verify(game).tickImmediately();
    }

    @Test
    void preservesZeroFullArenaCountdownAtTheAcceleratedThreshold() {
        initializeTimer(120);
        when(game.getPlayers()).thenReturn(List.of(player));
        when(game.getHandler(GameState.WAITING)).thenReturn(mock(WaitingState.class));
        when(arena.getOption(ArenaKeys.MAX_PLAYERS)).thenReturn(1);
        when(options.get(IntOption.FULL_GAME_STARTING_TIME)).thenReturn(0);
        when(options.get(IntOption.GAME_STARTING_TIME)).thenReturn(30);

        new StartingState(game).join(user);

        assertEquals(0, game.getTimer());
        verify(game, atLeastOnce()).tickImmediately();
        verify(game, never()).setTimer(30);
    }

    @Test
    void neverExtendsAnAlreadyShortenedCountdown() {
        initializeTimer(5);

        new StartingState(game).shortenTimer(30);

        assertEquals(5, game.getTimer());
        verify(game, never()).setTimer(anyInt());
        verify(game, never()).tickImmediately();
    }

    @ParameterizedTest
    @EnumSource(value = IntOption.class, names = {
        "LOBBY_WAITING_TIME", "PRE_GAME_WAITING_TIME", "GAME_STARTING_TIME", "FULL_GAME_STARTING_TIME", "ENDING_TIME"
    })
    void replacesNegativeTimersWithDefaults(IntOption option) {
        YamlConfiguration config = new YamlConfiguration();
        config.set(option.getPath(), -1);
        when(plugin.getConfig()).thenReturn(config);

        Logger logger = mock(Logger.class);
        when(plugin.getLogger()).thenReturn(logger);

        ConfigOptions loadedOptions = new ConfigOptions(plugin);

        assertEquals(option.getDefaultValue(), loadedOptions.get(option));
        verify(logger).warning(contains(option.getPath()));
    }

    @ParameterizedTest
    @EnumSource(value = IntOption.class, names = {
        "LOBBY_WAITING_TIME", "PRE_GAME_WAITING_TIME", "GAME_STARTING_TIME", "FULL_GAME_STARTING_TIME", "ENDING_TIME"
    })
    void acceptsZeroTimers(IntOption option) {
        YamlConfiguration config = new YamlConfiguration();
        config.set(option.getPath(), 0);
        when(plugin.getConfig()).thenReturn(config);

        ConfigOptions loadedOptions = new ConfigOptions(plugin);

        assertEquals(0, loadedOptions.get(option));
    }

    @Test
    void validatesNegativeTimersAfterReload() {
        YamlConfiguration config = new YamlConfiguration();
        when(plugin.getConfig()).thenReturn(config);
        when(plugin.getLogger()).thenReturn(mock(Logger.class));
        ConfigOptions loadedOptions = new ConfigOptions(plugin);

        config.set(IntOption.PRE_GAME_WAITING_TIME.getPath(), -1);
        loadedOptions.reloadOptions();

        assertEquals(IntOption.PRE_GAME_WAITING_TIME.getDefaultValue(), loadedOptions.get(IntOption.PRE_GAME_WAITING_TIME));
        verify(plugin).reloadConfig();
    }

    @Test
    void consumesImmediateTickRequestsAtTheRegularTickBoundary() throws ReflectiveOperationException {
        Game tickingGame = initializeTickingGame(20);
        GameStateHandler handler = tickingGame.getHandler(GameState.STARTING);

        tickingGame.tickImmediately();
        tickingGame.run();
        setGameField(tickingGame, "period", 5);
        tickingGame.run();

        verify(handler).tick();
    }

    @Test
    void waitsForAFullIntervalAfterAnImmediateTick() throws ReflectiveOperationException {
        Game tickingGame = initializeTickingGame(5);
        GameStateHandler handler = tickingGame.getHandler(GameState.STARTING);

        tickingGame.tickImmediately();
        tickingGame.run();
        tickingGame.run();
        tickingGame.run();
        tickingGame.run();
        verify(handler).tick();

        tickingGame.run();
        verify(handler, times(2)).tick();
    }

    @Test
    void startsRewardTrackingAfterTheSurvivalRoundIsPrepared() {
        ScoreboardManager scoreboardManager = mock(ScoreboardManager.class);
        ScoreRegistry scores = mock(ScoreRegistry.class);
        BlockRemovalManager blockRemovalManager = mock(BlockRemovalManager.class);
        when(game.getScoreboardManager()).thenReturn(scoreboardManager);
        when(game.getScores()).thenReturn(scores);
        when(game.getBlockRemovalManager()).thenReturn(blockRemovalManager);

        new InGameState(game).firstTick();

        var order = inOrder(game, roundRewards, eventManager);
        order.verify(game).startSurvivalRound();
        order.verify(roundRewards).start();
        order.verify(eventManager).gameStart(game);
    }

    @Test
    void liveTickEvaluatesRewardsAfterUpdatingSurvivalStatistics() {
        when(game.getUsers()).thenReturn(List.of(user));
        when(game.getScores()).thenReturn(mock(ScoreRegistry.class));
        when(user.getStatistic(Statistics.LOCAL_DOUBLE_JUMPS)).thenReturn(0);
        when(user.getStatistic(Statistics.LOCAL_SURVIVE_TIME)).thenReturn(30);

        new InGameState(game).tick();

        var order = inOrder(user, roundRewards);
        order.verify(user).addStat(Statistics.LOCAL_SURVIVE_TIME, 1);
        order.verify(roundRewards).tick(user);
    }

    @Test
    void earlyParticipantLeaveDiscardsRewardEligibilityBeforeWinnerDetection() {
        when(game.removeRoundParticipant(user)).thenReturn(true);
        when(game.getUsers()).thenReturn(List.of(mock(User.class)));
        when(user.getStatistic(Statistics.WIN_STREAK)).thenReturn(0);
        when(user.getStatistic(Statistics.LOCAL_SURVIVE_TIME)).thenReturn(30);
        when(user.getStatistic(Statistics.ARENA_BEST_TIMES)).thenReturn(java.util.Map.of());

        new InGameState(game).leave(user);

        var order = inOrder(roundRewards, game);
        order.verify(roundRewards).quit(user);
        order.verify(game).finishIfLastSurvivor();
    }

    @Test
    void spectatorLeaveDoesNotTriggerParticipantRewards() {
        when(user.isSpectator()).thenReturn(true);
        when(game.getUsers()).thenReturn(List.of(mock(User.class)));

        new InGameState(game).leave(user);

        verifyNoInteractions(roundRewards);
        verify(user, never()).addStat(Statistics.GAMES_PLAYED);
    }

    @Test
    void cancelledDoubleJumpNeverEarnsAReward() {
        PlayerDoubleJumpEvent jump = mock(PlayerDoubleJumpEvent.class);
        when(jump.isCancelled()).thenReturn(true);
        PlayerToggleFlightEvent flight = prepareDoubleJump(jump);

        new GameItemEvents().onDoubleJump(flight);

        verifyNoInteractions(roundRewards);
        verify(user, never()).setStatistic(eq(Statistics.LOCAL_DOUBLE_JUMPS), anyInt());
    }

    @Test
    void successfulDoubleJumpUpdatesRemainingJumpsBeforeRewarding() {
        SoundManager soundManager = mock(SoundManager.class);
        ScoreboardManager scoreboardManager = mock(ScoreboardManager.class);
        when(plugin.getSoundManager()).thenReturn(soundManager);
        when(game.getScoreboardManager()).thenReturn(scoreboardManager);
        PlayerDoubleJumpEvent jump = mock(PlayerDoubleJumpEvent.class);
        when(jump.getJumpsLeft()).thenReturn(4);
        when(jump.getVelocity()).thenReturn(new Vector(0, 0.7, 1.5));
        PlayerToggleFlightEvent flight = prepareDoubleJump(jump);

        new GameItemEvents().onDoubleJump(flight);

        var order = inOrder(user, roundRewards);
        order.verify(user).setStatistic(Statistics.LOCAL_DOUBLE_JUMPS, 4);
        order.verify(roundRewards).doubleJump(user);
    }

    @Test
    void exhaustedDoubleJumpsCannotEarnRewards() {
        PlayerToggleFlightEvent flight = prepareDoubleJump(mock(PlayerDoubleJumpEvent.class));
        when(user.getStatistic(Statistics.LOCAL_DOUBLE_JUMPS)).thenReturn(0);

        new GameItemEvents().onDoubleJump(flight);

        verifyNoInteractions(roundRewards);
        verify(eventManager, never()).playerDoubleJump(any(), any(), anyInt(), anyDouble(), any());
    }

    private PlayerToggleFlightEvent prepareDoubleJump(PlayerDoubleJumpEvent jump) {
        PlayerToggleFlightEvent flight = mock(PlayerToggleFlightEvent.class);
        when(flight.getPlayer()).thenReturn(player);
        when(flight.isFlying()).thenReturn(true);
        when(player.getGameMode()).thenReturn(GameMode.ADVENTURE);
        when(player.getLocation()).thenReturn(new Location(null, 0, 64, 0));
        when(user.getArena()).thenReturn(arena);
        when(arena.isState(GameState.IN_GAME)).thenReturn(true);
        when(user.getStatistic(Statistics.LOCAL_DOUBLE_JUMPS)).thenReturn(5);
        when(options.get(IntOption.DOUBLE_JUMP_DELAY)).thenReturn(0);
        when(eventManager.playerDoubleJump(eq(player), eq(game), eq(4), eq(0D), any())).thenReturn(jump);
        return flight;
    }

    private Game initializeTickingGame(int period) throws ReflectiveOperationException {
        Game tickingGame = mock(Game.class, CALLS_REAL_METHODS);
        GameStateHandler handler = mock(StartingState.class);

        setGameField(tickingGame, "period", period);
        setGameField(tickingGame, "stateHandler", handler);
        setGameField(tickingGame, "messageTicker", mock(MessageTicker.class));
        doReturn(handler).when(tickingGame).getHandler(GameState.STARTING);
        return tickingGame;
    }

    private void setGameField(Game target, String name, Object value) throws ReflectiveOperationException {
        Field field = Game.class.getDeclaredField(name);
        field.setAccessible(true);
        field.set(target, value);
    }

    private void initializeTimer(int timer) {
        AtomicInteger currentTimer = new AtomicInteger(timer);
        when(game.getTimer()).thenAnswer(_ -> currentTimer.get());
        doAnswer(invocation -> {
            currentTimer.set(invocation.getArgument(0));
            return null;
        }).when(game).setTimer(anyInt());
    }

    private EntityDamageByEntityEvent createPvpDamage(double damage) {
        Player attacker = mock(Player.class);
        when(userManager.getUser(attacker)).thenReturn(mock(User.class));
        when(arenaRegistry.getArena(attacker)).thenReturn(arena);
        when(arenaRegistry.getArena(player)).thenReturn(arena);
        when(arena.getOption(ArenaKeys.ARENA_PVP_ENABLED)).thenReturn(true);
        when(arena.isState(GameState.IN_GAME)).thenReturn(true);

        BlockRemovalManager blockRemovalManager = mock(BlockRemovalManager.class);
        when(game.getBlockRemovalManager()).thenReturn(blockRemovalManager);
        when(blockRemovalManager.hasRemovalStarted()).thenReturn(true);

        EntityDamageByEntityEvent event = mock(EntityDamageByEntityEvent.class);
        when(event.getEntity()).thenReturn(player);
        when(event.getDamager()).thenReturn(attacker);
        when(event.getFinalDamage()).thenReturn(damage);
        when(player.getHealth()).thenReturn(20D);
        return event;
    }
}
