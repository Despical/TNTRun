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

package dev.despical.tntrun.api.event.player;

import dev.despical.tntrun.game.Game;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * Fired when a player attempts to join a TNTRun game.
 * <p>
 * This event is called before the player is added to the target {@link Game}.
 * It is fired for both normal player joins and spectator joins. Cancelling it
 * prevents the join and leaves the player outside the game.
 * <p>
 * Typical use cases:
 * <ul>
 *     <li>Checking permissions, parties, cooldowns, or queue state</li>
 *     <li>Blocking joins for custom arena conditions</li>
 *     <li>Logging or announcing join attempts</li>
 * </ul>
 *
 * @author Despical
 * <p>
 * Created at 18.06.2026
 */
@Getter
public class PlayerJoinAttemptEvent extends PlayerEvent implements Cancellable {

    private static final HandlerList HANDLER_LIST = new HandlerList();

    /**
     * Whether this join attempt is cancelled.
     * Listeners can set this to true to prevent the player from joining.
     */
    @Setter
    private boolean cancelled;

    /**
     * The game that the player is attempting to join.
     */
    private final Game game;

    /**
     * Whether this join attempt will place the player into spectator mode.
     */
    private final boolean spectatorJoin;

    /**
     * Constructs a new PlayerJoinAttemptEvent.
     *
     * @param player the player attempting to join
     * @param game the target game
     */
    public PlayerJoinAttemptEvent(Player player, Game game) {
        this(player, game, false);
    }

    /**
     * Constructs a new PlayerJoinAttemptEvent.
     *
     * @param player the player attempting to join
     * @param game the target game
     * @param spectatorJoin whether the player will join as a spectator
     */
    public PlayerJoinAttemptEvent(Player player, Game game, boolean spectatorJoin) {
        super(player);
        this.game = game;
        this.spectatorJoin = spectatorJoin;
    }

    @NotNull
    @Override
    public HandlerList getHandlers() {
        return HANDLER_LIST;
    }

    public static HandlerList getHandlerList() {
        return HANDLER_LIST;
    }
}
