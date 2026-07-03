package dev.despical.tntrun.game.states;

import dev.despical.tntrun.arena.options.ArenaKeys;
import dev.despical.tntrun.game.Game;
import dev.despical.tntrun.game.GameState;
import dev.despical.tntrun.option.BooleanOption;
import dev.despical.tntrun.option.IntOption;
import dev.despical.tntrun.stats.Statistics;
import dev.despical.tntrun.user.User;
import dev.despical.tntrun.utils.Utils;
import dev.despical.tntrun.utils.Var;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;

import java.util.List;

/**
 * @author Despical
 * <p>
 * Created at 12.12.2025
 */
public class WaitingState extends GameStateHandler {

    private int oldTimer;

    public WaitingState(Game game) {
        super(game);
    }

    @Override
    public void tick() {
        List<User> players = game.getUsers();

        if (players.isEmpty()) {
            return;
        }

        int timer = game.getTimer();

        if (timer > 0) {
            game.setTimer(--timer);
            handleLevelBarTimer(timer);

            if (timer % 60 == 0) {
                broadcastWaitingMessageIfNeeded();
            }

            return;
        }

        game.setTimer(IntOption.LOBBY_WAITING_TIME.value());
    }

    @Override
    public void firstTick() {
        if (oldTimer != 0) {
            game.setTimer(oldTimer);

            oldTimer = 0;
            return;
        }

        game.setTimer(IntOption.LOBBY_WAITING_TIME.value());
    }

    @Override
    public void join(User user) {
        handlePlayerJoin(user);
        handleTimerOnPlayerJoin();
    }

    public void handlePlayerJoin(User user) {
        Player player = user.getPlayer();
        player.teleport(getLocation(ArenaKeys.LOBBY_LOCATION));

        visibilityManager.hidePlayerToOutsideGame(player);
        visibilityManager.showPlayerToInGamePlayers(player);

        game.getScoreboardManager().createScoreboard(player);
        game.getBossBarManager().addPlayer(player);

        int playerAmount = game.getUsers().size();
        int maxPlayerAmount = arena.getOption(ArenaKeys.MAX_PLAYERS);
        Var[] vars = {
            Var.of("%player%", player.getDisplayName()),
            Var.of("%players%", playerAmount),
            Var.of("%max_players%", maxPlayerAmount)
        };

        game.broadcastMessage("player-joined", vars);
        plugin.getChatManager().sendCenteredMessage(player, "game-explanation");

        handleInventory(player);
        resetPlayerAttributes(player);
    }

    private void handleTimerOnPlayerJoin() {
        int playerAmount = game.getUsers().size();
        int minPlayerAmount = arena.getOption(ArenaKeys.MIN_PLAYERS);
        int preGameWaitingTime = IntOption.PRE_GAME_WAITING_TIME.value();

        if (playerAmount == minPlayerAmount) {
            if (game.getTimer() > preGameWaitingTime) {
                oldTimer = game.getTimer();
            }

            game.setGameState(GameState.STARTING);
        }
    }

    @Override
    public void leave(User user) {
        game.broadcastMessage("player-left", Var.ofPlayer(user));

        handleTimerOnLeave();
    }

    private void handleTimerOnLeave() {
        int playerAmount = game.getUsers().size();
        int minPlayerAmount = arena.getOption(ArenaKeys.MIN_PLAYERS);
        int preGameWaitingTime = IntOption.PRE_GAME_WAITING_TIME.value();

        if (playerAmount < minPlayerAmount && game.getTimer() <= preGameWaitingTime) {
            game.setTimer(oldTimer);
        }
    }

    @Override
    public void resetPlayerAttributes(Player player) {
        Utils.resetPlayerAttributes(player);
        player.setLevel(BooleanOption.LEVEL_BAR_TIMER.value() ? game.getTimer() : 0);
        player.setInvulnerable(false);
        player.clearTitle();
        player.sendActionBar(Component.empty());
    }

    private void handleInventory(Player player) {
        saveAndClearInventory(player);
        giveLobbyItems(player);
        User user = plugin.getUserManager().getUser(player);
        user.setStatistic(Statistics.LOCAL_DOUBLE_JUMPS, plugin.getPermissionManager().getDoubleJumps(player));
    }

    private void giveLobbyItems(Player player) {
        plugin.getItemManager().getItem("leave-item").giveTo(player, "slot");
    }

    private void broadcastWaitingMessageIfNeeded() {
        int playerAmount = game.getUsers().size();
        int minAmount = arena.getOption(ArenaKeys.MIN_PLAYERS);
        int playersNeeded = Math.max(0, minAmount - playerAmount);

        if (playersNeeded == 0) {
            return;
        }

        boolean singular = playersNeeded == 1;
        game.broadcastMessage("waiting-for-players",
            Var.of("%s%", singular ? "" : "s"),
            Var.of("%to_be_form%", singular ? "is" : "are"),
            Var.of("%players_needed%", playersNeeded)
        );
    }
}
