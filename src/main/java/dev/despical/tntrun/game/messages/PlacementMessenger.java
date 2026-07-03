package dev.despical.tntrun.game.messages;

import dev.despical.tntrun.Main;
import dev.despical.tntrun.chat.ChatManager;
import dev.despical.tntrun.game.Game;
import dev.despical.tntrun.user.User;
import dev.despical.tntrun.utils.Schedulers;
import dev.despical.tntrun.utils.StringUtils;
import dev.despical.tntrun.utils.Var;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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
        String noOpponent = chatManager.getRawString("summary.no-opponent");
        List<String> messages = chatManager.getStringList("summary.messages");

        Map<UUID, Integer> top3 = game.getScores().getTop3();
        List<UUID> ranked = new ArrayList<>(top3.keySet());

        String p1 = getNameOrFallback(ranked, 0, noOpponent);
        String p2 = getNameOrFallback(ranked, 1, noOpponent);
        String p3 = getNameOrFallback(ranked, 2, noOpponent);

        for (String line : messages) {
            String formatted = line
                .replace("%player_1%", p1)
                .replace("%player_2%", p2)
                .replace("%player_3%", p3);

            if (line.startsWith("%no_center%")) {
                game.broadcastRawMessage(formatted.substring(11));
                continue;
            }

            players.forEach(player -> StringUtils.sendCenteredMessage(player, formatted));
        }

        User winner = game.getScores().getWinner();
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
        Player player = Bukkit.getPlayer(uuid);

        if (player != null) {
            return player.getName();
        }

        OfflinePlayer offline = Bukkit.getOfflinePlayer(uuid);
        return offline.hasPlayedBefore() && offline.getName() != null
            ? offline.getName()
            : fallback;
    }


    private void message(User user, TickerMessage message, Var... vars) {
        this.message(user, message, 40, vars);
    }

    private void message(User user, TickerMessage message, int stay, Var... vars) {
        user.sendRawComponent(message.message(), vars);
        user.sendTitleComponent(message.title(), message.subtitle(), 0, stay, 20, vars);
    }
}
