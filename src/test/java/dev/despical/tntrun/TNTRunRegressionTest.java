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
import dev.despical.tntrun.arena.Arena;
import dev.despical.tntrun.arena.ArenaManager;
import dev.despical.tntrun.arena.options.ArenaKeys;
import dev.despical.tntrun.game.Game;
import dev.despical.tntrun.game.GameState;
import dev.despical.tntrun.option.ConfigOptions;
import dev.despical.tntrun.user.User;
import dev.despical.tntrun.user.UserManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

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
    private static final ConfigOptions options = mock(ConfigOptions.class);

    private static MockedStatic<TNTRun> pluginAccess;
    private static MockedStatic<Bukkit> bukkitAccess;

    private Arena arena;
    private Game game;
    private User user;
    private Player player;

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
        reset(plugin, eventManager, userManager, options);
        when(plugin.getEventManager()).thenReturn(eventManager);
        when(plugin.getUserManager()).thenReturn(userManager);
        when(plugin.getOptions()).thenReturn(options);

        arena = mock(Arena.class);
        game = mock(Game.class);
        user = mock(User.class);
        player = mock(Player.class);

        when(arena.getGame()).thenReturn(game);
        when(game.getArena()).thenReturn(arena);
        when(user.getPlayer()).thenReturn(player);
        when(userManager.getUser(player)).thenReturn(user);
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
}
