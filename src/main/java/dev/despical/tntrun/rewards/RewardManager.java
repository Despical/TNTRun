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
import dev.despical.commons.util.Strings;
import dev.despical.tntrun.TNTRun;
import dev.despical.tntrun.game.Game;
import dev.despical.tntrun.user.User;
import org.bukkit.Bukkit;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;

/**
 * Loads reward commands and executes captured dispatches on the server thread.
 * Game eligibility and per-round progress belong to {@link RoundRewards}.
 *
 * @author Despical
 * <p>
 * Created at 20.10.2026
 */
public final class RewardManager {

    private volatile RewardConfiguration configuration;
    private final TNTRun plugin;

    public RewardManager(TNTRun plugin) {
        this.plugin = plugin;
        this.reload();
    }

    public void reload() {
        configuration = RewardConfiguration.load(ConfigUtils.getConfig(plugin, "rewards"), plugin.getLogger());
    }

    /**
     * Captures an arena join after the player has successfully joined the lobby.
     * Call on the server thread, as with the other gameplay reward hooks.
     *
     * @param game joined game
     * @param user player joining as a participant
     */
    public void joined(Game game, User user) {
        dispatch(RewardType.GAME_JOIN, RewardContext.from(game, user));
    }

    Set<Integer> thresholds(RewardType type) {
        return configuration.milestones().getOrDefault(type, Map.of()).keySet();
    }

    String survivalReward(int seconds) {
        return configuration.survivalReward(seconds);
    }

    void dispatch(RewardType type, RewardContext context) {
        dispatch(type, context, 0);
    }

    void dispatch(RewardType type, RewardContext context, int threshold) {
        RewardConfiguration snapshot = configuration;
        List<String> commands = snapshot.commands(type, threshold);

        if (!snapshot.enabled() || commands.isEmpty()) {
            return;
        }

        RewardContext captured = context.with("%threshold%", threshold);

        // Queue immutable values so commands cannot re-enter a half-finished game transition.
        Bukkit.getScheduler().runTask(plugin, () -> commands.forEach(command -> execute(command, captured)));
    }

    private void execute(String configuredCommand, RewardContext context) {
        String command = configuredCommand.trim();
        if (command.isEmpty()) {
            return;
        }

        boolean playerCommand = command.regionMatches(true, 0, "p:", 0, 2);
        if (playerCommand) {
            command = command.substring(2).trim();
        }

        command = Strings.format(context.format(command));
        if (command.startsWith("/")) {
            command = command.substring(1);
        }

        if (command.isBlank()) {
            return;
        }

        try {
            if (playerCommand) {
                if (context.player() != null && context.player().isOnline()) {
                    context.player().performCommand(command);
                }
            } else {
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
            }
        } catch (RuntimeException exception) {
            plugin.getLogger().log(Level.WARNING, "Could not execute reward command: " + command, exception);
        }
    }
}
