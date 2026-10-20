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

package dev.despical.tntrun.rewards;

import dev.despical.commons.configuration.ConfigUtils;
import dev.despical.tntrun.TNTRun;
import org.bukkit.Bukkit;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitScheduler;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * @author Despical
 * <p>
 * Created at 20.10.2026
 */
class RewardManagerTest {

    private TNTRun plugin;
    private Player player;
    private ConsoleCommandSender console;
    private YamlConfiguration config;
    private RewardManager manager;
    private List<Runnable> queued;
    private MockedStatic<ConfigUtils> configs;
    private MockedStatic<Bukkit> bukkit;

    @BeforeEach
    void initializeDispatch() {
        plugin = mock(TNTRun.class);
        player = mock(Player.class);
        console = mock(ConsoleCommandSender.class);
        config = new YamlConfiguration();
        queued = new ArrayList<>();

        when(plugin.getLogger()).thenReturn(Logger.getLogger("RewardManagerTest"));

        configs = mockStatic(ConfigUtils.class);
        configs.when(() -> ConfigUtils.getConfig(plugin, "rewards")).thenReturn(config);
        bukkit = mockStatic(Bukkit.class);

        BukkitScheduler scheduler = mock(BukkitScheduler.class);
        bukkit.when(Bukkit::getScheduler).thenReturn(scheduler);
        bukkit.when(Bukkit::getConsoleSender).thenReturn(console);

        doAnswer(invocation -> {
            queued.add(invocation.getArgument(1, Runnable.class));
            return null;
        }).when(scheduler).runTask(eq(plugin), any(Runnable.class));

        manager = new RewardManager(plugin);
    }

    @AfterEach
    void closeStaticAccess() {
        bukkit.close();
        configs.close();
    }

    @Test
    void dispatchesCapturedConsoleAndPlayerCommandsOnTheNextTick() {
        config.set("first-place", List.of(" /eco give %player% 100 ", "P: /me won %arena%"));
        manager.reload();

        when(player.isOnline()).thenReturn(true);
        manager.dispatch(RewardType.FIRST_PLACE, context());

        bukkit.verify(() -> Bukkit.dispatchCommand(any(), anyString()), never());
        verify(player, never()).performCommand(anyString());

        queued.forEach(Runnable::run);

        bukkit.verify(() -> Bukkit.dispatchCommand(console, "eco give Alice 100"));
        verify(player).performCommand("me won test");
    }

    @Test
    void reloadAndStatResetCannotChangeAlreadyQueuedRewards() {
        config.set("first-place", List.of("eco give %player% %survival-time%"));
        manager.reload();

        Map<String, String> values = new HashMap<>(Map.of("%player%", "Alice", "%survival-time%", "65"));
        manager.dispatch(RewardType.FIRST_PLACE, new RewardContext(player, values));
        values.put("%survival-time%", "0");

        config.set("first-place", List.of("changed"));
        config.set("enabled", false);

        manager.reload();
        queued.forEach(Runnable::run);

        bukkit.verify(() -> Bukkit.dispatchCommand(console, "eco give Alice 65"));
        bukkit.verify(() -> Bukkit.dispatchCommand(console, "changed"), never());
    }

    @Test
    void disabledRewardsAndEmptyListsQueueNothing() {
        manager.dispatch(RewardType.FIRST_PLACE, context());
        config.set("first-place", List.of("eco give Alice 100"));
        config.set("enabled", false);

        manager.reload();
        manager.dispatch(RewardType.FIRST_PLACE, context());

        assertTrue(queued.isEmpty());
    }

    @Test
    void disconnectSkipsPlayerCommandsButRetainsConsoleRewards() {
        config.set("game-quit", List.of("p:me quit", "eco give %player% 1"));

        manager.reload();
        manager.dispatch(RewardType.GAME_QUIT, context());
        queued.forEach(Runnable::run);

        verify(player, never()).performCommand(anyString());
        bukkit.verify(() -> Bukkit.dispatchCommand(console, "eco give Alice 1"));
    }

    @Test
    void malformedEmptyCommandsAreIgnored() {
        config.set("first-place", List.of("", " ", "/", "p:", "P: /", "valid"));
        manager.reload();
        when(player.isOnline()).thenReturn(true);
        manager.dispatch(RewardType.FIRST_PLACE, context());
        queued.forEach(Runnable::run);

        bukkit.verify(() -> Bukkit.dispatchCommand(console, "valid"));
        bukkit.verify(() -> Bukkit.dispatchCommand(console, ""), never());
        verify(player, never()).performCommand(anyString());
    }

    @Test
    void failedCommandDoesNotPreventLaterCommands() {
        config.set("first-place", List.of("broken", "valid"));

        manager.reload();
        bukkit.when(() -> Bukkit.dispatchCommand(console, "broken")).thenThrow(new IllegalStateException("test failure"));
        manager.dispatch(RewardType.FIRST_PLACE, context());

        assertDoesNotThrow(() -> queued.forEach(Runnable::run));
        bukkit.verify(() -> Bukkit.dispatchCommand(console, "valid"));
    }

    @Test
    void thresholdCommandsReceiveTheSelectedThreshold() {
        config.set("win-streak.thresholds.3", List.of("say %player% won %threshold% times"));
        config.set("win-streak.thresholds.5", List.of("wrong"));

        manager.reload();
        manager.dispatch(RewardType.WIN_STREAK, context(), 3);
        queued.forEach(Runnable::run);

        bukkit.verify(() -> Bukkit.dispatchCommand(console, "say Alice won 3 times"));
        bukkit.verify(() -> Bukkit.dispatchCommand(console, "wrong"), never());
    }

    private RewardContext context() {
        return new RewardContext(player, Map.of("%player%", "Alice", "%arena%", "test"));
    }
}
