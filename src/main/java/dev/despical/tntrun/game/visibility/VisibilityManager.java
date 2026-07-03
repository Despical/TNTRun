package dev.despical.tntrun.game.visibility;

import dev.despical.tntrun.Main;
import dev.despical.tntrun.game.Game;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

/**
 * @author Despical
 * <p>
 * Created at 12.12.2025
 */
public class VisibilityManager {

    private static final Main plugin = Main.getInstance();

    private final Game game;

    public VisibilityManager(Game game) {
        this.game = game;
    }

    public void hidePlayersFromEachOther() {
        for (Player player : game.getPlayers()) {
            for (Player other : game.getPlayers()) {
                if (player.equals(other)) continue;

                hide(player, other);
            }
        }
    }

    public void showPlayersToEachOther() {
        for (Player player : game.getPlayers()) {
            for (Player other : game.getPlayers()) {
                if (player.equals(other)) continue;

                show(player, other);
            }
        }
    }

    public void hidePlayerToInGamePlayers(Player player) {
        for (Player inGamePlayer : game.getPlayers()) {
            hide(inGamePlayer, player);
            show(player, inGamePlayer);
        }
    }

    public void hidePlayerFromGame(Player player) {
        for (Player inGamePlayer : game.getPlayers()) {
            hide(inGamePlayer, player);
            hide(player, inGamePlayer);
        }
    }

    public void hidePlayerToOutsideGame(Player player) {
        for (Player outsidePlayer : getOthers()) {
            hide(outsidePlayer, player);
            hide(player, outsidePlayer);
        }
    }

    public void showPlayerToInGamePlayers(Player player) {
        for (Player inGamePlayer : game.getPlayers()) {
            show(inGamePlayer, player);
            show(player, inGamePlayer);
        }
    }

    public void showPlayerOutsideTheGame(Player player) {
        var arenaRegistry = plugin.getArenaRegistry();

        getOthers()
            .stream()
            .filter(Predicate.not(arenaRegistry::isInArena))
            .forEach(outsidePlayer -> {
                show(player, outsidePlayer);
                show(outsidePlayer, player);
            });
    }

    private void hide(Player player, Player from) {
        player.hidePlayer(plugin, from);
    }

    private void show(Player player, Player from) {
        player.showPlayer(plugin, from);
    }

    private List<Player> getOthers() {
        List<Player> onlinePlayers = new ArrayList<>(Bukkit.getOnlinePlayers());
        onlinePlayers.removeAll(game.getPlayers());
        return onlinePlayers;
    }
}
