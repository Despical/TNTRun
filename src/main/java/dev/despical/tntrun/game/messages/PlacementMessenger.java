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

package dev.despical.tntrun.game.messages;

import dev.despical.tntrun.Main;
import dev.despical.tntrun.chat.ChatManager;
import dev.despical.tntrun.game.Game;
import dev.despical.tntrun.user.User;
import dev.despical.tntrun.utils.Schedulers;
import dev.despical.tntrun.utils.StringUtils;
import dev.despical.tntrun.utils.Var;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * @author Despical
 * <p>
 * Created at 21.01.2026
 */
public final class PlacementMessenger {

    private static final Main plugin = Main.getInstance();

    private final Game game;
    private final MessageTicker ticker;

    public PlacementMessenger(Game game) {
        this.game = game;
        this.ticker = game.getMessageTicker();
    }

    public void sendSummaryMessages() {
        List<Player> players = game.getPlayers();
        if (players.isEmpty()) return;

        ChatManager chatManager = plugin.getChatManager();
        String noOpponent = chatManager.getRawString("summary-message.no-opponent");
        List<String> messages = chatManager.getStringList("summary-message.content");

        Map<UUID, Integer> top3 = game.getScores().getTop3();
        List<UUID> ranked = new ArrayList<>(top3.keySet());

        for (String line : messages) {
            Component formatted = createSummaryLine(line, ranked, noOpponent);

            if (line.startsWith("%no_center%")) {
                Component stripped = createSummaryLine(line.substring(11), ranked, noOpponent);
                game.broadcastRawComponent(stripped);
                continue;
            }

            players.forEach(player -> StringUtils.sendCenteredMessage(player, formatted));
        }

        User winner = game.getScores().getWinner();
        if (winner == null) {
            return;
        }

        TickerMessage winnerMessage = ticker.getGameOverMessage("you-won");

        TickerMessage loserMessage = ticker.getGameOverMessage("you-lost");
        Var[] vars = {Var.ofPlayer(winner)};

        Schedulers.runTaskLater(() -> {
            message(winner, winnerMessage, 80);

            List<User> users = new ArrayList<>(game.getUsers());
            users.remove(winner);

            users.forEach(user -> message(user, loserMessage, 80, vars));
        }, 40L);
    }

    private String getNameOrFallback(List<UUID> ranked, int index, String fallback) {
        if (ranked.size() <= index) return fallback;

        UUID uuid = ranked.get(index);
        Game.PlayerMetadata metadata = game.getPlayerMetadata(uuid);
        if (metadata != null) {
            return metadata.name();
        }

        Player player = Bukkit.getPlayer(uuid);

        if (player != null) {
            return player.getName();
        }

        OfflinePlayer offline = Bukkit.getOfflinePlayer(uuid);
        return offline.hasPlayedBefore() && offline.getName() != null
            ? offline.getName()
            : fallback;
    }

    private Component createSummaryLine(String line, List<UUID> ranked, String noOpponent) {
        Component component = Component.empty();
        String remaining = line;

        for (int i = 0; i < 3; i++) {
            String placeholder = "%player_" + (i + 1) + "%";
            int index = remaining.indexOf(placeholder);

            if (index == -1) {
                continue;
            }

            component = component.append(plugin.getChatManager().parseMessage(remaining.substring(0, index)));
            component = component.append(createPlayerComponent(ranked, i, noOpponent));
            remaining = remaining.substring(index + placeholder.length());
        }

        return component.append(plugin.getChatManager().parseMessage(remaining));
    }

    private Component createPlayerComponent(List<UUID> ranked, int index, String noOpponent) {
        if (ranked.size() <= index) {
            return plugin.getChatManager().parseMessage(noOpponent);
        }

        UUID uuid = ranked.get(index);
        Component playerName = plugin.getChatManager().parseMessage(getNameOrFallback(ranked, index, noOpponent));
        Component hover = createHover(uuid);

        return Component.empty().equals(hover) ? playerName : playerName.hoverEvent(hover);
    }

    private Component createHover(UUID uuid) {
        Game.PlayerMetadata metadata = game.getPlayerMetadata(uuid);
        if (metadata == null) {
            return Component.empty();
        }

        Var[] vars = {
            Var.of("%double_jumps%", metadata.doubleJumps()),
            Var.of("%max_double_jumps%", metadata.maxDoubleJumps())
        };

        List<Component> lines = plugin.getChatManager().getStringList("summary-message.hover")
            .stream()
            .map(line -> plugin.getChatManager().parseMessage(line, vars))
            .toList();

        return lines.stream()
            .filter(Objects::nonNull)
            .reduce((first, second) -> first.append(Component.newline()).append(second))
            .orElse(Component.empty());
    }

    private void message(User user, TickerMessage message, Var... vars) {
        this.message(user, message, 40, vars);
    }

    private void message(User user, TickerMessage message, int stay, Var... vars) {
        user.sendRawComponent(message.message(), vars);
        user.sendTitleComponent(message.title(), message.subtitle(), 0, stay, 20, vars);
    }
}
