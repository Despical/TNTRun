package dev.despical.tntrun.api.event.game;

import dev.despical.tntrun.game.Game;

/**
 * Called when the game has finished initialization and is about to start.
 * <p>
 * At the time this event is fired, all game setup logic has been completed
 * (players teleported, scores reset, tasks prepared, etc.).
 * <p>
 * The actual game loop will become active on the <b>next server tick</b>,
 * meaning gameplay logic, timers, and round processing will begin immediately after.
 *
 * @author Despical
 * @since 29.01.2026
 */
public class GameStartEvent extends GameEvent {

    public GameStartEvent(Game game) {
        super(game);
    }
}
